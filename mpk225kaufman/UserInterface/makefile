Main: mpk225

mpk225: jar


classCompile:
	javac UserInterface.java

jar: classCompile
	jar cfmv UserInterface.jar Manifest.txt UserInterface.class

test:
	java -jar UserInterface.jar

clean:
	rm UserInterface.class
	rm UserInterface.jar
