## About

Swing Paint began as a school project. Although I submitted it for my eleventh-grade culminating project, it spawned from my hatred for one of the units during the course: the Graphics unit. Don't get me wrong, it was easy, but positioning sprites through trial and error of coordinates pushed me to the limits of my sanity. I created this tool so no student would ever have to suffer through that unit again.

Learn all about the development process [here](https://drive.google.com/drive/folders/1pDX1jO8diyv3K6tVwmyDM7JcqeXboh26?usp=sharing). The linked folder includes a project proposal and a development journal from before I moved this project to GitHub.  

## Running the Program
Simply clone and execute the Swing-Paint.jar file. Voila! Easy, right?

## Building from Command Line

Mainly for my own reference haha.

1. Compilation  
Source files (.java) -> bytecode (.class) to be interpreted by JVM.  
example: `javac -d target -sourcepath src/main/java src/main/java/swingpaint/Main.java`  

2. Compressing into JAR (optional)  
Combines and compresses class files into one file.  
example: `jar --create --file Swing-Paint.jar --main-class swingpaint.Main -C target .`  
example (short): `jar cfe Swing-Paint.jar swingpaint.Main -C target .`  

3. Executing Program  
Example from compiled files: `java -cp target swingpaint.Main`  
Example from JAR: `java -jar Swing-Paint.jar`  