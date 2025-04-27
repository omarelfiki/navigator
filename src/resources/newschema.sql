-- GTFS Schema for MySQL Server (FULL - matches ERD)

-- Service
CREATE TABLE service
(
    service_id NVARCHAR(255) PRIMARY KEY
);

-- Calendar (weekly recurring service)
CREATE TABLE calendar
(
    service_id NVARCHAR(255) PRIMARY KEY,
    monday     ENUM ('0', '1') NOT NULL,
    tuesday    ENUM ('0', '1') NOT NULL,
    wednesday  ENUM ('0', '1') NOT NULL,
    thursday   ENUM ('0', '1') NOT NULL,
    friday     ENUM ('0', '1') NOT NULL,
    saturday   ENUM ('0', '1') NOT NULL,
    sunday     ENUM ('0', '1') NOT NULL,
    start_date DATE            NOT NULL,
    end_date   DATE            NOT NULL,
    FOREIGN KEY (service_id) REFERENCES service (service_id)
);

-- Calendar Dates (exceptions)
CREATE TABLE calendar_dates
(
    service_id     NVARCHAR(255)   NOT NULL,
    date           DATE            NOT NULL,
    exception_type ENUM ('1', '2') NOT NULL,
    PRIMARY KEY (service_id, date),
    FOREIGN KEY (service_id) REFERENCES service (service_id)
);

-- Agency
CREATE TABLE agency
(
    agency_id       NVARCHAR(255) PRIMARY KEY,
    agency_name     NVARCHAR(255) NOT NULL,
    agency_url      NVARCHAR(255) NOT NULL,
    agency_lang     NVARCHAR(50)  NOT NULL,
    agency_phone    NVARCHAR(50),
    agency_timezone NVARCHAR(50)  NOT NULL,
    agency_fare_url NVARCHAR(255),
    agency_email    NVARCHAR(50),
    ticketing_deep_link_id NVARCHAR(255)
);

-- Routes
CREATE TABLE routes
(
    route_id            NVARCHAR(255) PRIMARY KEY,
    agency_id           NVARCHAR(255)                          NOT NULL,
    route_short_name    NVARCHAR(50)                           NOT NULL,
    route_long_name     NVARCHAR(255),
    route_desc          NVARCHAR(255),
    route_type          ENUM ('0','1','2','3','4','5','6','7','11','12') NOT NULL,
    route_url           NVARCHAR(255),
    route_color         NVARCHAR(10),
    route_text_color    NVARCHAR(10),
    route_sort_order    VARCHAR(255),
    continuous_pickup   ENUM ('0','1','2','3'),
    continuous_drop_off ENUM ('0','1','2','3'),
    network_id          NVARCHAR(255),
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

-- Shape Index
CREATE TABLE shape_index
(
    shape_id NVARCHAR(255) PRIMARY KEY
);


-- Stops
CREATE TABLE stops
(
    stop_id             NVARCHAR(255) PRIMARY KEY,
    stop_code           NVARCHAR(50),
    stop_name           NVARCHAR(255) NOT NULL,
    tts_stop_name       NVARCHAR(255),
    stop_desc           NVARCHAR(255),
    stop_lat            FLOAT         NOT NULL,
    stop_lon            FLOAT         NOT NULL,
    zone_id             NVARCHAR(255),
    stop_url            NVARCHAR(255),
    location_type       ENUM ('0','1','2','3','4'),
    parent_station      NVARCHAR(255),
    stop_timezone       NVARCHAR(50),
    wheelchair_boarding ENUM ('0','1','2'),
    level_id            NVARCHAR(255),
    platform_code       NVARCHAR(255)
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
    direction_id          ENUM ('0', '1'),
    block_id              NVARCHAR(50),
    wheelchair_accessible ENUM ('0', '1', '2'),
    bikes_allowed         ENUM ('0', '1', '2'),
    exceptional           INT,
    FOREIGN KEY (route_id) REFERENCES routes (route_id),
    FOREIGN KEY (service_id) REFERENCES service (service_id),
    FOREIGN KEY (shape_id) REFERENCES shape_index (shape_id)
);

-- Stop Times
CREATE TABLE stop_times
(
    trip_id                      NVARCHAR(255),
    arrival_time                 TIME,
    departure_time               TIME,
    stop_id                      NVARCHAR(255),
    location_group_id            NVARCHAR(255),
    location_id                  NVARCHAR(255),
    stop_sequence                INT NOT NULL,
    stop_headsign                NVARCHAR(255),
    start_pickup_drop_off_window TIME,
    end_pickup_drop_off_window   TIME,
    pickup_type                  ENUM ('0','1','2','3'),
    drop_off_type                ENUM ('0','1','2','3'),
    continuous_pickup            ENUM ('0','1','2','3'),
    continuous_drop_off          ENUM ('0','1','2','3'),
    shape_dist_traveled          FLOAT,
    timepoint                    ENUM ('0','1'),
    pickup_booking_rule_id       NVARCHAR(255),
    drop_off_booking_rule_id     NVARCHAR(255),
    PRIMARY KEY (trip_id, stop_sequence),
    FOREIGN KEY (trip_id) REFERENCES trips (trip_id),
    FOREIGN KEY (stop_id) REFERENCES stops (stop_id)
);
