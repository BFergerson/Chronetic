package io.chronetic.evolution.pool;

import io.chronetic.data.ChronoSeries;
import io.chronetic.data.measure.ChronoScaleUnit;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import org.jenetics.util.*;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import static java.util.Objects.requireNonNull;

/**
 * Represents one-to-many time pattern(s) of a ChronoSeries.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class Chronotype implements Iterable<Chronosome>, Verifiable, Serializable {

    private final ChronoSeries chronoSeries;
    private final ISeq<Chronosome> chronosomes;

    public Chronotype(@NotNull ChronoSeries chronoSeries, @NotNull ISeq<Chronosome> chronosomes) {
        this.chronoSeries = requireNonNull(chronoSeries);
        this.chronosomes = requireNonNull(chronosomes);
    }

    @NotNull
    public Chronotype newInstance(@NotNull ISeq<Chronosome> chronosomes) {
        return new Chronotype(chronoSeries, requireNonNull(chronosomes));
    }

    /**
     * Returns the ChronoSeries which the Chronotype represents.
     *
     * @return Chronotype's underlying ChronoSeries
     */
    @NotNull
    public ChronoSeries getChronoSeries() {
        return chronoSeries;
    }

    /**
     * Returns the Chronosome(s) of this Chronotype.
     *
     * @return Chronotype's underlying Chronosome(s)
     */
    @NotNull
    public ISeq<Chronosome> getChronosomes() {
        return chronosomes;
    }

    /**
     * Returns iterator over Chronosome(s) of this Chronotype.
     *
     * @return iterator over Chronotype's underlying Chronosome(s)
     */
    @NotNull
    @Override
    public Iterator<Chronosome> iterator() {
        return chronosomes.iterator();
    }

    /**
     * Chronotype is valid if:
     * <ul>
     * <li>at least one Chronosome</li>
     * <li>all Chronosomes are valid</li>
     * <li>Chronosomes don't share temporal inclusions</li>
     * </ul>
     *
     * @return Chronotype validity
     */
    @Override
    public boolean isValid() {
        if (chronosomes.isEmpty()) {
            return false;
        }

        //ensure all Chronosomes are valid and don't share chrono ranges
        for (int i = 0; i < chronosomes.size(); i++) {
            Chronosome c1 = chronosomes.get(i);
            if (!c1.isValid()) {
                return false;
            }

            for (int j = i + 1; j < chronosomes.size(); j++) {
                Chronosome c2 = chronosomes.get(j);
                if (!c2.isValid()) {
                    return false;
                } else if (c1.getChronoRange().isSameChronoRange(c2.getChronoRange())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Create new Chronotype with single Chronosome and a single ChronoGene based on the given ChronoSeries.
     *
     * @param chronoSeries time series data to create Chronotype from
     * @return Chronotype created from the given ChronoSeries
     */
    @NotNull
    public static Chronotype nextChronotype(@NotNull ChronoSeries chronoSeries) {
        final Random random = RandomRegistry.getRandom();
        MSeq<ChronoGene> geneSeq = MSeq.of();

        //seek to random point in series, grab two consecutive timestamps for frequency and pattern setup
        int startPosition = random.nextInt(requireNonNull(chronoSeries).getSize() - 1);

        LocalDateTime firstDateTime = chronoSeries.getTimestamp(startPosition++).atZone(ZoneOffset.UTC).toLocalDateTime();

        if (random.nextBoolean()) {
            //add chrono frequency gene
            LocalDateTime secondDateTime = chronoSeries.getTimestamp(startPosition++).atZone(ZoneOffset.UTC).toLocalDateTime();
            ChronoScaleUnit randomScaleUnit = chronoSeries.getChronoScale().getRandomEnabledChronoScaleUnit(random);
            long frequency = randomScaleUnit.getChronoUnit().between(firstDateTime, secondDateTime);
            if (frequency == 0) {
                //no point in a frequency of nothing
                frequency++;
            }

            ChronoFrequency chronoFrequency = new ChronoFrequency(randomScaleUnit.getChronoUnit(), startPosition,
                    frequency, frequency, secondDateTime.toInstant(ZoneOffset.UTC));
            geneSeq = geneSeq.append(new ChronoGene(chronoFrequency));
        }

        if (random.nextBoolean() || geneSeq.isEmpty()) {
            //add chrono pattern gene
            ChronoScaleUnit randomScaleUnit = chronoSeries.getChronoScale().getRandomEnabledChronoScaleUnit(random);
            ChronoPattern chronoPattern;
            if (random.nextBoolean()) {
                chronoPattern = new ChronoPattern(
                        randomScaleUnit, startPosition, 0);
            } else {
                chronoPattern = new ChronoPattern(
                        randomScaleUnit, startPosition, firstDateTime.get(randomScaleUnit.getChronoField()));
            }

            geneSeq = geneSeq.append(new ChronoGene(chronoPattern));
        }

        Chronosome chronosome = new Chronosome(geneSeq.asISeq(), chronoSeries);
        return new Chronotype(chronoSeries, Seq.of(Collections.singletonList(chronosome)).asISeq());
    }

    @NotNull
    @Override
    public String toString() {
        return "Chronotype: {\n\t" + chronosomes.toString("\n\t") + "}";
    }

}
