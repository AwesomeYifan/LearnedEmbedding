from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn import datasets
from numpy import genfromtxt
import numpy as np

new_dim = 2

TEST_CSV = "../../Data/data/MNIST/mnist_train.csv"
temp = genfromtxt(TEST_CSV, delimiter=',')
y = temp[:,0]
X = temp[:,1:]
train_X = X[0:10000,:]
train_y = y[0:10000]

lda = LinearDiscriminantAnalysis(n_components=new_dim)
#reduced_vectors = lda.fit(X, y).transform(X)
reduced_vectors = lda.fit(train_X, train_y).transform(X)
print(reduced_vectors.shape)
path = '../../Data/data/reducedVectors-LDA'
np.savetxt(path, reduced_vectors, delimiter=" ")
