from keras.models import Sequential
from keras.layers import Dense, Activation

from keras.models import Model

model = Sequential()
model.add(Dense(32, activation='relu', input_dim=100))
model.add(Dense(16, activation='relu', name="Dense_1"))
model.add(Dense(1, activation='sigmoid', name="Dense_2"))
model.compile(optimizer='rmsprop',
              loss='binary_crossentropy',
              metrics=['accuracy'])


import numpy as np

data = np.random.random((1000, 100))
print("##################################################")
print(data.shape)
print("##################################################")
labels = np.random.randint(2, size=(1000, 1))

model.fit(data, labels, epochs=10, batch_size=32)

dense1_layer_model = Model(inputs=model.input,
                           outputs=model.get_layer(index = 1).output)

dense1_output = dense1_layer_model.predict(data)
print("##################################################")
print(dense1_output)