CREATE FUNCTION get_closest_stop(lat FLOAT, lon FLOAT)
    RETURNS JSON
    DETERMINISTIC
BEGIN
    DECLARE
result JSON;

SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                       'stop_id', s.stop_id,
                       'stop_name', s.stop_name,
                       'stop_lat', s.stop_lat,
                       'stop_lon', s.stop_lon,
                       'distance', SQRT(POW(s.stop_lat - lat, 2) + POW(s.stop_lon - lon, 2))
               )
       )
INTO result
FROM stops s
WHERE SQRT(POW(s.stop_lat - lat, 2) + POW(s.stop_lon - lon, 2)) <= radius;


RETURN result;
END;
