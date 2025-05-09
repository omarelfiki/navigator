DELIMITER $$
CREATE PROCEDURE get_closest_stops(IN lat FLOAT, IN lon FLOAT, IN radius FLOAT)
BEGIN
SELECT
    s.stop_id,
    s.stop_name,
    s.stop_lat,
    s.stop_lon,
    SQRT(POW(s.stop_lat - lat, 2) + POW(s.stop_lon - lon, 2)) AS distance
FROM stops s
WHERE SQRT(POW(s.stop_lat - lat, 2) + POW(s.stop_lon - lon, 2)) <= radius
LIMIT 5;
END$$
DELIMITER ;