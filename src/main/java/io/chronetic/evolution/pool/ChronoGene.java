package io.chronetic.evolution.pool;

import io.chronetic.evolution.pool.allele.ChronoAllele;
import io.jenetics.Gene;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Represent a single gene in a Chronosome.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoGene implements Gene<ChronoAllele, ChronoGene> {

    private final ChronoAllele allele;

    public ChronoGene(@NotNull ChronoAllele allele) {
        this.allele = requireNonNull(allele);
    }

    @NotNull
    @Override
    public ChronoAllele getAllele() {
        return allele;
    }

    @NotNull
    @Override
    public ChronoGene newInstance() {
        return newInstance(allele);
    }

    @NotNull
    @Override
    public ChronoGene newInstance(@NotNull ChronoAllele allele) {
        return new ChronoGene(allele);
    }

    @Override
    public boolean isValid() {
        return allele.isValid();
    }

    @NotNull
    @Override
    public String toString() {
        return "ChronoGene: {" + allele + "}";
    }

}
