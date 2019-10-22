package utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

public final class DateFormatUtils {
    public static final DateTimeFormatter TIME_OF_SECONDS_FORMAT = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendLiteral('.')
        .appendValue(ChronoField.MILLI_OF_SECOND, 2, 3, SignStyle.NEVER).toFormatter();

    private DateFormatUtils() {
    }

    public static long convertTimeToMillis(String text) {
        return ChronoUnit.MILLIS.between(LocalTime.MIN, TIME_OF_SECONDS_FORMAT.parse(text, LocalTime::from));
    }

    public static LocalDate extractDate(final String children) {
        return SupplierEx.get(() -> LocalDate.parse(children, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    public static String format(TemporalAccessor text) {
        return TIME_OF_SECONDS_FORMAT.format(text);
    }

    public static String formatDate(TemporalAccessor temporal) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        return dateFormat.format(temporal);
    }

    public static TemporalAccessor parse(CharSequence text) {
        return TIME_OF_SECONDS_FORMAT.parse(text);
    }
}
