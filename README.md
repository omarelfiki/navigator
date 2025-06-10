# Nav-App – Public Transport Route Planner

A **JavaFX-powered system** to plan your journey across any public transportation network.

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
- **Multi-City Support** (CLI and UI)
- **Heatmap Visualization** (for trip duration from origin)
- **Stop Expulsion** (to remove stops from your route)


 ### Project Structure
    group14/                                    
    ├── pom.xml                                 
    ├── src/                                
    │   ├── main/                                                   
    │   │   ├── java/
    │   │   │   ├── com.navigator14/    # Main Java Package (UI + CLI)
    │   │   │   ├── closureAnalysis/    # Stop Closure Analysis (for GTFS data)
    │   │   │   ├── db/                 # Database Classes
    │   │   │   ├── map/                # GUI Map Classes
    │   │   │   ├── models/             # Data Models (Stop, Trip, Route, etc.)                                             
    │   │   │   ├── router/             # A* + Routing Engine
    │   │   │   ├── ui/                 # JavaFX GUI Components
    │   │   │   └── util/               # Utility Classes  
    │   │   └── resources/              # JavaFX and SQL Resources (.sql, .css, Images)
    │   └── test/                       # Test Classes
    │       └── java/
    │            └── com.navigator14/
    ├── README.md
    └── pom.xml                     # Maven Project File

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
   - Set the `ROUTING_ENGINE_MYSQL_JDBC` variable to your MySQL connection string with `allowLoadLocalInfile=true&useCursorFetch=true`.
   - Set the `ROUTING_ENGINE_STORAGE_DIRECTORY` variable to the path where you want to store the extracted GTFS data.

