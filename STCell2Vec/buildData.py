import json

def computeSim(trace1, trace2):
    common = 0.0
    for i in range(len(trace1)):
        if trace1[i] == trace2[i]:
            common += 1
    return common / len(trace1)

def transferList(list):
    s = ""
    for i in range(len(list)):
        s += list[i] + " "
    return s

file = open("./data/traces")
traces = []
while 1:
    line = file.readline().strip()
    if not line:
        break
    trace = line.split(" ")
    traces.append(trace)
file.close()

scores = []
for i in range(len(traces)):
    scores_of_i = []
    for j in range(len(traces)):
        scores_of_i.append(computeSim(traces[i], traces[j]))
    scores.append(scores_of_i)

file = open("./data/train.csv", "w+")
file.write("id,qid1,qid2,question1,question2,is_duplicate\n")
id = 0
for i in range(len(traces)):
    for j in range(len(traces)):
        file.write(str(id) + "," +
                   str(i) + "," +
                   str(j) + "," +
                   transferList(traces[i]) + "," +
                   transferList(traces[j]) + "," +
                   str(scores[i][j]) + "\n")
        id += 1

file.close()

