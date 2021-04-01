package utils;

import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class DateFormatUtils {
    private static final DateTimeFormatter TIME_OF_SECONDS_FORMAT = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendLiteral('.')
            .appendValue(ChronoField.MILLI_OF_SECOND, 2, 3, SignStyle.NEVER).toFormatter();

    private DateFormatUtils() {
    }

    public static long convertTimeToMillis(String text) {
        return ChronoUnit.MILLIS.between(LocalTime.MIN, TIME_OF_SECONDS_FORMAT.parse(text, LocalTime::from));
    }

    public static String currentDate() {
        return currentTime("dd/MM/yyyy");
    }

    public static String currentHour() {
        return currentTime("HH:mm");
    }

    public static String currentTime(String fmt) {
        return format(fmt, LocalDateTime.now());
    }

    public static LocalDate extractDate(final String children) {
        return SupplierEx.get(() -> LocalDate.parse(children, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    public static String format(String fmt, long now) {
        return SupplierEx.get(() -> DateTimeFormatter.ofPattern(fmt)
                .format(Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDateTime()));
    }

    public static String format(String fmt, TemporalAccessor now) {
        return SupplierEx.getIgnore(() -> DateTimeFormatter.ofPattern(fmt).format(now));
    }

    public static String format(TemporalAccessor text) {
        return TIME_OF_SECONDS_FORMAT.format(text);
    }

    public static String formatDate(TemporalAccessor temporal) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(temporal);
    }

    public static int getYearCreation(Path path) {
        return SupplierEx.getFirst(() -> ResourceFXUtils.computeAttributes(path.toFile()).creationTime().toInstant()
                .atZone(ZoneId.systemDefault()).getYear(), () -> ZonedDateTime.now().getYear());
    }

    public static TemporalAccessor parse(CharSequence text) {
        return TIME_OF_SECONDS_FORMAT.parse(text);
    }

    public static <T> T parse(String now, String fmt, TemporalQuery<T> query) {
        return SupplierEx.getFirst(() -> DateTimeFormatter.ofPattern(fmt, Locale.US).parse(now).query(query),
                () -> DateTimeFormatter.ofPattern(fmt, Locale.getDefault()).parse(now).query(query), () -> {
                    HasLogging.log(2).error("\"{}\" could be parsed fmt=\"{}\" ", now, fmt);
                    return null;
                });
    }

    public static long toNumber(String fmt, String now) {
        return SupplierEx.get(() -> DateTimeFormatter.ofPattern(fmt).parse(now).get(ChronoField.INSTANT_SECONDS));
    }
}
