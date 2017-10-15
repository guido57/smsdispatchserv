# smsdispatchserv
### Overview
This app dispatches received SMS to other phone numbers.

### Options
- choose one or more SMS senders
- filter the received SMS text content
- choose one or more SMS recipients

### Highlights
 - silently started on boot
 - it works when:
    - the device is sleeping, 
    - the screen is off 
    - and when another app is on foreground and running
 - it stops working when the app is stop forced:
    - swiping on recent apps 
    - going to Settings/Apps/Apps Info/Force Stop
 
### Screenshots
In this main page you see your SMS log, with all the received and sent SMS

[![N|Solid](https://www.dogsally.com/github/smsdispatchserv_main.jpg)](https://www.dogsally.com/github/smsdispatchserv_main.jpg)

In this settings page, you set the SMS sender(s), the recipient(s) and one or more optional text filters.

[![N|Solid](https://www.dogsally.com/github/smsdispatchserv_profile.jpg)](https://www.dogsally.com/github/smsdispatchserv_profile.jpg)


### Logic Diagram - Fig.1
[![N|Solid](https://www.dogsally.com/github/smsdispatchserv_logic.jpg)](https://www.dogsally.com/github/smsdispatchserv_logic.jpg)

Logic Diagram Explanation
- When SMSBroadcastReceiver receives:
    - BOOT_COMPLETED -> it starts JobService
    - SMS_RECEIVED -> it processes the received SMS
- MyJobService runs silently and continuosly, even when screen is off or the device is in sleep mode. 
- The MainActivity contains a ListView to log all the SMS received and sent
- The ProfileActivity contains a RecyclerView with the following items:
    - 1 Button ButtonAddFrom to add one or more "From" items
    - EditView "From" item to set one SMS sender
    - 1 Button ButtonAddTo to add one or more "To" items
    - EditView "To" item to set one SMS recipient
    - 1 Button ButtonAddFilter  to add one or more "Filter" item 
    - EditView "Filter" item to set one SMS test message filter, accepting Regular Espression (REGEX) sintax, as: 
        - (.*) any number of characters
