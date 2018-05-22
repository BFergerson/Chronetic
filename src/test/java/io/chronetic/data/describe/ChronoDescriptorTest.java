package io.chronetic.data.describe;

import io.chronetic.data.ChronoSeries;
import io.chronetic.data.measure.ChronoScaleUnit;
import io.chronetic.evolution.pool.ChronoGene;
import io.chronetic.evolution.pool.Chronosome;
import io.chronetic.evolution.pool.Chronotype;
import io.chronetic.evolution.pool.allele.ChronoAllele;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import io.jenetics.util.ISeq;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ChronoDescriptorTest {

    @Test
    public void chronoDescriptorTest1() {
        ChronoSeries chronoSeries = testChronoSeries();
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 4),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, 5),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 11),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 10)
        );

        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        String description = ChronoDescriptor.describe(chronotype).humanReadable();
        assertEquals("Expected equals", "Once a year on the last Friday of November between 8AM - 10AM", description);
    }

    @Test
    public void chronoDescriptorTest2() {
        ChronoSeries chronoSeries = testChronoSeries();
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.MONTHS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, 1),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 0),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MONTHS), 0, 10)
        );

        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        String description = ChronoDescriptor.describe(chronotype).humanReadable();
        assertEquals("Expected equals", "Once a month on every Monday in September and October", description);
    }

    @Test
    public void chronoDescriptorTest3() {
        ChronoSeries chronoSeries = testChronoSeries();
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 10),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.DAYS), 0, DayOfWeek.FRIDAY.getValue()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.WEEKS), 0, 5),
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

        String description = ChronoDescriptor.describe(chronotype).humanReadable();
        assertEquals("Expected equals",
                "Once a year from 2011 to 2015 on the last Friday of November between 8AM - 10AM", description);
    }

    @Test
    public void chronoDescriptorTest4() {
        ChronoSeries chronoSeries = testChronoSeries();
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.YEARS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 8),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 9),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 10),
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

        String description = ChronoDescriptor.describe(chronotype).humanReadable();
        assertEquals("Expected equals",
                "Once a year from 2011 to 2015 on the first Friday of November between 8AM - 10AM", description);
    }

    @Test
    public void chronoDescriptorChronoFrequencySimplifyTest1() {
        ChronoSeries chronoSeries = testChronoSeries();
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.MONTHS, 0, 12, Instant.now())
        );

        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        String description = ChronoDescriptor.describe(chronotype).humanReadable();
        assertEquals("Expected equals", "Once a year", description);
    }

    @Test
    public void chronoDescriptorChronoFrequencySimplifyTest2() {
        ChronoSeries chronoSeries = testChronoSeries();
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.NANOS, 0, 1000000000, Instant.now())
        );

        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));

        String description = ChronoDescriptor.describe(chronotype).humanReadable();
        assertEquals("Expected equals", "Once a second", description);
    }


    @NotNull
    public static ChronoSeries testChronoSeries() {
        return ChronoSeries.of(false, Instant.now(), Instant.now());
    }

}
