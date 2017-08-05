package io.chronetic.evolution.pool.allele;

import io.chronetic.data.ChronoSeries;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an allele type of the ChronoGene. Current types include:
 * <ul>
 * <li>ChronoFrequency</li>
 * <li>ChronoPattern</li>
 * </ul>
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public abstract class ChronoAllele {

    final int seriesPosition;

    ChronoAllele(int seriesPosition) {
        this.seriesPosition = seriesPosition;
    }

    @NotNull
    public abstract ChronoAllele mutate(@NotNull ChronoSeries chronoSeries);

    public abstract boolean isValid();

}
