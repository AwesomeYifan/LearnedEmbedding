set JAVA_HOME=C:\APP\JDK
set classpath=.;Data.jar;
set path=C:\APP\JDK\bin
for %%s in ("Uniform") do (
	for %%d in (10,20) do (	
		for %%e in (0) do (
			for %%h in ("hard") do (
				C:\Users\esoadmin\AppData\Local\Programs\Python\Python36\python.exe C:\Projects\LearnedEmbedding\Model\SiameseTripletNetwork\trainSiamese.py %%s %%d %%e %%h
				for %%t in (0.1,0.2,0.3) do (
				java -Xms10240m -Xmx12800m -Xnoclassgc LinearScan %%s %%d %%t %%h
				)
			)
		)
		for %%t in (0.1,0.2,0.3) do (	
			for %%a in ("soft") do (
				C:\Users\esoadmin\AppData\Local\Programs\Python\Python36\python.exe C:\Projects\LearnedEmbedding\Model\SiameseTripletNetwork\trainSiamese.py %%s %%d %%t %%a	
				java -Xms10240m -Xmx12800m -Xnoclassgc LinearScan %%s %%d %%t %%a
			)			
		)		  
   )
)
pause