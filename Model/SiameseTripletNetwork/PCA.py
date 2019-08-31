import numpy as np
from sklearn.model_selection import train_test_split
from sklearn import datasets
import torch
from sklearn import svm
from numpy import genfromtxt
import matplotlib.pyplot as plt

new_dim = 20
#TEST_CSV = "../../Data/data/originalVectors-" + Dataset
TEST_CSV = "../../Data/data/originalVectors-Cluster"
X = genfromtxt(TEST_CSV, delimiter=' ')
X = torch.from_numpy(X)

X_mean = torch.mean(X, 0)
X = X - X_mean.expand_as(X)
U, S, V = torch.svd(torch.t(X))

temp = genfromtxt(TEST_CSV, delimiter=' ')
temp = torch.from_numpy(temp)
temp_mean = torch.mean(temp, 0)
temp = temp - temp_mean.expand_as(temp)
C = torch.mm(temp, U[:, :new_dim])
path = '../../Data/data/reducedVectors-PCA' + "-Cluster"
C = np.around(C, decimals=4)
np.savetxt(path, C.data, fmt='%.4f', delimiter=" ")


# for i in range(num_threads):
#     TEST_CSV = "../../Data/data/thread-" + str(i)
#     temp = genfromtxt(TEST_CSV, delimiter=' ')
#     temp = torch.from_numpy(temp)
#     if flag:
#         X = temp
#         #flag = False
#     else:
#         X = torch.cat((X, temp))
#
# X_mean = torch.mean(X, 0)
# X = X - X_mean.expand_as(X)
# U, S, V = torch.svd(torch.t(X))
# for i in range(num_threads):
#     TEST_CSV = "../../Data/data/thread-" + str(i)
#     temp = genfromtxt(TEST_CSV, delimiter=' ')
#     temp = torch.from_numpy(temp)
#     temp_mean = torch.mean(temp, 0)
#     temp = temp - temp_mean.expand_as(temp)
#     C = torch.mm(temp, U[:, :new_dim])
#     path = '../../Data/data/siamese-reducedVectors-' + str(i)
#     np.savetxt(path, C, delimiter=" ")
