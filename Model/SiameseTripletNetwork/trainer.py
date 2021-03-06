import torch
import numpy as np
from pytorchtools import EarlyStopping
import Parameters


def fit_siamese(train_loader, val_loader, model, loss_fn, optimizer, scheduler, patience, n_epochs, cuda, log_interval, metrics=[],
        start_epoch=0):
    """
    Loaders, model, loss function and metrics should work together for a given task,
    i.e. The model should be able to process data output of loaders,
    loss function should process target output of loaders and outputs from the model

    Examples: Classification: batch loader, classification model, NLL loss, accuracy metric
    Siamese network: Siamese loader, siamese model, contrastive loss
    Online triplet learning: batch loader, embedding model, online triplet loss
    """
    for epoch in range(0, start_epoch):
        scheduler.step()
    early_stopping = EarlyStopping(patience=patience, verbose=True)
    for epoch in range(start_epoch, n_epochs):
        scheduler.step()

        # Train stage
        train_loss, metrics = train_siamese(train_loader, model, loss_fn, optimizer, cuda, log_interval, metrics)
        Parameters.epoch += 1

        message = 'Epoch: {}/{}. Train set: Average loss: {:.4f}'.format(epoch + 1, n_epochs, train_loss)
        for metric in metrics:
            message += '\t{}: {}'.format(metric.name(), metric.value())

        val_loss, metrics = test_siamese(val_loader, model, loss_fn, cuda, metrics)
        val_loss /= len(val_loader)

        early_stopping(val_loss, model)

        for param in model.parameters():
            print(param.data)

        if early_stopping.early_stop:
            print("Early stopping")
            break

        message += '\nEpoch: {}/{}. Validation set: Average loss: {:.4f}'.format(epoch + 1, n_epochs,
                                                                                 val_loss)
        for metric in metrics:
            message += '\t{}: {}'.format(metric.name(), metric.value())

        print(message)

def fit_triplet(train_loader, val_loader, model, loss_fn, optimizer, scheduler, patience, n_epochs, cuda, log_interval, metrics=[],
        start_epoch=0):
    """
    Loaders, model, loss function and metrics should work together for a given task,
    i.e. The model should be able to process data output of loaders,
    loss function should process target output of loaders and outputs from the model

    Examples: Classification: batch loader, classification model, NLL loss, accuracy metric
    Siamese network: Siamese loader, siamese model, contrastive loss
    Online triplet learning: batch loader, embedding model, online triplet loss
    """
    for epoch in range(0, start_epoch):
        scheduler.step()
    early_stopping = EarlyStopping(patience=patience, verbose=True)
    for epoch in range(start_epoch, n_epochs):
        scheduler.step()

        # Train stage
        train_loss, metrics = train_triplet(train_loader, model, loss_fn, optimizer, cuda, log_interval, metrics)

        message = 'Epoch: {}/{}. Train set: Average loss: {:.4f}'.format(epoch + 1, n_epochs, train_loss)
        for metric in metrics:
            message += '\t{}: {}'.format(metric.name(), metric.value())

        val_loss, metrics = test_triplet(val_loader, model, loss_fn, cuda, metrics)
        val_loss /= len(val_loader)

        early_stopping(val_loss, model)

        #for param in model.parameters():
        #    print(param.data)

        if early_stopping.early_stop:
            print("Early stopping")
            break

        message += '\nEpoch: {}/{}. Validation set: Average loss: {:.4f}'.format(epoch + 1, n_epochs,
                                                                                 val_loss)
        for metric in metrics:
            message += '\t{}: {}'.format(metric.name(), metric.value())

        print(message)


def train_siamese(train_loader, model, loss_fn, optimizer, cuda, log_interval, metrics):
    for metric in metrics:
        metric.reset()

    model.train()
    losses = []
    total_loss = 0

    #this line for siamese
    #for batch_idx, (data, target, threshold) in enumerate(train_loader):
    #this line for triplet
    for batch_idx, (data, target, threshold, thisCluster, otherCluster) in enumerate(train_loader):
        target = target if len(target) > 0 else None
        threshold = threshold if len(threshold) > 0 else None
        #weight = weight if len(weight) > 0 else None
        # mindist = mindist if len(mindist) > 0 else None
        if not type(data) in (tuple, list):
            data = (data,)
        if cuda:
            data = tuple(d.cuda() for d in data)
            if target is not None:
                target = target.cuda()
            if threshold is not None:
                threshold = threshold.cuda()
            if thisCluster is not None:
                thisCluster = thisCluster.cuda()
            if otherCluster is not None:
                otherCluster = otherCluster.cuda()
            #if weight is not None:
                #weight = weight.cuda()
            # if mindist is not None:
            #     mindist = mindist.cuda()


        optimizer.zero_grad()
        outputs = model(*data)

        if type(outputs) not in (tuple, list):
            outputs = (outputs,)

        loss_inputs = outputs
        if target is not None:
            target = (target,)
            loss_inputs += target

        if threshold is not None:
            threshold = (threshold,)
            loss_inputs += threshold
        if thisCluster is not None:
            thisCluster = (thisCluster,)
            loss_inputs += thisCluster
        if otherCluster is not None:
            otherCluster = (otherCluster,)
            loss_inputs += otherCluster
        # if weight is not None:
        #     weight = (weight,)
        #     loss_inputs += weight

        # if mindist is not None:
        #     mindist = (mindist,)
        #     loss_inputs += mindist

        loss_outputs = loss_fn(*loss_inputs)
        loss = loss_outputs[0] if type(loss_outputs) in (tuple, list) else loss_outputs
        losses.append(loss.item())
        total_loss += loss.item()
        loss.backward()
        optimizer.step()

        #for metric in metrics:
        #    metric(outputs, target, loss_outputs)

        if batch_idx % log_interval == 0:
            message = 'Train: [{}/{} ({:.0f}%)] Loss: {:.6f}'.format(
                batch_idx * len(data[0]), len(train_loader.dataset),
                100. * batch_idx / len(train_loader), np.mean(losses))
            for metric in metrics:
                message += '\t{}: {}'.format(metric.name(), metric.value())

            print(message)
            losses = []

    total_loss /= (batch_idx + 1)
    return total_loss, metrics

