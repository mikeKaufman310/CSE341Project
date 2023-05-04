Main: mpk225

mpk225: jar


classCompile:
	javac mpk225.java

jar: classCompile
	jar cfmv mpk225.jar Manifest.txt mpk225.class

test:
	java -jar mpk225.jar

clean:
	rm mpk225.class
	rm mpk225.jar

tester:
	javac Tester.java mpk225.java
	java Tester
	rm Tester.class
	rm mpk225.class
