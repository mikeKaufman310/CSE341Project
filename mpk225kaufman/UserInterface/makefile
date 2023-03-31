Main: mpk225

mpk225: jar
	mkdir -p mpk225
	mv UserInterface.class mpk225/UserInterface.class
	mv Manifest.txt mpk225/Manifest.txt

classCompile:
	javac UserInterface.java

jar: classCompile
	jar cfmv UserInterface.jar Manifest.txt UserInterface.class

test:
	java -jar UserInterface.jar

clean:
	mv mpk225/Manifest.txt Manifest.txt
	rm -r mpk225
	rm UserInterface.jar