### 5. Add vmArguments for JavaFX
   - If you are using an IDE, add the following VM arguments to your run configuration:
   
    --module-path /path/to/javafx/lib
    --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing
    -Djavafx.platform=Desktop

 ## Running the Application (from Terminal)

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
{"ok":[
  {
    "duration":4,
    "fromLat":41.904,
    "fromLon":12.5004,
    "mode":"walk",
    "startTime":"09:30",
    "to":{"lat":41.90115737915039,"lon":12.500046730041504}
  },
  {
    "duration":3,
    "mode":"ride",
    "route":{"headSign":"COSTAMAGNA","longName":"Unknown","operator":"Atac","shortName":"16"},
    "startTime":"09:30",
    "stop":"FARINI",
    "to":{"lat":41.89816665649414,"lon":12.49951457977295}
  },
  {
    "duration":1,
    "mode":"ride",
    "route":{"headSign":"TONINO BELLO","longName":"Unknown","operator":"Atac","shortName":"150F"},
    "startTime":"09:34",
    "stop":"P.ZA VITTORIO EMANUELE (MA)",
    "to":{"lat":41.895179748535156,"lon":12.504576683044434}
  },
  {
    "duration":1,
    "mode":"ride",
    "route": {"headSign":"TONINO BELLO","longName":"Unknown","operator":"Atac","shortName":"150F"},
    "startTime":"09:35",
    "stop":"Vittorio Emanuele",
    "to":{"lat":41.89450454711914,"lon":12.504260063171387}
  },
  {
    "duration":6,
    "fromLat":41.895179748535156,
    "fromLon":12.504576683044434,
    "mode":"walk",
    "startTime":"09:37",
    "to":{"lat":41.89054489135742,"lon":12.506333351135254}
  },
  {
    "duration":8,
    "mode":"ride",
    "route":{"headSign":"Anagnina","longName":"Metro A","operator":"Atac","shortName":"MEA"},
    "startTime":"09:38",
    "stop":"S. Giovanni",
    "to":{"lat":41.88554000854492,"lon":12.509440422058105}
  },
  {
    "duration":1,
    "mode":"ride",
    "route":{"headSign":"Anagnina","longName":"Metro A","operator":"Atac","shortName":"MEA"},
    "startTime":"09:47",
    "stop":"Re di Roma",
    "to": {"lat":41.8817253112793,"lon":12.513994216918945}
  },
  {
    "duration":1,
    "mode":"ride",
    "route":{"headSign":"Anagnina","longName":"Metro A","operator":"Atac","shortName":"MEA"},
    "startTime":"09:48",
    "stop":"Ponte Lungo",
    "to":{"lat":41.877662658691406,"lon":12.518898963928223}
  },
  {
    "duration":4,
    "fromLat":41.877662658691406,
    "fromLon":12.518898963928223,
    "mode":"walk",
    "startTime":"09:50",
    "to":{"lat":41.8791,"lon":12.5221}
  }
]}
```
## Interacting with the GUI
- The GUI provides a user-friendly interface to interact with the routing engine. You can enter the starting and destination coordinates, select the time of departure, and click on the search button to get the route.
- Once the route is found, it will be displayed on the map with the corresponding stops and routes.
- You can select a stop on your trip info panel to exclude it from the route. You can then recalculate the route without that stop.
- Upon activating the "Heatmap" button, you can enter an origin point and see a visualization of the trip duration to various stops in the vicinity.
- You can zoom in and out of the map, and hide the navigator to focus on the map itself.


## Initalizing GTFS datasets
- To initialize the GTFS datasets, call the "load" request with the GTFS dataset zip path as a parameter.
- Example:
```json
{"load":"path/to/gtfs.zip"}
```
- The system will extract the GTFS data and load it into the MySQL database.
- The system will also create the necessary tables and indexes for efficient querying.

### Initializing the database through GUI
- The GUI provides a simple interface to initialize the database. You can add the GTFS dataset directory path to the `GTFS_DIR` environment variable and click on the "Load GTFS Data" button to initialize the database.
- The system will extract the GTFS data and load it into the MySQL database.

## Contributors
- [@I6344736] - Omar Elfiki
- [@I6377598] - Vlad Rusu
- [@I6356746] - Filippo Morini
- [@I6366266] - Mohamad Yahia Lababidi
- [@I6385002] - Mu Ye
- [@I6313682] - Rens Gielen
- [@I6320718] - Olivier van Leeuwen

## Third-Party Libraries/Licenses

This project uses the following external libraries:

- JXMapViewer2
    - Author: Martin Steiger, et al.
    - License: LGPL-3.0
    - [https://github.com/msteiger/jxmapviewer2](https://github.com/msteiger/jxmapviewer2)
- JavaFX
    - Author: OpenJFX
    - License: GPL v2 with Classpath Exception
    - [https://openjfx.io/](https://openjfx.io/)
- MySQL Connector/J
    - Author: Oracle
    - License: GPL v2
    - [https://dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/)
- Protocol Buffers (protobuf-java)
    - Author: Google
    - License: BSD 3-Clause
    - [https://github.com/protocolbuffers/protobuf](https://github.com/protocolbuffers/protobuf)
- leastfixedpoint-json
    - Author: Tony Garnock-Jones
    - License: Apache License 2.0
    - [https://github.com/tonyg/java-json-leastfixedpoint](https://github.com/tonyg/java-json-leastfixedpoint)
- univocity-parsers
    - Author: Univocity
    - License: Apache License 2.0
    - [https://github.com/uniVocity/univocity-parsers](https://github.com/uniVocity/univocity-parsers)
- Apache Commons CSV
    - Author: The Apache Software Foundation
    - License: Apache License 2.0
    - [https://commons.apache.org/proper/commons-csv/](https://commons.apache.org/proper/commons-csv/)
- JUnit Jupiter
    - Author: JUnit Team
    - License: EPL-2.0
    - [https://junit.org/junit5/](https://junit.org/junit5/)
- TestFX
    - Author: TestFX contributors
    - License: EUPL-1.1
    - [https://github.com/TestFX/TestFX](https://github.com/TestFX/TestFX)
  
Please refer to each library’s repository for full license details.