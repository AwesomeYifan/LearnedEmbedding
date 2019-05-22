import numpy as np
from sklearn.model_selection import train_test_split
from sklearn import datasets
import torch
from sklearn import svm
from numpy import genfromtxt
num_threads = 8
new_dim = 50
flag = True
for i in range(num_threads):
    TEST_CSV = "../../Data/data/thread-" + str(i)
    temp = genfromtxt(TEST_CSV, delimiter=' ')
    temp = torch.from_numpy(temp)
    if flag:
        X = temp
        #flag = False
    else:
        X = torch.cat((X, temp))

X_mean = torch.mean(X, 0)
X = X - X_mean.expand_as(X)
U, S, V = torch.svd(torch.t(X))
for i in range(num_threads):
    TEST_CSV = "../../Data/data/thread-" + str(i)
    temp = genfromtxt(TEST_CSV, delimiter=' ')
    temp = torch.from_numpy(temp)
    temp_mean = torch.mean(temp, 0)
    temp = temp - temp_mean.expand_as(temp)
    C = torch.mm(temp, U[:, :new_dim])
    path = '../../Data/data/siamese-reducedVectors-' + str(i)
    np.savetxt(path, C, delimiter=" ")
