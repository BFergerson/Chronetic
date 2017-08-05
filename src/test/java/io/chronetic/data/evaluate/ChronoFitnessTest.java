package io.chronetic.data.evaluate;

import io.chronetic.data.ChronoSeries;
import io.chronetic.data.measure.ChronoScaleUnit;
import io.chronetic.evolution.pool.ChronoGene;
import io.chronetic.evolution.pool.Chronosome;
import io.chronetic.evolution.pool.Chronotype;
import io.chronetic.evolution.pool.allele.ChronoAllele;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import org.jenetics.util.ISeq;
import org.junit.Test;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ChronoFitnessTest {

    @Test
    public void chronoFitnessTest1() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 11)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness = ChronoFitness.evaluate(chronotype);
        assertEquals(100.0, chronoFitness.getFrequencyPrecision(), 0.0);
        assertEquals(100.0, chronoFitness.getPatternAccuracy(), 0.0);
        assertEquals(100.0, chronoFitness.getPatternInclusion(), 0.0);
        assertEquals(1, chronoFitness.getChronosomeCount());
        assertEquals(2, chronoFitness.getChronoGeneCount());
    }

    @Test
    public void chronoFitnessExactFrequencyTest1() {
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
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness = ChronoFitness.evaluate(chronotype);
        assertTrue(chronoFitness.isValidFitness());

        assertEquals(100.0, chronoFitness.getFrequencyPrecision(), 0.0);
    }

    @Test
    public void chronoFitnessExactFrequencyTest2() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 1),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness = ChronoFitness.evaluate(chronotype);
        assertTrue(chronoFitness.isValidFitness());

        assertEquals(100.0, chronoFitness.getFrequencyPrecision(), 0.0);
    }

    @Test
    public void chronoFitnessInvalidExactFrequencyTest1() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.MONTHS, 0, 11, 12, Instant.now())
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness = ChronoFitness.evaluate(chronotype);
        assertTrue(chronoFitness.isValidFitness());

        assertNotEquals(100.0D, chronoFitness.getFrequencyPrecision(), 0.0D);
    }

    @Test
    public void chronoFitnessCompareTest1() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        //every November
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue())
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //every Friday in November
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue())
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessExactPatternAccuracyTest1() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 1),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness = ChronoFitness.evaluate(chronotype);
        assertTrue(chronoFitness.isValidFitness());

        assertEquals(100.0, chronoFitness.getPatternAccuracy(), 0.0);
    }

    @Test
    public void chronoFitnessCompareTest2() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        //every Friday in November
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue())
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //every Friday in November (2011-2015)
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest3() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        //every Friday in November (2011-2015)
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //every Friday in November between 8-10 AM (2011-2015)
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest4() throws ParseException {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        //every Friday in November between 8-10 AM (2011-2015)
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //every first Friday in November between 8-10 AM (2011-2015)
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 1),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest5() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );

        //once a second
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 1, Instant.now())
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //once a second every second
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 0)
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest6() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-04T08:48:11Z"),
                Instant.parse("2012-11-02T09:23:16Z"),
                Instant.parse("2013-11-01T09:51:49Z"),
                Instant.parse("2014-11-07T08:43:00Z"),
                Instant.parse("2015-11-06T08:22:25Z")
        );

        //once a year
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now())
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //once a year on November
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue())
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest7() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );

        //once a second on second 11
        //once a second on second 12
        //once a second on second 13
        //once a second on second 14
        //once a second on second 15
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 11),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 12),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 13),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 14),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 15)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //once a second every second
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 0)
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest8() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2012-11-30T09:23:16Z"),
                Instant.parse("2013-11-29T09:51:49Z"),
                Instant.parse("2014-11-28T08:43:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        );

        //every 11 to 12 months
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.MONTHS, 0, 11, 12, Instant.now())
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //once a year
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now())
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest9() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2012-11-30T09:23:16Z"),
                Instant.parse("2013-11-29T09:51:49Z"),
                Instant.parse("2014-11-28T08:43:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        );

        //once a year from 2011 to 2011 on Friday on November at 8am - 9am
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //once a year from 2011 to 2015 on Friday on November at 8am - 9am
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest10() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2012-11-30T09:23:16Z"),
                Instant.parse("2013-11-29T09:51:49Z"),
                Instant.parse("2014-11-28T08:43:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        );

        //once a year on Friday on November between 8AM - 9AM
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue())
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //once a year from 2011 to 2015 on Friday on November at 8am - 9am
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest11() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2012-11-30T09:23:16Z"),
                Instant.parse("2013-11-29T09:51:49Z"),
                Instant.parse("2014-11-28T08:43:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        );

        //every 1 to 2 years from 2011 to 2015 on Friday on November between 8AM - 10AM (not including 10AM)
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 2, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //once a year from 2011 to 2015 on Friday on November between 8AM - 10AM (not including 10AM)
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest12() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2011-11-25T08:48:11Z"),
                Instant.parse("2012-11-30T09:23:16Z"),
                Instant.parse("2013-11-29T09:51:49Z"),
                Instant.parse("2014-11-28T08:43:00Z"),
                Instant.parse("2015-11-27T08:22:25Z")
        );

        //every 51 to 53 weeks from 2011 to 2015 on Friday on November between 8AM - 10AM (not including 10AM)
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.WEEKS, 0, 51, 53, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //once a year from 2011 to 2015 on Friday on November between 8AM - 10AM (not including 10AM)
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, Month.NOVEMBER.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2011),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2012),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2013),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2014),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.YEARS), 0, 2015)
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

    @Test
    public void chronoFitnessCompareTest13() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:15Z"),
                Instant.parse("2017-02-28T08:48:17Z"),
                Instant.parse("2017-02-28T08:48:19Z")
        );

        //every 1 to 2 seconds every second
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 2, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 0)
        );
        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype1 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        //every 2 seconds
        alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 2, 2, Instant.now())
        );
        geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype2 = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        ChronoFitness chronoFitness1 = ChronoFitness.evaluate(chronotype1);
        ChronoFitness chronoFitness2 = ChronoFitness.evaluate(chronotype2);
        assertTrue(chronoFitness1.isValidFitness());
        assertTrue(chronoFitness2.isValidFitness());

        assertTrue(chronoFitness2.score().compareTo(chronoFitness1.score()) > 0);
    }

}
