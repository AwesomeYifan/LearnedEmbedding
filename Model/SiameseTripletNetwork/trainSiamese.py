import csv
import sys
import pandas as pd
import torch
import torch.optim as optim
from sklearn.model_selection import train_test_split
from torch.optim import lr_scheduler

from datasets import SiameseDataset
from losses import ContrastiveLoss
from networks import SiameseNet, EmbeddingNetMLP
from trainer import fit_siamese
#args: dataset, d', epss
# below are the input values required by the script
#
# flag=1: consider weight
# flag=0: not
# dir = sys.argv[1]
# embedding_dim = int(sys.argv[2])
# flag = float(sys.argv[3])

embedding_dim = int(sys.argv[2])
m = float(sys.argv[1])
#embedding_dim = 2
#m = 10
dir = "data/Gaussian/"

TRAIN_CSV = dir + "training"
TEST_CSV = dir + "points"
WRITE_CSV = dir + "reducedPoints"

# training_dataset = "SIFT
# originalSampleDim = 10
# originalSampleRatio = 0.1
# embedding_dim = 5
# epsilon = 0.0
# pf=0.1

# TRAIN_CSV = "../../Data/data/trainingData-SIFT"
# TEST_CSV = "../../Data/data/originalVectors-SIFT-10"
# WRITE_CSV = '../../Data/data/reducedVectors-siameseNet-SIFT-10-0.1-0.0'

gpus = 0
n_epoch = 50
num_threads = 8
test_size = 0.2

params = {'batch_size': 10240,
          'shuffle': True}
#loss_fn = ContrastiveLossHard()
loss_fn = ContrastiveLoss(m)

# Load training set
train_df = pd.read_csv(TRAIN_CSV, delimiter=',', encoding="utf-8-sig")
#test_df = pd.read_csv(TEST_CSV, delimiter=',', encoding="utf-8-sig")

# Split to train validation
#validation_size = int(len(train_df) * 0.01)
#training_size = len(train_df) - validation_size

training_samples = train_df[['P1', 'P2']]
#training_labels = train_df[['distance', 'cutoff']]
training_labels = train_df[['distance', 'cutoff', 'thisCluster', 'otherCluster']]
#testing_samples = test_df[['P1', 'P2']]
#testing_labels = test_df[['distance', 'cutoff']]

input_dim = len(training_samples['P1'][0].split())

training_samples = training_samples.values
training_labels = training_labels.values
#testing_samples = testing_samples.values
#testing_labels = testing_labels.values

cuda = torch.cuda.is_available()

X_train, X_validation, Y_train, Y_validation = train_test_split(training_samples, training_labels, test_size=test_size)
siamese_train_dataset = SiameseDataset(X_train, Y_train)  # Returns pairs of images and target same/different
siamese_test_dataset = SiameseDataset(X_validation, Y_validation)

siamese_train_loader = torch.utils.data.DataLoader(siamese_train_dataset, **params)
siamese_test_loader = torch.utils.data.DataLoader(siamese_test_dataset, **params)

embedding_net = EmbeddingNetMLP(input_dim, embedding_dim)

model = SiameseNet(embedding_net)
if cuda:
    model.cuda()

#loss_fn = ContrastiveLossSoftNN()
lr = 1e-4
optimizer = optim.SGD(model.parameters(), lr=lr, momentum=0.9)
#optimizer = optim.Adam(model.parameters(), lr=lr)
scheduler = lr_scheduler.StepLR(optimizer, 8, gamma=0.1, last_epoch=-1)
log_interval = 100
patience = 50
fit_siamese(siamese_train_loader, siamese_test_loader, model, loss_fn, optimizer, scheduler, patience, n_epoch, cuda, log_interval)
torch.save(model, "data/SiameseNetwork.pt")

model = torch.load("data/SiameseNetwork.pt")
#below for unlabeled data

with open(TEST_CSV) as csv_file:
    with open(WRITE_CSV, 'w') as writeFile:
        csv_reader = csv.reader(csv_file, delimiter=' ')
        for row in csv_reader:
            vec = [float(k) for k in row]
            vec = torch.FloatTensor(vec)
            writeFile.write(' '.join(map(str, model.get_embedding(vec).data.numpy())) + "\n")
        writeFile.close()
# below for labeled data
# TEST_CSV = "../../Data/data/originalVectors-MNIST"
# with open(TEST_CSV) as csv_file:
#     with open('../../Data/data/reducedVectors-siameseNet-MNIST', 'w') as writeFile:
#         csv_reader = csv.reader(csv_file, delimiter=' ')
#         for row in csv_reader:
#             vec = [float(k) for k in row]
#             vec = torch.FloatTensor(vec)
#             writeFile.write(' '.join(map(str, model.get_embedding(vec).data.numpy())) + "\n")
#         writeFile.close()