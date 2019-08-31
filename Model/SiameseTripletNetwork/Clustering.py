from sklearn.cluster import DBSCAN, KMeans
import numpy as np
from numpy import genfromtxt
dir = "../../Data/data/Gaussian/"
TEST_CSV = dir + "points"
TEST_CSV = dir + "reducedPoints"
X = genfromtxt(TEST_CSV, delimiter=' ')
#clusters = DBSCAN(eps=0.1, min_samples=50).fit(X)
clusters = KMeans(n_clusters=2, random_state=0).fit(X)
print(clusters.labels_)
with open(dir + "clusterInfo", 'w') as writeFile:
    writeFile.write('\n'.join(map(str, clusters.labels_)))
    writeFile.close()
