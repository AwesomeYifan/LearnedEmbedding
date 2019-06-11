import csv

import pandas as pd
import torch
import torch.optim as optim
from sklearn.model_selection import train_test_split
from torch.optim import lr_scheduler

from datasets import TripletDataset, MultipletDataset
from losses import TripletLossMLP, MultipletLoss
from networks import TripletNet, EmbeddingNetMLP, MultipletNet
from trainer import fit_triplet

embedding_dim = 20
num_clusters = 2
gpus = 0
n_epoch = 100
test_size = 0.2
params = {'batch_size': 32,
          'shuffle': True}

TRAIN_CSV = "../../Data/data/trainingData-triplet"

# Load training set
train_df = pd.read_csv(TRAIN_CSV, delimiter=',', encoding="utf-8-sig")

# Split to train validation
validation_size = int(len(train_df) * 0.1)
training_size = len(train_df) - validation_size

#X = train_df[['anchor', 'positive', 'negative']]
X = train_df[['anchor', 'others']]
#Y = train_df['diff']

input_dim=len(X['anchor'][0].split())
cuda = torch.cuda.is_available()

X = X.values

X_train, X_validation = train_test_split(X, test_size=test_size)

triplet_train_dataset = MultipletDataset(X_train)
triplet_test_dataset = MultipletDataset(X_validation)

triplet_train_loader = torch.utils.data.DataLoader(triplet_train_dataset, **params)
triplet_test_loader = torch.utils.data.DataLoader(triplet_test_dataset, **params)

embedding_net = EmbeddingNetMLP(input_dim, embedding_dim)
#model = TripletNet(embedding_net)
model = MultipletNet(embedding_net)
if cuda:
    model.cuda()
# loss_fn = TripletLossMLP()
loss_fn = MultipletLoss()
lr = 1e-2
optimizer = optim.Adam(model.parameters(), lr=lr)
scheduler = lr_scheduler.StepLR(optimizer, 8, gamma=0.1, last_epoch=-1)
log_interval = 10
patience = 4
fit_triplet(triplet_train_loader, triplet_test_loader, model, loss_fn, optimizer, scheduler, patience, n_epoch, cuda, log_interval)
torch.save(model, "./data/TripletNetwork.pt")

model = torch.load("./data/TripletNetwork.pt")
#below for unlabeled data

TEST_CSV = "../../Data/data/originalVectors"
with open(TEST_CSV) as csv_file:
    with open('../../Data/data/reducedVectors-tripletNet', 'w') as writeFile:
        csv_reader = csv.reader(csv_file, delimiter=' ')
        for row in csv_reader:
            vec = [float(k) for k in row]
            vec = torch.FloatTensor(vec)
            writeFile.write(' '.join(map(str, model.get_embedding(vec).data.numpy())) + "\n")
        writeFile.close()
#below for labeled data
# TEST_CSV = "../../Data/data/originalVectors-MNIST"
# with open(TEST_CSV) as csv_file:
#     with open('../../Data/data/reducedVectors-tripletNet-MNIST', 'w') as writeFile:
#         csv_reader = csv.reader(csv_file, delimiter=' ')
#         for row in csv_reader:
#             vec = [float(k) for k in row]
#             vec = torch.FloatTensor(vec)
#             writeFile.write(' '.join(map(str, model.get_embedding(vec).data.numpy())) + "\n")
#         writeFile.close()
