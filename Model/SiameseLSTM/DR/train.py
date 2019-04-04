from time import time

import matplotlib
import pandas as pd
import numpy as np
import keras
import theano

import matplotlib.pyplot as plt

from sklearn.model_selection import train_test_split
from keras import backend as K

import tensorflow as tf

from keras.models import Sequential
from keras.layers import Dense, Activation, Input
from keras.models import Model

#from tensorflow.python.keras.models import Model, Sequential
#from tensorflow.python.keras.layers import Input, Embedding, LSTM
#from tensorflow.python.keras.layers.core import Dense, Activation, Dropout

from util import ManDist
from util import reformat

matplotlib.use('Agg')
# File paths
TRAIN_CSV = "../../../Data/data/train.csv"

#print(os.path.abspath(TRAIN_CSV))

# Load training set
train_df = pd.read_csv(TRAIN_CSV)

# Split to train validation
validation_size = int(len(train_df) * 0.1)
training_size = len(train_df) - validation_size

X = train_df[['P1', 'P2']]
Y = train_df['dist']

X_train, X_validation, Y_train, Y_validation = train_test_split(X, Y, test_size=validation_size)

# Reformt data to ndarray to feed into the ML model
X_train = reformat(X_train)
X_validation = reformat(X_validation)
#print(X_train)
input_dim = 2
# Model variables
gpus = 0
batch_size = 1024 * (gpus+1)
n_epoch = 5

# Define the shared model
x = Sequential()
#x.add(Dense(4))
#x.add(Activation('relu'))
x.add(Dense(5,activation='softmax',name="Dense_1"))

shared_model = x

# The visible layer
left_input = Input(shape=(input_dim,), dtype='float32')
right_input = Input(shape=(input_dim,), dtype='float32')

# Pack it all up into a Manhattan Distance model
malstm_distance = ManDist()([shared_model(left_input), shared_model(right_input)])
model = Model(inputs=[left_input, right_input], outputs=[malstm_distance])

if gpus >= 2:
    model = keras.utils.multi_gpu_model(model, gpus=gpus)

model.compile(loss='mean_squared_error', optimizer='sgd', metrics=['accuracy'])
model.summary()
#shared_model.summary()

# Start trainings
training_start_time = time()

malstm_trained = model.fit(X_train, Y_train,
                           batch_size=batch_size, epochs=n_epoch,
                           validation_data=(X_validation, Y_validation))

training_end_time = time()

dense1_layer_model = Model(inputs=model.input,outputs=model.layers[2].get_output_at(0))
print(dense1_layer_model)
left = np.random.random((2,2))
right = np.random.random((2,2))
temp_data = [left,right]
dense1_output = dense1_layer_model.predict(temp_data)
print(dense1_output)

#get_activations = theano.function([model.layers[0].input,model.layers[1].input], model.layers[2].get_output_at(0), allow_input_downcast=True)
#activations = get_activations(X_train) # same result as above

layer_output = model.layers[2].get_output_at(0)
print(layer_output)

TEST_CSV = "../../../Data/data/train.csv"

# Load training set
test_df = pd.read_csv(TEST_CSV)
X = test_df[['P1', 'P2']]
X = reformat(X)