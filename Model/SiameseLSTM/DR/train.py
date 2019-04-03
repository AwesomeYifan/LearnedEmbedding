import os
from time import time
#from keras.models import Sequential
#from keras.utils import np_utils
#from keras.layers.core import Dense, Activation, Dropout

import pandas as pd
import numpy as np

import matplotlib
import pandas as pd

matplotlib.use('Agg')
import matplotlib.pyplot as plt

from sklearn.model_selection import train_test_split

import tensorflow as tf

from tensorflow.python.keras.models import Model, Sequential
from tensorflow.python.keras.layers import Input, Embedding, LSTM
from tensorflow.python.keras.layers.core import Dense, Activation, Dropout

from util import split_and_zero_padding
from util import ManDist
from util import reformat
#import time

# File paths
TRAIN_CSV = "../../../Data/data/train.csv"

#print(os.path.abspath(TRAIN_CSV))

# Load training set
train_df = pd.read_csv(TRAIN_CSV)
#for q in ['P1', 'P2']:
#    train_df[q + '_n'] = train_df[q]
#print(train_df)


# Make word2vec embeddings
#embedding_dim = 100
#input_dim = 20
#use_w2v = True
#train_df, embeddings = make_w2v_embeddings(train_df, embedding_dim=embedding_dim, empty_w2v=not use_w2v)

# Split to train validation
validation_size = int(len(train_df) * 0.1)
training_size = len(train_df) - validation_size

X = train_df[['P1', 'P2']]
Y = train_df['dist']
#print(X)
#time.sleep(10)
X_train, X_validation, Y_train, Y_validation = train_test_split(X, Y, test_size=validation_size)
X_train = reformat(X_train)
#print(X_train)
X_validation = reformat(X_validation)
'''
X_train['P1'] = X_train['P1'].values
X_train['P2'] = X_train['P2'].values
X_validation['P1'] = X_validation['P1'].values
X_validation['P2'] = X_validation['P2'].values
'''

#X_train = split_and_zero_padding(X_train)
#X_validation = split_and_zero_padding(X_validation)

#print(X_validation)
#print("##################################################")
#input_dim = 2
#X_train = split_and_zero_padding(X_train, input_dim)
#X_validation = split_and_zero_padding(X_validation, input_dim)

#time.sleep(100)
# Convert labels to their numpy representations

# Make sure everything is ok
#assert X_train['P1'].shape == X_train['P2'].shape
#assert len(X_train['P1']) == len(Y_train)
'''
train_left = np.asarray(X_train['P1'])
#print(train_left.shape)
#print(train_left)
train_left.transpose()
#print(train_left.shape)
train_right = np.asarray(X_train['P2'])
train_right.transpose()
train_label = np.asarray(Y_train)
test_left = np.asarray(X_validation['P1'])
test_left.transpose()
test_right = np.asarray(X_validation['P2'])
test_right.transpose()
test_label = np.asarray(Y_validation)
'''
# --
input_dim = 10
# Model variables
gpus = 0
batch_size = 1024 * (gpus+1)
n_epoch = 2
n_hidden = 50

# Define the shared model
x = Sequential()
x.add(Dense(128))
x.add(Activation('relu'))
#model.add(Dropout(0.15))
x.add(Dense(5))
x.add(Activation('softmax'))

shared_model = x

# The visible layer
left_input = Input(shape=(input_dim,), dtype='float32')
right_input = Input(shape=(input_dim,), dtype='float32')

# Pack it all up into a Manhattan Distance model
malstm_distance = ManDist()([shared_model(left_input), shared_model(right_input)])
#print(malstm_distance)
#time.sleep(5000)
model = Model(inputs=[left_input, right_input], outputs=[malstm_distance])

if gpus >= 2:
    # `multi_gpu_model()` is a so quite buggy. it breaks the saved model.
    model = tf.keras.utils.multi_gpu_model(model, gpus=gpus)
model.compile(loss='mean_squared_error', optimizer=tf.keras.optimizers.Adam(), metrics=['accuracy'])
model.summary()
shared_model.summary()

# Start trainings
training_start_time = time()
#print(X_validation['P1'] + "&&&&&&")
#train_data = np.asarray([train_left, train_right])
#test_data = np.asarray([test_left,test_right])
#print(train_left.shape)
#print(test_right.shape)
#print(train_left)
#print(test_data.shape)

malstm_trained = model.fit(X_train, Y_train,
                           batch_size=batch_size, epochs=n_epoch,
                           validation_data=(X_validation, Y_validation))

training_end_time = time()
print("Training time finished.\n%d epochs in %12.2f" % (n_epoch,
                                                        training_end_time - training_start_time))

model.save('./data/SiameseLSTM.h5')

# Plot accuracy
plt.subplot(211)
plt.plot(malstm_trained.history['acc'])
plt.plot(malstm_trained.history['val_acc'])
plt.title('Model Accuracy')
plt.ylabel('Accuracy')
plt.xlabel('Epoch')
plt.legend(['Train', 'Validation'], loc='upper left')

# Plot loss
plt.subplot(212)
plt.plot(malstm_trained.history['loss'])
plt.plot(malstm_trained.history['val_loss'])
plt.title('Model Loss')
plt.ylabel('Loss')
plt.xlabel('Epoch')
plt.legend(['Train', 'Validation'], loc='upper right')

plt.tight_layout(h_pad=1.0)
plt.savefig('./data/history-graph.png')

print(str(malstm_trained.history['val_acc'][-1])[:6] +
      "(max: " + str(max(malstm_trained.history['val_acc']))[:6] + ")")
print("Done.")
