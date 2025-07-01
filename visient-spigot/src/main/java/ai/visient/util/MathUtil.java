package ai.visient.util;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class MathUtil {

    public float interiorAngle(float a, float b) {
        float delta = Math.abs(a - b);

        if (delta > 180) {
            delta = 360 - delta;
        }

        return delta;
    }

    public double mean(Collection<? extends Number> samples) {
        double sum = 0;
        for (Number sample : samples) {
            sum += sample.doubleValue();
        }
        return sum / samples.size();
    }

    // Calculate standard deviation
    public double deviation(Collection<? extends Number> samples) {
        double mean = mean(samples);
        double sumSquaredDeviations = 0;
        for (Number sample : samples) {
            double deviation = sample.doubleValue() - mean;
            sumSquaredDeviations += deviation * deviation;
        }
        return Math.sqrt(sumSquaredDeviations / (samples.size() - 1));
    }

    public double median(Collection<? extends Number> samples) {
        List<Double> converted = samples.stream()
                .map(Number::doubleValue)
                .sorted()
                .collect(Collectors.toList());

        return converted.size() % 2 == 0
                ? converted.get(converted.size() / 2)
                : (converted.get((converted.size() - 1) / 2) + converted.get((converted.size() + 1) / 2)) / 2;
    }

    public boolean checkRange(double lower, double upper, double val) {
        return val <= upper && val >= lower;
    }

    public double iqr(Collection<? extends Number> samples) {
        List<Double> sorted = samples.stream()
                .map(Number::doubleValue)
                .sorted()
                .collect(Collectors.toList());

        double q1 = median(sorted.subList(0, sorted.size() / 2));
        double q3 = median(sorted.subList(sorted.size() / 2, sorted.size()));

        return Math.abs(q3 - q1);
    }

    public double mad(Collection<? extends Number> samples) {
        double median = median(samples);

        List<Double> deviations = samples.stream()
                .map(n -> Math.abs(n.doubleValue() - median))
                .collect(Collectors.toList());

        return median(deviations);
    }

    public double max(Collection<? extends Number> samples) {
        double max = Double.NEGATIVE_INFINITY;

        for (Number sample : samples) {
            if (sample.doubleValue() > max) {
                max = sample.doubleValue();
            }
        }

        return max;
    }

    public double min(Collection<? extends Number> samples) {
        double min = Double.POSITIVE_INFINITY;

        for (Number sample : samples) {
            if (sample.doubleValue() < min) {
                min = sample.doubleValue();
            }
        }

        return min;
    }

    public double absoluteMean(Collection<? extends Number> samples) {
        double sum = 0;
        for (Number n : samples) {
            sum += Math.abs(n.doubleValue());
        }
        return sum / samples.size();
    }

}