set JAVA_HOME=C:\APP\JDK
set path=C:\APP\JDK\bin
for %%a in (3.0) do (
	for %%d in (2) do (
		for %%m in (1.5) do (
			C:\Users\esoadmin\AppData\Local\Programs\Python\Python36\python.exe C:\Projects\LearnedEmbedding\Model\SiameseTripletNetwork\trainSiamese.py %%m %%d
			java -Xms10240m -Xmx12800m -Xnoclassgc -cp ./Data.jar MTree %%a %%d %%m
			java -Xms10240m -Xmx12800m -Xnoclassgc -cp ./Data.jar LinearScan %%a %%d %%m
		)
	)	
)
pause