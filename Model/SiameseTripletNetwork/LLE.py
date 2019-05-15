from sklearn.datasets import load_digits
from sklearn.manifold import LocallyLinearEmbedding
from numpy import genfromtxt
import torch
import numpy as np

num_threads = 8
new_dim = 20
flag = True
for i in range(num_threads):
    TEST_CSV = "../../Data/data/thread-" + str(i)
    temp = genfromtxt(TEST_CSV, delimiter=' ')
    temp = torch.from_numpy(temp)
    if flag:
        X = temp
        flag = False
    else:
        X = np.concatenate((X, temp))
print(X.shape)
embedding = LocallyLinearEmbedding(n_neighbors=10, n_components=new_dim)
X_transformed = embedding.fit_transform(X)
print(X_transformed.shape)
path = '../../Data/data/siamese-reducedVectors-0'
np.savetxt(path, X_transformed, delimiter=" ")
