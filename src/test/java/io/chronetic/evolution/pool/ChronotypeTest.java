package io.chronetic.evolution.pool;

import io.chronetic.data.ChronoSeries;
import io.chronetic.data.measure.ChronoScaleUnit;
import io.chronetic.evolution.pool.allele.ChronoAllele;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import io.jenetics.util.ISeq;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChronotypeTest {

    @Test
    public void invalidChronotype1() {
        ChronoSeries chronoSeries = testChronoSeries();
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 49),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 0)
        );

        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(Collections.singleton(chronosome)));
        assertFalse(chronotype.isValid());
    }

    @Test
    public void invalidChronotype2() {
        ChronoSeries chronoSeries = testChronoSeries();
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 1, Instant.now())
        );

        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);
        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(chronosome, chronosome));
        assertFalse(chronotype.isValid());
    }

    @Test
    public void validChronotypeTest1() {
        ChronoSeries chronoSeries = testChronoSeries();
        ISeq<ChronoAllele> alleleSeq = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 0),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 1)
        );
        ISeq<ChronoAllele> alleleSeq2 = ISeq.of(
                new ChronoFrequency(ChronoUnit.SECONDS, 0, 1, 1, Instant.now()),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS), 0, 0),
                new ChronoPattern(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS), 0, 2)
        );

        ISeq<ChronoGene> geneSeq = ISeq.of(alleleSeq.map(ChronoGene::new));
        Chronosome chronosome = new Chronosome(geneSeq, chronoSeries);

        geneSeq = ISeq.of(alleleSeq2.map(ChronoGene::new));
        Chronosome chronosome2 = new Chronosome(geneSeq, chronoSeries);

        Chronotype chronotype = new Chronotype(chronoSeries, ISeq.of(chronosome, chronosome2));
        assertTrue(chronotype.isValid());
    }

    @NotNull
    public static ChronoSeries testChronoSeries() {
        return ChronoSeries.of(false, Instant.now(), Instant.now());
    }

}
