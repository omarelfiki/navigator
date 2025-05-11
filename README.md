Nav-App – Public Transport Route Planner

A JavaFX-powered system to plan your journey across [Rome's] public transportation network.

 Overview

Nav-App transforms raw GTFS transit data into an intelligent routing engine with a graphical interface.
Designed for public transit systems, it enables real-time route discovery using buses, trains, trams, and walking transfers.

“Never miss your next stop again.”

 Features

 	GTFS Data Integration (Stops, Trips, Routes, Agency, Calendar, ...)

	MySQL Database Backend

 	Graph-Based Transit Model
	
 	Realistic Time-Based Weights (Including Transfers & Waiting Time)

 	JavaFX GUI with Interactive Controls

 	Console-Based Database Initializer

	Multi-City Support (routing only)

                            
 Tech Stack
Technology	Version:                         
Java	23                  
JavaFX	23.0.1                      
MySQL	8+                              
Maven	Build Tool                            

 Project Structure
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

 Setup Guide

 1.  Install Requirements          
   Java 23+                         
    Maven                                                                                                    
    MySQL Server with GTFS schema loaded
         

2.  Clone the Repository                            
   git clone https://gitlab.maastrichtuniversity.nl/bcs1600-2025/group14.git                   
   cd nav-app   


3.  Configure Database Credentials                                         
   Edit /src/main/java/service/DBaccessProvider.java 


Building the Project          
Run the following code to install all the necessary dependencies for the application             
mvn clean install

 Running the Application

1. Configuration before running

add GTFS_DIR=path/rome_static_gtfs;ROUTING_ENGINE_MYSQL_JDBC=jdbc:mysql://root:password@localhost:3306/gtfs?allowLoadLocalInfile=true&useCursorFetch=true;ROUTING_ENGINE_STORAGE_DIRECTORY=path/storage to environment variable.

add --module-path path\JavaFX\lib --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing to VM options.

In project structure, set src and java folder as source code, set resources folder as resources.

2. Initialize Database 
Run the homeUI, click the setting button, fill in the path of the gtfs file, insert MySQL connection details, click test connection and import the gtfs.


