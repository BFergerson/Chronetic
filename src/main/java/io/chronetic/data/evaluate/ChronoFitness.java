package io.chronetic.data.evaluate;

import io.chronetic.data.ChronoSeries;
import io.chronetic.data.measure.ChronoRange;
import io.chronetic.evolution.pool.ChronoGene;
import io.chronetic.evolution.pool.Chronosome;
import io.chronetic.evolution.pool.Chronotype;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import io.jenetics.util.ISeq;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * I'm not sure how to explain what I did here. I thought I understood math at one point.
 * If you understand how multi-objective optimization works I would greately appreciate the help in cleaning this up.
 *
 * My general approach was to add to the fitness for qualities that were desired and subtracting on qualities that
 * were not desired. This comes to something of a weighted sum model for multi-objective optimization. Probably the worse
 * method to use. Again, math isn't my strong suite and it was pointed out pretty heavily as I made this.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoFitness implements Comparable<ChronoFitness> {

    /**
     * Evaluates Chronotype
     *
     * @param chronotype
     * @return evaluated Chronotype
     */
    @NotNull
    public static ChronoFitness evaluate(@NotNull final Chronotype chronotype) {
        if (!requireNonNull(chronotype).isValid()) {
            return new ChronoFitness(chronotype);
        }

        //calculate fitness of each Chronosome individually
        ISeq<ChronoFitness> fitnessSeq = chronotype.getChronosomes().stream()
                .map(chronoGenes -> evaluate(chronotype, chronoGenes))
                .collect(ISeq.toISeq());

        int chronosomes = chronotype.getChronosomes().size();
        double frequencyPrecision = fitnessSeq.stream()
                .mapToDouble(ChronoFitness::getFrequencyPrecision).average().orElse(0);
        double patternAccuracy = fitnessSeq.stream()
                .mapToDouble(ChronoFitness::getPatternAccuracy).average().orElse(0);
        double patternInclusion = fitnessSeq.stream()
                .mapToDouble(ChronoFitness::getPatternInclusion).sum();
        Duration temporalInclusion = fitnessSeq.stream()
                .map(ChronoFitness::getTemporalInclusion)
                .reduce(Duration::plus).orElse(Duration.ZERO);

        int fitnessMultiplier = 1;
        if (frequencyPrecision == 100.00D) {
            fitnessMultiplier += 1000;
        }
        if (patternInclusion == 100.00D) {
            fitnessMultiplier += 1000;
        }

        BigDecimal chronosomeScoreSum = fitnessSeq.stream().map(chronoFitness -> chronoFitness.score)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        return new ChronoFitness(chronotype, frequencyPrecision, patternAccuracy, patternInclusion, temporalInclusion,
                BigDecimal.valueOf(Math.pow(fitnessMultiplier, 9)).add(BigDecimal.valueOf(Math.pow(patternInclusion, 7))
                        .subtract(BigDecimal.valueOf(Math.pow(temporalInclusion.getSeconds(), 5)))
                        .subtract(BigDecimal.valueOf(Math.pow(temporalInclusion.getSeconds(), 4)))
                        .add(chronosomeScoreSum)));
    }

    /**
     * Evaluates Chronosome
     *
     * @param chronosome
     * @return evaluated Chronosome
     */
    @NotNull
    private static ChronoFitness evaluate(@NotNull final Chronotype chronotype, @NotNull final Chronosome chronosome) {
        if (!requireNonNull(chronosome).isValid()) {
            return new ChronoFitness(chronotype);
        }

        int patternCount = (int) chronotype.getChronosomes().stream()
                .flatMap(chronoGenes -> chronoGenes.getGenes().stream())
                .map(ChronoGene::getAllele)
                .filter(chronoAllele -> chronoAllele instanceof ChronoPattern)
                .count();
        int distinctPatternCount = (int) chronotype.getChronosomes().stream()
                .flatMap(chronoGenes -> chronoGenes.getGenes().stream())
                .map(ChronoGene::getAllele)
                .filter(chronoAllele -> chronoAllele instanceof ChronoPattern)
                .map(chronoAllele -> ((ChronoPattern) chronoAllele).getChronoScaleUnit().getChronoUnit())
                .distinct().count();

        ChronoSeries chronoSeries = requireNonNull(chronosome).getChronoSeries();
        ChronoRange chronoRange = chronosome.getChronoRange();
        Optional<ChronoFrequency> chronoFrequency = chronosome.getGenes().stream()
                .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoFrequency)
                .map(chronoGene -> (ChronoFrequency) chronoGene.getAllele())
                .findAny();
        double frequencyPrecision = Double.NaN;
        if (chronoFrequency.isPresent()) {
            frequencyPrecision = calculateFrequencyPrecision(chronoFrequency.get(), chronoSeries, chronoRange);
        }

        double patternAccuracy = calculatePatternAccuracy(chronoSeries, chronoRange);
        double patternInclusion = calculatePatternInclusion(chronoSeries, chronoRange);

        int fitnessMultiplier = 1;
        if (frequencyPrecision == 100.00D) {
            fitnessMultiplier += 100000;
        }
        if (patternAccuracy == 100.00D) {
            fitnessMultiplier += 100000;
        }
        if (patternInclusion == 100.00D) {
            fitnessMultiplier += 100000;
        }

        if (Double.isNaN(frequencyPrecision)) {
            BigDecimal chronosomeScore = BigDecimal.valueOf(fitnessMultiplier).multiply(BigDecimal.valueOf(Math.pow(patternAccuracy, 6))).add(
                    BigDecimal.valueOf(patternInclusion))
                    .subtract(BigDecimal.valueOf(patternCount))
                    .add(BigDecimal.valueOf(Math.pow(distinctPatternCount, 8)));
            return new ChronoFitness(chronotype, frequencyPrecision, patternAccuracy, patternInclusion,
                    chronoRange.getRangeDuration(), chronosomeScore);
        } else {
            BigDecimal chronosomeScore = BigDecimal.valueOf(fitnessMultiplier).multiply(BigDecimal.valueOf(Math.pow(patternAccuracy, 6)))
                    .add(new BigDecimal(Math.pow(frequencyPrecision, 9)))
                    .add(BigDecimal.valueOf(patternInclusion))
                    .subtract(BigDecimal.valueOf(patternCount))
                    .add(BigDecimal.valueOf(Math.pow(distinctPatternCount, 8)))
                    .subtract(BigDecimal.valueOf(Math.pow(chronoFrequency.get().getMaximumFrequency(), 6)));
            return new ChronoFitness(chronotype, frequencyPrecision, patternAccuracy, patternInclusion,
                    chronoRange.getRangeDuration(), chronosomeScore);
        }
    }

    private final Chronotype chronotype;
    private final boolean validFitness;
    private final int chronosomeCount;
    private final int chronoGeneCount;
    private final double frequencyPrecision;
    private final double patternAccuracy;
    private final double patternInclusion;
    private final Duration temporalInclusion;
    private final BigDecimal score;

    private ChronoFitness(@NotNull Chronotype chronotype) {
        this.chronotype = requireNonNull(chronotype);
        this.validFitness = false;
        this.chronosomeCount = 0;
        this.chronoGeneCount = 0;
        this.frequencyPrecision = Double.NaN;
        this.patternAccuracy = Double.NaN;
        this.patternInclusion = Double.NaN;
        this.temporalInclusion = Duration.ZERO;
        this.score = BigDecimal.valueOf(Double.MIN_VALUE);
    }

    public ChronoFitness(@NotNull Chronotype chronotype,
                         double frequencyPrecision, double patternAccuracy,
                         double patternInclusion, @NotNull Duration temporalInclusion, BigDecimal score) {
        this.chronotype= requireNonNull(chronotype);
        this.validFitness = true;
        this.chronosomeCount = chronotype.getChronosomes().size();
        this.chronoGeneCount = (int) chronotype.getChronosomes().stream()
                .flatMap(chronoGenes -> chronoGenes.getGenes().stream()).count();
        this.frequencyPrecision = frequencyPrecision;
        this.patternAccuracy = patternAccuracy;
        this.patternInclusion = patternInclusion;
        this.temporalInclusion = temporalInclusion;
        this.score = requireNonNull(score);
    }

    @NotNull
    public Chronotype getChronotype() {
        return chronotype;
    }

    public boolean isValidFitness() {
        return validFitness;
    }

    public int getChronosomeCount() {
        return chronosomeCount;
    }

    public int getChronoGeneCount() {
        return chronoGeneCount;
    }

    public double getFrequencyPrecision() {
        return frequencyPrecision;
    }

    public double getPatternAccuracy() {
        return patternAccuracy;
    }

    public double getPatternInclusion() {
        return patternInclusion;
    }

    @NotNull
    public Duration getTemporalInclusion() {
        return temporalInclusion;
    }

    public BigDecimal score() {
        return score;
    }

    private static double calculatePatternAccuracy(@NotNull ChronoSeries chronoSeries, @NotNull ChronoRange chronoRange) {
        if (!chronoRange.isValidRange()) {
            return 0;
        }

        double actualCount = chronoSeries.countEventsBetween(chronoRange);
        double predictedCountReal = chronoSeries.countEventsBetween(chronoRange);
        if (actualCount == 0.0 && predictedCountReal == 0.0) {
            return 100.0;
        }

        double patternAccuracy;
        if (actualCount > predictedCountReal) {
            patternAccuracy = (predictedCountReal / actualCount) * 100.00D;
        } else {
            patternAccuracy = (actualCount / predictedCountReal) * 100.00D;
        }

        return patternAccuracy;
    }

    private static double calculatePatternInclusion(@NotNull ChronoSeries chronoSeries, @NotNull ChronoRange chronoRange) {
        if (!chronoRange.isValidRange()) {
            return 100.0; //not valid = includes everything
        }

        double actualCount = chronoSeries.getSize();
        double predictedCountReal = chronoSeries.countEventsBetween(chronoRange);
        if (actualCount == 0.0 && predictedCountReal == 0.0) {
            return 100.0;
        }

        double patternInclusion;
        if (actualCount > predictedCountReal) {
            patternInclusion = (predictedCountReal / actualCount) * 100.00D;
        } else {
            patternInclusion = (actualCount / predictedCountReal) * 100.00D;
        }

        return patternInclusion;
    }

    private static double calculateFrequencyPrecision(@NotNull ChronoFrequency chronoFrequency,
                                                      @NotNull ChronoSeries chronoSeries,
                                                      @NotNull ChronoRange chronoRange) {
        double actualCount = chronoSeries.countEventsBetween(chronoRange);
        double predictedCountReal = chronoSeries.countDistinctChronoUnitAppearance(
                chronoRange, chronoFrequency.getChronoUnit());
        if (actualCount == 0.0 && predictedCountReal == 0.0) {
            return 100.0;
        }

        double minPredictedCount = predictedCountReal / chronoFrequency.getMaximumFrequency();
        double maxPredictedCount = predictedCountReal / chronoFrequency.getMinimumFrequency();

        double minimumFrequencyPrecision;
        if (actualCount > minPredictedCount) {
            minimumFrequencyPrecision = (minPredictedCount / actualCount) * 100.00D;
        } else {
            minimumFrequencyPrecision = (actualCount / minPredictedCount) * 100.00D;
        }

        double maximumFrequencyPrecision;
        if (actualCount > maxPredictedCount) {
            maximumFrequencyPrecision = (maxPredictedCount / actualCount) * 100.00D;
        } else {
            maximumFrequencyPrecision = (actualCount / maxPredictedCount) * 100.00D;
        }

        return (minimumFrequencyPrecision + maximumFrequencyPrecision) / 2.0D;
    }

    @Override
    public int compareTo(@NotNull ChronoFitness o) {
        return score.compareTo(requireNonNull(o).score);
    }

    @Override
    public String toString() {
        return String.format("ChronoFitness: { Score: %s - Cc: %s; Fp: %s; Pa: %s; Pi %s; Ti: %s; Valid: %s}",
                score, chronosomeCount, frequencyPrecision, patternAccuracy, patternInclusion, temporalInclusion, validFitness);
    }

}
