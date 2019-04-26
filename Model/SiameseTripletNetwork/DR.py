import matplotlib.pyplot as plt
import matplotlib
import pandas as pd
import numpy as np
import keras

from sklearn.model_selection import train_test_split

from keras.models import Sequential
from keras.layers import Dense, Activation, Input
from keras.models import Model

from losses import ContrastiveLossMLP
from util import ManDist
from util import reformat
from util import custom_loss
from util import EucDist
from datasets import SiameseDataset
from networks import SiameseNet, EmbeddingNetMLP
import torch
from torch.optim import lr_scheduler
import torch.optim as optim
from trainer import fit

embedding_dim = 6
gpus = 0
n_epoch = 1
params = {'batch_size': 1024,
          'shuffle': True}

TRAIN_CSV = "../../Data/data/train.csv"

# Load training set
train_df = pd.read_csv(TRAIN_CSV, delimiter=',', encoding="utf-8-sig")

# Split to train validation
validation_size = int(len(train_df) * 0.1)
training_size = len(train_df) - validation_size

X = train_df[['P1', 'P2']]
Y = train_df['dist']

input_dim=len(X['P1'][0].split())
cuda = torch.cuda.is_available()

X_train, X_temp, Y_train, Y_temp = train_test_split(X, Y, test_size=0)
X_validation, X_temp, Y_validation, Y_temp = train_test_split(X, Y, test_size=0)

siamese_train_dataset = SiameseDataset(X_train, Y_train) #Returns pairs of images and target same/different
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

vec = [float(i) for i in str(X.loc[0, 'P1']).split()]
vec = torch.FloatTensor(vec)
print(model.get_embedding(vec).data.numpy())