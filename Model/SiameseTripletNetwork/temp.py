from torchvision.datasets import MNIST
from torchvision import transforms
import torch
a = torch.tensor([3, 4]).type(torch.FloatTensor)
b = a.pow(2).sum(-1).sqrt()
print(a)
print(b)