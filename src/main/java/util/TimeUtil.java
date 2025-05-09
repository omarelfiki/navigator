package util;

import java.time.LocalTime;

public class TimeUtil {
    public double calculateTimePastMidnight(String s){
        // calculate the seconds past midnight from format hh::mm:ss
        String[] timeParts = s.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        int seconds = Integer.parseInt(timeParts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }
    public double calculateDifference(String s1,String s2){
        // calculate the difference between two times in seconds
        LocalTime time1 = LocalTime.parse(s1);
        LocalTime time2 = LocalTime.parse(s2);
        return Math.abs(time1.toSecondOfDay() - time2.toSecondOfDay());
    }
    public String addTime(String departureTime, double requiredTime){
        // add time to a given time in format hh:mm:ss
        String[] timeParts = departureTime.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        int seconds = Integer.parseInt(timeParts[2]);

        // Convert requiredTime from seconds to hours, minutes, and seconds
        int additionalHours = (int) (requiredTime / 3600);
        int additionalMinutes = (int) ((requiredTime % 3600) / 60);
        int additionalSeconds = (int) (requiredTime % 60);

        // Add the time
        seconds += additionalSeconds;
        if (seconds >= 60) {
            seconds -= 60;
            minutes++;
        }
        minutes += additionalMinutes;
        if (minutes >= 60) {
            minutes -= 60;
            hours++;
        }
        hours += additionalHours;
        if (hours >= 24) {
            hours -= 24;
        }

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
