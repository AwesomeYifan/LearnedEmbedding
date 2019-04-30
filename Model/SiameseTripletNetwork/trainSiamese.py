import csv

import pandas as pd
import torch
import torch.optim as optim
from sklearn.model_selection import train_test_split
from torch.optim import lr_scheduler

from datasets import SiameseDataset
from losses import ContrastiveLossMLP
from networks import SiameseNet, EmbeddingNetMLP
from trainer import fit

embedding_dim = 6
gpus = 0
n_epoch = 10
num_clusters = 2
params = {'batch_size': 10,
          'shuffle': True}

TRAIN_CSV = "../../Data/data/SiameseData.csv"

# Load training set
train_df = pd.read_csv(TRAIN_CSV, delimiter=',', encoding="utf-8-sig")

# Split to train validation
validation_size = int(len(train_df) * 0.1)
training_size = len(train_df) - validation_size

X = train_df[['P1', 'P2']]
Y = train_df['dist']

input_dim = len(X['P1'][0].split())
cuda = torch.cuda.is_available()

X_train, X_temp, Y_train, Y_temp = train_test_split(X, Y, test_size=0)
X_validation, X_temp, Y_validation, Y_temp = train_test_split(X, Y, test_size=0)

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
optimizer = optim.Adam(model.parameters(), lr=lr)
scheduler = lr_scheduler.StepLR(optimizer, 8, gamma=0.1, last_epoch=-1)
log_interval = 100
fit(siamese_train_loader, siamese_test_loader, model, loss_fn, optimizer, scheduler, n_epoch, cuda, log_interval)

for i in range(num_clusters):
    TEST_CSV = "../../Data/data/class-" + str(i) + ".csv"
    with open(TEST_CSV) as csv_file:
        with open('../../Data/data/siamese-reducedVectors-' + str(i) + ".csv", 'w') as writeFile:
            csv_reader = csv.reader(csv_file, delimiter=' ')
            for row in csv_reader:
                vec = [float(k) for k in row]
                vec = torch.FloatTensor(vec)
                writeFile.write(' '.join(map(str, model.get_embedding(vec).data.numpy())) + "\n")
            writeFile.close()
