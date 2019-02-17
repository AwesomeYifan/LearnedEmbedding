def transfer_list(list):
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

traces = traces[0:1000]

scores = [[0 for x in range(len(traces))] for y in range (len(traces))]
for i in range(len(traces)):
    print("computing scores for the " + str(i) + "th entity...")
    scores_of_i = []
    for j in range(i, len(traces)):
        score = len([m for m, n in zip(traces[i], traces[j]) if m == n]) / len(traces[i])
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
                   transfer_list(traces[i]) + "," +
                   transfer_list(traces[j]) + "," +
                   str(scores[i][j]) + "\n")
        id += 1

file.close()

