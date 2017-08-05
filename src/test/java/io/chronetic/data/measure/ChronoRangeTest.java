package io.chronetic.data.measure;

import io.chronetic.data.ChronoSeries;
import io.chronetic.evolution.pool.ChronoGene;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import org.jenetics.util.ISeq;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Month;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;

public class ChronoRangeTest {

    @Test
    public void chronoRangeTest1() {
        ChronoSeries chronoSeries = ChronoSeries.fromFrequency(1, ChronoUnit.SECONDS,
                Instant.parse("2017-07-29T21:48:33Z"), Instant.parse("2017-07-29T22:05:12Z"));
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 4)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 17))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.getTimestampRanges().size() == 1);
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2017-07-29T22:04:17Z"),
                Instant.parse("2017-07-29T22:04:18Z"),
        }, chronoRange.getTimestampRanges().get(0));
        assertTrue(chronoRange.getRangeDuration().getSeconds() == 1);
    }

    @Test
    public void chronoRangeTest2() {
        ChronoSeries chronoSeries = ChronoSeries.fromFrequency(1, ChronoUnit.SECONDS,
                Instant.parse("2017-07-30T14:08:20Z"), Instant.parse("2017-07-30T14:24:59Z"));
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 14)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 18)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 32))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.getTimestampRanges().size() == 2);
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2017-07-30T14:14:32Z"),
                Instant.parse("2017-07-30T14:14:33Z"),
        }, chronoRange.getTimestampRanges().get(0));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2017-07-30T14:18:32Z"),
                Instant.parse("2017-07-30T14:18:33Z"),
        }, chronoRange.getTimestampRanges().get(1));
        assertTrue(chronoRange.getRangeDuration().getSeconds() == 2);
    }

    @Test
    public void chronoRangeTest3() {
        ChronoSeries chronoSeries = ChronoSeries.fromFrequency(1, ChronoUnit.SECONDS,
                Instant.parse("2017-07-30T14:08:20Z"), Instant.parse("2017-07-30T14:18:24Z"));
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 14)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 18))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.getTimestampRanges().size() == 2);
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2017-07-30T14:14:00Z"),
                Instant.parse("2017-07-30T14:15:00Z"),
        }, chronoRange.getTimestampRanges().get(0));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2017-07-30T14:18:00Z"),
                Instant.parse("2017-07-30T14:18:24Z"),
        }, chronoRange.getTimestampRanges().get(1));
        assertTrue(chronoRange.getRangeDuration().getSeconds() == (60 + 24));
    }

    @Test
    public void chronoRangeTest4() {
        ChronoSeries chronoSeries = ChronoSeries.fromFrequency(1, ChronoUnit.SECONDS,
                Instant.parse("2017-07-30T15:13:31Z"), Instant.parse("2017-07-30T15:30:10Z"));
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 19)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 29)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 30)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 20))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.getTimestampRanges().size() == 2);
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2017-07-30T15:19:20Z"),
                Instant.parse("2017-07-30T15:19:21Z"),
        }, chronoRange.getTimestampRanges().get(0));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2017-07-30T15:29:20Z"),
                Instant.parse("2017-07-30T15:29:21Z"),
        }, chronoRange.getTimestampRanges().get(1));
        assertTrue(chronoRange.getRangeDuration().getSeconds() == 2);
    }

    @Test
    public void chronoRangeTest5() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2012-11-30T09:23:16Z"),
                Instant.parse("2013-11-29T09:51:49Z"),
                Instant.parse("2014-11-28T08:43:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 11))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.getTimestampRanges().size() == 5);
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2011-12-01T00:00:00Z")
        }, chronoRange.getTimestampRanges().get(0));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2012-11-01T00:00:00Z"),
                Instant.parse("2012-12-01T00:00:00Z")
        }, chronoRange.getTimestampRanges().get(1));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2013-11-01T00:00:00Z"),
                Instant.parse("2013-12-01T00:00:00Z")
        }, chronoRange.getTimestampRanges().get(2));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2014-11-01T00:00:00Z"),
                Instant.parse("2014-12-01T00:00:00Z")
        }, chronoRange.getTimestampRanges().get(3));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2015-11-01T00:00:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        }, chronoRange.getTimestampRanges().get(4));
        assertTrue(chronoRange.isIncludeEndingTimestamp());
    }

    @Test
    public void chronoRangeTest6() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2012-11-30T09:23:16Z"),
                Instant.parse("2013-11-29T09:51:49Z"),
                Instant.parse("2014-11-28T08:43:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now())),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 10)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue())),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue())),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.getTimestampRanges().size() == 19);
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2011-11-25T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(0));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2012-11-02T08:00:00Z"),
                Instant.parse("2012-11-02T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(1));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2012-11-09T08:00:00Z"),
                Instant.parse("2012-11-09T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(2));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2012-11-16T08:00:00Z"),
                Instant.parse("2012-11-16T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(3));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2012-11-23T08:00:00Z"),
                Instant.parse("2012-11-23T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(4));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2012-11-30T08:00:00Z"),
                Instant.parse("2012-11-30T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(5));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2013-11-01T08:00:00Z"),
                Instant.parse("2013-11-01T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(6));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2013-11-08T08:00:00Z"),
                Instant.parse("2013-11-08T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(7));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2013-11-15T08:00:00Z"),
                Instant.parse("2013-11-15T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(8));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2013-11-22T08:00:00Z"),
                Instant.parse("2013-11-22T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(9));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2013-11-29T08:00:00Z"),
                Instant.parse("2013-11-29T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(10));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2014-11-07T08:00:00Z"),
                Instant.parse("2014-11-07T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(11));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2014-11-14T08:00:00Z"),
                Instant.parse("2014-11-14T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(12));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2014-11-21T08:00:00Z"),
                Instant.parse("2014-11-21T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(13));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2014-11-28T08:00:00Z"),
                Instant.parse("2014-11-28T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(14));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2015-11-06T08:00:00Z"),
                Instant.parse("2015-11-06T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(15));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2015-11-13T08:00:00Z"),
                Instant.parse("2015-11-13T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(16));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2015-11-20T08:00:00Z"),
                Instant.parse("2015-11-20T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(17));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2015-11-27T08:00:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        }, chronoRange.getTimestampRanges().get(18));
    }

    @Test
    public void chronoRangeTest7() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now())),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 10)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 1)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue())),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue())),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.getTimestampRanges().size() == 5);
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2011-11-04T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(0));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2012-11-02T08:00:00Z"),
                Instant.parse("2012-11-02T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(1));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2013-11-01T08:00:00Z"),
                Instant.parse("2013-11-01T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(2));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2014-11-07T08:00:00Z"),
                Instant.parse("2014-11-07T11:00:00Z")
        }, chronoRange.getTimestampRanges().get(3));
        assertArrayEquals("Invalid ChronoRange", new Instant[]{
                Instant.parse("2015-11-06T08:00:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        }, chronoRange.getTimestampRanges().get(4));
    }

    @Test
    public void chronoRangeCompareTest1() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 1))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest2() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 1))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest3() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 0))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest4() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 0)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 1))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 2))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertFalse(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest5() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 0)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 1))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 2))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest6() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 43))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 11))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest7() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 11))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 43))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest8() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 11)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 43))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 1))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest9() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 1))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 11)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES), 0, 43))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest10() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 10)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 11))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 11)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 10))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest11() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 10)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 11))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 11))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest12() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 11))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 10)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 11))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest13() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 0))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 0)),
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 11))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

    @Test
    public void chronoRangeCompareTest14() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );
        ISeq<ChronoGene> genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 11))
        );
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);

        genes = ISeq.of(
                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 11))
        );
        ChronoRange chronoRange2 = ChronoRange.getChronoRange(chronoSeries, genes);

        assertTrue(chronoRange.isSameChronoRange(chronoRange2));
    }

//    @Test
//    public void chronoRangeTest8() {
//        ChronoSeries chronoSeries = ChronoSeries.of(
//                Instant.parse("2011-11-25T08:48:11Z"),
//                Instant.parse("2012-11-30T09:23:16Z"),
//                Instant.parse("2013-11-29T09:51:49Z"),
//                Instant.parse("2014-11-28T08:43:00Z"),
//                Instant.parse("2015-11-27T08:22:25Z")
//        );
//        ISeq<ChronoGene> genes = ISeq.of(
//                new ChronoGene(new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now())),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8)),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9)),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 10)),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 4)),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 5)),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue())),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue())),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011)),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012)),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013)),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014)),
//                new ChronoGene(new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015))
//        );
//        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, genes);
//    }

}
