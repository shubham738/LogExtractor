# LogExtractor
To display logs of particular timestamp within seconds.
##########################################################
NOTE: To run the code successfully

1.)You have to all add log file names in LogFileNames.txt (./demo/LogFileNames). you can see some names are already added for
 testing purpose.

###########################################################
Tools Required for running code: Maven,Java,Spring dependencies.

Command To Run the Code:
1.) mvn spring-boot:run

Command for making Build/Jar:
1.) mvn clean install

After running the last cmd, target folder will be created which will contains Jar file.

##################################################
To Run the program from Jar file --------->
1.) java -jar -f "from time" -t "to time" -i "Log file directory location"

  For eg. java -jar -f "2020-07-15T20:13:00.1234Z" -t "2020-07-15T23:56:38.3668Z" -i "C:\Users\Namita\Downloads\demo\demo\LogFiles" 
	
	NOTE: (Make sure to change the directory in the cmd, Directory should contain all log File with requires name format)

2.) For making an .exe file use launch4j or check launch4j config for already build configuration for the file.

##########################################
Running .exe file
1.) LogExtractor.exe -f "2020-07-15T20:13:00.1234Z" -t "2020-07-15T22:56:38.3668Z" -i "C:\Users\Namita\Downloads\demo\demo\LogFiles"

 