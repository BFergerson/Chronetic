package io.chronetic.evolution.pool.allele;

import io.chronetic.data.ChronoSeries;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static java.util.Objects.requireNonNull;

/**
 * Represents the frequency at which timestamps are being collected.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoFrequency extends ChronoAllele {

    private final ChronoUnit chronoUnit;
    private final SummaryStatistics frequencyStatistics;
    private final Instant lastOccurrenceTimestamp;

    public ChronoFrequency(@NotNull ChronoUnit chronoUnit, int chronoSeriesPosition,
                           long exactFrequency, @NotNull Instant lastOccurrenceTimestamp) {
        this(requireNonNull(chronoUnit), chronoSeriesPosition, exactFrequency, exactFrequency,
                requireNonNull(lastOccurrenceTimestamp));
    }

    public ChronoFrequency(@NotNull ChronoUnit chronoUnit, int chronoSeriesPosition,
                           long minimumFrequency, long maximumFrequency, @NotNull Instant lastOccurrenceTimestamp) {
        super(chronoSeriesPosition);
        this.chronoUnit = requireNonNull(chronoUnit);
        this.frequencyStatistics = new SummaryStatistics();
        this.frequencyStatistics.addValue(minimumFrequency);
        if (minimumFrequency != maximumFrequency) {
            this.frequencyStatistics.addValue(maximumFrequency);
        }
        this.lastOccurrenceTimestamp = requireNonNull(lastOccurrenceTimestamp);

        if (minimumFrequency < 0 || maximumFrequency < 0) {
            throw new IllegalArgumentException("Minimum and maximum frequency must be greater than zero");
        } else if (minimumFrequency > maximumFrequency || maximumFrequency < minimumFrequency) {
            throw new IllegalArgumentException("Invalid minimum and maximum frequency combination");
        }
    }

    public ChronoFrequency(@NotNull ChronoUnit chronoUnit, int chronoSeriesPosition,
                           @NotNull SummaryStatistics frequencyStatistics, @NotNull Instant lastOccurrenceTimestamp) {
        super(chronoSeriesPosition);
        this.chronoUnit = requireNonNull(chronoUnit);
        this.frequencyStatistics = new SummaryStatistics(requireNonNull(frequencyStatistics));
        this.lastOccurrenceTimestamp = requireNonNull(lastOccurrenceTimestamp);
    }

    /**
     * Creates a new ChronoFrequency which has progressed in the ChronoSeries by one step.
     *
     * @param chronoSeries time series data to traverse
     * @return new ChronoFrequency with latest frequency from traversing time series data
     */
    @NotNull
    @Override
    public ChronoFrequency mutate(@NotNull ChronoSeries chronoSeries) {
        if (seriesPosition >= requireNonNull(chronoSeries).getSize()) {
            //start from beginning of series
            return new ChronoFrequency(chronoUnit, 0, getMinimumFrequency(), getMaximumFrequency(),
                    chronoSeries.getTimestamp(0));
        }

        Instant nextTimestamp = chronoSeries.getTimestamp(seriesPosition);
        LocalDateTime firstDateTime = getLastOccurrenceTimestamp().atZone(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime secondDateTime = nextTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();

        if (secondDateTime.isBefore(firstDateTime)) {
            throw new RuntimeException("first: " + firstDateTime + "; second: " + secondDateTime);
        }

        //update chrono frequency gene
        long frequency = getChronoUnit().between(firstDateTime, secondDateTime);
        LocalDateTime addedDateTime = firstDateTime.plus(frequency, getChronoUnit());
        if (addedDateTime.isBefore(secondDateTime) && isWithinRange(frequency)) {
            frequency++;
        } else if (frequency == 0) {
            //no point in a frequency of nothing
            frequency++;
        }
        return mutateChronoFrequency(frequency, nextTimestamp);
    }

    @Override
    public boolean isValid() {
        //must have a frequency of 1 or more
        return !(getMinimumFrequency() < 1 || getMaximumFrequency() < 1);
    }

    @NotNull
    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    public long getMinimumFrequency() {
        return (long) frequencyStatistics.getMin();
    }

    public long getMaximumFrequency() {
        return (long) frequencyStatistics.getMax();
    }

    @NotNull
    public SummaryStatistics getFrequencyStatistics() {
        return frequencyStatistics;
    }

    @NotNull
    @Contract(pure = true)
    private Instant getLastOccurrenceTimestamp() {
        return lastOccurrenceTimestamp;
    }

    @NotNull
    private ChronoFrequency mutateChronoFrequency(long actualFrequency, @NotNull Instant lastOccurrenceTimestamp) {
        if (actualFrequency < 0) {
            throw new IllegalArgumentException("Invalid frequency: " + actualFrequency);
        }

        frequencyStatistics.addValue(actualFrequency);
        return new ChronoFrequency(chronoUnit, seriesPosition + 1,
                frequencyStatistics, requireNonNull(lastOccurrenceTimestamp));
    }

    private boolean isWithinRange(long frequency) {
        return frequency >= getMinimumFrequency() && frequency <= getMaximumFrequency();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChronoFrequency that = (ChronoFrequency) o;

        if (getMinimumFrequency() != that.getMinimumFrequency()) return false;
        if (getMaximumFrequency() != that.getMaximumFrequency()) return false;
        if (chronoUnit != that.chronoUnit) return false;
        return lastOccurrenceTimestamp.equals(that.lastOccurrenceTimestamp);
    }

    @Override
    public int hashCode() {
        int result = chronoUnit.hashCode();
        result = 31 * result + (int) (getMinimumFrequency() ^ (getMinimumFrequency() >>> 32));
        result = 31 * result + (int) (getMaximumFrequency() ^ (getMaximumFrequency() >>> 32));
        result = 31 * result + lastOccurrenceTimestamp.hashCode();
        return result;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("ChronoFrequency: {%d -> %d; Unit: %s}",
                getMinimumFrequency(), getMaximumFrequency(), chronoUnit);
    }

}
