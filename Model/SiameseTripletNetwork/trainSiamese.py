import csv

import pandas as pd
import torch
import torch.optim as optim
from sklearn.model_selection import train_test_split
from torch.optim import lr_scheduler

from pytorchtools import EarlyStopping
from datasets import SiameseDataset
from losses import ContrastiveLossMLP
from networks import SiameseNet, EmbeddingNetMLP, EmbeddingNet
from trainer import fit

embedding_dim = 10
gpus = 0
n_epoch = 100
num_threads = 8
test_size = 0.2
params = {'batch_size': 100,
          'shuffle': True}

TRAIN_CSV = "../../Data/data/trainingData.csv"
TEST_CSV = "../../Data/data/validationData.csv"

# Load training set
train_df = pd.read_csv(TRAIN_CSV, delimiter=',', encoding="utf-8-sig")
test_df = pd.read_csv(TEST_CSV, delimiter=',', encoding="utf-8-sig")

# Split to train validation
#validation_size = int(len(train_df) * 0.01)
#training_size = len(train_df) - validation_size

training_samples = train_df[['P1', 'P2']]
training_labels = train_df[['distance', 'cutoff']]
testing_samples = test_df[['P1', 'P2']]
testing_labels = test_df[['distance', 'cutoff']]

input_dim = len(training_samples['P1'][0].split())

training_samples = training_samples.values
training_labels = training_labels.values
testing_samples = testing_samples.values
testing_labels = testing_labels.values

cuda = torch.cuda.is_available()

X_train, X_validation, Y_train, Y_validation = train_test_split(training_samples, training_labels, test_size=test_size)
# X_train, X_temp, Y_train, Y_temp = train_test_split(training_samples, training_labels, test_size=0)
# X_validation, X_temp, Y_validation, Y_temp = train_test_split(testing_samples, testing_labels, test_size=0)

siamese_train_dataset = SiameseDataset(X_train, Y_train)  # Returns pairs of images and target same/different
siamese_test_dataset = SiameseDataset(X_validation, Y_validation)

siamese_train_loader = torch.utils.data.DataLoader(siamese_train_dataset, **params)
siamese_test_loader = torch.utils.data.DataLoader(siamese_test_dataset, **params)

embedding_net = EmbeddingNetMLP(input_dim, embedding_dim)

model = SiameseNet(embedding_net)
if cuda:
    model.cuda()
loss_fn = ContrastiveLossMLP()
lr = 1e-3
#optimizer = optim.SGD(model.parameters(), lr=lr, momentum=0.9)
optimizer = optim.Adam(model.parameters(), lr=lr)
scheduler = lr_scheduler.StepLR(optimizer, 8, gamma=0.1, last_epoch=-1)
log_interval = 100
patience = 10
fit(siamese_train_loader, siamese_test_loader, model, loss_fn, optimizer, scheduler, patience, n_epoch, cuda, log_interval)
torch.save(model, "./data/SiameseNetwork.pt")

#model = torch.load("./data/SiameseNetwork.pt")
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
