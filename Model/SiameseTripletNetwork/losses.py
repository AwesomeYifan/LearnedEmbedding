import torch
import torch.nn as nn
import torch.nn.functional as F
import numpy as np
from torch.autograd import Variable
import math

# class ContrastiveLossMLP(nn.Module):
#     """
#     Contrastive loss
#     Takes embeddings of two samples and a target label == 1 if samples are from the same class and label == 0 otherwise
#     """
#
#     def __init__(self):
#         super(ContrastiveLossMLP, self).__init__()
#
#     #follows the input format?
#     def forward(self, output1, output2, target, size_average=True):
#         distances = (output2 - output1).pow(2).sum(1)  # squared distances
#         losses = 0.5 * (target.float() * distances)
#         return losses.mean() if size_average else losses.sum()

'''
class ContrastiveLossMLP(nn.Module):
    """
    Contrastive loss
    Takes embeddings of two samples and a target label == 1 if samples are from the same class and label == 0 otherwise
    """

    def __init__(self):
        super(ContrastiveLossMLP, self).__init__()

    def forward(self, output1, output2, y):
        _y = (output2 - output1).pow(2).sum(1).sqrt()
        mask1 = (y < 0.4).type(torch.FloatTensor)
        losses1 = mask1 * (y.float() - _y).abs()
        mask2 = ((_y < 0.8) * (y >= 0.4)).type(torch.FloatTensor)
        losses2 = mask2 * (0.8 - _y)
        loss = losses1 + losses2
        return loss.mean()
'''

'''
class ContrastiveLossMLP(nn.Module):

    def __init__(self):
        super(ContrastiveLossMLP, self).__init__()
        self.eps = 1e-9
        self.alpha = 0.9 # alpha * accuracy + (1-alpha) * efficiency

    def forward(self, output1, output2, y, cutoff):
        #print(output1.data)
        _y = ((output2 - output1).pow(2).sum(1) + self.eps).sqrt()
        _y = _y.type(torch.FloatTensor)
        y = y.type(torch.FloatTensor)
        cutoff = cutoff.type(torch.FloatTensor)

        mask1 = torch.le(y, cutoff).type(torch.FloatTensor)
        losses1 = (mask1 * (y - _y).pow(2) + self.eps).sqrt()

        mask2 = (torch.le(cutoff, y) * torch.le(_y, cutoff)).type(torch.FloatTensor)
        losses2 = mask2 * torch.le(_y, cutoff).type(torch.FloatTensor)

        #scale = (losses1.mean().data.numpy() + self.eps) / (losses2.mean().data.numpy() + self.eps)
        #accuracy_loss = self.alpha * (losses1 + scale * losses2)
        accuracy_loss = self.alpha * losses1
        #print(str(y < cutoff) + "-label: " + str(y) + "-pred: " + str(_y))
        mask3 = (torch.le(cutoff, y) * torch.le(_y, cutoff)).type(torch.FloatTensor) # highlight points which should be distant but is actually within cutoff
        losses3 = mask3 * torch.le(_y, cutoff).type(torch.FloatTensor)
        losses3 = mask3 * (cutoff - _y)

        scale = (losses1.mean().data.numpy() + self.eps) / (losses3.mean().data.numpy() + self.eps)
        #efficiency_loss = (1 - self.alpha) * scale * losses3
        efficiency_loss = (1 - self.alpha) * losses3
        efficiency_loss = Variable(efficiency_loss.data, requires_grad=True)
        #print(str(accuracy_loss.mean()) + "-" + str(efficiency_loss.mean()))

        #losses = accuracy_loss + efficiency_loss
        #losses = accuracy_loss
        losses = efficiency_loss
        if(y.data.numpy() > cutoff.data.numpy() and _y.data.numpy() < cutoff.data.numpy()):
            print(str(cutoff) + "-" + str(_y))
        return losses.mean()
        #return losses1.mean()
'''


