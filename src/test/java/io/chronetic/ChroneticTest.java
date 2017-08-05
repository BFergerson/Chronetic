package io.chronetic;

import com.google.common.collect.Sets;
import io.chronetic.data.ChronoSeries;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.junit.Assert.*;

public class ChroneticTest {

    private final static Logger logger = LoggerFactory.getLogger(ChroneticTest.class);

    @Test
    public void chroneticTestStartOfWeekTest1() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        String description = Chronetic.defaultEngine()
                .analyze(chronoSeries).withHourPrecision()
                .describe().humanReadable();
        logger.info("Result description: " + description);

        assertTrue(Sets.newHashSet(
                "Once a year from 2011 to 2015 on the first Friday of November between 8AM - 10AM",
                "Once a year on the first Friday of November between 8AM - 10AM",
                "Once a year from 2011 to 2015 on Friday on November between 8AM - 10AM",
                "Once a year on Friday on November between 8AM - 10AM"
        ).contains(description));
    }

    @Test
    public void chroneticTest2() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );

        String description = Chronetic.defaultEngine().analyze(chronoSeries)
                .withSecondPrecision()
                .describe().humanReadable();
        logger.info("Result description: " + description);

        assertTrue(Sets.newHashSet(
                "Expected equals", "Once a second every second",
                "Once a second between seconds 11 and 15"
        ).contains(description));
    }

//    @Test
//    public void influxDBEverySecondTest() {
//        logger.info("Connecting to InfluxDB...");
//
//        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
//        httpBuilder.readTimeout(0, TimeUnit.SECONDS); //no read timeout
//
//        InfluxDB influxDB = InfluxDBFactory.connect(
//                "http://192.168.99.100:32773", "root", "root", httpBuilder).enableGzip();
//
//        String dbName = "influx_test" + System.currentTimeMillis();
//        influxDB.createDatabase(dbName);
//        logger.info("Successfully connected to InfluxDB");
//
//        BatchPoints batchPoints = BatchPoints
//                .database(dbName)
//                .tag("async", "true")
//                .retentionPolicy("autogen")
//                .consistency(InfluxDB.ConsistencyLevel.ALL)
//                .build();
//
//        logger.info("Batch inserting data into InfluxDB...");
//
//        long counter = 0;
//        long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());;
//        long startBatch = System.currentTimeMillis();
//        for (int i = 0; i < 1000; i++){
//            Point point1 = Point.measurement("cpu")
//                    .time(time++, TimeUnit.SECONDS)
//                    .addField("idle", counter++)
//                    .build();
//
//            batchPoints.point(point1);
//        }
//        influxDB.write(batchPoints);
//
//        logger.info(String.format("Finished batch inserting data into InfluxDB. Time elapsed: %dms",
//                System.currentTimeMillis() - startBatch));
//
//        ChronoSeries chronoSeries = ChronoSeries.of(influxDB, dbName, "cpu", "idle");
//        String description = Chronetic.analyze(chronoSeries)
//                .withSecondPrecision()
//                .describe().humanReadable();
//
//        influxDB.deleteDatabase(dbName);
//        assertEquals("Expected equals",
//                "Once a second every second",
//                description);
//    }

}
