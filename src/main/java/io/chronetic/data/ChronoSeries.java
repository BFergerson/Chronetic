package io.chronetic.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.chronetic.data.measure.ChronoRange;
import io.chronetic.data.measure.ChronoScale;
import io.chronetic.data.measure.ChronoScaleUnit;
import io.jenetics.util.RandomRegistry;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.util.Objects.requireNonNull;

/**
 * Represents a series of timestamps produced by a single source.
 * Series must contain at least two elements.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoSeries {

    private final static Logger logger = LoggerFactory.getLogger(ChronoSeries.class);

    private final Cache<ChronoRange, Integer> cachePatternCount = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();
    private final Cache<Integer, Instant> timestampCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();
    private final Cache<String, Instant[]> multiTimestampCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    private ChronoScale chronoScale;
    private Instant beginTimestamp;
    private Instant endTimestamp;
    private int size;
    private List<Instant> seriesList;
    private InfluxDB influxDB;
    private String database;
    private String table;
    private String column;

    private ChronoSeries() {
        //suppresses default constructor
    }

    /**
     * Returns ChronoScale used by ChronoSeries.
     *
     * @return ChronoSeries' underlying ChronoScale
     */
    @NotNull
    public ChronoScale getChronoScale() {
        return chronoScale;
    }

    /**
     * Returns earliest timestamp in ChronoSeries.
     *
     * @return earliest timestamp
     */
    @NotNull
    public Instant getBeginTimestamp() {
        return beginTimestamp;
    }

    /**
     * Returns latest timestamp in ChronoSeries.
     *
     * @return latest timestamp
     */
    @NotNull
    public Instant getEndTimestamp() {
        return endTimestamp;
    }

    /**
     * Returns earliest timestamp as LocalDateTime (UTC).
     *
     * @return earliest timestamp as LocalDateTime (UTC)
     */
    @NotNull
    public LocalDateTime getBeginLocalDateTime() {
        return beginTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * Returns latest timestamp as LocalDateTime (UTC).
     *
     * @return latest timestamp as LocalDateTime (UTC)
     */
    @NotNull
    public LocalDateTime getEndLocalDateTime() {
        return endTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * Counts the distinct amount of appearances of the given
     * ChronoUnit in this ChronoSeries using the given ChronoRange.
     *
     * @param chronoRange desired ChronoRange
     * @param chronoUnit desired ChronoUnit
     * @return distinct amount of times ChronoUnit appears in ChronoSeries with given ChronoRange
     */
    public int countDistinctChronoUnitAppearance(@NotNull ChronoRange chronoRange, @NotNull ChronoUnit chronoUnit) {
        final LocalDateTime startTime = requireNonNull(chronoRange).getPatternStartLocalDateTime().orElse(getBeginLocalDateTime());
        final LocalDateTime endTime = requireNonNull(chronoRange).getPatternEndLocalDateTime().orElse(getEndLocalDateTime());

        LocalDateTime patternStartTime = startTime;
        LocalDateTime patternEndTime = endTime;
        //truncate startTime to chronoUnit (start)
        try {
            patternStartTime = patternStartTime.truncatedTo(chronoUnit);
        } catch (UnsupportedTemporalTypeException ex) {
            if (chronoUnit == ChronoUnit.YEARS) {
                //truncate to beginning of year
                patternStartTime = patternStartTime.with(firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
            } else if (chronoUnit == ChronoUnit.MONTHS) {
                //truncate to beginning of month
                patternStartTime = patternStartTime.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
            } else {
                //throw new UnsupportedOperationException();
            }
        }

        //truncate endTime to chronoUnit (end)
        try {
            patternEndTime = patternEndTime.truncatedTo(chronoUnit);
        } catch (UnsupportedTemporalTypeException ex) {
            if (chronoUnit == ChronoUnit.YEARS) {
                //truncate to beginning of year
                patternEndTime = patternEndTime.with(firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
            } else if (chronoUnit == ChronoUnit.MONTHS) {
                //truncate to beginning of month
                patternEndTime = patternEndTime.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
            } else {
                //throw new UnsupportedOperationException();
            }
        }
        patternEndTime = patternEndTime.plus(1, chronoUnit);

        return (int) chronoUnit.between(patternStartTime, patternEndTime);
    }

    /**
     * Counts the number of time events that occur during the given ChronoRange.
     *
     * @param chronoRange desired ChronoRange
     * @return amount of time events that occur during the given ChronoRange
     */
    public int countEventsBetween(@NotNull ChronoRange chronoRange) {
        Integer cacheCount = cachePatternCount.getIfPresent(requireNonNull(chronoRange));
        if (cacheCount != null) {
            return cacheCount;
        } else if (chronoRange.getTimestampRanges().isEmpty()) {
            return 0;
        } else {
            logger.debug("Counting events between: " + chronoRange);
        }

        int count = 0;
        if (seriesList != null) {
            for (Instant timestamp : seriesList) {
                if (chronoRange.containsTime(timestamp)) {
                    count++;
                }
            }
        } else {
            List<Instant[]> timestampRanges = chronoRange.getTimestampRanges();
            StringBuilder whereClause = new StringBuilder();
            boolean first = true;
            for (Instant[] timestampRange : timestampRanges) {
                if (first) {
                    whereClause = whereClause.append("(");
                    first = false;
                } else {
                    whereClause = whereClause.append("OR (");
                }

                long startTime = timestampRange[0].getEpochSecond();
                startTime *= 1000000000L; //convert to nanoseconds
                startTime += timestampRange[0].getNano();

                long endTime = timestampRange[1].getEpochSecond();
                endTime *= 1000000000L; //convert to nanoseconds
                endTime += timestampRange[1].getNano();

                whereClause = whereClause.append("time >= ").append(startTime);
                whereClause = whereClause.append(" AND ");
                whereClause = whereClause.append("time <= ").append(endTime);
                whereClause = whereClause.append(") ");
            }

            QueryResult queryResult = influxDB.query(new Query(String.format(
                    "SELECT COUNT(%s) FROM \"%s\" WHERE %s",
                    column, table, whereClause.toString()), database));

            for (QueryResult.Result result : queryResult.getResults()) {
                if (result.getSeries() != null) {
                    Double dbCount = (Double) result.getSeries().get(0).getValues().get(0).get(1);
                    count += dbCount.intValue();
                }
            }
        }

        cachePatternCount.put(chronoRange, count);
        return count;
    }

    /**
     * Returns the timestamp at the given series position.
     *
     * @param seriesPosition desired position
     * @return timestamp at given series position
     */
    @NotNull
    public Instant getTimestamp(int seriesPosition) {
        Instant cacheTimestamp = timestampCache.getIfPresent(seriesPosition);
        if (cacheTimestamp != null) {
            return cacheTimestamp;
        } else {
            logger.debug("Getting timestamp at position: " + seriesPosition);
        }

        Instant timestamp = null;
        if (seriesList != null) {
            timestamp = seriesList.get(seriesPosition);
        } else {
            QueryResult queryResult = influxDB.query(new Query(String.format(
                    "SELECT %s FROM \"%s\" LIMIT 1 OFFSET %d",
                    column, table, seriesPosition), database));

            for (QueryResult.Result result : queryResult.getResults()) {
                String timeString = (String) result.getSeries().get(0).getValues().get(0).get(0);
                timestamp = Instant.parse(timeString);
            }
        }

        if (timestamp != null) {
            timestampCache.put(seriesPosition, timestamp);
            return timestamp;
        } else {
            throw new IllegalStateException("Unable to determine timestamp at series position: " + seriesPosition);
        }
    }

    /**
     * Returns one-to-many timestamp(s) at the given series position for the specified limit.
     *
     * @param seriesPosition desired position
     * @param limit desired limit
     * @return one-to-many timestamp(s) at given series position and limit
     */
    @NotNull
    public Instant[] getTimestamps(int seriesPosition, int limit) {
        Instant[] cacheTimestamps = multiTimestampCache.getIfPresent(seriesPosition + "/" + limit);
        if (cacheTimestamps != null) {
            return cacheTimestamps;
        } else {
            logger.debug("Getting multiple timestamps at position: " + seriesPosition + "; Limit: " + limit);
        }

        Instant[] longArr = new Instant[limit];
        if (seriesList != null) {
            for (int i = 0; i < limit; i++) {
                longArr[i] = seriesList.get(seriesPosition + i);
            }
        } else {
            QueryResult queryResult = influxDB.query(new Query(String.format(
                    "SELECT %s FROM \"%s\" LIMIT %d OFFSET %d",
                    column, table, limit, seriesPosition), database));

            int i = 0;
            for (QueryResult.Result result : queryResult.getResults()) {
                for (List<Object> values : result.getSeries().get(0).getValues()) {
                    String timeString = (String) values.get(0);
                    longArr[i++] = Instant.parse(timeString);
                }
            }
        }

        multiTimestampCache.put(seriesPosition + "/" + limit, longArr);
        return longArr;
    }

    /**
     * Returns the size of this ChronoSeries.
     *
     * @return size of ChronoSeries
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the duration of this ChronoSeries.
     *
     * @return Duration of ChronoSeries
     */
    @NotNull
    public Duration getDuration() {
        LocalDateTime startDate = beginTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endDate = endTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
        return Duration.between(startDate, endDate);
    }

    /**
     * Create a ChronoSeries from the given frequency information.
     *
     * @param exactFrequency desired exact frequency
     * @param frequencyUnit frequency unit
     * @param startInstant start timestamp
     * @param endInstant end timestamp
     * @return ChronoSeries from the given frequency information
     */
    @NotNull
    public static ChronoSeries fromFrequency(long exactFrequency, @NotNull ChronoUnit frequencyUnit,
                                             @NotNull Instant startInstant, @NotNull Instant endInstant) {
        return fromFrequency(exactFrequency, exactFrequency, requireNonNull(frequencyUnit),
                requireNonNull(startInstant), requireNonNull(endInstant));
    }

    /**
     * Create a ChronoSeries from the given frequency information.
     *
     * @param minimumFrequency minimum frequency
     * @param maximumFrequency maximum frequency
     * @param frequencyUnit frequency unit
     * @param startInstant start timestamp
     * @param endInstant end stamp
     * @return ChronoSeries from the given frequency information
     */
    @NotNull
    public static ChronoSeries fromFrequency(long minimumFrequency, long maximumFrequency, @NotNull ChronoUnit frequencyUnit,
                                             @NotNull Instant startInstant, @NotNull Instant endInstant) {
        List<Instant> instants = new ArrayList<>();
        Instant itrTime = startInstant;
        while (itrTime.isBefore(endInstant) || itrTime.equals(endInstant)) {
            instants.add(itrTime);
            itrTime = itrTime.plus(RandomRegistry.getRandom().nextInt(
                    (int) (maximumFrequency - minimumFrequency) + 1) + minimumFrequency, frequencyUnit);
        }
        return of(instants.toArray(new Instant[0]));
    }

    /**
     * Create ChronoSeries from the given Dates.
     *
     * @param timestampSeries desired Dates
     * @return ChronoSeries with the given Dates
     */
    @NotNull
    public static ChronoSeries of(@NotNull Date... timestampSeries) {
        Instant[] instants = new Instant[timestampSeries.length];
        for (int i = 0; i < timestampSeries.length; i++) {
            instants[i] = requireNonNull(timestampSeries[i]).toInstant();
        }
        return of(instants);
    }

    /**
     * Create ChronoSeries from the given Instants.
     *
     * @param timestampSeries desired Instants
     * @return ChronoSeries with the given Instants
     */
    @NotNull
    public static ChronoSeries of(@NotNull Instant... timestampSeries) {
        return of(true, timestampSeries);
    }

    /**
     * Create ChronoSeries from the given Instants.
     * Allows specifying whether to disable default ChronoScaleUnits.
     *
     * @param disableScaleUnits whether to disable default ChronoScaleUnits
     * @param timestampSeries desired Instants
     * @return ChronoSeries with the given Instants
     */
    @NotNull
    public static ChronoSeries of(boolean disableScaleUnits, @NotNull Instant... timestampSeries) {
        if (timestampSeries.length < 2) {
            throw new IllegalArgumentException("ChronoSeries requires at least two elements to initiate");
        }

        ChronoSeries series = new ChronoSeries();
        series.chronoScale = new ChronoScale();

        series.seriesList = new ArrayList<>(timestampSeries.length);
        series.seriesList.addAll(Arrays.asList(timestampSeries));

        series.beginTimestamp = series.seriesList.get(0);
        series.endTimestamp = series.seriesList.get(series.seriesList.size() - 1);
        series.size = series.seriesList.size();

        //todo: calculate ChronoScale
        LocalDateTime startDate = series.beginTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endDate = series.endTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
        if (disableScaleUnits) {
            disableUnnecessaryUnits(series, startDate, endDate);
        } else {
            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.CENTURIES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DECADES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.YEARS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MONTHS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.WEEKS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DAYS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.HOURS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MINUTES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        }

        return series;
    }

    /**
     * Create ChronoSeries with the given InfluxDB information.
     *
     * @param influxDB InfluxDB instance
     * @param database database name
     * @param table table name
     * @param column column name
     * @return ChronoSeries from the given InfluxDB information
     */
    @NotNull
    public static ChronoSeries of(@NotNull InfluxDB influxDB,
                                  @NotNull String database, @NotNull String table, @NotNull String column) {
        ChronoSeries series = new ChronoSeries();
        series.chronoScale = new ChronoScale();
        series.influxDB = requireNonNull(influxDB);
        series.database = requireNonNull(database);
        series.table = requireNonNull(table);
        series.column = requireNonNull(column);
        influxDB.setDatabase(database);

        //general info
        QueryResult queryResult = series.influxDB.query(new Query(String.format(
                "SELECT COUNT(\"%s\") FROM \"%s\"",
                column, table), database));

        for (QueryResult.Result result : queryResult.getResults()) {
            series.size = ((Double) result.getSeries().get(0).getValues().get(0).get(1)).intValue();
            if (series.size < 2) {
                throw new IllegalStateException("ChronoSeries requires at least two elements to initiate");
            }
        }

        queryResult = series.influxDB.query(new Query(String.format(
                "SELECT FIRST(\"%s\") FROM \"%s\"",
                column, table), database));

        for (QueryResult.Result result : queryResult.getResults()) {
            String timeString = (String) result.getSeries().get(0).getValues().get(0).get(0);
            series.beginTimestamp = Instant.parse(timeString);
        }

        queryResult = series.influxDB.query(new Query(String.format(
                "SELECT LAST(\"%s\") FROM \"%s\"",
                column, table), database));

        for (QueryResult.Result result : queryResult.getResults()) {
            String timeString = (String) result.getSeries().get(0).getValues().get(0).get(0);
            series.endTimestamp = Instant.parse(timeString);
        }

        //todo: calculate ChronoScale
        LocalDateTime startDate = series.beginTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endDate = series.endTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime();
        disableUnnecessaryUnits(series, startDate, endDate);

        return series;
    }

    private static void disableUnnecessaryUnits(@NotNull ChronoSeries series,
                                                @NotNull LocalDateTime startDate, @NotNull LocalDateTime endDate) {
        if (ChronoUnit.NANOS.between(startDate, endDate) == 0) {
            throw new IllegalStateException("ChronoSeries contains duration of zero nanoseconds");
        } else if (ChronoUnit.MICROS.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.NANOS, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.MILLIS.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.MICROS, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.SECONDS.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.MILLIS, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.MINUTES.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.SECONDS, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.HOURS.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.MINUTES, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MINUTES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        }
        else if (ChronoUnit.DAYS.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.HALF_DAYS, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.HOURS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MINUTES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.WEEKS.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.DAYS, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DAYS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.HOURS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MINUTES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.MONTHS.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.WEEKS, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.WEEKS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DAYS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.HOURS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MINUTES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.YEARS.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.MONTHS, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MONTHS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.WEEKS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DAYS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.HOURS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MINUTES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.DECADES.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.YEARS, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.YEARS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MONTHS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.WEEKS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DAYS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.HOURS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MINUTES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.CENTURIES.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.DECADES, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DECADES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.YEARS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MONTHS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.WEEKS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DAYS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.HOURS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MINUTES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else if (ChronoUnit.MILLENNIA.between(startDate, endDate) == 0) {
            disableBiggerThan(ChronoUnit.CENTURIES, series);

            //factuals
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.CENTURIES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DECADES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.YEARS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MONTHS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.WEEKS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.DAYS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.HOURS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MINUTES));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.SECONDS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MILLIS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.MICROS));
            series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asFactual(series, ChronoUnit.NANOS));
        } else {
            throw new UnsupportedOperationException("Unable to disable");
        }
    }

    private static void disableBiggerThan(@NotNull ChronoUnit chronoUnit, @NotNull ChronoSeries series) {
        switch (requireNonNull(chronoUnit)) {
            case NANOS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.MICROS));
            case MICROS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.MILLIS));
            case MILLIS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.SECONDS));
            case SECONDS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.MINUTES));
            case MINUTES:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.HOURS));
            case HOURS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.HALF_DAYS));
            case HALF_DAYS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.DAYS));
            case DAYS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.WEEKS));
            case WEEKS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.MONTHS));
            case MONTHS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.YEARS));
            case YEARS:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.DECADES));
            case DECADES:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.CENTURIES));
            case CENTURIES:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.MILLENNIA));
            case MILLENNIA:
                series.chronoScale.updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.ERAS));
                break;
            default:
                throw new UnsupportedOperationException("Unable to disable bigger than unit: " + chronoUnit);
        }
    }

    @Override
    public String toString() {
        return String.format("ChronoSeries: { Start: %s - End: %s ; Size: %d }",
                getBeginLocalDateTime(), getEndLocalDateTime(), getSize());
    }

}