class ContrastiveLossHard(nn.Module):
    def __init__(self):
        super(ContrastiveLossHard, self).__init__()
        self.delta = 1e-9
        self.alpha = 0.1

    def forward(self, output1, output2, y, cutoff):
        _y = ((output2 - output1).pow(2).sum(1) + self.delta).sqrt().type(torch.FloatTensor)
        y = y.type(torch.FloatTensor)
        cutoff = cutoff.type(torch.FloatTensor)

        weights = cutoff / y
        weights = weights * weights * weights

        mask1 = torch.le(y, cutoff).type(torch.FloatTensor)
        losses1 = mask1 * (_y - y).abs()

        mask2 = torch.le(cutoff, y).type(torch.FloatTensor)
        losses2 = mask2 * F.relu(cutoff - _y)

        #losses = self.alpha * weights * losses1 + (1 - self.alpha) * losses2
        losses = self.alpha * losses1 + (1 - self.alpha) * losses2
        return losses.mean()


class ContrastiveLossSoft(nn.Module):
    def __init__(self, eps):
        super(ContrastiveLossSoft, self).__init__()
        self.delta = 1e-9
        self.eps = eps
        self.alpha = 0.1  # alpha * accuracy + (1-alpha) * efficiency
        self.weight_scale = 1.0

    def forward(self, output1, output2, y, cutoff):
        _y = ((output2 - output1).pow(2).sum(1) + self.delta).sqrt().type(torch.FloatTensor)
        y = y.type(torch.FloatTensor)
        cutoff = cutoff.type(torch.FloatTensor)

        weights = cutoff / y
        weights = weights * weights * weights
        #weights = torch.exp(self.weight_scale * weights)

        mask1 = torch.le(y, cutoff).type(torch.FloatTensor)
        losses1 = mask1 * (F.relu(y - _y) + F.relu(_y - (1 + self.eps) * y))

        mask2 = torch.le(cutoff, y).type(torch.FloatTensor)
        losses2 = mask2 * F.relu((1 + self.eps + self.delta) * cutoff - _y)

        losses = self.alpha * weights * losses1 + (1-self.alpha) * losses2
        return losses.mean()


# class ContrastiveLossSoftNN(nn.Module):
#     def __init__(self):
#         super(ContrastiveLossSoftNN, self).__init__()
#         self.T = 1.5
#         self.delta = 1e-9
#
#     def forward(self, output1, output2, y1, y2):
#         b = y1.size()[0]
#         loss = Variable(torch.zeros(1, 1), requires_grad=True)
#         for row1, label1 in zip(output1.split(1), y1.split(1)):
#             diff = ((row1 - output2).pow(2).sum(1) + self.delta).sqrt()
#             diff = torch.exp(-diff / self.T)
#             class_agree_mask = torch.eq(label1, y2).type(torch.FloatTensor)
#             nomi = (diff * class_agree_mask).sum() + self.delta
#             # print(diff)
#             denomi = diff.sum() + self.delta
#             loss = loss + torch.log(nomi / denomi)
#         return -loss / b


class ContrastiveLoss(nn.Module):
    """
    Contrastive loss
    Takes embeddings of two samples and a target label == 1 if samples are from the same class and label == 0 otherwise
    """

    def __init__(self, margin):
        super(ContrastiveLoss, self).__init__()
        self.margin = margin
        self.eps = 1e-9

    def forward(self, output1, output2, target, size_average=True):
        distances = (output2 - output1).pow(2).sum(1)  # squared distances
        losses = 0.5 * (target.float() * distances +
                        (1 + -1 * target).float() * F.relu(self.margin - (distances + self.eps).sqrt()).pow(2))
        return losses.mean() if size_average else losses.sum()


