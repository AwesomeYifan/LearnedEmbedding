import csv

import pandas as pd
import torch
import torch.optim as optim
from sklearn.model_selection import train_test_split
from torch.optim import lr_scheduler

from datasets import TripletDataset
from losses import TripletLossMLP
from networks import TripletNet, EmbeddingNetMLP
from trainer import fit_triplet

embedding_dim = 20
num_clusters = 2
num_threads = 8
gpus = 0
n_epoch = 100
test_size = 0.2
params = {'batch_size': 32,
          'shuffle': True}

TRAIN_CSV = "../../Data/data/trainingData.csv"

# Load training set
train_df = pd.read_csv(TRAIN_CSV, delimiter=',', encoding="utf-8-sig")

# Split to train validation
validation_size = int(len(train_df) * 0.1)
training_size = len(train_df) - validation_size

X = train_df[['anchor', 'positive', 'negative']]
#Y = train_df['diff']

input_dim=len(X['anchor'][0].split())
cuda = torch.cuda.is_available()

X = X.values

X_train, X_validation = train_test_split(X, test_size=test_size)

triplet_train_dataset = TripletDataset(X_train)
triplet_test_dataset = TripletDataset(X_validation)

triplet_train_loader = torch.utils.data.DataLoader(triplet_train_dataset, **params)
triplet_test_loader = torch.utils.data.DataLoader(triplet_test_dataset, **params)

embedding_net = EmbeddingNetMLP(input_dim, embedding_dim)
model = TripletNet(embedding_net)
if cuda:
    model.cuda()
loss_fn = TripletLossMLP()
lr = 1e-3
optimizer = optim.Adam(model.parameters(), lr=lr)
scheduler = lr_scheduler.StepLR(optimizer, 8, gamma=0.1, last_epoch=-1)
log_interval = 100
patience = 4
fit_triplet(triplet_train_loader, triplet_test_loader, model, loss_fn, optimizer, scheduler, patience, n_epoch, cuda, log_interval)
torch.save(model, "./data/TripletNetwork.pt")

#below for unlabeled data
for i in range(num_threads):
    TEST_CSV = "../../Data/data/thread-" + str(i)
    with open(TEST_CSV) as csv_file:
        with open('../../Data/data/siamese-reducedVectors-' + str(i), 'w') as writeFile:
            csv_reader = csv.reader(csv_file, delimiter=' ')
            for row in csv_reader:
                vec = [float(k) for k in row]
                vec = torch.FloatTensor(vec)
                writeFile.write(' '.join(map(str, model.get_embedding(vec).data.numpy())) + "\n")
            writeFile.close()
#below for labeled data
# TEST_CSV = "../../Data/data/mnist.csv"
# with open(TEST_CSV) as csv_file:
#     with open('../../Data/data/siamese-reducedVectors-MNIST', 'w') as writeFile:
#         csv_reader = csv.reader(csv_file, delimiter=' ')
#         for row in csv_reader:
#             vec = [float(k) for k in row]
#             vec = torch.FloatTensor(vec)
#             writeFile.write(' '.join(map(str, model.get_embedding(vec).data.numpy())) + "\n")
#         writeFile.close()
# TEST_CSV = "../../Data/data/mnist-test"
# with open(TEST_CSV) as csv_file:
#     with open('../../Data/data/siamese-reducedVectors-MNIST-test', 'w') as writeFile:
#         csv_reader = csv.reader(csv_file, delimiter=' ')
#         for row in csv_reader:
#             vec = [float(k) for k in row]
#             vec = torch.FloatTensor(vec)
#             writeFile.write(' '.join(map(str, model.get_embedding(vec).data.numpy())) + "\n")
#         writeFile.close()
