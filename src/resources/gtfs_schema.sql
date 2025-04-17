-- GTFS Schema for MySQL Server
-- Strong Entity: Service
CREATE TABLE service
(
    service_id NVARCHAR(255) PRIMARY KEY
);

-- Calendar (weekly recurring service)
CREATE TABLE calendar
(
    service_id NVARCHAR(255) PRIMARY KEY,
    monday     INT  NOT NULL,
    tuesday    INT  NOT NULL,
    wednesday  INT  NOT NULL,
    thursday   INT  NOT NULL,
    friday     INT  NOT NULL,
    saturday   INT  NOT NULL,
    sunday     INT  NOT NULL,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    FOREIGN KEY (service_id) REFERENCES service (service_id)
);

-- Calendar Dates (exceptions)
CREATE TABLE calendar_dates
(
    service_id     NVARCHAR(255) NOT NULL,
    date           DATE          NOT NULL,
    exception_type INT           NOT NULL CHECK (exception_type IN (1, 2)),
    PRIMARY KEY (service_id, date),
    FOREIGN KEY (service_id) REFERENCES service (service_id)
);

-- Agency
CREATE TABLE agency
(
    agency_id       NVARCHAR(255) PRIMARY KEY,
    agency_name     NVARCHAR(255) NOT NULL,
    agency_url      NVARCHAR(255) NOT NULL,
    agency_lang     NVARCHAR(10)  NOT NULL,
    agency_phone    NVARCHAR(50)  NOT NULL,
    agency_timezone NVARCHAR(50)  NOT NULL,
    agency_fare_url NVARCHAR(255)
);

-- Routes
CREATE TABLE routes
(
    route_id         NVARCHAR(255) PRIMARY KEY,
    agency_id        NVARCHAR(255) NOT NULL,
    route_short_name NVARCHAR(50)  NOT NULL,
    route_url        NVARCHAR(255) NOT NULL,
    route_type       INT           NOT NULL,
    route_long_name  NVARCHAR(255),
    route_color      NVARCHAR(10),
    route_text_color NVARCHAR(10),
    FOREIGN KEY (agency_id) REFERENCES agency (agency_id)
);

-- Shapes (points)
CREATE TABLE shapes
(
    shape_id            NVARCHAR(255) NOT NULL,
    shape_pt_lat        FLOAT         NOT NULL,
    shape_pt_lon        FLOAT         NOT NULL,
    shape_pt_sequence   INT           NOT NULL,
    shape_dist_traveled FLOAT,
    PRIMARY KEY (shape_id, shape_pt_sequence)
);

-- Shape Index (unique shape IDs)
CREATE TABLE shape_index
(
    shape_id NVARCHAR(255) PRIMARY KEY
);

-- Trips
CREATE TABLE trips
(
    trip_id               NVARCHAR(255) PRIMARY KEY,
    route_id              NVARCHAR(255) NOT NULL,
    service_id            NVARCHAR(255) NOT NULL,
    shape_id              NVARCHAR(255),
    trip_short_name       NVARCHAR(100),
    trip_headsign         NVARCHAR(255),
    direction_id          INT,
    block_id              NVARCHAR(255),
    wheelchair_accessible INT,
    exceptional           INT,
    FOREIGN KEY (route_id) REFERENCES routes (route_id),
    FOREIGN KEY (service_id) REFERENCES service (service_id),
    FOREIGN KEY (shape_id) REFERENCES shape_index (shape_id)
);

-- Stops
CREATE TABLE stops
(
    stop_id             NVARCHAR(255) PRIMARY KEY,
    stop_code           NVARCHAR(50),
    stop_name           NVARCHAR(255) NOT NULL,
    stop_desc           NVARCHAR(255),
    stop_lat            FLOAT         NOT NULL,
    stop_lon            FLOAT         NOT NULL,
    stop_url            NVARCHAR(255),
    stop_timezone       NVARCHAR(50),
    location_type       INT,
    parent_station      NVARCHAR(255),
    wheelchair_boarding INT
);

-- Stop Times
CREATE TABLE stop_times
(
    trip_id             NVARCHAR(255),
    stop_id             NVARCHAR(255),
    arrival_time        NVARCHAR(8),
    departure_time      NVARCHAR(8),
    stop_sequence       INT,
    stop_headsign       NVARCHAR(255),
    pickup_type         INT,
    drop_off_type       INT,
    shape_dist_traveled FLOAT,
    timepoint           INT,
    PRIMARY KEY (trip_id, stop_sequence),
    FOREIGN KEY (trip_id) REFERENCES trips (trip_id),
    FOREIGN KEY (stop_id) REFERENCES stops (stop_id)
);