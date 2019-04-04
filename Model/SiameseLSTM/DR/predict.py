import pandas as pd
import numpy as np
import tensorflow as tf

from DR.util import ManDist
from DR.util import reformat

model = tf.keras.models.load_weights('./data/SiameseLSTM.h5', custom_objects={'ManDist': ManDist})
model.summary()

# File paths
TEST_CSV = "../../../Data/data/train.csv"

# Load training set
test_df = pd.read_csv(TEST_CSV)
X = test_df[['P1', 'P2']]
X = reformat(X)

prediction = model.predict([X['P1'], X['P2']])
np.savetxt('./data/scores', prediction)
