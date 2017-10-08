# smsdispatchserv
### Overview
This app dispatches received SMS to other phone numbers.

###Options
- choose one or more SMS senders
- filter the received SMS text content
- choose one or more SMS recipients

###Highlights
 - silently started on boot
 - it works when:
    - the device is sleeping, 
    - the screen is off 
    - and when another app is on foreground and running
 - it stops working when the app is stop forced:
    - swiping on recent apps 
    - going to Settings/Apps/Apps Info/Force Stop
 
###Screenshots
In this main page you see your SMS log, with all the received and sent SMS

[![N|Solid](https://www.dogsally.com/github/smsdispatchserv_main.jpg)](https://www.dogsally.com/github/smsdispatchserv_main.jpg)

In this settings page, you set the SMS sender(s), the recipient(s) and one or more optional text filters.

[![N|Solid](https://www.dogsally.com/github/smsdispatchserv_profile.jpg)](https://www.dogsally.com/github/smsdispatchserv_profile.jpg)


###Logic Diagram - Fig.1
[![N|Solid](https://www.dogsally.com/github/neverendingservice.jpg)](https://nodesource.com/products/nsolid)

Logic Diagram Explanation
- After booting, BootActivity receives BOOT_COMPLETED from Android System
- BootActivity starts the SensorService service and finish
- SensorService runs continuosly, even when screen is off or the device is in sleep mode. 
- Whenever Android kills SensorService, onDestroy broadcasts an intent to SensorRestasterBroadcastReceiver
- SensorRestasterBroadcastReceiver starts the SensorService service
- The MainActivity is useless at the moment, but it can be filled with user interactions
