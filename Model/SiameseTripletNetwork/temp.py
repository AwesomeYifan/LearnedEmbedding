import cv2
import numpy as np
import torch

input = torch.tensor([[1., -1.], [1., -1.]]).type(torch.FloatTensor)
standard = torch.zeros(input.size()).type(torch.FloatTensor)
shift = torch.ones(input.size()).type(torch.FloatTensor)
positive_mask = torch.le(standard, input).type(torch.FloatTensor)
negative_mask = torch.le(input, standard).type(torch.FloatTensor)
print(positive_mask * (input + shift) + negative_mask * (input - shift))