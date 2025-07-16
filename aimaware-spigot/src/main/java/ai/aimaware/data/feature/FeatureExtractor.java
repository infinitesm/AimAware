package ai.aimaware.data.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureExtractor {

    public static Map<String, Double> extractFeatures(Map<String, List<Double>> rawWindow) {
        Map<String, Double> features = new HashMap<>();

        for (Map.Entry<String, List<Double>> entry : rawWindow.entrySet()) {
            String signal = entry.getKey();
            List<Double> values = entry.getValue();

            features.put(signal + "_mean", mean(values));
            features.put(signal + "_std", std(values));
            features.put(signal + "_skewness", skewness(values));
            features.put(signal + "_kurtosis", kurtosis(values));
            features.put(signal + "_autocorr1", autocorr(values, 1));
            features.put(signal + "_autocorr3", autocorr(values, 3));
            features.put(signal + "_autocorr5", autocorr(values, 5));
            features.put(signal + "_slope", linearSlope(values));
            features.put(signal + "_zeroCrossings", (double) zeroCrossings(values));
            features.put(signal + "_energy", energy(values));
            features.put(signal + "_entropy", entropy(values, 10));
        }

        return features;
    }

    // Generated using LLM
    private static double mean(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double v : values) sum += v;
        return sum / values.size();
    }

    private static double std(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double m = mean(values);
        double sumSq = 0.0;
        for (double v : values) sumSq += (v - m) * (v - m);
        return Math.sqrt(sumSq / values.size());
    }

    private static double skewness(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double m = mean(values);
        double s = std(values);
        if (s == 0.0) return 0.0;

        double sum = 0.0;
        for (double v : values) {
            sum += Math.pow((v - m) / s, 3);
        }
        return sum / values.size();
    }

    private static double kurtosis(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double m = mean(values);
        double s = std(values);
        if (s == 0.0) return 0.0;

        double sum = 0.0;
        for (double v : values) {
            sum += Math.pow((v - m) / s, 4);
        }
        return sum / values.size() - 3.0; // excess kurtosis
    }

    private static double autocorr(List<Double> values, int lag) {
        if (values.size() <= lag) return 0.0;

        double mean = mean(values);
        double num = 0.0;
        double denom = 0.0;

        for (int i = 0; i < values.size() - lag; i++) {
            num += (values.get(i) - mean) * (values.get(i + lag) - mean);
        }
        for (double v : values) {
            denom += Math.pow(v - mean, 2);
        }

        return denom == 0.0 ? 0.0 : num / denom;
    }

    private static double linearSlope(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        int n = values.size();
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumXX = 0.0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumXX += i * i;
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = n * sumXX - sumX * sumX;

        return denominator == 0.0 ? 0.0 : numerator / denominator;
    }

    private static int zeroCrossings(List<Double> values) {
        if (values.size() < 2) return 0;
        int count = 0;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i - 1) * values.get(i) < 0) {
                count++;
            }
        }
        return count;
    }

    private static double energy(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double sumSquares = 0.0;
        for (double v : values) {
            sumSquares += v * v;
        }
        return sumSquares;
    }

    private static double entropy(List<Double> values, int numBins) {
        if (values.isEmpty()) return 0.0;

        // Compute histogram
        double min = Collections.min(values);
        double max = Collections.max(values);
        if (min == max) return 0.0; // flat signal → zero entropy

        double binWidth = (max - min) / numBins;
        int[] histogram = new int[numBins];

        for (double v : values) {
            int bin = (int) ((v - min) / binWidth);
            if (bin >= numBins) bin = numBins - 1;
            histogram[bin]++;
        }

        double entropy = 0.0;
        for (int count : histogram) {
            if (count > 0) {
                double p = (double) count / values.size();
                entropy -= p * Math.log(p);
            }
        }
        return entropy;
    }
}
