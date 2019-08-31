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

embedding_dim = int(sys.argv[2])
m = float(sys.argv[1])
dir = "data/Gaussian/"

TRAIN_CSV = dir + "training"
TEST_CSV = dir + "points"
WRITE_CSV = dir + "reducedPoints"

gpus = 0
n_epoch = 20
num_threads = 8
test_size = 0.2

params = {'batch_size': 10240,
          'shuffle': True}
loss_fn = ContrastiveLoss(m)

# Load training set
train_df = pd.read_csv(TRAIN_CSV, delimiter=',', encoding="utf-8-sig")

training_samples = train_df[['P1', 'P2']]
training_labels = train_df[['distance', 'cutoff', 'thisCluster', 'otherCluster']]

input_dim = len(training_samples['P1'][0].split())

training_samples = training_samples.values
training_labels = training_labels.values

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

lr = 1e-3
optimizer = optim.SGD(model.parameters(), lr=lr, momentum=0.9)
scheduler = lr_scheduler.StepLR(optimizer, 8, gamma=0.1, last_epoch=-1)
log_interval = 10
patience = 50
fit_siamese(siamese_train_loader, siamese_test_loader, model, loss_fn, optimizer, scheduler, patience, n_epoch, cuda, log_interval)
torch.save(model, "data/SiameseNetwork.pt")

model = torch.load("data/SiameseNetwork.pt")

with open(TEST_CSV) as csv_file:
    with open(WRITE_CSV, 'w') as writeFile:
        csv_reader = csv.reader(csv_file, delimiter=' ')
        for row in csv_reader:
            vec = [float(k) for k in row]
            vec = torch.FloatTensor(vec)
            writeFile.write(' '.join(map(str, model.get_embedding(vec).data.numpy())) + "\n")
        writeFile.close()