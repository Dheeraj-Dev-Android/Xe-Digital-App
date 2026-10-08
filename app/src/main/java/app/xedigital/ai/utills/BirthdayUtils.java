package app.xedigital.ai.utills;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BirthdayUtils {

    private static final SimpleDateFormat PRIMARY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat ALTERNATE_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MMM dd", Locale.getDefault());

    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    /**
     * Parse date string with multiple format support
     */
    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            // Try primary format first (yyyy-MM-dd)
            return PRIMARY_DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            try {
                // Try alternate format (dd-MM-yyyy)
                return ALTERNATE_DATE_FORMAT.parse(dateString);
            } catch (ParseException e2) {
                // Return null if both formats fail
                return null;
            }
        }
    }

    /**
     * Check if a date falls in the current month
     */
    public static boolean isCurrentMonth(Date date) {
        if (date == null) {
            return false;
        }

        Calendar current = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTime(date);

        return current.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH);
    }

    /**
     * Format date for birthday display (e.g., "July 15")
     */
    public static String formatBirthdayDate(String dateString) {
        Date date = parseDate(dateString);
        if (date == null) {
            return "Invalid Date";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return MONTH_NAMES[month] + " " + day;
    }

    /**
     * Get the current month name
     */
    public static String getCurrentMonthName() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        return MONTH_NAMES[currentMonth];
    }

    /**
     * Check if today is the employee's birthday
     */
    public static boolean isBirthdayToday(String dateString) {
        Date birthDate = parseDate(dateString);
        if (birthDate == null) {
            return false;
        }

        Calendar today = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);

        return today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == birth.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get days until next birthday (within current month)
     */
    public static int getDaysUntilBirthday(String dateString) {
        Date birthDate = parseDate(dateString);
        if (birthDate == null) {
            return -1;
        }

        Calendar today = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);

        // Set birth year to current year
        birth.set(Calendar.YEAR, today.get(Calendar.YEAR));

        // If birthday has passed this year, set to next year
        if (birth.before(today)) {
            birth.add(Calendar.YEAR, 1);
        }

        long diffInMillis = birth.getTimeInMillis() - today.getTimeInMillis();
        return (int) (diffInMillis / (24 * 60 * 60 * 1000));
    }
}