#Technical Requirements

This projects requires:
- Java 1.8
- MySQL 5.6
- MQTT Broker (running)
- Maven

Database configurations can be made in
	/src/main/resources/hibernate.cfg.xml
	

#Compilation

After you made changes in source code, create an executable *.jar by executing:

	mvn clean compile assembly:single
	
#Credits
All icons are taken from
	https://icons8.com/
	
The RadAR+ Logo is taken from University of Kassel:
	https://www.uni-kassel.de/maschinenbau/institute/ifa/mensch-maschine-systemtechnik/startseite.html