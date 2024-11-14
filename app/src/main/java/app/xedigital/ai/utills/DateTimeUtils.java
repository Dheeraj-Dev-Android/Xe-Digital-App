package app.xedigital.ai.utills;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    public static String calculateTotalTime(String punchInTime, String punchOutTime) {
        if (punchInTime == null || punchOutTime == null || punchInTime.equals("N/A") || punchOutTime.equals("N/A")) {
            return "N/A";
        }

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        try {
            Date punchIn = format.parse(punchInTime);
            Date punchOut = format.parse(punchOutTime);

            if (punchOut != null && punchIn != null) {
                long differenceInMillis = punchOut.getTime() - punchIn.getTime();
                long diffHours = differenceInMillis / (60 * 60 * 1000);
                long diffMinutes = (differenceInMillis % (60 * 60 * 1000)) / (60 * 1000);

                return String.format(Locale.getDefault(), "%02d:%02d Hrs", diffHours, diffMinutes);
            }
        } catch (ParseException e) {
            Log.e("DateTimeUtils", "Error calculating total time: " + e.getMessage());
        }
        return "N/A";
    }

    public static String calculateLateTime(String punchInTime, String shiftStartTime) {
        if (punchInTime == null || shiftStartTime == null || punchInTime.equals("N/A") || shiftStartTime.equals("N/A")) {
            return "N/A";
        }
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat shiftFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat time12Format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        try {
            Date punchInISO = isoFormat.parse(punchInTime);
            if (punchInISO != null) {
                String punchIn12 = time12Format.format(punchInISO);

                Date shiftStart = shiftFormat.parse(shiftStartTime);
                Date punchIn = time12Format.parse(punchIn12);

//                if (punchIn != null && punchIn.after(shiftStart)) {
//                    if (shiftStart != null) {
//                        long lateInMillis = punchIn.getTime() - shiftStart.getTime();
//                        long lateHours = lateInMillis / (60 * 60 * 1000);
//                        long lateMinutes = (lateInMillis % (60 * 60 * 1000)) / (60 * 1000);
//                        return String.format(Locale.getDefault(), "%02d:%02d Hrs", lateHours, lateMinutes);
//                    }
//                } else {
//                    return "0 Min's";
//                }

                if (punchIn != null && punchIn.after(shiftStart)) {
                    if (shiftStart != null) {
                        long lateInMillis = punchIn.getTime() - shiftStart.getTime();
                        long lateMinutes = lateInMillis / (60 * 1000);

                        if (lateMinutes < 60) {
                            return lateMinutes + " Min's";
                        } else {
                            long lateHours = lateMinutes / 60;
                            long remainingMinutes = lateMinutes % 60;
                            return String.format(Locale.getDefault(), "%02d:%02d Hrs", lateHours, remainingMinutes);
                        }
                    }
                } else {
                    return "0 Min's";
                }
            }
        } catch (ParseException e) {
            Log.e("DateTimeUtils", "Error calculating late time: " + e.getMessage());
        }
        return "N/A";
    }


    public static String calculateOvertime(String totalTime) {
        if (totalTime == null || totalTime.equals("N/A") || totalTime.trim().isEmpty() || totalTime.trim().equals(":00")) {
            return "N/A";
        }
        totalTime = totalTime.replace(" Hrs", "");
        if (!totalTime.contains(":")) {
            totalTime += ":00";
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date totalTimeDate = format.parse(totalTime);
            if (totalTimeDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(totalTimeDate);

                int totalHours = calendar.get(Calendar.HOUR_OF_DAY);
                int totalMinutes = calendar.get(Calendar.MINUTE);
                int totalTimeInMinutes = (totalHours * 60) + totalMinutes;

                if (totalTimeInMinutes > (9 * 60)) {
                    int overtimeMinutes = totalTimeInMinutes - (9 * 60);
                    if (overtimeMinutes >= 60) {
                        int overtimeHours = overtimeMinutes / 60;
                        int remainingMinutes = overtimeMinutes % 60;
                        return String.format(Locale.getDefault(), "%02d:%02d Hrs", overtimeHours, remainingMinutes);
                    } else {
                        return String.format(Locale.getDefault(), "%02d Min's", overtimeMinutes);
                    }
                } else {
                    return "0";
                }
            }
        } catch (ParseException e) {
            Log.e("DateTimeUtils", "Error calculating overtime: " + e.getMessage());
        }
        return "N/A";
    }


    public static String formatTime(String timeString) {
        if (timeString == null || timeString.equals("1900-01-01T00:00:00.000Z")) {
            return "N/A";
        }
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        try {
            Date date = inputFormat.parse(timeString);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            Log.e("DateTimeUtils", "Error parsing time: " + e.getMessage());
        }
        return "N/A";
    }

    public static String getDayOfWeekAndDate(String dateString) {
        if (dateString == null || dateString.equals("1900-01-01T00:00:00.000Z")) {
            return "N/A";
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                String dayOfWeek = dayOfWeekFormat.format(date);
                String dateOnly = outputDateFormat.format(date);
                return dayOfWeek + ", " + dateOnly;
            }
        } catch (ParseException e) {
            Log.e("DateTimeUtils", "Error getting day of week and date: " + e.getMessage());
        }
        return "N/A";
    }
}