DELIMITER $$
CREATE PROCEDURE get_closest_stops(IN lat DOUBLE, IN lon DOUBLE, IN radius DOUBLE)
BEGIN
SELECT
    s.stop_id,
    s.stop_name,
    s.stop_lat,
    s.stop_lon,
    6371000 * 2 * ASIN(SQRT(
            POWER(SIN(RADIANS(s.stop_lat - lat) / 2), 2) +
            COS(RADIANS(lat)) * COS(RADIANS(s.stop_lat)) *
            POWER(SIN(RADIANS(s.stop_lon - lon) / 2), 2)
                       )) AS distance
FROM stops s
HAVING distance <= radius
ORDER BY distance;
END$$
DELIMITER ;
