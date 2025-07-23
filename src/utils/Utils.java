package utils;

import java.util.Calendar;

public class Utils {
    public static String getDate() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        return String.format("%04d-%02d-%02d", year, month, day);
    }
}
