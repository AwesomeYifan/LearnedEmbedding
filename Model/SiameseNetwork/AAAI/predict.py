import numpy as np
import pandas as pd
import tensorflow as tf

from AAAI.util import ManDist
from AAAI.util import make_w2v_embeddings
from AAAI.util import split_and_zero_padding



# File paths
TEST_CSV = "../../../Data/data/train1.csv"

# Load training set
test_df = pd.read_csv(TEST_CSV)
for q in ['question1', 'question2']:
    test_df[q + '_n'] = test_df[q]

# Make word2vec embeddings
embedding_dim = 100
max_seq_length = 20
test_df, embeddings = make_w2v_embeddings(test_df, embedding_dim=embedding_dim, empty_w2v=False)

# Split to dicts and append zero padding.
X_test = split_and_zero_padding(test_df, max_seq_length)

# Make sure everything is ok
assert X_test['left'].shape == X_test['right'].shape

# --

model = tf.keras.models.load_model('./data/SiameseNetwork.h5', custom_objects={'ManDist': ManDist})
model.summary()

prediction = model.predict([X_test['left'], X_test['right']])
np.savetxt('./data/scores', prediction)
