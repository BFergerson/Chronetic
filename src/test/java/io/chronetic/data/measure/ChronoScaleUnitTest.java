package io.chronetic.data.measure;

import io.chronetic.Chronetic;
import io.chronetic.data.ChronoSeries;
import io.chronetic.evolution.pool.Chronotype;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;

public class ChronoScaleUnitTest {

    @Test
    public void chronoScaleUnitTest1() {
        ChronoSeries chronoSeries = ChronoSeries.of(
                Instant.parse("2017-02-28T08:48:11Z"),
                Instant.parse("2017-02-28T08:48:12Z"),
                Instant.parse("2017-02-28T08:48:13Z"),
                Instant.parse("2017-02-28T08:48:14Z"),
                Instant.parse("2017-02-28T08:48:15Z")
        );
        Chronotype chronotype = Chronetic.defaultEngine().analyze(chronoSeries)
                .withSecondPrecision().topSolution().getChronotype();
        ChronoScaleUnit scaleUnit = ChronoScaleUnit.asFactual(chronotype.getChronoSeries(), ChronoUnit.SECONDS);

        assertTrue(scaleUnit.getObservedMinimum().isPresent());
        assertTrue(scaleUnit.getObservedMaximum().isPresent());
        assertTrue(scaleUnit.getObservedMinimum().getAsLong() == 11);
        assertTrue(scaleUnit.getObservedMaximum().getAsLong() == 15);
    }

}
