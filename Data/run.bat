set JAVA_HOME=C:\APP\JDK
set path=C:\APP\JDK\bin
for %%a in (2.0) do (
	java -Xms10240m -Xmx12800m -Xnoclassgc -cp ./Data.jar DataSet %%a
	java -Xms10240m -Xmx12800m -Xnoclassgc -cp ./Data.jar Point %%a
	for %%r in (1.0) do (
		java -Xms10240m -Xmx12800m -Xnoclassgc -cp ./Data.jar Pair %%r
		for %%m in (0,2,4,8) do (
			C:\Users\esoadmin\AppData\Local\Programs\Python\Python36\python.exe C:\Projects\LearnedEmbedding\Model\SiameseTripletNetwork\trainSiamese.py %%m
			java -Xms10240m -Xmx12800m -Xnoclassgc -cp ./Data.jar MTree %%a %%r %%m
			java -Xms10240m -Xmx12800m -Xnoclassgc -cp ./Data.jar LinearScan %%a %%r %%m
		)
	)	
)
pause