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

scores = [[0 for x in range(len(traces))] for y in range (len(traces))]
for i in range(len(traces)):
    print("computing score for the " + str(i) + "th entity...")
    scores_of_i = []
    for j in range(i, len(traces)):
        score = len([m for m, n in zip(traces[i], traces[j]) if m == n]) / len(traces[i])
        #print(score)
        scores[i][j] = score
        scores[j][i] = score
print("score computed")
file = open("./data/train.csv", "w+")
file.write("id,qid1,qid2,question1,question2,is_duplicate\n")
id = 0
for i in range(len(traces)):
    print("writing the " + str(i) + "th entity")
    for j in range(len(traces)):
        file.write(str(id) + "," +
                   str(i) + "," +
                   str(j) + "," +
                   transferList(traces[i]) + "," +
                   transferList(traces[j]) + "," +
                   str(scores[i][j]) + "\n")
        id += 1

file.close()

