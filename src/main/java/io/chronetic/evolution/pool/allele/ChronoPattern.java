package io.chronetic.evolution.pool.allele;

import io.chronetic.data.ChronoSeries;
import io.chronetic.data.measure.ChronoScaleUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;

/**
 * Represents a single chronological pattern in time.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoPattern extends ChronoAllele {

    private final ChronoScaleUnit chronoScaleUnit;
    private int temporalValue;

    public ChronoPattern(@NotNull ChronoScaleUnit chronoScaleUnit, int seriesPosition, int temporalValue) {
        super(seriesPosition);
        this.chronoScaleUnit = requireNonNull(chronoScaleUnit);
        this.temporalValue = temporalValue;

        if (temporalValue != 0) {
            chronoScaleUnit.observeValue(temporalValue);
        }
    }

    /**
     * Create a new ChronoPattern instance with the given temporal pattern value.
     *
     * @param temporalValue temporal pattern value, 0 signifies for every chrono scale unit
     * @return ChronoPattern with the given temporal pattern value
     */
    @NotNull
    public ChronoPattern newInstance(int temporalValue) {
        return new ChronoPattern(chronoScaleUnit, seriesPosition, temporalValue);
    }

    /**
     * Creates a new ChronoPattern which has progressed in the ChronoSeries by one step.
     *
     * @param chronoSeries time series data to traverse
     * @return new ChronoPattern of the same chrono scale unit with updated temporal pattern value
     */
    @NotNull
    @Override
    public ChronoPattern mutate(@NotNull ChronoSeries chronoSeries) {
        if (seriesPosition >= requireNonNull(chronoSeries).getSize()) {
            //start from beginning of series
            return new ChronoPattern(chronoScaleUnit, 0, temporalValue);
        }

        Instant nextTimestamp = chronoSeries.getTimestamp(seriesPosition);
        LocalDateTime nextDateTime = nextTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();

        int temporalValue = nextDateTime.get(getChronoScaleUnit().getChronoField());
        return new ChronoPattern(chronoScaleUnit, seriesPosition + 1, temporalValue);
    }

    @Override
    public boolean isValid() {
        return !getTemporalValue().isPresent() || chronoScaleUnit.isValid(temporalValue);
    }

    /**
     * Returns the ChronoScale unit of this ChronoPattern.
     *
     * @return current chrono scale unit
     */
    @NotNull
    public ChronoScaleUnit getChronoScaleUnit() {
        return chronoScaleUnit;
    }

    /**
     * Returns the temporal pattern value of this ChronoPattern.
     * A missing temporal patter value means "every" of the unit.
     *
     * For example, with a chrono scale unit of SECONDS and a missing temporal value
     * this translates to: every second.
     *
     * @return temporal pattern value, if present
     */
    @NotNull
    public OptionalInt getTemporalValue() {
        if (temporalValue == 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(temporalValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChronoPattern that = (ChronoPattern) o;

        if (temporalValue != that.temporalValue) return false;
        return chronoScaleUnit.equals(that.chronoScaleUnit);
    }

    @Override
    public int hashCode() {
        int result = chronoScaleUnit.hashCode();
        result = 31 * result + temporalValue;
        return result;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("ChronoPattern: {Unit: %s; Value: %d}", chronoScaleUnit.getChronoUnit(), temporalValue);
    }

}
