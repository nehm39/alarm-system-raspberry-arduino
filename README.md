# Description

Implementation of alarm system using Arduino and Raspberry.

Arduino is used as a sensor for motion detection - using PIR module. It sends information about motion to Raspberry Pi using nrf24 module. After end of motion it sends another information. Raspberry Pi takes appropiate actions after receiving information (saving log, taking pictures, sending notification). There is possibility to add many Arduino sensors.


## Wiring
![arduino](/screens/arduino_scheme.jpg?raw=true)

![raspberry](/screens/raspberry_scheme.jpg?raw=true)


## Raspberry Pi
Main system is implemented on Raspberry Pi using Python. Raspberry Pi needs to have USB camera connected. It also needs to have connection to MySQL database - that's where the configuration is stored. For using mail notifications - it also needs and SMTP server (it can be Gmail for example).

After first launch, database will be initialised with default configuration (config.sql file).

Possible configuration:
- pictures per second (try different settings, on newer Raspberry Pi models it can be higher, but it also depends on USB camera possibilities),
- saving logs to database,
- e-mail notifications.

## Arduino

Arduino implementation is pretty simple. It needs to have nrf24 and PIR module connected. It's only job is to detect motion and send correct information with nrf24.

## PHP API

PHP API was created to expose alarm system data to mobile application. It allows to:
- log in,
- get system status,
- start, stop and restart system.
- get alarms, configuration, pictures,
- delete alarms,
- delete pictures,
- edit configurations.

## Android app

Android application was created to simply manage the alarm system. It uses all methods exposed by PHP API. With android app it is possible to log in, then to display information about status systems and all logs and pictures. It also allow to remove data. Alarm system can also be remotely turned on or off.

![sample1](/screens/sample1.png?raw=true)

![sample2](/screens/sample2.png?raw=true)

![sample3](/screens/sample3.png?raw=true)

![sample4](/screens/sample4.png?raw=true)

![sample5](/screens/sample5.png?raw=true)

![sample6](/screens/sample6.png?raw=true)
