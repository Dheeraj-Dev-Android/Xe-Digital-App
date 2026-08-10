package app.xedigital.ai.utills;

import android.util.Log;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeUtils {

    private static final String TAG = "DateTimeUtils";
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    // Thread-safe formatters
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).withZone(UTC_ZONE);
    private static final DateTimeFormatter READABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault());
    private static final DateTimeFormatter TIME_12H_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
    private static final DateTimeFormatter TIME_12H_FORMATTER_NO_SPACE = DateTimeFormatter.ofPattern("hh:mma", Locale.getDefault());
    private static final DateTimeFormatter SHIFT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    public static String calculateTotalTime(String punchIn, String punchOut) {
        if (isInvalidInput(punchIn) || isInvalidInput(punchOut)) {
            return "00:00 Hrs";
        }

        try {
            LocalTime time1 = parseFlexibleTime(punchIn);
            LocalTime time2 = parseFlexibleTime(punchOut);

            if (time1 == null || time2 == null) return "00:00 Hrs";

            Duration duration = Duration.between(time1, time2);
            if (duration.isNegative()) {
                duration = duration.plusDays(1); // Overnight shift handling
            }

            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;

            return String.format(Locale.getDefault(), "%02d:%02d Hrs", hours, minutes);
        } catch (Exception e) {
            Log.e(TAG, "Error calculating total time: " + e.getMessage());
            return "00:00 Hrs";
        }
    }

    public static String formatToReadableDate(String isoString) {
        if (isInvalidInput(isoString)) return "";
        try {
            Instant instant = Instant.parse(isoString);
            LocalDate date = instant.atZone(UTC_ZONE).toLocalDate();
            return date.format(READABLE_DATE_FORMATTER);
        } catch (Exception e) {
            return isoString;
        }
    }

    public static String extractTime(String dateTimeString) {
        if (isInvalidInput(dateTimeString)) return "N/A";
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return offsetDateTime.toLocalTime().format(TIME_12H_FORMATTER);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting time: " + e.getMessage());
            return "N/A";
        }
    }

    public static String calculateLateTime(String punchInTime, String shiftStartTime) {
        if (isInvalidInput(punchInTime) || isInvalidInput(shiftStartTime)) {
            return "0 Min's";
        }

        try {
            LocalTime punchIn = parseFlexibleTime(punchInTime);
            LocalTime shiftStart = parseFlexibleTime(shiftStartTime);

            if (punchIn != null && shiftStart != null && punchIn.isAfter(shiftStart)) {
                long lateMinutes = Duration.between(shiftStart, punchIn).toMinutes();

                if (lateMinutes < 60) {
                    return lateMinutes + " Min's";
                } else {
                    return String.format(Locale.getDefault(), "%02d:%02d Hrs", lateMinutes / 60, lateMinutes % 60);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating late time: " + e.getMessage() + " for input: " + punchInTime);
        }
        return "0 Min's";
    }

    public static String calculateOvertime(String totalTimeStr, String shiftStartTime, String shiftEndTime) {
        if (isInvalidInput(totalTimeStr) || totalTimeStr.equalsIgnoreCase("00:00 Hrs")) {
            return "0 Min's";
        }

        try {
            String cleaned = totalTimeStr.replace("Hrs", "").trim();
            String[] parts = cleaned.split(":");
            if (parts.length < 2) return "0 Min's";

            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());
            int totalWorkedMinutes = (hours * 60) + minutes;

            // Calculate standard shift duration from employee shift times
            LocalTime shiftStart = parseFlexibleTime(shiftStartTime);
            LocalTime shiftEnd = parseFlexibleTime(shiftEndTime);

            int standardWorkMinutes = 9 * 60; // 9 Hours default fallback
            if (shiftStart != null && shiftEnd != null) {
                Duration shiftDuration = Duration.between(shiftStart, shiftEnd);
                if (shiftDuration.isNegative()) {
                    shiftDuration = shiftDuration.plusDays(1); // Overnight shift support
                }
                standardWorkMinutes = (int) shiftDuration.toMinutes();
            }

            if (totalWorkedMinutes > standardWorkMinutes) {
                int overtimeMinutes = totalWorkedMinutes - standardWorkMinutes;
                if (overtimeMinutes >= 60) {
                    return String.format(Locale.getDefault(), "%02d:%02d Hrs", overtimeMinutes / 60, overtimeMinutes % 60);
                } else {
                    return String.format(Locale.getDefault(), "%d Min's", overtimeMinutes);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating overtime: " + e.getMessage());
        }
        return "0 Min's";
    }

    public static String formatTime(String timeString) {
        if (isInvalidInput(timeString) || timeString.equals("1900-01-01T00:00:00.000Z")) {
            return "N/A";
        }
        try {
            LocalTime parsedLocalTime = parseFlexibleTime(timeString);
            if (parsedLocalTime != null) {
                return parsedLocalTime.format(TIME_12H_FORMATTER);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing time: " + e.getMessage());
        }
        return "N/A";
    }

    public static String getDayOfWeekAndDate(String dateString) {
        if (isInvalidInput(dateString) || dateString.equals("1900-01-01T00:00:00.000Z")) {
            return "N/A";
        }

        try {
            Instant instant = Instant.parse(dateString);
            ZonedDateTime dateTime = instant.atZone(UTC_ZONE);
            String dayOfWeek = dateTime.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()));
            String dateOnly = dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault()));
            return dayOfWeek + ", " + dateOnly;
        } catch (Exception e) {
            Log.e(TAG, "Error getting day of week and date: " + e.getMessage());
        }
        return "N/A";
    }

    public static String getCurrentDateInISOFormat() {
        return ZonedDateTime.now(UTC_ZONE).format(ISO_FORMATTER);
    }

    public static String getMonthDayFromISO(String isoDate) {
        if (isInvalidInput(isoDate)) return "";
        try {
            Instant instant = Instant.parse(isoDate);
            LocalDate dob = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            return dob.format(DateTimeFormatter.ofPattern("MM-dd"));
        } catch (Exception e) {
            return "";
        }
    }

    private static LocalTime parseFlexibleTime(String dateStr) {
        if (isInvalidInput(dateStr)) return null;

        String cleanStr = dateStr.trim();

        if (cleanStr.contains("T")) {
            try {
                Instant instant = Instant.parse(cleanStr);
                return instant.atZone(IST_ZONE).toLocalTime();
            } catch (Exception ignored) {
            }
        }

        try {
            return LocalTime.parse(cleanStr.toUpperCase(Locale.getDefault()), TIME_12H_FORMATTER);
        } catch (Exception ignored) {
        }

        try {
            return LocalTime.parse(cleanStr.toUpperCase(Locale.getDefault()), TIME_12H_FORMATTER_NO_SPACE);
        } catch (Exception ignored) {
        }

        try {
            return LocalTime.parse(cleanStr, SHIFT_TIME_FORMATTER);
        } catch (Exception ignored) {
        }

        return null;
    }

    private static boolean isInvalidInput(String input) {
        if (input == null) return true;

        String trimmed = input.trim();
        return trimmed.isEmpty()
                || trimmed.equalsIgnoreCase("N/A")
                || trimmed.equalsIgnoreCase("NA")
                || trimmed.equalsIgnoreCase("NULL")
                || trimmed.equalsIgnoreCase("NONE");
    }
}