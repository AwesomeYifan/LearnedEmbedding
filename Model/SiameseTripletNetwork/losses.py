import torch
import torch.nn as nn
import torch.nn.functional as F
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


class ContrastiveLossMLP(nn.Module):

    def __init__(self):
        super(ContrastiveLossMLP, self).__init__()
        self.eps = 1e-9
        self.alpha = 0.9 # alpha * accuracy + (1-alpha) * efficiency

    # def forward(self, output1, output2, y, cutoff):
    #     _y = (output2 - output1 + self.eps).pow(2).sum(1).sqrt()
    #     _y = _y.type(torch.FloatTensor)
    #     y = y.type(torch.FloatTensor)
    #     cutoff = cutoff.type(torch.FloatTensor)
    #
    #     mask1 = torch.le(y, cutoff).type(torch.FloatTensor)
    #     losses1 = mask1 * (y - _y + self.eps).pow(2).sqrt()
    #
    #     mask2 = (torch.le(cutoff, y) * torch.le(_y, cutoff)).type(torch.FloatTensor)
    #     losses2 = mask2 * torch.le(_y, cutoff).type(torch.FloatTensor)
    #
    #     scale = (losses1.mean().data.numpy() + self.eps) / (losses2.mean().data.numpy() + self.eps)
    #     accuracy_loss = self.alpha * (losses1 + scale * losses2)
    #
    #     mask3 = (torch.le(cutoff, y) * torch.le(_y, cutoff)).type(torch.FloatTensor)
    #     losses3 = mask3 * torch.le(_y, cutoff).type(torch.FloatTensor)
    #
    #     scale = (losses1.mean().data.numpy() + self.eps) / (losses3.mean().data.numpy() + self.eps)
    #     efficiency_loss = (1 - self.alpha) * losses3
    #
    #     losses = accuracy_loss + efficiency_loss
    #     #losses = accuracy_loss
    #     return losses.mean()
    #     #return losses1.mean()

    def forward(self, output1, output2, y, cutoff):
        #print(output1.data)
        _y = ((output2 - output1).pow(2).sum(1) + self.eps).sqrt().type(torch.FloatTensor)
        y = y.type(torch.FloatTensor)
        cutoff = cutoff.type(torch.FloatTensor)

        mask1 = torch.le(y, cutoff).type(torch.FloatTensor)
        #losses1 = mask1 * (torch.exp(-y) * (_y - y).abs())
        #losses1 = mask1 *  (_y - y).abs()
        losses1 = mask1 * (torch.exp(-y) * _y)

        mask1 = (torch.le(y, cutoff) * torch.le(cutoff, _y)).type(torch.FloatTensor)
        #losses1 = mask1 * (torch.exp(-y) * (_y - cutoff).abs())
        losses1 = mask1 * (_y - cutoff).abs()

        mask2 = (torch.le(cutoff, y) * torch.le(_y, cutoff)).type(torch.FloatTensor)
        losses2 = mask2 * (_y - cutoff).abs()
        #losses2 = mask2 * torch.exp(-_y)

        #losses = self.alpha * losses1 + (1-self.alpha) * losses2
        losses = losses1 + losses2
        #losses = losses1
        #print(str(losses1.mean().data) + str(losses2.mean().data))
        return losses.mean()


class TripletLossMLP(nn.Module):
    """
    Triplet loss
    Takes embeddings of an anchor sample, a positive sample and a negative sample
    """

    def __init__(self):
        super(TripletLossMLP, self).__init__()

    def forward(self, anchor, positive, negative, label, size_average=True):
        distance_positive = (anchor - positive).pow(2).sum(1)  # .pow(.5)
        distance_negative = (anchor - negative).pow(2).sum(1)  # .pow(.5)
        d_p = torch.exp(-distance_positive) / (torch.exp(-distance_positive) + torch.exp(-distance_negative))
        losses = d_p.pow(2).double() * label.double()
        return losses.mean() if size_average else losses.sum()


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
