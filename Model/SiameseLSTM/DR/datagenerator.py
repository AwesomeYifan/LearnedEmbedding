import numpy as np
import keras


class DataGenerator(keras.utils.Sequence):

    def __init__(self, X, labels, batch_size):
        self.X = X
        self.labels = labels
        self.batch_size = batch_size

    def __len__(self):
        return int(np.ceil(len(self.X) / float(self.batch_size)))

    def __getitem__(self, idx):
        batch_x = self.X[idx * self.batch_size:(idx + 1) * self.batch_size]
        batch_y = self.labels[idx * self.batch_size:(idx + 1) * self.batch_size]

        flag = True
        idx = 0
        for index, row in batch_x.iterrows():
            idx += 1
            temp_left = np.array([[float(i) for i in str(row['P1']).split()]])
            temp_right = np.array([[float(i) for i in str(row['P2']).split()]])
            if flag:
                left = temp_left
                right = temp_right
                flag = False
            else:
                left = np.append(left, temp_left, axis=0)
                right = np.append(right, temp_right, axis=0)
        return [left, right], np.array(batch_y)
