package io.chronetic.data.measure;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.chronetic.data.ChronoSeries;
import io.chronetic.evolution.pool.ChronoGene;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import io.jenetics.util.ISeq;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.util.Objects.requireNonNull;

/**
 * Represents the time range(s) that a given ChronoPattern sequences includes.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoRange {

    /**
     * Create a ChronoRange for the given ChronoSeries and sequence of ChronoGenes.
     *
     * @param chronoSeries ChronoSeries to create ChronoRange for
     * @param genes ChronoGene sequence containing ChronoPattern(s) to use for creating ChronoRange
     * @return ChronoRange for given ChronoSeries and ChronoGene sequence
     */
    @NotNull
    public static ChronoRange getChronoRange(@NotNull ChronoSeries chronoSeries, @NotNull ISeq<ChronoGene> genes) {
        ChronoRange range = new ChronoRange(requireNonNull(chronoSeries), requireNonNull(genes));
        Cache<ISeq<ChronoPattern>, ChronoRange> cacheChronoRange = cacheMap.get(chronoSeries);
        if (cacheChronoRange == null) {
            cacheChronoRange = CacheBuilder.newBuilder().build();
            cacheMap.put(chronoSeries, cacheChronoRange);
        }

        ChronoRange cacheRange = cacheChronoRange.getIfPresent(range.chronoPatternSeq);
        if (cacheRange != null) {
            return cacheRange;
        } else {
            if (range.validRange) {
                range.calculateTimestampRanges();
            }

            cacheChronoRange.put(range.chronoPatternSeq, range);
            return range;
        }
    }

    private final static Logger logger = LoggerFactory.getLogger(ChronoRange.class);

    private static final Map<ChronoSeries, Cache<ISeq<ChronoPattern>, ChronoRange>> cacheMap = new IdentityHashMap<>();
    private final ChronoSeries chronoSeries;
    private final ChronoScale chronoScale;
    private final ArrayList<Instant[]> timestampRanges;
    private final ISeq<ChronoPattern> chronoPatternSeq;
    private final ChronoPattern smallestPattern;
    private LocalDateTime patternStartLocalDateTime;
    private LocalDateTime patternEndLocalDateTime;
    private boolean includeEndingTimestamp;
    private boolean fullyConceptual;
    private boolean searchRange = true;
    private Duration rangeDuration = Duration.ZERO;
    private final ChronoScaleUnit limitScaleUnit;
    private ChronoScaleUnit tempSkipUnit;
    private boolean validRange = true;

    private ChronoRange(@NotNull ChronoSeries chronoSeries, @NotNull ISeq<ChronoGene> genes) {
        this.chronoSeries = requireNonNull(chronoSeries);
        chronoPatternSeq = requireNonNull(genes).stream()
                .filter(g -> g.getAllele() instanceof ChronoPattern)
                .map(g -> (ChronoPattern) g.getAllele())
                .sorted((o1, o2) -> o2.getChronoScaleUnit().getChronoUnit().compareTo(o1.getChronoScaleUnit().getChronoUnit()))
                .collect(ISeq.toISeq());

        timestampRanges = new ArrayList<>();
        chronoScale = chronoSeries.getChronoScale();
        limitScaleUnit = chronoScale.getParentChronoScaleUnitLimit(chronoSeries.getDuration());
        fullyConceptual = chronoPatternSeq.stream()
                .noneMatch(chronoPattern -> chronoPattern.getTemporalValue().isPresent());

        if (chronoPatternSeq.isEmpty()) {
            validRange = false;
            smallestPattern = null;
            rangeDuration = chronoSeries.getDuration();
            addRange(chronoSeries.getBeginLocalDateTime(), chronoSeries.getEndLocalDateTime());
        } else {
            smallestPattern = chronoPatternSeq.get(chronoPatternSeq.size() - 1);
        }
    }

    private void calculateTimestampRanges() {
        LocalDateTime endTime = chronoSeries.getEndLocalDateTime();
        LocalDateTime itrTime = chronoSeries.getBeginLocalDateTime();

        logger.debug("Starting range determine loop");
        while (searchRange && (itrTime.isEqual(endTime) || itrTime.isBefore(endTime))) {
            for (ChronoPattern chronoPattern : chronoPatternSeq) {
                if (tempSkipUnit != null && chronoPattern != smallestPattern
                        && chronoPattern.getChronoScaleUnit().getChronoUnit() == tempSkipUnit.getChronoUnit()) {
                    continue;
                } else {
                    tempSkipUnit = null;
                }

                LocalDateTime startItrTime = itrTime;
                logger.trace("Start itrTime: " + startItrTime);
                itrTime = progressTime(endTime, itrTime, chronoPattern);
                logger.trace("End itrTime: " + itrTime);
            }
        }
        logger.debug("Finished range determine loop");

        if (patternStartLocalDateTime != null && patternStartLocalDateTime.isBefore(chronoSeries.getBeginLocalDateTime())) {
            patternStartLocalDateTime = chronoSeries.getBeginLocalDateTime();
        }
        if (patternEndLocalDateTime != null && patternEndLocalDateTime.isAfter(chronoSeries.getEndLocalDateTime())) {
            patternEndLocalDateTime = chronoSeries.getEndLocalDateTime();
        }

        for (Instant[] instants : timestampRanges) {
            rangeDuration = rangeDuration.plus(Duration.between(instants[0], instants[1]));
        }
        logger.debug("Range duration: " + rangeDuration);
    }

    @NotNull
    private LocalDateTime progressTime(@NotNull LocalDateTime endTime, @NotNull LocalDateTime itrTime,
                                       @NotNull ChronoPattern chronoPattern) {
        ChronoScaleUnit chronoScaleUnit = chronoPattern.getChronoScaleUnit();
        ChronoUnit chronoUnit = chronoScaleUnit.getChronoUnit();

        LocalDateTime startItrTime = itrTime;
        try {
            itrTime = itrTime.truncatedTo(chronoUnit);
        } catch (UnsupportedTemporalTypeException ex) {
            //do nothing
        } finally {
            if (itrTime.isBefore(startItrTime)) {
                itrTime = startItrTime;
            }
        }

        LocalDateTime itrStartTime = itrTime;
        if (!allMatch(itrTime)) {
            if (isMultiUnit(chronoPattern.getChronoScaleUnit().getChronoUnit())) {
                if (anyUnitMatch(itrTime, chronoPattern.getChronoScaleUnit().getChronoUnit())) {
                    tempSkipUnit = chronoPattern.getChronoScaleUnit();
                    return itrTime;
                } else if (pastPatternMatch(itrTime, chronoPattern) && !allPastPatternMatch(itrTime, chronoUnit)) {
                    return itrTime;
                }
            }

            if (chronoPattern.getTemporalValue().isPresent()) {
                int patternValue = chronoPattern.getTemporalValue().getAsInt();
                LocalDateTime asTime = chronoScaleUnit.getChronoField().adjustInto(itrTime, patternValue);
                try {
                    asTime = asTime.truncatedTo(chronoUnit);
                } catch (UnsupportedTemporalTypeException ex) {
                    if (chronoUnit == ChronoUnit.YEARS) {
                        //truncate to beginning of year
                        asTime = asTime.with(firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
                    } else if (chronoUnit == ChronoUnit.MONTHS) {
                        //truncate to beginning of month
                        asTime = asTime.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
                    } else {
                        //throw new UnsupportedOperationException();
                    }
                } finally {
                    if (asTime.isBefore(startItrTime)) {
                        asTime = chronoScaleUnit.getChronoField().adjustInto(itrTime, patternValue);
                    }
                }

                if (asTime.isBefore(itrTime)) {
                    //skip to next occurrence
                    ChronoScaleUnit parentScaleUnit = chronoScale.getParentChronoScaleUnit(chronoUnit);
                    LocalDateTime desiredTime = asTime.plus(1, parentScaleUnit.getChronoUnit());

                    try {
                        desiredTime = desiredTime.truncatedTo(chronoUnit);
                    } catch (UnsupportedTemporalTypeException ex) {
                        if (chronoUnit == ChronoUnit.YEARS) {
                            //truncate to beginning of year
                            desiredTime = desiredTime.with(firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
                        } else if (chronoUnit == ChronoUnit.MONTHS) {
                            //truncate to beginning of month
                            desiredTime = desiredTime.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
                        } else {
                            //throw new UnsupportedOperationException();
                        }
                    } finally {
                        if (desiredTime.isBefore(startItrTime)) {
                            desiredTime = asTime.plus(1, parentScaleUnit.getChronoUnit());
                        }
                    }

                    long until = itrTime.until(desiredTime, chronoUnit);
                    if (until == 0) {
                        itrTime = desiredTime;
                    } else {
                        itrTime = itrTime.plus(until, chronoUnit);
                    }
                } else {
                    //after itrTime. make itrTime asTime
                    itrTime = asTime;
                }
            }

            if (isMultiUnit(chronoPattern.getChronoScaleUnit().getChronoUnit())
                    && anyUnitMatch(itrTime, chronoPattern.getChronoScaleUnit().getChronoUnit())) {
                tempSkipUnit = chronoPattern.getChronoScaleUnit();
                return itrTime;
            }

            return itrTime;
        }

        OptionalInt temporalValue = chronoPattern.getTemporalValue();
        if (temporalValue.isPresent()) {
            if (chronoPattern.getChronoScaleUnit().getChronoUnit() == smallestPattern.getChronoScaleUnit().getChronoUnit()) {
                int patternValue = temporalValue.getAsInt();
                if (itrTime.get(chronoScaleUnit.getChronoField()) == patternValue) {
                    if (patternStartLocalDateTime == null) {
                        patternStartLocalDateTime = itrTime;
                    }
                }

                itrTime = itrTime.plus(1, chronoUnit);
                try {
                    itrTime = itrTime.truncatedTo(chronoUnit);
                } catch (UnsupportedTemporalTypeException ex) {
                    if (chronoUnit == ChronoUnit.MONTHS) {
                        //truncate to beginning of month
                        itrTime = itrTime.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
                    } else {
                        //throw new UnsupportedOperationException();
                    }
                } finally {
                    if (itrTime.isBefore(startItrTime)) {
                        itrTime = itrTime.plus(1, chronoUnit);
                    }
                }

                if (itrTime.isAfter(endTime)) {
                    includeEndingTimestamp = !itrTime.isEqual(endTime);
                }
                addRange(itrStartTime, itrTime);
                patternEndLocalDateTime = itrTime;
            }
        } else {
            if (patternStartLocalDateTime == null) {
                patternStartLocalDateTime = itrTime;
            }

            if (chronoPattern.getChronoScaleUnit().getChronoUnit() == smallestPattern.getChronoScaleUnit().getChronoUnit()) {
                if (fullyConceptual) {
                    //short circuit
                    addRange(itrTime, endTime);
                    patternEndLocalDateTime = endTime;
                    includeEndingTimestamp = true;
                    searchRange = false;
                } else {
                    ChronoScaleUnit parentScaleUnit = getLocalParent(chronoUnit);
                    LocalDateTime desiredTime = itrTime.plus(1, parentScaleUnit.getChronoUnit());
                    if (desiredTime.isAfter(endTime)) {
                        desiredTime = endTime;
                        includeEndingTimestamp = !desiredTime.isEqual(endTime);
                        searchRange = false;
                    }
                    long until = itrTime.until(desiredTime, chronoUnit);

                    itrTime = itrTime.plus(until, chronoUnit);
                    addRange(itrStartTime, itrTime);
                    patternEndLocalDateTime = itrTime;
                }
            }
        }
        return itrTime;
    }

    @NotNull
    private ChronoScaleUnit getLocalParent(@NotNull ChronoUnit chronoUnit) {
        ChronoScaleUnit parentScaleUnit = chronoScale.getParentChronoScaleUnit(requireNonNull(chronoUnit));
        if (chronoUnit == limitScaleUnit.getChronoUnit()) {
            return limitScaleUnit;
        }

        //find limit
        while (true) {
            boolean match = false;
            for (ChronoPattern chronoPattern : chronoPatternSeq) {
                if (chronoPattern.getChronoScaleUnit().getChronoUnit() == parentScaleUnit.getChronoUnit()
                        && chronoPattern.getTemporalValue().isPresent()) {
                    match = true;
                    break;
                }
            }
            if (match || parentScaleUnit == limitScaleUnit) {
                return parentScaleUnit;
            } else {
                parentScaleUnit = chronoScale.getParentChronoScaleUnit(parentScaleUnit.getChronoUnit());
            }
        }
    }

    private void addRange(@NotNull LocalDateTime start, @NotNull LocalDateTime end) {
        if (requireNonNull(start).isBefore(chronoSeries.getBeginLocalDateTime())) {
            start = chronoSeries.getBeginLocalDateTime();
        }
        if (requireNonNull(end).isAfter(chronoSeries.getEndLocalDateTime())) {
            end = chronoSeries.getEndLocalDateTime();
        }

        Instant startEpoch = start.atZone(ZoneOffset.UTC).toInstant();
        Instant endEpoch = end.atZone(ZoneOffset.UTC).toInstant();

        boolean updatedRange = false;
        if (!timestampRanges.isEmpty()) {
            Instant[] range = timestampRanges.get(timestampRanges.size() - 1);
            if (range[1].equals(startEpoch)) {
                timestampRanges.set(timestampRanges.size() - 1, new Instant[]{range[0], endEpoch});
                updatedRange = true;
            }
        }

        if (!updatedRange) {
            timestampRanges.add(new Instant[]{startEpoch, endEpoch});
        }
    }

    /**
     * Returns validity of ChronoRange.
     *
     * @return whether or not ChronoRange is valid
     */
    public boolean isValidRange() {
        return validRange;
    }

    /**
     * Returns list of the being/end timestamps of this ChronoRange.
     *
     * @return list Instant[] (begin/end timestamp)
     */
    @NotNull
    public List<Instant[]> getTimestampRanges() {
        return timestampRanges;
    }

    /**
     * Determines whether the given timestamp is within this ChronoRange.
     *
     * @param timestamp Instant to consider
     * @return whether or not this ChronoRange includes the given timestamp
     */
    public boolean containsTime(Instant timestamp) {
        if (fullyConceptual) {
            return true;
        }

        for (Instant[] longArr : timestampRanges) {
            if ((timestamp.isAfter(longArr[0]) || timestamp.equals(longArr[0])) && (timestamp.isBefore(longArr[1]))) {
                return true;
            }
            if (timestamp.equals(longArr[1]) && patternEndLocalDateTime != null &&
                    timestamp.equals(patternEndLocalDateTime.toInstant(ZoneOffset.UTC)) && includeEndingTimestamp) {
                return true;
            }
        }
        return false;
    }

    /**
     * Earliest appearance of this ChronoRange's pattern sequence.
     *
     * @return earliest appearance of this ChronoRange's pattern sequence, if present
     */
    @NotNull
    public Optional<LocalDateTime> getPatternStartLocalDateTime() {
        if (patternStartLocalDateTime == null) {
            return Optional.empty();
        }
        return Optional.of(patternStartLocalDateTime);
    }

    /**
     * Latest appearance of this ChronoRange's pattern sequence.
     *
     * @return latest appearance of this ChronoRange's pattern sequence, if present
     */
    @NotNull
    public Optional<LocalDateTime> getPatternEndLocalDateTime() {
        if (patternEndLocalDateTime == null) {
            return Optional.empty();
        }
        return Optional.of(patternEndLocalDateTime);
    }

    /**
     * Returns Duration of this ChronoRange.
     *
     * @return Duration of ChronoRange
     */
    @NotNull
    public Duration getRangeDuration() {
        return rangeDuration;
    }

    /**
     * Returns the ChronoPattern sequence of this ChronoRange.
     *
     * @return current ChronoPattern sequence
     */
    @NotNull
    public ISeq<ChronoPattern> getChronoPatternSeq() {
        return chronoPatternSeq;
    }

    boolean isIncludeEndingTimestamp() {
        return includeEndingTimestamp;
    }

    /**
     * Determines if this ChronoRange only contains ChronoPattern(s) without temporal values.
     *
     * @return fully conceptual ChronoPattern sequence or not
     */
    public boolean isFullyConceptual() {
        return fullyConceptual;
    }

    /**
     * Determines if this ChronoRange and the given ChronoRange overlap.
     *
     * @param chronoRange other ChronoRange to consider
     * @return whether or not ChronoRanges share temporal inclusion
     */
    public boolean isSameChronoRange(@NotNull ChronoRange chronoRange) {
        if (!validRange || !requireNonNull(chronoRange).validRange) {
            return true;
        }

        ISeq<ChronoPattern> chronoPatterns = getChronoPatternSeq();
        ISeq<ChronoPattern> otherChronoPatterns = chronoRange.getChronoPatternSeq();

        //single gene compare
        if (chronoPatterns.size() == 1 && otherChronoPatterns.size() == 1) {
            ChronoPattern pattern = chronoPatterns.get(0);
            ChronoUnit unit = pattern.getChronoScaleUnit().getChronoUnit();

            ChronoPattern otherPattern = otherChronoPatterns.get(0);
            ChronoUnit otherUnit = otherPattern.getChronoScaleUnit().getChronoUnit();

            if (unit == otherUnit) {
                if (pattern.getTemporalValue().isPresent() && otherPattern.getTemporalValue().isPresent()) {
                    if (pattern.getTemporalValue().getAsInt() == otherPattern.getTemporalValue().getAsInt()) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (pattern.getTemporalValue().isPresent() || otherPattern.getTemporalValue().isPresent()) {
                    return true;
                }
            } else {
                return true;
            }
        }

        //exact gene compare
        if (chronoPatterns.equals(otherChronoPatterns)) {
            return true;
        }

        //distinct gene compare
        ISeq<ChronoUnit> units = chronoPatterns.stream()
                .map(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit())
                .distinct().collect(ISeq.toISeq());
        ISeq<ChronoUnit> otherUnits = otherChronoPatterns.stream()
                .map(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit())
                .distinct().collect(ISeq.toISeq());

        boolean hasRelatedUnits = false;
        for (ChronoUnit unit : units) {
            for (ChronoUnit otherUnit : otherUnits) {
                if (unit == otherUnit) {
                    hasRelatedUnits = true;
                    break;
                }
            }

            if (hasRelatedUnits) {
                break;
            }
        }

        if (!hasRelatedUnits) {
            return true;
        }

        //duplicate gene compare
        Map<ChronoUnit, Set<Integer>> temporalMap = new HashMap<>();
        for (int i = 0; i < chronoPatterns.size(); i++) {
            ChronoPattern pattern = chronoPatterns.get(i);
            ChronoUnit unit = pattern.getChronoScaleUnit().getChronoUnit();
            temporalMap.put(unit, new HashSet<>());

            if (pattern.getTemporalValue().isPresent()) {
                temporalMap.get(unit).add(pattern.getTemporalValue().getAsInt());
            }
        }
        for (int i = 0; i < otherChronoPatterns.size(); i++) {
            ChronoPattern otherPattern = otherChronoPatterns.get(i);
            ChronoUnit otherUnit = otherPattern.getChronoScaleUnit().getChronoUnit();

            if (otherPattern.getTemporalValue().isPresent()) {
                if (temporalMap.get(otherUnit) != null
                        && temporalMap.get(otherUnit).contains(otherPattern.getTemporalValue().getAsInt())) {
                    return true;
                }
            }
        }

        //more precise gene compare
        int z = 0;
        for (int i = 0; i < chronoPatterns.size(); i++) {
            ChronoPattern pattern = chronoPatterns.get(i);
            ChronoUnit unit = pattern.getChronoScaleUnit().getChronoUnit();

            ChronoPattern otherPattern = otherChronoPatterns.get(z++);
            ChronoUnit otherUnit = otherPattern.getChronoScaleUnit().getChronoUnit();

            if (unit == otherUnit) {
                if (pattern.getTemporalValue().isPresent() && otherPattern.getTemporalValue().isPresent()) {
                    if (pattern.getTemporalValue().getAsInt() == otherPattern.getTemporalValue().getAsInt()) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (pattern.getTemporalValue().isPresent() || otherPattern.getTemporalValue().isPresent()) {
                    return true;
                } else if (!pattern.getTemporalValue().isPresent() && !otherPattern.getTemporalValue().isPresent()) {
                    return true;
                }
            } else if (unit.compareTo(otherUnit) > 0) {
                z--;
            } else {
                i--;
            }
        }

        return false;
    }

    private boolean allMatch(@NotNull LocalDateTime itrTime) {
        //do 'all match' by unit, do 'any match' on units
        Stream<ChronoUnit> chronoUnitStream = chronoPatternSeq.stream()
                .map(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit())
                .distinct();

        AtomicBoolean allMatch = new AtomicBoolean(true);
        chronoUnitStream.forEach(
                chronoUnit -> {
                    boolean match = chronoPatternSeq.stream()
                            .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(chronoUnit))
                            .anyMatch(chronoPattern -> !chronoPattern.getTemporalValue().isPresent() ||
                                    itrTime.get(chronoPattern.getChronoScaleUnit().getChronoField()) ==
                                            chronoPattern.getTemporalValue().getAsInt());
                    if (!match) {
                        allMatch.set(false);
                    }
                }
        );
        return allMatch.get();
    }

    private boolean isMultiUnit(@NotNull ChronoUnit chronoUnit) {
        return chronoPatternSeq.stream()
                .map(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit())
                .filter(unit -> unit.equals(chronoUnit))
                .count() > 1;
    }

    private boolean anyUnitMatch(@NotNull LocalDateTime itrTime, @NotNull ChronoUnit currentChronoUnit) {
        return chronoPatternSeq.stream()
                .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(currentChronoUnit))
                .anyMatch(chronoPattern -> !chronoPattern.getTemporalValue().isPresent() ||
                        itrTime.get(chronoPattern.getChronoScaleUnit().getChronoField()) ==
                                chronoPattern.getTemporalValue().getAsInt());
    }

    private boolean pastPatternMatch(@NotNull LocalDateTime itrTime, @NotNull ChronoPattern chronoPattern) {
        return !requireNonNull(chronoPattern).getTemporalValue().isPresent() ||
                requireNonNull(itrTime).get(chronoPattern.getChronoScaleUnit().getChronoField()) >
                        chronoPattern.getTemporalValue().getAsInt();
    }

    private boolean allPastPatternMatch(@NotNull LocalDateTime itrTime, @NotNull ChronoUnit chronoUnit) {
        AtomicBoolean allPast = new AtomicBoolean(true);
        chronoPatternSeq.stream()
                .filter(pattern -> pattern.getChronoScaleUnit().getChronoUnit().equals(chronoUnit))
                .forEach(
                        chronoPattern -> {
                            if (itrTime.get(chronoPattern.getChronoScaleUnit().getChronoField()) < chronoPattern.getTemporalValue().getAsInt()) {
                                allPast.set(false);
                            }
                        }
                );
        return allPast.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChronoRange that = (ChronoRange) o;

        return chronoPatternSeq.equals(that.chronoPatternSeq);
    }

    @Override
    public int hashCode() {
        return chronoPatternSeq.hashCode();
    }

}
