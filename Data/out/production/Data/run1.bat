set JAVA_HOME=C:\APP\JDK
set path=C:\APP\JDK\bin
for %%a in (0.1) do (
	java -Xms10240m -Xmx12800m -Xnoclassgc -jar Data.jar MTree %%a
)
pause