package io.chronetic.data.describe;

import io.chronetic.data.ChronoSeries;
import io.chronetic.data.evaluate.ChronoFitness;
import io.chronetic.data.measure.ChronoScale;
import io.chronetic.data.measure.ChronoScaleUnit;
import io.chronetic.evolution.pool.ChronoGene;
import io.chronetic.evolution.pool.Chronosome;
import io.chronetic.evolution.pool.Chronotype;
import io.chronetic.evolution.pool.allele.ChronoFrequency;
import io.chronetic.evolution.pool.allele.ChronoPattern;
import io.jenetics.util.ISeq;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Used to describe Chronotype solutions in a human-readable format.
 *
 * @version 1.0
 * @since 1.0
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
public class ChronoDescriptor {

    /**
     * Create a ChronoDescriptor with the given Chronotype.
     *
     * @param chronotype Chronotype to describe
     * @return ChronoDescriptor for the given Chronotype
     */
    @NotNull
    public static ChronoDescriptor describe(@NotNull Chronotype chronotype) {
        ChronoDescriptor chronoDescriptor = new ChronoDescriptor(requireNonNull(chronotype).getChronoSeries());
        chronoDescriptor.humanReadable = chronoDescriptor.describe(chronotype, null);
        return chronoDescriptor;
    }

    /**
     * Create a ChronoDescriptor with the given ChronoFitness.
     *
     * @param chronoFitness ChronoFitness to describe
     * @return ChronoDescriptor for the given ChronoFitness
     */
    @NotNull
    public static ChronoDescriptor describe(@NotNull ChronoFitness chronoFitness) {
        ChronoDescriptor chronoDescriptor = new ChronoDescriptor(requireNonNull(chronoFitness).getChronotype().getChronoSeries());
        chronoDescriptor.humanReadable = chronoDescriptor.describe(chronoFitness.getChronotype(), chronoFitness);
        return chronoDescriptor;
    }

    private final ChronoSeries chronoSeries;
    private String humanReadable;

    private ChronoDescriptor(ChronoSeries chronoSeries) {
        this.chronoSeries = chronoSeries;
    }

    /**
     * Returns a human-readable description of the given ChronoSeries.
     *
     * @return human-readable description
     */
    @NotNull
    public String humanReadable() {
        return humanReadable;
    }

