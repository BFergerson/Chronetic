package io.chronetic.evolution.pool;

import com.google.common.collect.Sets;
import io.chronetic.data.ChronoSeries;
import io.chronetic.data.measure.ChronoRange;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Represents a single time pattern of a ChronoSeries.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class Chronosome implements Chromosome<ChronoGene> {

    private final ISeq<ChronoGene> genes;
    private final ChronoSeries chronoSeries;

    public Chronosome(@NotNull ISeq<ChronoGene> genes, @NotNull ChronoSeries chronoSeries) {
        this.genes = requireNonNull(genes);
        this.chronoSeries = requireNonNull(chronoSeries);
        if (genes.isEmpty()) {
            throw new IllegalArgumentException("No chrono genes given.");
        }
    }

    /**
     * Returns the ChronoSeries which the Chronosome represents.
     *
     * @return Chronosome's underlying ChronoSeries
     */
    @NotNull
    public ChronoSeries getChronoSeries() {
        return chronoSeries;
    }

    /**
     * Returns the ChronoGene(s) of this Chronosome.
     *
     * @return Chronosome's underlying ChronoGene(s)
     */
    @NotNull
    public ISeq<ChronoGene> getGenes() {
        return genes;
    }

    /**
     * Returns iterator over ChronoGenes(s) of this Chronosome.
     *
     * @return iterator over Chronosome's underlying ChronoGenes(s)
     */
    @NotNull
    @Override
    public Iterator<ChronoGene> iterator() {
        return genes.iterator();
    }

    /**
     * Returns the ChronoRange which the Chronosome occupies.
     *
     * @return Chronosome's occupying ChronoRange
     */
    @NotNull
    public ChronoRange getChronoRange() {
        return ChronoRange.getChronoRange(chronoSeries, genes);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Chronosome newInstance(@NotNull ISeq<ChronoGene> genes) {
        return new Chronosome(genes, chronoSeries);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public ChronoGene getGene(int index) {
        return genes.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int length() {
        return genes.length();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public ISeq<ChronoGene> toSeq() {
        return genes;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Chromosome<ChronoGene> newInstance() {
        return new Chronosome(genes, chronoSeries);
    }

    /**
     * Chronosome is valid if:
     * <ul>
     * <li>all the ChronoGenes are valid</li>
     * <li>no duplicate genes</li>
     * <li>no duplicate ChronoFrequency</li>
     * <li>ChronoPatterns don't share temporal inclusions</li>
     * </ul>
     *
     * @return Chronosome validity
     */
    @Override
    public boolean isValid() {
        //dupe genes check
        final AtomicBoolean dupeGenes = new AtomicBoolean(false);
        Stream<ChronoUnit> chronoUnits = getGenes().stream()
                .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                .map(chronoGene -> ((ChronoPattern) chronoGene.getAllele()).getChronoScaleUnit().getChronoUnit())
                .distinct();
        chronoUnits.forEach(
                chronoUnit -> {
                    ISeq<ChronoPattern> unitPatterns = getGenes().stream()
                            .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                            .map(chronoGene -> (ChronoPattern) chronoGene.getAllele())
                            .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(chronoUnit))
                            .collect(ISeq.toISeq());
                    if (unitPatterns.size() > 1) {
                        Set<Integer> temporalInclusionSet = Sets.newHashSet();
                        unitPatterns.forEach(
                                chronoPattern -> {
                                    if (!chronoPattern.getTemporalValue().isPresent()) {
                                        dupeGenes.set(true);
                                    } else {
                                        int patternValue = chronoPattern.getTemporalValue().getAsInt();
                                        if (temporalInclusionSet.contains(patternValue)) {
                                            dupeGenes.set(true);
                                        } else {
                                            temporalInclusionSet.add(patternValue);
                                        }
                                    }
                                }
                        );
                    }
                }
        );
        if (dupeGenes.get()) {
            return false;
        }

        boolean hasChronoFrequency = false;
        for (ChronoGene gene : genes) {
            if (!gene.isValid()) {
                return false;
            }

            if (gene.getAllele() instanceof ChronoFrequency) {
                if (hasChronoFrequency) {
                    //multiple chrono frequencies aren't allowed
                    return false;
                }
                hasChronoFrequency = true;
            }
        }
        return true;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("Chronosome: {%s} - Range duration: %s",
                genes.toString("\n\t\t"), getChronoRange().getRangeDuration());
    }

}
