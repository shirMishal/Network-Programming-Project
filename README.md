# Network Programming Project
### Description
implementation of a simple social network server and client. 
The communication between the server and the client is performed using a binary communication protocol.
protocol supports 11 types of messages:  
1-8 are Client-to-Server messages  
9-11 are Server-to-Client messages  
| Opcode      | Operation |
| ----------- | ----------- |
| 1      | Register request (REGISTER)       |
| 2   | Login request (LOGIN)        |
| 3 | Logout request (LOGOUT) |
| 4 | Follow / Unfollow request (FOLLOW) |
| 5 | Post request (POST) |
| 6 | PM request (PM)|
| 7 | User list request (USERLIST) |
| 8 | Stats request (STAT) |
| 9 | Notification (NOTIFICATION) |
| 10 | Ack (ACK) |
| 11 | Error (ERROR) |

Unlike real social network there is no real databases (data is saved locally from the time the server starts and keep it in memory until the server closes).

This project was written as part of Ben-Gurion University's course "System Programming".