class TripletLossMLP(nn.Module):
    """
    Triplet loss
    Takes embeddings of an anchor sample, a positive sample and a negative sample
    """

    def __init__(self):
        super(TripletLossMLP, self).__init__()
        self.eps = 1e-9
        self.margin = 1e-9

    def forward(self, anchor, positive, negative, size_average=True):
        distance_positive = (anchor - positive).pow(2).sum(1)  # .pow(.5)
        distance_negative = (anchor - negative).pow(2).sum(1)  # .pow(.5)
        losses = F.relu(distance_positive - distance_negative + self.margin)
        return losses.mean() if size_average else losses.sum()


class MultipletLoss(nn.Module):
    """
    Triplet loss
    Takes embeddings of an anchor sample, a positive sample and a negative sample
    """

    def __init__(self):
        super(MultipletLoss, self).__init__()
        self.eps = 1e-9
        self.margin = 1e-9
        self.loss_scale = 1000

    def forward(self, anchor, others):
        loss = 0
        for batch_id in range(len(others[0])):
            rank_dict = {}
            idx = 0
            for nn_id in range(len(others)):
                distance = (anchor[batch_id] - others[nn_id][batch_id]).pow(2).sum()
                rank_dict[distance] = idx
                idx += 1
            idx = 0
            for key in sorted(rank_dict.keys()):
                loss += abs(rank_dict[key] - idx)
                idx += 1

        loss /= self.loss_scale
        loss = Variable(torch.tensor(loss), requires_grad=True)
        return loss


class TripletLoss(nn.Module):
    """
    Triplet loss
    Takes embeddings of an anchor sample, a positive sample and a negative sample
    """

    def __init__(self, margin):
        super(TripletLoss, self).__init__()
        self.margin = margin

    def forward(self, anchor, positive, negative, size_average=True):
        distance_positive = (anchor - positive).pow(2).sum(1)  # .pow(.5)
        distance_negative = (anchor - negative).pow(2).sum(1)  # .pow(.5)
        losses = F.relu(distance_positive - distance_negative + self.margin)
        return losses.mean() if size_average else losses.sum()


class OnlineContrastiveLoss(nn.Module):
    """
    Online Contrastive loss
    Takes a batch of embeddings and corresponding labels.
    Pairs are generated using pair_selector object that take embeddings and targets and return indices of positive
    and negative pairs
    """

    def __init__(self, margin, pair_selector):
        super(OnlineContrastiveLoss, self).__init__()
        self.margin = margin
        self.pair_selector = pair_selector

    def forward(self, embeddings, target):
        positive_pairs, negative_pairs = self.pair_selector.get_pairs(embeddings, target)
        if embeddings.is_cuda:
            positive_pairs = positive_pairs.cuda()
            negative_pairs = negative_pairs.cuda()
        positive_loss = (embeddings[positive_pairs[:, 0]] - embeddings[positive_pairs[:, 1]]).pow(2).sum(1)
        negative_loss = F.relu(
            self.margin - (embeddings[negative_pairs[:, 0]] - embeddings[negative_pairs[:, 1]]).pow(2).sum(
                1).sqrt()).pow(2)
        loss = torch.cat([positive_loss, negative_loss], dim=0)
        return loss.mean()


class OnlineTripletLoss(nn.Module):
    """
    Online Triplets loss
    Takes a batch of embeddings and corresponding labels.
    Triplets are generated using triplet_selector object that take embeddings and targets and return indices of
    triplets
    """

    def __init__(self, margin, triplet_selector):
        super(OnlineTripletLoss, self).__init__()
        self.margin = margin
        self.triplet_selector = triplet_selector

    def forward(self, embeddings, target):
        triplets = self.triplet_selector.get_triplets(embeddings, target)

        if embeddings.is_cuda:
            triplets = triplets.cuda()

        ap_distances = (embeddings[triplets[:, 0]] - embeddings[triplets[:, 1]]).pow(2).sum(1)  # .pow(.5)
        an_distances = (embeddings[triplets[:, 0]] - embeddings[triplets[:, 2]]).pow(2).sum(1)  # .pow(.5)
        losses = F.relu(ap_distances - an_distances + self.margin)

        return losses.mean(), len(triplets)
