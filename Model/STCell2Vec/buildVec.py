from gensim.models import Word2Vec

file = open("../../Data/data/traces")
data = []
while 1:
    line = file.readline().strip()
    if not line:
        break
    trace = line.split(' ')
    data.append(trace)

model = Word2Vec(data, size=100, window=100, min_count=1, workers=6)
model.train(data, total_examples=len(data), epochs=10)
model.save("./data/traceVec.model")
# print(model.wv.most_similar(positive="0", topn = 30))
# print(model.wv.most_similar(positive="0", topn = 30)[0][0])
# total_number_entity = 1000
# embedding_rank =  []
# for i in range(0,total_number_entity):
#     list = []
#     for j in range(0,topk):
#         #print(model.wv.most_similar(positive=str(i), topn=j + 1))
#         list.append(model.wv.most_similar(positive=str(i),topn=j+1)[j][0])
#     embedding_rank.append(list)
#
# file = open("C:\\workspace\\java\\IM\\rank")
# ranges = [1,2,5,10,20,40,50,60,70,80,90,100,200,300,500]
# entity = 0
#
# performance = []
# for i in range(len(ranges)):
#     performance.append(0.0)
# while 1:
#     line = file.readline().strip()
#     if not line:
#         break
#     list = line.split(',')
#     for i in range(0,len(ranges)):
#         max = ranges[i]
#         sublist1 = list[0:max]
#         sublist2 = embedding_rank[entity][0:max]
#         performance[i] += len(set(sublist1).intersection(sublist2))
#     entity += 1
#     if entity == total_number_entity:
#         break
# for i in range(len(performance)):
#     performance[i] /= (ranges[i] * total_number_entity)
#
#
# file = open("C:\\Users\\esoadmin\\Desktop\\performance.csv", "w+")
# for i in ranges:
#     file.write("Top-" + str(i) + ",")
# file.write("\n")
# for i in performance:
#     file.write(str(i) + ",")
# file.close()
# print(performance)
