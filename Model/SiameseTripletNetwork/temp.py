import csv

from lshash import LSHash
from numpy import genfromtxt
import numpy as np
num_threads = 8
old_dim = 128
new_dim = 5
flag = True
lsh = LSHash(new_dim, old_dim)
count = 0
for i in range(num_threads):
    TEST_CSV = "../../Data/data/thread-" + str(i)
    temp = genfromtxt(TEST_CSV, delimiter=' ')
    for x in temp:
        a = [count]
        a.extend(x.tolist())
        lsh.index(a)
        count = count + 1
TEST_CSV = "../../Data/data/thread-0"
temp = genfromtxt(TEST_CSV, delimiter=' ')
with open('../../Data/data/mtree.csv', 'w') as writeFile:
    for x in temp:
        all = lsh.query(x.tolist(), num_results=50)
        for result in all:
            writeFile.write(str(result[0][0]) + ",")
        writeFile.write("\n")

