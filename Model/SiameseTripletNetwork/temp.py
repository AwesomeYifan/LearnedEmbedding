import numpy as np
import torch
a = np.full((1,5), 3)
a = np.arange(10)
b = np.arange(10)
for tempa, tempb in zip(a, b):
    print(tempa)
    print(tempb)
a = torch.tensor(a)
size = a.size()[0]
print(type(size))

for row in a.split(1):
    newa = np.full((1, size), row.data[0].item())
    newa = (torch.tensor(newa)).type(torch.FloatTensor)
    print(newa)
# b = a.data[5].item()
# print(b)