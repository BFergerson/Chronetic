package io.chronetic;

import org.jetbrains.annotations.NotNull;

/**
 * Allows custom configuration of the Chronetic instance.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChroneticBuilder {

    int populationSize = 5000;
    int offspringSize = 5000;
    int survivorsSize = 5000;
    int maxGeneration = 25;

    ChroneticBuilder() {
    }

    /**
     * Set custom population size.
     *
     * @param populationSize Chronotype population size
     */
    @NotNull
    public ChroneticBuilder populationSize(int populationSize) {
        this.populationSize = populationSize;
        return this;
    }

    /**
     * Set custom offspring size.
     *
     * @param offspringSize Chronotype offspring size
     */
    @NotNull
    public ChroneticBuilder offspringSize(int offspringSize) {
        this.offspringSize = offspringSize;
        return this;
    }

    /**
     * Set custom survivors size.
     *
     * @param survivorsSize Chronotype survivors size
     */
    @NotNull
    public ChroneticBuilder survivorsSize(int survivorsSize) {
        this.survivorsSize = survivorsSize;
        return this;
    }

    /**
     * Set max generation limit.
     *
     * @param maxGeneration evolution process max generation limit
     */
    @NotNull
    public ChroneticBuilder maxGeneration(int maxGeneration) {
        this.maxGeneration = maxGeneration;
        return this;
    }

    /**
     * Build custom Chronetic instance.
     *
     * @return custom built Chronetic instance
     */
    @NotNull
    public Chronetic build() {
        return new Chronetic(this);
    }

}
