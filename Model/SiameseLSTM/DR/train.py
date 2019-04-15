from time import time

import matplotlib.pyplot as plt
import matplotlib
import pandas as pd
import numpy as np
import keras

from sklearn.model_selection import train_test_split

from keras.models import Sequential
from keras.layers import Dense, Activation, Input
from keras.models import Model

from util import ManDist
from util import reformat
from util import euc_dist
from datagenerator import DataGenerator

'''
The complexity of the network has great influence on the accuracy,
generally, when the original dimension is small, the more simple the 
network is, the more accurate it is. The best is of course let the
reduced dimension equals the original dimension and use single layer
to keep the vectors unchanged.
'''
# global variables
DR_dim = 2
gpus = 0
n_epoch = 100
params = {'batch_size': 16,
          'shuffle': True}

matplotlib.use('Agg')
# File paths
TRAIN_CSV = "../../../Data/data/train.csv"

# Load training set
train_df = pd.read_csv(TRAIN_CSV)

# Split to train validation
validation_size = int(len(train_df) * 0.1)
training_size = len(train_df) - validation_size

X = train_df[['P1', 'P2']]
Y = train_df['dist']

X_train, X_validation, Y_train, Y_validation = train_test_split(X, Y, test_size=validation_size)

# Reformt data to ndarray to feed into the ML model
#X_train = reformat(X_train)
#X_validation = reformat(X_validation)
#batch_size = 100

training_generator = DataGenerator(X_train, Y_train, **params)
validation_generator = DataGenerator(X_validation, Y_validation, **params)

# print(X_train)
#input_dim = X_train[0][0].size
input_dim=len(X['P1'][0].split())
# Model variables

# Define the shared model
x = Sequential()
#x.add(Dense(20,activation='relu'))
x.add(Dense(DR_dim,activation='relu'))
shared_model = x

# The visible layer
left_input = Input(shape=(input_dim,), dtype='float32')
right_input = Input(shape=(input_dim,), dtype='float32')

# Pack it all up into a Manhattan Distance model
malstm_distance = ManDist()([shared_model(left_input), shared_model(right_input)])
model = Model(inputs=[left_input, right_input], outputs=[malstm_distance])

if gpus >= 2:
    model = keras.utils.multi_gpu_model(model, gpus=gpus)

#model.compile(loss='mean_squared_error', optimizer='sgd', metrics=['mse'])
model.compile(loss='mean_squared_error', optimizer='sgd')
#model.summary()

# Start trainings
training_start_time = time()

#malstm_trained = model.fit(X_train, Y_train,
#                           batch_size=batch_size, epochs=n_epoch,
#                           validation_data=(X_validation, Y_validation))

callbacks = [keras.callbacks.EarlyStopping(monitor='val_loss', min_delta=0,
                                          patience=10, verbose=0, mode='auto',
                                          baseline=None, restore_best_weights=False)]

malstm_trained = model.fit_generator(generator=training_generator,
                    validation_data=validation_generator,
                    epochs=n_epoch,
                    use_multiprocessing=False,
                    workers=1,
                    callbacks=callbacks)


training_end_time = time()

#print(model.get_weights())


# Load entire set
ORIGIN_CSV = "../../../Data/data/origin.csv"
origin_df = pd.read_csv(ORIGIN_CSV)
origin_data = origin_df[['P1', 'P2']]
origin_data = reformat(origin_data)
num_layers=len(model.layers)
desired_layer = Model(inputs=model.inputs,outputs=model.layers[num_layers-1].get_input_at(0))
dr_start_time = time()
desired_output = desired_layer.predict(origin_data)
dr_end_time = time()
np.savetxt('./data/reducedVectors.txt', desired_output[0])

# Plot loss
#plt.subplot(212)
plt.plot(malstm_trained.history['loss'])
plt.plot(malstm_trained.history['val_loss'])
plt.title('Model Loss')
plt.ylabel('Loss')
plt.xlabel('Epoch')
plt.legend(['Train', 'Validation'], loc='upper right')

plt.tight_layout(h_pad=1.0)
plt.savefig('./data/history-graph.png')