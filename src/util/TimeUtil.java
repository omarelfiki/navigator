

import java.time.LocalTime;

public class TimeUtil {
    public static int timeDiffMinutes(String start, String end) {
        LocalTime t1 = LocalTime.parse(start);
        LocalTime t2 = LocalTime.parse(end);
        return (int) java.time.Duration.between(t1, t2).toMinutes();
    }
}
