package io.chronetic.data.measure;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Holds the minimum/maximum scale ChronoScaleUnits can take as well
 * as the current enabled and disabled units.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoScale {

    private final Map<ChronoUnit, ChronoScaleUnit> chronoScaleMap;

    public ChronoScale() {
        chronoScaleMap = new EnumMap<>(ChronoUnit.class);
    }

    /**
     * Returns random enabled ChronoScaleUnit.
     *
     * @param random Random to use
     * @return random enabled ChronoScaleUnit
     */
    @NotNull
    public ChronoScaleUnit getRandomEnabledChronoScaleUnit(@NotNull Random random) {
        List<ChronoScaleUnit> enabledUnits = chronoScaleMap.values().stream()
                .filter(chronoScaleUnit -> !chronoScaleUnit.isDisabled())
                .collect(Collectors.toList());
        if (enabledUnits.isEmpty()) {
            throw new IllegalStateException("Couldn't find any enabled chrono scale units");
        } else {
            return enabledUnits.get(requireNonNull(random).nextInt(enabledUnits.size()));
        }
    }

    /**
     * Returns all enabled ChronoScaleUnit(s).
     *
     * @return all enabled ChronoScaleUnit(s)
     */
    @NotNull
    public List<ChronoScaleUnit> getEnabledChronoScaleUnits() {
        List<ChronoScaleUnit> enabledUnits = chronoScaleMap.values().stream()
                .filter(chronoScaleUnit -> !chronoScaleUnit.isDisabled())
                .collect(Collectors.toList());
        if (enabledUnits.isEmpty()) {
            throw new IllegalStateException("Couldn't find any enabled chrono scale units");
        } else {
            return enabledUnits;
        }
    }

    /**
     * Returns the ChronoScaleUnit for the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return ChronoScaleUnit for given ChronoUnit
     */
    @NotNull
    public ChronoScaleUnit getChronoScaleUnit(@NotNull ChronoUnit chronoUnit) {
        List<ChronoScaleUnit> unitList = chronoScaleMap.values().stream()
                .filter(chronoScaleUnit -> chronoScaleUnit.getChronoUnit() == requireNonNull(chronoUnit))
                .collect(Collectors.toList());
        if (unitList.isEmpty()) {
            throw new IllegalStateException("Couldn't find any chrono scale units");
        } else {
            return unitList.get(0);
        }
    }

    /**
     * Returns the parent ChronoScaleUnit limit based on the given Duration.
     *
     * @param duration desired Duration
     * @return parent limit ChronoScaleUnit
     */
    @NotNull
    public ChronoScaleUnit getParentChronoScaleUnitLimit(@NotNull Duration duration) {
        long decades = ChronoUnit.DECADES.between(LocalDateTime.now(), LocalDateTime.now().plus(duration));
        if (decades == 0) {
            return getChronoScaleUnit(ChronoUnit.DECADES);
        }

        long years = ChronoUnit.YEARS.between(LocalDateTime.now(), LocalDateTime.now().plus(duration));
        if (years == 0) {
            return getChronoScaleUnit(ChronoUnit.YEARS);
        }

        long months = ChronoUnit.MONTHS.between(LocalDateTime.now(), LocalDateTime.now().plus(duration));
        if (months == 0) {
            return getChronoScaleUnit(ChronoUnit.MONTHS);
        }

        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), LocalDateTime.now().plus(duration));
        if (days == 0) {
            return getChronoScaleUnit(ChronoUnit.DAYS);
        }

        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.now().plus(duration));
        if (hours == 0) {
            return getChronoScaleUnit(ChronoUnit.HOURS);
        }

        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), LocalDateTime.now().plus(duration));
        if (minutes == 0) {
            return getChronoScaleUnit(ChronoUnit.MINUTES);
        }

        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), LocalDateTime.now().plus(duration));
        if (seconds == 0) {
            return getChronoScaleUnit(ChronoUnit.SECONDS);
        }

        long millis = ChronoUnit.MILLIS.between(LocalDateTime.now(), LocalDateTime.now().plus(duration));
        if (millis == 0) {
            return getChronoScaleUnit(ChronoUnit.MILLIS);
        }

        long micros = ChronoUnit.MICROS.between(LocalDateTime.now(), LocalDateTime.now().plus(duration));
        if (micros == 0) {
            return getChronoScaleUnit(ChronoUnit.MICROS);
        } else {
            return getChronoScaleUnit(ChronoUnit.NANOS);
        }
    }

    /**
     * Returns the parent ChronoScaleUnit for the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return parent ChronoScaleUnit of given ChronoUnit
     */
    @NotNull
    public ChronoScaleUnit getParentChronoScaleUnit(@NotNull ChronoUnit chronoUnit) {
        return getChronoScaleUnit(getParentChronoUnit(requireNonNull(chronoUnit)));
    }

    /**
     * Returns the child ChronoScaleUnit for the given ChronoUnit.
     * If there is no child ChronoScaleUnit, empty optional is returned.
     *
     * @param chronoUnit desired ChronoUnit
     * @return the child ChronoScaleUnit, if present
     */
    @NotNull
    public Optional<ChronoScaleUnit> getChildScaleUnit(@NotNull ChronoUnit chronoUnit) {
        final ChronoUnit childChronoUnit;
        try {
            childChronoUnit = getChildChronoUnit(chronoUnit);
        } catch (Exception ex) {
            return Optional.empty();
        }

        return Optional.of(getChronoScaleUnit(childChronoUnit));
    }

    /**
     * Returns the parent ChronoUnit for the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return parent ChronoUnit of the given ChronoUnit
     */
    @NotNull
    public static ChronoUnit getParentChronoUnit(@NotNull ChronoUnit chronoUnit) {
        switch (requireNonNull(chronoUnit)) {
            case NANOS:
                return ChronoUnit.SECONDS;
            case MICROS:
                return ChronoUnit.SECONDS;
            case MILLIS:
                return ChronoUnit.SECONDS;
            case SECONDS:
                return ChronoUnit.MINUTES;
            case MINUTES:
                return ChronoUnit.HOURS;
            case HOURS:
                return ChronoUnit.DAYS;
            case DAYS:
                return ChronoUnit.WEEKS;
            case WEEKS:
                return ChronoUnit.MONTHS;
            case MONTHS:
                return ChronoUnit.YEARS;
            case YEARS:
                return ChronoUnit.DECADES;
            case DECADES:
                return ChronoUnit.CENTURIES;
            case CENTURIES:
                return ChronoUnit.ERAS;
            default:
                throw new UnsupportedOperationException("Unsupported chrono unit: " + chronoUnit);
        }
    }

    /**
     * Returns the child ChronoUnit for the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return child ChronoUnit of the given ChronoUnit
     */
    @NotNull
    public static ChronoUnit getChildChronoUnit(@NotNull ChronoUnit chronoUnit) {
        switch (requireNonNull(chronoUnit)) {
            case MICROS:
                return ChronoUnit.NANOS;
            case MILLIS:
                return ChronoUnit.MICROS;
            case SECONDS:
                return ChronoUnit.MILLIS;
            case MINUTES:
                return ChronoUnit.SECONDS;
            case HOURS:
                return ChronoUnit.MINUTES;
            case DAYS:
                return ChronoUnit.HOURS;
            case WEEKS:
                return ChronoUnit.DAYS;
            case MONTHS:
                return ChronoUnit.WEEKS;
            case YEARS:
                return ChronoUnit.MONTHS;
            case DECADES:
                return ChronoUnit.YEARS;
            case MILLENNIA:
                return ChronoUnit.DECADES;
            case ERAS:
                return ChronoUnit.MILLENNIA;
            default:
                throw new UnsupportedOperationException("Unsupported chrono unit: " + chronoUnit);
        }
    }

    /**
     * Updates the current stored ChronoScaleUnit with the given ChronoScaleUnit.
     *
     * @param chronoScaleUnit updated ChronoScaleUnit
     */
    public void updateChronoScaleUnit(@NotNull ChronoScaleUnit chronoScaleUnit) {
        chronoScaleMap.put(requireNonNull(chronoScaleUnit).getChronoUnit(), chronoScaleUnit);
    }

    /**
     * Returns the actual minimum value for the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return actual minimum value for the given ChronoUnit
     */
    public long getActualMinimum(@NotNull ChronoUnit chronoUnit) {
        if (!chronoScaleMap.containsKey(requireNonNull(chronoUnit))) {
            throw new IllegalArgumentException("Missing chrono unit: " + chronoUnit);
        }
        return chronoScaleMap.get(chronoUnit).getActualMinimum();
    }

    /**
     * Returns the actual maximum value for the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return actual maximum value for the given ChronoUnit
     */
    public long getActualMaximum(@NotNull ChronoUnit chronoUnit) {
        if (!chronoScaleMap.containsKey(requireNonNull(chronoUnit))) {
            throw new IllegalArgumentException("Missing chrono unit: " + chronoUnit);
        }
        return chronoScaleMap.get(chronoUnit).getActualMaximum();
    }

    /**
     * Returns the factual minimum value for the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return factual minimum value for the given ChronoUnit
     */
    public static long getFactualMinimum(@NotNull ChronoUnit chronoUnit) {
        switch (requireNonNull(chronoUnit)) {
            case NANOS:
            case MICROS:
            case MILLIS:
            case SECONDS:
            case MINUTES:
            case HOURS:
            case DAYS:
            case WEEKS:
            case MONTHS:
            case YEARS:
            case DECADES:
            case CENTURIES:
                return 0;
            default:
                throw new UnsupportedOperationException("Unsupported chrono unit: " + chronoUnit);
        }
    }

    /**
     * Returns the factual maximum value for the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return factual maximum value for the given ChronoUnit
     */
    public static long getFactualMaximum(@NotNull ChronoUnit chronoUnit) {
        switch (requireNonNull(chronoUnit)) {
            case NANOS:
                return 1_000_000_000;
            case MICROS:
                return 1_000_000;
            case MILLIS:
                return 1000;
            case SECONDS:
            case MINUTES:
                return 60;
            case HOURS:
                return 24;
            case DAYS:
                return 7;
            case WEEKS:
                return 6;
            case MONTHS:
                return 12;
            case YEARS:
                return 4000; //someone else should probably take over from here
            case DECADES:
            case CENTURIES:
                return 10;
            default:
                throw new UnsupportedOperationException("Unsupported chrono unit: " + chronoUnit);
        }
    }

    /**
     * Returns the equivalent ChronoField for the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return equivalent ChronoField of the given ChronoUnit
     */
    @NotNull
    public static ChronoField getChronoField(@NotNull ChronoUnit chronoUnit) {
        switch (requireNonNull(chronoUnit)) {
            case NANOS:
                return ChronoField.NANO_OF_SECOND;
            case MICROS:
                return ChronoField.MICRO_OF_SECOND;
            case MILLIS:
                return ChronoField.MILLI_OF_SECOND;
            case SECONDS:
                return ChronoField.SECOND_OF_MINUTE;
            case MINUTES:
                return ChronoField.MINUTE_OF_HOUR;
            case HOURS:
                return ChronoField.HOUR_OF_DAY;
            case DAYS:
                return ChronoField.DAY_OF_WEEK;
            case WEEKS:
                return ChronoField.ALIGNED_WEEK_OF_MONTH;
            case MONTHS:
                return ChronoField.MONTH_OF_YEAR;
            case YEARS:
                return ChronoField.YEAR_OF_ERA;
            default:
                throw new UnsupportedOperationException("Unsupported chrono unit: " + chronoUnit);
        }
    }

}
