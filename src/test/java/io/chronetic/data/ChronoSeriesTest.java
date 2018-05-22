package io.chronetic.data;

import io.chronetic.data.measure.ChronoRange;
import io.chronetic.data.measure.ChronoScaleUnit;
import io.chronetic.evolution.pool.ChronoGene;
import io.chronetic.evolution.pool.allele.ChronoAllele;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import io.jenetics.util.ISeq;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Month;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertTrue;

public class ChronoSeriesTest {

    @Test
    public void chronoSeriesTest1() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2012-11-30T09:23:16Z"),
                Instant.parse("2013-11-29T09:51:49Z"),
                Instant.parse("2014-11-28T08:43:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        );

        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 10),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, geneSeq);

        int actualCount = chronoSeries.countEventsBetween(chronoRange);
        assertTrue(actualCount == 5);
    }

    @Test
    public void chronoSeriesTest2() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );

        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 1, Instant.now())
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        ChronoRange chronoRange = ChronoRange.getChronoRange(chronoSeries, geneSeq);

        int actualCount = chronoSeries.countEventsBetween(chronoRange);
        assertTrue(actualCount == 5);
    }

}
