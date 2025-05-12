# Nav-App – Public Transport Route Planner

A **JavaFX-powered system** to plan your journey across **Rome**'s public transportation network.

## Overview

Nav-App transforms raw GTFS transit data into an intelligent routing engine with a graphical interface.
Designed for public transit systems, it enables real-time route discovery using buses, trains, trams, and walking transfers.
**“Never miss your next stop again.”**

 ## Features
- **GTFS Data Integration** (Stops, Trips, Routes, Agency, Calendar, ...)
- **MySQL Database Backend** 
- **Graph-Based Transit Model**
- **Realistic Time-Based Weights** (Including Transfers & Waiting Time)
- **JavaFX GUI with Interactive Controls**
- **Console-Based Database Initializer**
- **Multi-City Support** (through CLI routing only, for now)


 ### Project Structure
    group14/                                    
    ├── pom.xml                                 
    ├── src/                                
    │   ├── main/                                                   
    │   │   └── java/                           
    │   │       ├── model/              # Data Models (Stop, Trip, Route, etc.)                                             
    │   │       ├── service/            # Business Logic (Routing, Graph Building)                                                      
    │   │       ├── ui/                 # JavaFX GUI Components                                                         
    │   │       └── InitDatabase.java   # Database Setup Utility                                                  
    ├── oldGraphStuff/                  # Early Prototype (for reference)                                       
    └── README.md                                   

 ## Setup Guide

 ### 1.  Install Requirements
- Java 23+                         
- Maven                                                                                                    
- MySQL Server
         

### 2.  Clone the Repository                            
```bash
   git clone https://gitlab.maastrichtuniversity.nl/bcs1600-2025/group14.git                   
   cd nav-app   
```

### 3. Run Clean Install
Navigate to the project directory and run the following command to build the project and download dependencies:              
    `mvn clean install`

### 4. Configure environment variables
   - Set the `GTFS_DIR` variable to the path of your GTFS data directory.
   - Set the `ROUTING_ENGINE_MYSQL_JDBC` variable to your MySQL connection string with `allowLoadLocalInfile=true&useCursorFetch=true`.
   - Set the `ROUTING_ENGINE_STORAGE_DIRECTORY` variable to the path where you want to store the extracted GTFS data.

 ## Running the Application

### 1. CLI
   - Navigate to the project directory and run the following command:
     `mvn exec:java`

### 2. JavaFX GUI
   - Navigate to the project directory and run the following command:
     `mvn javafx:run`

## Interacting with the CLI
- The CLI provides a simple JSON interface to interact with the routing engine. You can send requests in JSON format and receive responses in JSON format.

- Example routing request:
```json
{"routeFrom":{"lat":41.904,"lon":12.5004},"to":{"lat":41.8791,"lon":12.5221},"startingAt":"09:30:00"}
```
- Example response:
```json
{"ok":[{"duration":0,"mode":"walk","startTime":"09:30:00","to":{"lat":12.123,"lon":12.123}},
  {"duration":0,"mode":"walk","startTime":"09:30:24","to":{"lat":41.903709411621094,"lon":12.500521659851074}},
  {"duration":0,"mode":"ride","route":{"headSign":"\"COSTAMAGNA\"","longName":"","operator":"N\/A","shortName":"16"},"startTime":"09:34:12","stop":"TERMINI (MA-MB-FS)","to":{"lat":41.90115737915039,"lon":12.500046730041504}},
  {"duration":0,"mode":"ride","route":{"headSign":"\"TONINO BELLO\"","longName":"","operator":"N\/A","shortName":"150F"},"startTime":"09:35:56","stop":"FARINI","to":{"lat":41.89816665649414,"lon":12.49951457977295}},
  {"duration":0,"mode":"ride","route":{"headSign":"\"TONINO BELLO\"","longName":"","operator":"N\/A","shortName":"150F"},"startTime":"09:37:24","stop":"P.ZA VITTORIO EMANUELE (MA)","to":{"lat":41.895179748535156,"lon":12.504576683044434}},
  {"duration":0,"mode":"walk","startTime":"09:38:21","to":{"lat":41.89450454711914,"lon":12.504260063171387}},
  {"duration":0,"mode":"ride","route":{"headSign":"Anagnina","longName":"Metro A","operator":"N\/A","shortName":"MEA"},"startTime":"09:47:13","stop":"Manzoni","to":{"lat":41.89054489135742,"lon":12.506333351135254}},
  {"duration":0,"mode":"ride","route":{"headSign":"Anagnina","longName":"Metro A","operator":"N\/A","shortName":"MEA"},"startTime":"09:48:30","stop":"S. Giovanni","to":{"lat":41.88554000854492,"lon":12.509440422058105}},
  {"duration":0,"mode":"ride","route":{"headSign":"Anagnina","longName":"Metro A","operator":"N\/A","shortName":"MEA"},"startTime":"09:49:41","stop":"Re di Roma","to":{"lat":41.8817253112793,"lon":12.513994216918945}},
  {"duration":0,"mode":"ride","route":{"headSign":"Anagnina","longName":"Metro A","operator":"N\/A","shortName":"MEA"},"startTime":"09:50:57","stop":"Ponte Lungo","to":{"lat":41.877662658691406,"lon":12.518898963928223}},
  {"duration":0,"mode":"walk","startTime":"09:51:15","to":{"lat":41.87782669067383,"lon":12.519108772277832}},
  {"duration":0,"mode":"walk","startTime":"09:51:38","to":{"lat":41.8779411315918,"lon":12.519471168518066}},
  {"duration":0,"mode":"walk","startTime":"09:52:03","to":{"lat":41.87815856933594,"lon":12.519775390625}}]}
```

## Initalizing GTFS datasets
- To initialize the GTFS datasets, call the "load" request with the GTFS dataset zip path as a parameter.
- Example:
```json
{"load":"path/to/gtfs.zip"}
```
- The system will extract the GTFS data and load it into the MySQL database.
- The system will also create the necessary tables and indexes for efficient querying.

### Initializing the database through GUI
- The GUI provides a simple interface to initialize the database. You can add the GTFS dataset directory path to the `GTFS_DIR` enviroment variable and click on the "Load GTFS Data" button to initialize the database.
- The system will extract the GTFS data and load it into the MySQL database.

Note: The CLI only accepts the GTFS dataset zip file path, while the GUI accepts the extracted GTFS dataset directory path. Do not include the zip file in the GUI. Dir path will be deprecated in the future.

## Contributors
- [@I6344736] - Omar Elfiki
- [@I6377598] - Vlad Rusu
- [@I6356746] - Filippo Morini
- [@I6366266] - Mohamad Yahia Lababidi
- [@I6385002] - Mu Ye
- [@I6313682] - Rens Gielen
- [@I6320718] - Olivier van Leeuwen