def train_triplet(train_loader, model, loss_fn, optimizer, cuda, log_interval, metrics):
    for metric in metrics:
        metric.reset()

    model.train()
    losses = []
    total_loss = 0

    for batch_idx, data in enumerate(train_loader):
        if not type(data) in (tuple, list):
            data = (data,)
        if cuda:
            data = tuple(d.cuda() for d in data)

        optimizer.zero_grad()
        outputs = model(*data)

        if type(outputs) not in (tuple, list):
            outputs = (outputs,)

        loss_inputs = outputs

        loss_outputs = loss_fn(*loss_inputs)
        loss = loss_outputs[0] if type(loss_outputs) in (tuple, list) else loss_outputs
        losses.append(loss.item())
        total_loss += loss.item()
        loss.backward()
        optimizer.step()

        #for metric in metrics:
        #    metric(outputs, target, loss_outputs)

        if batch_idx % log_interval == 0:
            message = 'Train: [{}/{} ({:.0f}%)] Loss: {:.6f}'.format(
                batch_idx * len(data[0]), len(train_loader.dataset),
                100. * batch_idx / len(train_loader), np.mean(losses))
            for metric in metrics:
                message += '\t{}: {}'.format(metric.name(), metric.value())

            print(message)
            losses = []

    total_loss /= (batch_idx + 1)
    return total_loss, metrics

def test_siamese(val_loader, model, loss_fn, cuda, metrics):
    with torch.no_grad():
        for metric in metrics:
            metric.reset()
        model.eval()
        val_loss = 0
        for batch_idx, (data, target, threshold, thisCluster, otherCluster) in enumerate(val_loader):
            target = target if len(target) > 0 else None
            threshold = threshold if len(threshold) > 0 else None
            #weight = weight if len(weight) > 0 else None
            # mindist = mindist if len(mindist) > 0 else None
            if not type(data) in (tuple, list):
                data = (data,)
            if cuda:
                data = tuple(d.cuda() for d in data)
                if target is not None:
                    target = target.cuda()
                if threshold is not None:
                    threshold = threshold.cuda()
                if thisCluster is not None:
                    thisCluster = thisCluster.cuda()
                if otherCluster is not None:
                    otherCluster = otherCluster.cuda()
                #if weight is not None:
                #    weight = weight.cuda()
                # if mindist is not None:
                #     mindist = mindist.cuda()

            outputs = model(*data)

            if type(outputs) not in (tuple, list):
                outputs = (outputs,)
            loss_inputs = outputs
            if target is not None:
                target = (target,)
                loss_inputs += target

            if threshold is not None:
                threshold = (threshold,)
                loss_inputs += threshold

            if thisCluster is not None:
                thisCluster = (thisCluster,)
                loss_inputs += thisCluster

            if otherCluster is not None:
                otherCluster = (otherCluster,)
                loss_inputs += otherCluster

            # if weight is not None:
            #     weight = (weight,)
            #     loss_inputs += weight

            # if mindist is not None:
            #     mindist = (mindist,)
            #     loss_inputs += mindist

            loss_outputs = loss_fn(*loss_inputs)
            loss = loss_outputs[0] if type(loss_outputs) in (tuple, list) else loss_outputs
            val_loss += loss.item()

            #for metric in metrics:
            #    metric(outputs, target, loss_outputs)

    return val_loss, metrics

def test_triplet(val_loader, model, loss_fn, cuda, metrics):
    with torch.no_grad():
        for metric in metrics:
            metric.reset()
        model.eval()
        val_loss = 0
        for batch_idx, data in enumerate(val_loader):
            if not type(data) in (tuple, list):
                data = (data,)
            if cuda:
                data = tuple(d.cuda() for d in data)

            outputs = model(*data)

            if type(outputs) not in (tuple, list):
                outputs = (outputs,)
            loss_inputs = outputs

            loss_outputs = loss_fn(*loss_inputs)
            loss = loss_outputs[0] if type(loss_outputs) in (tuple, list) else loss_outputs
            val_loss += loss.item()

            #for metric in metrics:
            #    metric(outputs, target, loss_outputs)

    return val_loss, metrics
