Socket Server application for managing meetings between employees. Each employee has a running server. The Central Server is the one that manages meetings. A client application allows employees to login with the Central Server, see their meetings, book new meetings or edit existing ones, after which the Central Server notifies all other running employee servers to update their respective meetings data. 

Code was developed in Java using the Observer pattern mixed with Mediator pattern. Each folder is an individual Netbeans project. Source code is under src/. Executable is under dist/.

Only 2 employee servers included since the code is the same, only the meetings file and socket port changes.


Final Project

Juan Javier Arosemena
David Mena

Products:
-1 Client app
-1 Central Server app
-5 Employee Server apps (each with their unique meetings.txt and port.txt)

Setup:
1. Run all 5 Employee Servers.
2. Run Central Server (initializes its data by requesting from all other servers at the beginning).
3. Run the Client and login with an employee name (caps sensitive), which you can find in properties.txt within the CentralServer folder.
4. Mess around with the client.