    @NotNull
    private String describe(@NotNull Chronotype chronotype, ChronoFitness chronoFitness) {
        final StringBuilder sb = new StringBuilder();

        //extract multi patterns (multiple instances of same chrono unit)

        //extract singular patterns (single instance of a chrono unit)
        chronotype.getChronosomes().stream()
                .forEach(chronosome -> {
                    if (!sb.toString().isEmpty()) {
                        sb.append("& ");
                    }

                    //describe frequency
                    Optional<ChronoFrequency> chronoFrequency = chronosome.getGenes().stream()
                            .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoFrequency)
                            .map(chronoGene -> (ChronoFrequency) chronoGene.getAllele())
                            .findAny();
                    chronoFrequency.ifPresent(chronoFrequency1 -> {
                        sb.append(describeChronoFrequency(chronotype.getChronoSeries(), chronoFrequency1)).append(" ");
                    });

                    //to describe pattern
                    List<ChronoPattern> chronoPatternSeq = chronosome.getGenes().stream()
                            .map(ChronoGene::getAllele)
                            .filter(chronoAllele -> chronoAllele instanceof ChronoPattern)
                            .map(chronoAllele -> (ChronoPattern) chronoAllele)
                            .collect(Collectors.toList());
                    if (hasYears(chronosome)) {
                        sb.append(doYears(chronoPatternSeq)).append(" ");
                    }
                    if (hasMonthsAndWeeksAndDays(chronosome)) {
                        sb.append(doMonthsAndWeeksAndDays(chronoPatternSeq));
                    } else if (hasWeeksAndDays(chronosome)) {
                        sb.append(doWeeksAndDays(chronoPatternSeq));
                    }


                    Stream<ChronoUnit> chronoUnitStream = chronoPatternSeq.stream()
                            .map(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit())
                            .distinct();
                    chronoUnitStream.forEach(
                            chronoUnit -> {
                                ISeq<ChronoPattern> alleleSeq = chronoPatternSeq.stream()
                                        .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(chronoUnit))
                                        .collect(ISeq.toISeq());
                                if (alleleSeq.size() == 1) {
                                    sb.append(describeSingleChronoPattern(alleleSeq.get(0)));
                                    sb.append(" ");
                                }
                            }
                    );

                    chronoUnitStream = chronoPatternSeq.stream()
                            .map(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit())
                            .distinct();
                    chronoUnitStream.forEach(
                            chronoUnit -> {
                                ISeq<ChronoPattern> patternSeq = chronoPatternSeq.stream()
                                        .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(chronoUnit))
                                        .collect(ISeq.toISeq());
                                if (patternSeq.size() > 1) {
                                    sb.append(describeMultiChronoPattern(patternSeq)).append(" ");
                                }
                            }
                    );
                });

        return sb.toString().trim();
    }

    @NotNull
    private String describeMultiChronoPattern(@NotNull ISeq<ChronoPattern> chronoPatternSeq) {
        chronoPatternSeq = requireNonNull(chronoPatternSeq).stream()
                .sorted(Comparator.comparingInt(o -> o.getTemporalValue().getAsInt()))
                .collect(ISeq.toISeq());

        int startValue = chronoPatternSeq.get(0).getTemporalValue().getAsInt();
        int endValue = chronoPatternSeq.get(chronoPatternSeq.size() - 1).getTemporalValue().getAsInt();

        ChronoScaleUnit scaleUnit = chronoPatternSeq.get(0).getChronoScaleUnit();
        Optional<ChronoScaleUnit> childScaleUnit = chronoSeries.getChronoScale().getChildScaleUnit(scaleUnit.getChronoUnit());
        if (childScaleUnit.isPresent() && childScaleUnit.get().isDisabled()) {
            endValue++;
        }

        switch (chronoPatternSeq.get(0).getChronoScaleUnit().getChronoUnit()) {
            case SECONDS:
                return "between seconds " + startValue + " and " + endValue;
            case MINUTES:
                return "between minutes " + startValue + " and " + endValue;
            case HOURS:
                return "between " + startValue + "AM - " + endValue + "AM";
            case WEEKS:
                return "between the " + doWeek(startValue) + " and the " + doWeek(endValue);
            case MONTHS:
                return "between " + doMonth(startValue) + " and " + doMonth(endValue);
            case YEARS:
                return "from " + startValue + " to " + endValue;
        }
        return null;
    }

    @NotNull
    private String describeSingleChronoPattern(@NotNull ChronoPattern chronoPattern) {
        if (!requireNonNull(chronoPattern).getTemporalValue().isPresent()) {
            return everyChronoUnit(chronoPattern.getChronoScaleUnit().getChronoUnit());
        }

        switch (chronoPattern.getChronoScaleUnit().getChronoUnit()) {
            case SECONDS:
                return doOnSecond(chronoPattern);
            case MINUTES:
                return doOnMinute(chronoPattern);
            case HOURS:
                return "at " + chronoPattern.getTemporalValue().getAsInt() + "am";
            case DAYS:
                return "on " + DayOfWeek.of(chronoPattern.getTemporalValue().getAsInt()).getDisplayName(TextStyle.FULL, Locale.US);
            case WEEKS:
                return doOnTheWeek(chronoPattern);
            case MONTHS:
                return doOnMonth(chronoPattern);
            case YEARS:
                return doOnYear(chronoPattern);
        }
        return null;
    }

    @NotNull
    private String doMonthsAndWeeksAndDays(@NotNull List<ChronoPattern> patternToDescribeList) {
        final ISeq<OptionalInt> monthSeq = patternToDescribeList.stream()
                .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.MONTHS))
                .map(ChronoPattern::getTemporalValue)
                .collect(ISeq.toISeq());
        final ISeq<OptionalInt> weekSeq = patternToDescribeList.stream()
                .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.WEEKS))
                .map(ChronoPattern::getTemporalValue)
                .collect(ISeq.toISeq());
        final ISeq<OptionalInt> daySeq = patternToDescribeList.stream()
                .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.DAYS))
                .map(ChronoPattern::getTemporalValue)
                .collect(ISeq.toISeq());

        //remove from to describe list
        patternToDescribeList.removeIf(chronoPattern ->
                chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.MONTHS)
                        || chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.WEEKS)
                        || chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.DAYS)
        );

        StringBuilder sb = new StringBuilder("on ");
        //todo: compare week to month to know if it's last
        if (weekSeq.size() == 1) {
            if (weekSeq.get(0).isPresent()) {
                switch (weekSeq.get(0).getAsInt()) {
                    case 1:
                        sb.append("the first");
                        break;
                    case 2:
                        sb.append("the second");
                        break;
                    case 3:
                        sb.append("the third");
                        break;
                    case 4:
                    case 5:
                        sb.append("the last");
                        break;
                }
            } else {
                sb.append("every");
            }
            sb.append(" ");
        }

        if (daySeq.size() == 1) {
            if (daySeq.get(0).isPresent()) {
                sb.append(DayOfWeek.of(daySeq.get(0).getAsInt()).getDisplayName(TextStyle.FULL, Locale.US));
            } else {
                sb.append("day");
            }
            sb.append(" ");
        } else {
            //describe multiple
            throw new UnsupportedOperationException();
        }

        if (monthSeq.size() == 1) {
            sb.append("of ");
        } else {
            sb.append("in ");
        }
        for (int i = 0; i < monthSeq.size(); i++) {
            OptionalInt month = monthSeq.get(i);
            if (month.isPresent()) {
                if (i != 0) {
                    sb.append(" and ");
                }
                sb.append(Month.of(month.getAsInt()).getDisplayName(TextStyle.FULL, Locale.US));
            } else {
                sb.append("every month");
                break;
            }
        }

        sb.append(" ");

        return sb.toString();
    }

    @NotNull
    private String doWeeksAndDays(@NotNull List<ChronoPattern> patternToDescribeList) {
        final ISeq<OptionalInt> weekSeq = patternToDescribeList.stream()
                .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.WEEKS))
                .map(ChronoPattern::getTemporalValue)
                .collect(ISeq.toISeq());
        final ISeq<OptionalInt> daySeq = patternToDescribeList.stream()
                .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.DAYS))
                .map(ChronoPattern::getTemporalValue)
                .collect(ISeq.toISeq());

        //remove from to describe list
        patternToDescribeList.removeIf(chronoPattern ->
                chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.WEEKS)
                        || chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.DAYS)
        );

        StringBuilder sb = new StringBuilder();
        if (weekSeq.size() == 1) {
            if (weekSeq.get(0).isPresent()) {
                switch (weekSeq.get(0).getAsInt()) {
                    case 1:
                        sb.append("first");
                        break;
                    case 2:
                        sb.append("second");
                        break;
                    case 3:
                        sb.append("third");
                        break;
                    case 4:
                        sb.append("forth");
                        break;
                    case 5:
                        sb.append("fifth");
                        break;
                }
            } else {
                sb.append("every week");
            }
            sb.append(" ");

            if (daySeq.size() == 1) {
                if (daySeq.get(0).isPresent()) {
                    sb.append(DayOfWeek.of(daySeq.get(0).getAsInt()).getDisplayName(TextStyle.FULL, Locale.US));
                } else {
                    sb.append("day");
                }
                sb.append(" ");
            } else {
                //describe multiple
                throw new UnsupportedOperationException();
            }
        } else {
            //describe multiple
            throw new UnsupportedOperationException();
        }

        return sb.toString();
    }

    @NotNull
    private String doYears(@NotNull List<ChronoPattern> patternToDescribeList) {
        final ISeq<ChronoPattern> yearSeq = patternToDescribeList.stream()
                .filter(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.YEARS))
                .collect(ISeq.toISeq());

        //remove from to describe list
        patternToDescribeList.removeIf(chronoPattern ->
                chronoPattern.getChronoScaleUnit().getChronoUnit().equals(ChronoUnit.YEARS)
        );
        if (yearSeq.size() == 1 && !yearSeq.get(0).getTemporalValue().isPresent()) {
            return "every year";
        }
        return describeMultiChronoPattern(yearSeq);
    }

    private boolean hasMonthsAndWeeksAndDays(@NotNull Chronosome chronosome) {
        return requireNonNull(chronosome).stream()
                .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                .map(chronoGene -> (ChronoPattern) chronoGene.getAllele())
                .map(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit())
                .filter(chronoUnit -> chronoUnit.equals(
                        ChronoUnit.MONTHS) || chronoUnit.equals(ChronoUnit.WEEKS) || chronoUnit.equals(ChronoUnit.DAYS)
                )
                .distinct().count() > 2;
    }

    private boolean hasWeeksAndDays(@NotNull Chronosome chronosome) {
        return requireNonNull(chronosome).stream()
                .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                .map(chronoGene -> (ChronoPattern) chronoGene.getAllele())
                .map(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit())
                .filter(chronoUnit -> chronoUnit.equals(ChronoUnit.WEEKS) || chronoUnit.equals(ChronoUnit.DAYS))
                .distinct().count() > 1;
    }

    private boolean hasYears(@NotNull Chronosome chronosome) {
        return requireNonNull(chronosome).stream()
                .filter(chronoGene -> chronoGene.getAllele() instanceof ChronoPattern)
                .map(chronoGene -> (ChronoPattern) chronoGene.getAllele())
                .map(chronoPattern -> chronoPattern.getChronoScaleUnit().getChronoUnit())
                .filter(chronoUnit -> chronoUnit.equals(ChronoUnit.YEARS))
                .distinct().count() > 0;
    }

    @NotNull
    private String doOnYear(@NotNull ChronoPattern chronoPattern) {
        if (!requireNonNull(chronoPattern).getTemporalValue().isPresent()) {
            return "on every year";
        }
        return "on year " + chronoPattern.getTemporalValue().getAsInt();
    }

    @NotNull
    private String doOnMonth(@NotNull ChronoPattern chronoPattern) {
        if (!requireNonNull(chronoPattern).getTemporalValue().isPresent()) {
            return "on every month";
        }
        return "on " + doMonth(chronoPattern.getTemporalValue().getAsInt());
    }

    @NotNull
    private String doMonth(int month) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.US);
    }

    @NotNull
    private String doWeek(@NotNull ChronoPattern chronoPattern) {
        if (!requireNonNull(chronoPattern).getTemporalValue().isPresent()) {
            throw new UnsupportedOperationException();
        }
        return doWeek(chronoPattern.getTemporalValue().getAsInt());
    }

    @NotNull
    @Contract(pure = true)
    private String doWeek(int weekNumber) {
        switch (weekNumber) {
            case 1:
                return "first week";
            case 2:
                return "second week";
            case 3:
                return "third week";
            case 4:
                return "forth week";
            case 5:
                return "fifth week";
            default:
                throw new UnsupportedOperationException();
        }
    }

    @NotNull
    private String doOnTheWeek(@NotNull ChronoPattern chronoPattern) {
        return "on the " + doWeek(requireNonNull(chronoPattern));
    }

    @NotNull
    private String doOnMinute(@NotNull ChronoPattern chronoPattern) {
        if (!requireNonNull(chronoPattern).getTemporalValue().isPresent()) {
            return "every minute";
        }
        return "on minute " + chronoPattern.getTemporalValue().getAsInt();
    }

    @NotNull
    private String doOnSecond(@NotNull ChronoPattern chronoPattern) {
        if (!requireNonNull(chronoPattern).getTemporalValue().isPresent()) {
            return "every second";
        }
        return "on second " + chronoPattern.getTemporalValue().getAsInt();
    }

    @NotNull
    private String describeChronoFrequency(@NotNull ChronoSeries chronoSeries, @NotNull ChronoFrequency chronoFrequency) {
        long minFreq = requireNonNull(chronoFrequency).getMinimumFrequency();
        long maxFreq = chronoFrequency.getMaximumFrequency();
        if (minFreq == maxFreq) {
            ChronoUnit reportUnit = chronoFrequency.getChronoUnit();

            //simplify frequency if possible
            ChronoScaleUnit scaleUnit = ChronoScaleUnit.asFactual(requireNonNull(chronoSeries), chronoFrequency.getChronoUnit());
            if (minFreq == scaleUnit.getActualMaximum()) {
                reportUnit = ChronoScale.getParentChronoUnit(chronoFrequency.getChronoUnit());
                minFreq = 1;
            }

            if (minFreq == 1) {
                return "Once a " + singularChronoUnit(reportUnit);
            }
            return "Every " + minFreq + " " + pluralChronoUnit(reportUnit);
        } else {
            return "Every " + minFreq + " to " + maxFreq + " " + pluralChronoUnit(chronoFrequency.getChronoUnit());
        }
    }

    @NotNull
    private String pluralChronoUnit(@NotNull ChronoUnit chronoUnit) {
        return singularChronoUnit(requireNonNull(chronoUnit)) + "s";
    }

    @NotNull
    private String singularChronoUnit(@NotNull ChronoUnit chronoUnit) {
        String thing = requireNonNull(chronoUnit).toString().toLowerCase();
        thing = thing.substring(0, thing.length() - 1);
        switch (thing) {
            case "nano":
                return "nanosecond";
            case "micro":
                return "microsecond";
            case "milli":
                return "millisecond";
            default:
                return thing;
        }
    }

    @NotNull
    private String everyChronoUnit(@NotNull ChronoUnit chronoUnit) {
        String singular = singularChronoUnit(requireNonNull(chronoUnit));
        switch (singular) {
            case "nanosecond":
                return "every nanosecond";
            case "microsecond":
                return "every microsecond";
            case "millisecond":
                return "every millisecond";
            case "second":
                return "every second";
            case "minute":
                return "every minute";
            case "hour":
                return "every hour";
            case "day":
                return "every day";
            case "week":
                return "every week";
            case "month":
                return "every month";
            case "year":
                return "every year";
            default:
                throw new RuntimeException("bad");
        }
    }

}
