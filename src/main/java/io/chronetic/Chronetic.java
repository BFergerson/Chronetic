package io.chronetic;

import io.chronetic.data.ChronoSeries;
import io.chronetic.data.measure.ChronoScaleUnit;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

import static java.util.Objects.requireNonNull;

/**
 * Main entry point of Chronetic. Provides static methods for building Chronetic.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class Chronetic {

    private final ChroneticBuilder builder;

    Chronetic(@NotNull ChroneticBuilder builder) {
        this.builder = builder;
    }

    /**
     * Create custom configured Chronetic instance.
     *
     * @return ChroneticBuilder
     */
    @NotNull
    public static ChroneticBuilder configure() {
        return new ChroneticBuilder();
    }

    /**
     * Create default configured Chronetic instance.
     * Default configuration:
     * <ul>
     * <li>population size = 5000</li>
     * <li>offspring size = 5000</li>
     * <li>survivors size = 5000</li>
     * <li>max generation = 25</li>
     * </ul>
     *
     * @return Default configured Chronetic instance
     */
    @NotNull
    public static Chronetic defaultEngine() {
        return new Chronetic(new ChroneticBuilder());
    }

    /**
     * Analyzes the given ChronoSeries dataset.
     *
     * @param chronoSeries dataset to analyze
     * @return ChroneticAnalyzer
     */
    @NotNull
    public ChroneticAnalyzer analyze(@NotNull ChronoSeries chronoSeries) {
        ChroneticAnalyzer analyzer = new ChroneticAnalyzer(this, requireNonNull(chronoSeries));

        //disable nanos/micros/millis/seconds/minutes/hours; have to be deliberately enabled
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.NANOS));
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.MICROS));
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.MILLIS));
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.SECONDS));
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.MINUTES));
        chronoSeries.getChronoScale().updateChronoScaleUnit(ChronoScaleUnit.asDisabled(ChronoUnit.HOURS));

        return analyzer;
    }

    public int getPopulationSize() {
        return builder.populationSize;
    }

    public int getOffspringSize() {
        return builder.offspringSize;
    }

    public int getSurvivorsSize() {
        return builder.survivorsSize;
    }

    public int getMaxGeneration() {
        return builder.maxGeneration;
    }

}
