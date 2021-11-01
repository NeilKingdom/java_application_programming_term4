Name: Neil Kingdom
Student Number: 040967309
Course: CST8221 300
Date: Oct 12, 2021

--------- README -----------

Hello Professor, 
As discussed during the office hours, my code was working fine on my IDE, but the JAR was not being created
properly, and I was unable to compile from CMD. This was for two reasons: 

1. I had placed my resource files in another folder under res/img for images and res/fonts for fonts. The compiler
was unable to detect these files. This wouldn't have normally been an issue except for problem 2:

2. I had implemented error handling that would purposely exit the program if a resource could not be loaded. This 
was only intended for the debug build, but I did not have time to implement a better solution.

-------- The Fix -----------

I have since fixed the path issues by loading all resources using Class.getResourceAsStream(), and have fixed the 
error handling to load some text if the resources can't be found. To save you time in case you had any doubts, I've 
included a .diff file for each class so you can see what was added and removed from the version I submitted on 
Oct 9, 2021. 

-------- Compiling From CMD ---------

In order to compile from CMD, run CMD as admin, cd into the src directory. Run the following:
"javac -cp . picross\*.java [-d output dir]". This should compile without any warnings or errors.

-------- Running JAR ---------

My laptop was having difficulties running the JAR file as an executable, although my JRE is quite possibly installed
incorrectly. I was able to run the JAR from CMD by running as admin, cd into src, and run "java -jar Picross.jar"

-------- A Potential Concern ---------

One concern I had is with the Action Listeners. The assignment instructions were not clear about the output of the 
action listeners. It simply states that whenever a button is clicked it must output text to the "console". I 
intepreted this to mean the output console of the IDE, or CMD if compiling from there. The alternative was that you
were referring to the game console window within the GUI. Just an FYI in case you missed it. The output is there, you
might just have to look at CMD, not the game window.

-------- File Locations --------

One final note is where I've placed the resources. To avoid complications, I've simply zipped the entire project.
I am running IntelliJ Idea 2021.2.1. Source files are located in src/picross. Resources such as Javadoc is located in
res/doc, res/img for images and res/fonts for font files. The .diff files are located in the root folder of the project
alongside this README. Finally, the JAR file is located in the src/ directory.

