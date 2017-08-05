package io.chronetic;

import io.chronetic.data.ChronoSeries;
import io.chronetic.data.describe.ChronoDescriptor;
import io.chronetic.data.evaluate.ChronoFitness;
import io.chronetic.data.measure.ChronoScaleUnit;
import io.chronetic.evolution.ChronoBreeder;
import io.chronetic.evolution.pool.Chronotype;
import org.jenetics.*;
import org.jenetics.engine.Codec;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.engine.EvolutionStatistics;
import org.jenetics.stat.MinMax;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

/**
 * Runs the Jenetics engine to provide Chronotype solutions.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChroneticAnalyzer {

    private final static Logger logger = LoggerFactory.getLogger(Chronetic.class);

    private final Chronetic chronetic;
    private final ChronoSeries chronoSeries;

    ChroneticAnalyzer(@NotNull Chronetic chronetic, @NotNull ChronoSeries chronoSeries) {
        this.chronetic = requireNonNull(chronetic);
        this.chronoSeries = requireNonNull(chronoSeries);
    }

    /**
     * Enables the HOURS chronological unit of precision.
     */
    @NotNull
    public ChroneticAnalyzer withHourPrecision() {
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.HOURS));
        return this;
    }

    /**
     * Enables the MINUTES chronological unit of precision.
     */
    @NotNull
    public ChroneticAnalyzer withMinutePrecision() {
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MINUTES));
        return this;
    }

    /**
     * Enables the SECONDS chronological unit of precision.
     */
    @NotNull
    public ChroneticAnalyzer withSecondPrecision() {
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.SECONDS));
        return this;
    }

    /**
     * Enables the MILLIS chronological unit of precision.
     */
    @NotNull
    public ChroneticAnalyzer withMillisecondPrecision() {
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MILLIS));
        return this;
    }

    /**
     * Enables the MICROS chronological unit of precision.
     */
    @NotNull
    public ChroneticAnalyzer withMicrosecondPrecision() {
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.MICROS));
        return this;
    }

    /**
     * Enables the NANOS chronological unit of precision.
     */
    @NotNull
    public ChroneticAnalyzer withNanosecondPrecision() {
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asFactual(chronoSeries, ChronoUnit.NANOS));
        return this;
    }

    /**
     * Runs the evolution process and captures the most fit Chronotype.
     *
     * @return Chronotype with highest fitness after running evolutionary process
     */
    @NotNull
    public ChronoFitness topSolution() {
        logger.info("Chrono series duration: " + chronoSeries.getDuration());
        logger.info("Begin: " + chronoSeries.getBeginLocalDateTime());
        logger.info("End: " + chronoSeries.getEndLocalDateTime());

        final Codec<Chronotype, AnyGene<Chronotype>> CODEC = Codec.of(
                Genotype.of(AnyChromosome.of(() -> Chronotype.nextChronotype(chronoSeries))),
                gt -> gt.getGene().getAllele()
        );

        final Engine<AnyGene<Chronotype>, ChronoFitness> engine = Engine
                .builder(ChronoFitness::evaluate, CODEC)
                .populationSize(chronetic.getPopulationSize())
                .alterers(new ChronoBreeder(chronetic))

                //survive with best fitness
                .survivorsSize(chronetic.getSurvivorsSize())
                .survivorsSelector((population, count, opt) -> population.stream()
                        .sorted((o1, o2) -> o2.getFitness().score().compareTo(o1.getFitness().score()))
                        .limit(chronetic.getSurvivorsSize())
                        .collect(Population.toPopulation()))

                //offspring with best fitness
                .offspringSize(chronetic.getOffspringSize())
                .offspringSelector((population, count, opt) -> population.stream()
                        .sorted((o1, o2) -> o2.getFitness().score().compareTo(o1.getFitness().score()))
                        .limit(chronetic.getOffspringSize())
                        .collect(Population.toPopulation()))
                .build();

        final EvolutionStatistics<ChronoFitness, MinMax<ChronoFitness>> stats = EvolutionStatistics.ofComparable();
        final Phenotype<AnyGene<Chronotype>, ChronoFitness> best = engine.stream()
                .peek(result -> {
                    Population<AnyGene<Chronotype>, ChronoFitness> population = result.getPopulation();
                    for (Phenotype<AnyGene<Chronotype>, ChronoFitness> phenotype : population) {
                        if (phenotype.getFitness() != null && phenotype.getFitness().isValidFitness()) {
                            logger.trace(phenotype.getGenotype().getGene().getAllele().toString());
                        }
                    }
                    logger.info("Generation: " + result.getGeneration() + "; Population: " + result.getPopulation().size());

                    emptyPopulation();
                })
                .peek(stats)
                .limit(chronetic.getMaxGeneration())
                .collect(EvolutionResult.toBestPhenotype());

        return best.getFitness();
    }

    /**
     * Runs the evolutionary process and captures the ChronoDescriptor of the most fit Chronotype.
     *
     * @return ChronoDescriptor of the Chronotype with the highest fitness after running evolutionary process
     */
    @NotNull
    public ChronoDescriptor describe() {
        return ChronoDescriptor.describe(topSolution());
    }

    /*
   * https://github.com/jenetics/jenetics/issues/234
   */
    @SuppressWarnings("unchecked")
    private static void emptyPopulation() {
        try {
            Field field = Population.class.getDeclaredField("EMPTY");
            Object newValue = new Population(Collections.EMPTY_LIST);
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, newValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
