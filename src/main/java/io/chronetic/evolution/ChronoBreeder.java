package io.chronetic.evolution;

import com.google.common.collect.Maps;
import com.google.common.collect.MinMaxPriorityQueue;
import io.chronetic.Chronetic;
import io.chronetic.data.evaluate.ChronoFitness;
import io.chronetic.evolution.pool.ChronoGene;
import io.chronetic.evolution.pool.Chronosome;
import io.chronetic.evolution.pool.Chronotype;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import org.jenetics.*;
import org.jenetics.internal.util.IntRef;
import org.jenetics.util.ISeq;
import org.jenetics.util.MSeq;
import org.jenetics.util.RandomRegistry;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Breeds Chronotype solutions to produce Chronotype offspring.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoBreeder extends AbstractAlterer<AnyGene<Chronotype>, ChronoFitness> {

    private final MinMaxPriorityQueue<ChronoFitness> topFitnessScore = MinMaxPriorityQueue
            .orderedBy((Comparator<ChronoFitness>) (o1, o2) -> o2.score().compareTo(o1.score()))
            .maximumSize(100).create();
    private final TreeMap<Double, ChronoFitness> topFrequencyPrecision = Maps.newTreeMap((o1, o2) -> Double.compare(o2, o1));
    private final TreeMap<Double, ChronoFitness> topPatternAccuracy = Maps.newTreeMap((o1, o2) -> Double.compare(o2, o1));
    private final TreeMap<Double, ChronoFitness> topPatternInclusion = Maps.newTreeMap((o1, o2) -> Double.compare(o2, o1));
    private final TreeMap<Duration, ChronoFitness> topTemporalInclusion = Maps.newTreeMap();
    private final Chronetic chronetic;

    public ChronoBreeder(Chronetic chronetic) {
        super(1); //always mutate
        this.chronetic = chronetic;
    }

    /**
     * alter population
     *
     * @param population
     * @param generation
     * @return
     */
    @Override
    public int alter(Population<AnyGene<Chronotype>, ChronoFitness> population, long generation) {
        final IntRef alterations = new IntRef(0);

        //record and age all Chronotypes in population
        for (int i = 0; i < population.size(); ++i) {
            final Phenotype<AnyGene<Chronotype>, ChronoFitness> pt = population.get(i);
            final Genotype<AnyGene<Chronotype>> gt = pt.getGenotype();

            //record Chronotype's fitness
            ChronoFitness chronoFitness = pt.getFitness();
            if (chronoFitness.isValidFitness()) {
                //fitness score
                if (!topFitnessScore.contains(chronoFitness)) {
                    topFitnessScore.add(chronoFitness);
                }

                //frequency precision
                ChronoFitness topFreq = topFrequencyPrecision.get(chronoFitness.getFrequencyPrecision());
                if (topFreq != null && !Double.isNaN(chronoFitness.getFrequencyPrecision())) {
                    if (topFreq.score().compareTo(chronoFitness.score()) < 0) {
                        topFrequencyPrecision.put(chronoFitness.getFrequencyPrecision(), chronoFitness);
                    }
                } else if (!Double.isNaN(chronoFitness.getFrequencyPrecision())) {
                    topFrequencyPrecision.put(chronoFitness.getFrequencyPrecision(), chronoFitness);
                }

                //pattern accuracy
                ChronoFitness topAccurate = topPatternAccuracy.get(chronoFitness.getPatternAccuracy());
                if (topAccurate != null) {
                    if (topAccurate.score().compareTo(chronoFitness.score()) < 0) {
                        topPatternAccuracy.put(chronoFitness.getPatternAccuracy(), chronoFitness);
                    }
                } else {
                    topPatternAccuracy.put(chronoFitness.getPatternAccuracy(), chronoFitness);
                }

                //pattern inclusion
                ChronoFitness topPattern = topPatternInclusion.get(chronoFitness.getPatternInclusion());
                if (topPattern != null) {
                    if (topPattern.score().compareTo(chronoFitness.score()) < 0) {
                        topPatternInclusion.put(chronoFitness.getPatternInclusion(), chronoFitness);
                    }
                } else {
                    topPatternInclusion.put(chronoFitness.getPatternInclusion(), chronoFitness);
                }

                //temporal inclusion
                ChronoFitness topInclusion = topTemporalInclusion.get(chronoFitness.getTemporalInclusion());
                if (topInclusion != null) {
                    if (topInclusion.score().compareTo(chronoFitness.score()) < 0) {
                        topTemporalInclusion.put(chronoFitness.getTemporalInclusion(), chronoFitness);
                    }
                } else {
                    topTemporalInclusion.put(chronoFitness.getTemporalInclusion(), chronoFitness);
                }
            }

            //age Chronotype
            final Genotype<AnyGene<Chronotype>> mgt = progress(gt);
            final Phenotype<AnyGene<Chronotype>, ChronoFitness> mpt = pt.newInstance(mgt, generation);
            population.set(i, mpt);
            alterations.value += 1;
        }

        //breed power solutions (80%)
        for (int i = 0; i < chronetic.getOffspringSize() - (chronetic.getOffspringSize() / 10) * 2; i++) {
            Chronotype parentA = getRandomTopChronotype();
            Chronotype parentB = getRandomTopChronotype();
            population.add(population.get(0).newInstance(
                    Genotype.of(AnyChromosome.of(() -> breed(parentA, parentB)))
            ));
        }

        //breed random solutions (20%)
        for (int i = 0; i < (chronetic.getOffspringSize() / 10) * 2; i++) {
            Chronotype parentA = population.get(RandomRegistry.getRandom().nextInt(
                    population.size())).getFitness().getChronotype();
            Chronotype parentB = population.get(RandomRegistry.getRandom().nextInt(
                    population.size())).getFitness().getChronotype();
            population.add(population.get(0).newInstance(
                    Genotype.of(AnyChromosome.of(() -> breed(parentA, parentB)))
            ));
        }

        return alterations.value;
    }

    @NotNull
    private Chronotype getRandomTopChronotype() {
        //best solution
        if (RandomRegistry.getRandom().nextBoolean()) {
            return topFitnessScore.peekFirst().getChronotype();
        }

        //random top solution
        switch (RandomRegistry.getRandom().nextInt(5)) { //todo: not hardcode
            case 0:
                return topFitnessScore.stream()
                        .limit(RandomRegistry.getRandom().nextInt(topFitnessScore.size()) + 1)
                        .findAny().orElseThrow(UnsupportedOperationException::new).getChronotype();
            case 1:
                return topFrequencyPrecision.values().stream()
                        .limit(RandomRegistry.getRandom().nextInt(topFrequencyPrecision.size()) + 1)
                        .findAny().orElseThrow(UnsupportedOperationException::new).getChronotype();
            case 2:
                return topPatternAccuracy.values().stream()
                        .limit(RandomRegistry.getRandom().nextInt(topPatternAccuracy.size()) + 1)
                        .findAny().orElseThrow(UnsupportedOperationException::new).getChronotype();
            case 3:
                return topPatternInclusion.values().stream()
                        .limit(RandomRegistry.getRandom().nextInt(topPatternInclusion.size()) + 1)
                        .findAny().orElseThrow(UnsupportedOperationException::new).getChronotype();
            case 4:
                return topTemporalInclusion.values().stream()
                        .limit(RandomRegistry.getRandom().nextInt(topTemporalInclusion.size()) + 1)
                        .findAny().orElseThrow(UnsupportedOperationException::new).getChronotype();
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * progress single chronotype
     *
     * @param genotype
     * @return
     */
    @NotNull
    private Genotype<AnyGene<Chronotype>> progress(@NotNull Genotype<AnyGene<Chronotype>> genotype) {
        Chronotype chronotype = genotype.getGene().getAllele();
        MSeq<Chronosome> chronosomes = chronotype.getChronosomes().copy();

        for (int i = 0; i < chronosomes.length(); i++) {
            Chronosome chronosome = chronosomes.get(i);
            MSeq<ChronoGene> genes = chronosome.getGenes().copy();
            for (int z = 0; z < genes.size(); z++) {
                ChronoGene gene = genes.get(z);
                genes.set(z, new ChronoGene(
                        gene.getAllele().mutate(chronotype.getChronoSeries())
                ));
            }

            Chronosome mutatedChronosome = new Chronosome(genes.toISeq(), chronotype.getChronoSeries());
            chronosomes.set(i, mutatedChronosome);
        }

        return Genotype.of(AnyChromosome.of(() -> new Chronotype(chronotype.getChronoSeries(), chronosomes.toISeq())));
    }

    /**
     * Create a single offspring Chronotype from two parent Chronotypes.
     *
     * Current breeding methods:
     * <ul>
     * <li>flat combine; all chronosomes in one new chronotype</li>
     * <li>single/multi chronosome steal (either direction)</li>
     * <li>single/multi switch genes in one Chronotype</li>
     * <li>single distinct unit steal genes in one Chronotype</li>
     * <li>single distinct unit expand genes in one Chronotype</li>
     * <li>switch ChronoFrequency for best current ChronoFrequency in one Chronotype</li>
     * </ul>
     *
     * @return offspring Chronotype breed with parent Chronotypes
     */
    @NotNull
    public Chronotype breed(@NotNull Chronotype parentA, @NotNull Chronotype parentB) {
        switch (RandomRegistry.getRandom().nextInt(6)) { //todo: not hardcode
            case 0:
                return doChronosomeCombineBreed(parentA, parentB);
            case 1:
                return doChronosomeStealBreed(parentA, parentB);
            case 2:
                return doChronoGeneStealBreed(parentA, parentB);
            case 3:
                return doDistinctSingleUnitChronoGeneStealBreed(parentA, parentB);
            case 4: {
                if (RandomRegistry.getRandom().nextBoolean()) {
                    return doChronotypeExpandBreed(parentA);
                } else {
                    return doChronotypeExpandBreed(parentB);
                }
            }
            case 5: {
                if (RandomRegistry.getRandom().nextBoolean()) {
                    return doChronotypeBestFrequencyBreed(parentA);
                } else {
                    return doChronotypeBestFrequencyBreed(parentB);
                }
            }
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Combines the Chronosomes from both Chronotypes into one Chronotype.
     *
     * @param parentA first parent
     * @param parentB second parent
     * @return offspring Chronotype
     */
    @NotNull
    public Chronotype doChronosomeCombineBreed(@NotNull Chronotype parentA, @NotNull Chronotype parentB) {
        return parentA.newInstance(ISeq.of(parentA.getChronosomes().append(parentB.getChronosomes())));
    }

    /**
     * Steals one-to-many Chronosomes from one Chronotype and gives to other.
     *
     * @param parentA first parent
     * @param parentB second parent
     * @return offspring Chronotype
     */
    @NotNull
    public Chronotype doChronosomeStealBreed(@NotNull Chronotype parentA, @NotNull Chronotype parentB) {
        Chronotype mainParent = parentA;
        if (RandomRegistry.getRandom().nextBoolean()) {
            mainParent = parentB;
        }
        return mainParent.newInstance(ISeq.of(
                mainParent.getChronosomes().append(parentB.getChronosomes().stream()
                        .limit(RandomRegistry.getRandom().nextInt(parentB.getChronosomes().size() + 1))
                        .collect(ISeq.toISeq()))));
    }

    /**
     * Steals one-to-many ChronoGenes from one Chronotype and gives to other.
     *
     * @param parentA first parent
     * @param parentB second parent
     * @return offspring Chronotype
     */
    @NotNull
    public Chronotype doChronoGeneStealBreed(@NotNull Chronotype parentA, @NotNull Chronotype parentB) {
        List<ChronoGene> stolenGenes;
        Chronotype mainParent = parentA;
        if (RandomRegistry.getRandom().nextBoolean()) {
            mainParent = parentB;

            int maxStealGeneCount = (int) parentA.getChronosomes().stream()
                    .flatMap(chronoGenes -> chronoGenes.getGenes().stream()).count();
            stolenGenes = parentA.getChronosomes().stream()
                    .flatMap(chronoGenes -> chronoGenes.getGenes().stream())
                    .limit(RandomRegistry.getRandom().nextInt(maxStealGeneCount + 1))
                    .collect(Collectors.toList());
        } else {
            int maxStealGeneCount = (int) parentB.getChronosomes().stream()
                    .flatMap(chronoGenes -> chronoGenes.getGenes().stream()).count();
            stolenGenes = parentB.getChronosomes().stream()
                    .flatMap(chronoGenes -> chronoGenes.getGenes().stream())
                    .limit(RandomRegistry.getRandom().nextInt(maxStealGeneCount + 1))
                    .collect(Collectors.toList());
        }

        Chronotype offspringChronotype = mainParent;
        while (stolenGenes.size() > 0) {
            int geneInsertCount = RandomRegistry.getRandom().nextInt(stolenGenes.size() + 1);

            if (geneInsertCount > 0) {
                MSeq<Chronosome> chronosomeSeq = offspringChronotype.getChronosomes().asMSeq();
                int alterChronosomeIndex = RandomRegistry.getRandom().nextInt(chronosomeSeq.size());
                Chronosome alterChronosome = chronosomeSeq.get(alterChronosomeIndex);
                MSeq<ChronoGene> geneSeq = alterChronosome.getGenes().asMSeq();

                for (int i = 0; i < geneInsertCount; i++) {
                    ChronoGene insertGene = stolenGenes.remove(RandomRegistry.getRandom().nextInt(stolenGenes.size()));
                    if (insertGene.getAllele() instanceof ChronoFrequency) {
                        //two ChronoFrequencies is invalid; remove one
                        geneSeq = geneSeq.stream()
                                .filter(chronoGene -> !(chronoGene.getAllele() instanceof ChronoFrequency))
                                .collect(MSeq.toMSeq());
                        geneSeq = MSeq.of(insertGene).append(geneSeq);
                    } else {
                        geneSeq = geneSeq.append(insertGene);
                    }
                }

                chronosomeSeq.set(alterChronosomeIndex, alterChronosome.newInstance(geneSeq.toISeq()));
                offspringChronotype = offspringChronotype.newInstance(chronosomeSeq.toISeq());
            }
        }

        return offspringChronotype;
    }

    /**
     * Steals all of a single distinct ChronoScaleUnit from one Chronotype and gives to other.
     *
     * @param parentA first parent
     * @param parentB second parent
     * @return offspring Chronotype
     */
    @NotNull
    public Chronotype doDistinctSingleUnitChronoGeneStealBreed(@NotNull Chronotype parentA, @NotNull Chronotype parentB) {
        Chronotype mainParent = parentA;
        Chronotype takeFromParent = parentB;
        if (RandomRegistry.getRandom().nextBoolean()) {
            mainParent = parentB;
            takeFromParent = parentA;
        }

        ChronoPattern randomPattern = parentB.getChronosomes().stream()
                .flatMap(chronoGenes -> chronoGenes.getGenes().stream())
                .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                .map(chronoGene -> (ChronoPattern) chronoGene.getAllele())
                .findAny().orElse(null);

        ChronoPattern stealPattern = null;
        if (randomPattern != null) {
            stealPattern = takeFromParent.getChronosomes().stream()
                    .flatMap(chronoGenes -> chronoGenes.getGenes().stream())
                    .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                    .map(chronoGene -> (ChronoPattern) chronoGene.getAllele())
                    .filter(chronoPattern ->
                            chronoPattern.getChronoScaleUnit().getChronoUnit() == randomPattern.getChronoScaleUnit().getChronoUnit()
                                    && chronoPattern.getTemporalValue().isPresent() && randomPattern.getTemporalValue().isPresent()
                                    && chronoPattern.getTemporalValue().getAsInt() != randomPattern.getTemporalValue().getAsInt()
                    )
                    .findAny().orElse(null);
        }

        if (stealPattern != null) {
            MSeq<Chronosome> mutateChronosomeSeq = mainParent.getChronosomes().asMSeq();
            for (int i = 0; i < mutateChronosomeSeq.size(); i++) {
                Chronosome chronosome = mutateChronosomeSeq.get(i);

                ISeq<ChronoPattern> collect = chronosome.getGenes().stream()
                        .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                        .map(chronoGene -> (ChronoPattern) chronoGene.getAllele())
                        .filter(chronoPattern -> chronoPattern == randomPattern)
                        .collect(ISeq.toISeq());
                if (!collect.isEmpty()) {
                    mutateChronosomeSeq.set(i, chronosome.newInstance(chronosome.getGenes()
                            .asMSeq().append(new ChronoGene(collect.get(0))).toISeq()));
                    return mainParent.newInstance(mutateChronosomeSeq.toISeq());
                }
            }
        }

        return mainParent;
    }

    /**
     * Expands all of the ChronoPattern(s) by the observed minimum and maximum values into a new Chronotype.
     *
     * @param chronotype Chronotype to expand
     * @return expanded Chronotype
     */
    @NotNull
    public Chronotype doChronotypeExpandBreed(@NotNull Chronotype chronotype) {
        MSeq<Chronosome> expandedChronosomes = MSeq.of(chronotype);
        for (int i = 0; i < chronotype.getChronosomes().size(); i++) {
            Chronosome chronosome = chronotype.getChronosomes().get(i);

            MSeq<ChronoGene> genes = chronosome.getGenes().asMSeq();
            for (ChronoGene gene : chronosome) {
                if (gene.getAllele() instanceof ChronoPattern) {
                    ChronoPattern pattern = (ChronoPattern) gene.getAllele();

                    for (final long temporalValue : pattern.getChronoScaleUnit().getObservedDistinctSet()) {
                        boolean hasGene = chronosome.getGenes().stream()
                                .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                                .map(chronoGene -> (ChronoPattern) chronoGene.getAllele())
                                .filter(chronoPattern -> chronoPattern.getTemporalValue().isPresent()
                                        && chronoPattern.getTemporalValue().getAsInt() == (int) temporalValue)
                                .count() > 0;
                        if (!hasGene) {
                            genes = genes.append(new ChronoGene(pattern.newInstance((int) temporalValue)));
                        }
                    }
                }
            }

            expandedChronosomes.set(i, chronosome.newInstance(genes.toISeq()));
        }
        return chronotype.newInstance(expandedChronosomes.toISeq());
    }

    /**
     * Replaces or inserts the current best observed ChronoFrequency in the Chronotype in one-to-all Chronosomes.
     *
     * @param chronotype Chronotype to expand
     * @return expanded Chronotype
     */
    @NotNull
    public Chronotype doChronotypeBestFrequencyBreed(@NotNull Chronotype chronotype) {
        MSeq<Chronosome> mutatedChronosomes = MSeq.of(chronotype);
        for (int i = 0; i < RandomRegistry.getRandom().nextInt(chronotype.getChronosomes().size() + 1); i++) {
            Chronosome chronosome = chronotype.getChronosomes().get(i);
            MSeq<ChronoGene> genes = chronosome.getGenes().stream()
                    .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                    .collect(MSeq.toMSeq());

            Chronotype bestFreqChronotype = topFrequencyPrecision.firstEntry().getValue().getChronotype();
            ChronoGene bestFreqGene = bestFreqChronotype.getChronosomes().stream()
                    .flatMap(chronoGenes -> chronoGenes.getGenes().stream())
                    .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoFrequency)
                    .findAny().orElse(null);
            if (bestFreqGene != null) {
                genes = genes.append(bestFreqGene);
            }

            mutatedChronosomes.set(i, chronosome.newInstance(genes.toISeq()));
        }
        return chronotype.newInstance(mutatedChronosomes.toISeq());
    }

}