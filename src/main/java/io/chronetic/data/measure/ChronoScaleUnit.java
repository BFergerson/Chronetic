package io.chronetic.data.measure;

import io.chronetic.data.ChronoSeries;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Represents a single chronological unit and the minimum/maximum
 * allotted units, the observed minimum/maximum units, and a set of distinct values observed.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoScaleUnit {

    private static final Map<ChronoSeries, Set<ChronoScaleUnit>> cacheMap = new IdentityHashMap<>();
    private final ChronoUnit chronoUnit;
    private final long actualMinimum;
    private final long actualMaximum;
    private Long observedMinimum;
    private Long observedMaximum;
    private Set<Long> observedDistinctSet;

    private ChronoScaleUnit(ChronoUnit chronoUnit, long actualMinimum, long actualMaximum,
                            Long observedMinimum, Long observedMaximum) {
        this.chronoUnit = requireNonNull(chronoUnit);
        this.actualMinimum = actualMinimum;
        this.actualMaximum = actualMaximum;
        this.observedMinimum = observedMinimum;
        this.observedMaximum = observedMaximum;
        this.observedDistinctSet = new HashSet<>();

        if (actualMinimum > actualMaximum || actualMaximum < actualMinimum) {
            throw new IllegalArgumentException("Invalid actual minimum and actual maximum combination");
        }
    }

    public boolean isValid(long temporalValue) {
        return !isDisabled() && (temporalValue < actualMaximum && temporalValue > actualMinimum);
    }

    public boolean isDisabled() {
        return actualMinimum == -1 || actualMaximum == -1;
    }

    @NotNull
    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    /**
     * Returns the actual minimum for this ChronoScaleUnit.
     *
     * @return actual minimum
     */
    public long getActualMinimum() {
        return actualMinimum;
    }

    /**
     * Returns the actual maximum for this ChronoScaleUnit.
     *
     * @return actual maximum
     */
    public long getActualMaximum() {
        return actualMaximum;
    }

    /**
     * Returns the observed minimum for this ChronoScaleUnit.
     * If nothing has been observed, returns empty Optional.
     *
     * @return observed minimum, if present
     */
    @NotNull
    public OptionalLong getObservedMinimum() {
        if (observedMinimum == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(observedMinimum);
    }

    /**
     * Returns the observed maximum for this ChronoScaleUnit.
     * If nothing has been observed, returns empty Optional.
     *
     * @return observed maximum, if present
     */
    @NotNull
    public OptionalLong getObservedMaximum() {
        if (observedMaximum == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(observedMaximum);
    }

    /**
     * Observes the given temporal value.
     *
     * @param temporalValue temporal value to observe
     */
    public void observeValue(long temporalValue) {
        if (temporalValue > actualMaximum || temporalValue < actualMinimum) {
            throw new IllegalArgumentException("Invalid temporal value: " + temporalValue);
        }

        if (observedMinimum == null || temporalValue < observedMinimum) {
            this.observedMinimum = temporalValue;
            observedDistinctSet.add(temporalValue);
        }
        if (observedMaximum == null || temporalValue > observedMaximum) {
            this.observedMaximum = temporalValue;
            observedDistinctSet.add(temporalValue);
        }
    }

    /**
     * Returns a set of the distinct observed values for this ChronoScaleUnit.
     *
     * @return set of observed temporal values
     */
    @NotNull
    public Set<Long> getObservedDistinctSet() {
        return observedDistinctSet;
    }

    /**
     * Returns the factual minimum for this ChronoScaleUnit.
     *
     * @return factual minimum
     */
    public long getFactualMinimum() {
        return ChronoScale.getFactualMinimum(chronoUnit);
    }

    /**
     * Returns the factual maximum for this ChronoScaleUnit.
     *
     * @return factual maximum
     */
    public long getFactualMaximum() {
        return ChronoScale.getFactualMaximum(chronoUnit);
    }

    /**
     * Create a disabled ChronoScaleUnit from the given ChronoUnit.
     *
     * @param chronoUnit desired ChronoUnit
     * @return disabled ChronoScaleUnit for the given ChronoUnit
     */
    @NotNull
    public static ChronoScaleUnit asDisabled(@NotNull ChronoUnit chronoUnit) {
        return new ChronoScaleUnit(requireNonNull(chronoUnit), -1, -1, null, null);
    }

    /**
     * Create a factual ChronoScaleUnit from the given ChronoUnit.
     * Factual ChronoScaleUnits are stored in cache with ChronoSeries as the key.
     *
     * @param chronoUnit desired ChronoUnit
     * @return factual ChronoScaleUnit for the given ChronoUnit
     */
    @NotNull
    public static ChronoScaleUnit asFactual(@NotNull ChronoSeries chronoSeries, @NotNull ChronoUnit chronoUnit) {
        Set<ChronoScaleUnit> chronoScaleUnits = cacheMap.get(requireNonNull(chronoSeries));
        if (chronoScaleUnits == null) {
            cacheMap.put(chronoSeries, chronoScaleUnits = new HashSet<>());
        }
        ChronoScaleUnit cacheScaleUnit = null;
        for (ChronoScaleUnit scaleUnit : chronoScaleUnits) {
            if (scaleUnit.getChronoUnit() == requireNonNull(chronoUnit)) {
                cacheScaleUnit = scaleUnit;
                break;
            }
        }

        if (cacheScaleUnit == null) {
            ChronoScaleUnit scaleUnit = new ChronoScaleUnit(chronoUnit,
                    ChronoScale.getFactualMinimum(chronoUnit), ChronoScale.getFactualMaximum(chronoUnit),
                    null, null);
            cacheMap.get(requireNonNull(chronoSeries)).add(scaleUnit);
            return scaleUnit;
        } else {
            return cacheScaleUnit;
        }
    }

    /**
     * Returns the equivalent ChronoField for this ChronoScaleUnit.
     *
     * @return equivalent ChronoField
     */
    @NotNull
    public ChronoField getChronoField() {
        return ChronoScale.getChronoField(chronoUnit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChronoScaleUnit that = (ChronoScaleUnit) o;

        if (actualMinimum != that.actualMinimum) return false;
        if (actualMaximum != that.actualMaximum) return false;
        return chronoUnit == that.chronoUnit;
    }

    @Override
    public int hashCode() {
        int result = chronoUnit.hashCode();
        result = 31 * result + (int) (actualMinimum ^ (actualMinimum >>> 32));
        result = 31 * result + (int) (actualMaximum ^ (actualMaximum >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.format("ChronoScaleUnit {Unit: %s; Min: %d - Max: %d}", chronoUnit, actualMinimum, actualMaximum);
    }

}
