package ex.j8;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Predicate;
import org.slf4j.Logger;
import utils.HasLogging;

public final class Chapter5 {
    private static final Logger LOGGER = HasLogging.log();

    private Chapter5() {

    }

    /* Compute Programmer's Day without using plusDays. */
    public static void ex1() {
        final LocalDate programmerDay = LocalDate.ofYearDay(2015, 256);
        LOGGER.trace("{}", programmerDay);
    }

    public static void ex10() {

        LocalDate date = LocalDate.now();
        LocalTime timeFlight = LocalTime.of(15, 5);
        ZoneId losAngeles = ZoneId.of("America/Los_Angeles");
        ZonedDateTime flightBegining = ZonedDateTime.of(date, timeFlight, losAngeles);
        final Duration flightDuration = Duration.ofMinutes(650);
        ZonedDateTime addTo = flightBegining.plus(flightDuration);
        Instant from = Instant.from(addTo);
        ZoneId frankfurt = ZoneId.of("Europe/Berlin");
        ZonedDateTime flightArrivalDateTime = from.atZone(frankfurt);
        LOGGER.trace("{}", flightBegining);
        LOGGER.trace("{}", flightArrivalDateTime);

        LOGGER.trace("\n");

    }

    /*
     * Your return flight leaves Frankfurt at 14:05 and arrives in Los Angeles at
     * 16:40. How long is the flight? Write a program that can handle calculations
     * like this.
     */
    public static void ex11() {

        LocalDate date = LocalDate.now();
        ZoneId losAngeles = ZoneId.of("America/Los_Angeles");
        ZoneId frankfurt = ZoneId.of("Europe/Berlin");
        final ZonedDateTime flightBegining = ZonedDateTime.of(date, LocalTime.of(14, 5), frankfurt);
        final ZonedDateTime flightArrivalDateTime = ZonedDateTime.of(date, LocalTime.of(16, 40), losAngeles);

        LOGGER.trace("{}", flightBegining);
        LOGGER.trace("{}", flightArrivalDateTime);
        String replace = Duration.between(flightBegining, flightArrivalDateTime).toString().replace("H", " hours ")
            .replace("M", " minutes").replace("PT", "");
        LOGGER.trace(replace);

    }

    /*
     * What happens when you add one year to LocalDate.of(2000, 2, 29)? Four years?
     * Four times one year?
     * 
     * Local date finds the closest match for that sum.
     */
    public static void ex2() {
        final LocalDate plusYears = LocalDate.of(2000, 2, 29).plusYears(1L);
        LOGGER.trace("{}", plusYears);
    }

    /*
     * Implement a method next that takes a Predicate<LocalDate> and returns an
     * adjuster yielding the next date fulfilling the predicate. For example,
     * 
     * today.with(next(w -> getDayOfWeek().getValue() < 6))
     * 
     * computes the next workday
     */
    public static void ex3() {

        LocalDate with = LocalDate.now().with(next(w -> w.getDayOfWeek().getValue() < 6));
        LOGGER.trace("{}", with);
    }

    /*
     * Write an equivalent of the Unix cal program that displays a calendar for a
     * month. For example, java Cal 3 2013 should display
     * 
     * 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29
     * 30 31
     *
     * indicating that March 1 is a Friday. (Show the weekend at the end of the
     * week.)
     * 
     */
    public static void ex4() {
        LocalDate now = LocalDate.now();
        int month = now.get(ChronoField.MONTH_OF_YEAR);
        int year = now.get(ChronoField.YEAR);
        now = LocalDate.of(year, month, 1);
        StringBuilder s = new StringBuilder();
        while (month == now.get(ChronoField.MONTH_OF_YEAR)) {
            for (int j = 1; j <= 7; j++) {
                if (month != now.get(ChronoField.MONTH_OF_YEAR)) {
                    break;
                }
                if (now.get(ChronoField.DAY_OF_WEEK) == j) {
                    s.append(String.format(" %2d", now.get(ChronoField.DAY_OF_MONTH)));
                    now = now.plusDays(1);
                } else {
                    s.append("   ");
                }
            }
            s.append("\n");
        }
        LOGGER.trace("\n{}", s);
    }

    /* Write a program that prints how many days you have been alive. */
    public static void ex5() {

        final LocalDate birth = LocalDate.of(1992, 2, 21);
        long l = birth.until(LocalDate.now(), ChronoUnit.DAYS);
        LOGGER.trace("{} days", l);

    }

    /* List all Friday the 13th in the twentieth century. */
    public static void ex6() {
        final int year = 1201;
        LocalDate nextFriday = LocalDate.of(year, 1, 1);
        final int century13th = 1300;
        while (nextFriday.get(ChronoField.YEAR) <= century13th) {

            nextFriday = nextFriday.with(w -> {
                LocalDate l = LocalDate.from(w);
                while (l.getDayOfWeek().getValue() != 5) {
                    l = l.plusDays(1);
                }
                return l;
            });

            LOGGER.trace("{}", nextFriday);
            nextFriday = nextFriday.plusDays(7);
        }
    }

    /*
     * Implement a TimeInterval class that represents an interval of time, suitable
     * for calendar events (such as a meeting on a given date from 10:00 to 11:00).
     * Provide a method to check whether two intervals overlap.
     */
    public static void ex7() {
        // Too lazy to do it
    }

    /*
     * Again using stream operations, find all time zones whose offsets aren't full
     * hours.
     */

    /*
     * Obtain the offsets of today's date in all supported time zones for the
     * current time instant, turning ZoneId.getAvailableIds into a stream and using
     * stream operations.
     */
    public static void ex8() {

        Instant now = Instant.now();
        ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).map(now::atZone)
            .sorted(Comparator.comparing(ZonedDateTime::toOffsetDateTime))
            .map(o -> o.getZone().getDisplayName(TextStyle.NARROW, Locale.getDefault()) + "\t" + o.getOffset())
            .forEach(LOGGER::trace);

    }

    /*
     * Your flight from Los Angeles to Frankfurt leaves at 3:05 pm local time and
     * takes 10 hours and 50 minutes. When does it arrive? Write a program that can
     * handle calculations like this.
     */

    public static void ex9() {

        final int secondsInAHour = 3600;
        Instant now = Instant.now();
        ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).map(now::atZone)
            .sorted(Comparator.comparing(ZonedDateTime::toOffsetDateTime))
            .filter(z -> z.getOffset().getTotalSeconds() % secondsInAHour != 0)
            .map(o -> o.getZone().getDisplayName(TextStyle.NARROW, Locale.getDefault()) + "\t" + o.getOffset())
            .forEach(LOGGER::trace);

    }

    public static void main(String[] args) {
        ex4();
    }

    private static TemporalAdjuster next(Predicate<LocalDate> predicate) {

        return temporal -> {
            int day = temporal.get(ChronoField.DAY_OF_YEAR);
            int year = temporal.get(ChronoField.YEAR);
            while (!predicate.test(LocalDate.ofYearDay(year, ++day))) {
                if (day - temporal.get(ChronoField.DAY_OF_YEAR) > 7) {
                    break;
                }
            }
            return LocalDate.ofYearDay(year, day);
        };
    }
}
