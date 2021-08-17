/**
 * @Author : wzdnzd
 * @Time :  2021-07-13
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.util;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.*;
import java.util.stream.Collectors;

public class DistanceUtils {
    // 计算两个个体之间的余弦值
    public static <S extends Solution<?>> double cosineDistance(S solution, double[] ref) {
        if (solution == null) {
            throw new JMetalException("无效的参数, solution不能为空");
        }

        return cosineDistance(solution.objectives(), ref);
    }

    public static <S extends Solution<?>> double cosineDistance(S s1, S s2) {
        if (s1 == null || s2 == null) {
            throw new JMetalException("无效的参数, solution不能为空");
        }

        return cosineDistance(s1.objectives(), s2.objectives());
    }

    public static double cosineDistance(double[] vec1, double[] vec2) {
        if (vec1 == null || vec2 == null || vec1.length != vec2.length) {
            throw new JMetalException("维度不一致");
        }

        double product = 0.0;
        double firstMode = 0.0;
        double secondMode = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            double div1 = vec1[i];
            double div2 = vec2[i];

            product += div1 * div2;
            firstMode += Math.pow(div1, 2);
            secondMode += Math.pow(div2, 2);

        }

        double distance = product / (Math.sqrt(firstMode * secondMode));
        distance = distance < 0 ? 1 - Math.abs(distance) : distance;
        return distance;
    }

    public static <S extends Solution<?>> double distance(S solution, double[] ref) {
        double diff;
        double distance = 0.0;

        // euclidean distance
        for (int i = 0; i < solution.objectives().length; i++) {
            diff = solution.objectives()[i] - ref[i];
            distance += Math.pow(diff, 2.0);
        }

        return Math.sqrt(distance);
    }

    public static double distance(double[] v1, double[] v2) {
        if (v1 == null) {
            v1 = new double[v2.length];
        }

        double diff;
        double distance = 0.0;

        // euclidean distance
        for (int i = 0; i < v1.length; i++) {
            diff = v1[i] - v2[i];
            distance += Math.pow(diff, 2.0);
        }

        return Math.sqrt(distance);
    }

    public static <S extends Solution<?>> double[] findMin(List<S> archives) {
        if (archives == null || archives.isEmpty()) {
            throw new JMetalException("archive cannot be null");
        }

        int size = archives.get(0).objectives().length;
        double[] lowers = new double[size];
        for (int i = 0; i < size; i++) {
            lowers[i] = Double.MAX_VALUE;
        }

        for (S s : archives) {
            double[] objectives = s.objectives();
            for (int i = 0; i < objectives.length; i++) {
                if (objectives[i] <= lowers[i]) {
                    lowers[i] = objectives[i];
                }
            }
        }

        return lowers;
    }


    public static double[] minMaxScale(double[] objectives, double[] min, double[] max) {
        if (objectives == null || min == null || max == null
                || objectives.length != min.length || objectives.length != max.length) {
            throw new JMetalException("维度不一致");
        }

        double[] normalizedObj = new double[objectives.length];
        for (int i = 0; i < objectives.length; i++) {
            normalizedObj[i] = (objectives[i] - min[i]) / (max[i] - min[i]);
        }

        return normalizedObj;
    }

    public static double mean(double[] arrays) {
        if (arrays == null || arrays.length <= 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (double v : arrays) {
            sum += v;
        }

        return sum / arrays.length;
    }

    public static double variance(double[] arrays) {
        if (arrays == null || arrays.length <= 0) {
            return 0.0;
        }

        double average = mean(arrays);
        double sum = 0.0;
        for (double v : arrays) {
            sum += Math.pow(v - average, 2);
        }

        return Math.sqrt(sum / arrays.length);
    }

    public static double variance(double[] arrays, double mean) {
        if (arrays == null || arrays.length <= 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (double v : arrays) {
            sum += Math.pow(v - mean, 2);
        }

        return Math.sqrt(sum / arrays.length);
    }

    public static double[] zScoreNormalize(double[] arrays) {
        if (arrays == null || arrays.length <= 0) {
            return arrays;
        }

        double average = mean(arrays);
        double sigma = variance(arrays, average);
        double[] normalized = new double[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            normalized[i] = (arrays[i] - average) / sigma;
        }

        return normalized;
    }

    public static double[] zScoreNormalize(double[] arrays, double[] means, double[] variances) {
        if (arrays == null || arrays.length <= 0
                || means == null || means.length != arrays.length
                || variances == null || variances.length != means.length) {
            return arrays;
        }

        double[] normalized = new double[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            normalized[i] = variances[i] == 0 ? 0 : (arrays[i] - means[i]) / variances[i];
        }

        return normalized;
    }


    public static double[][] transpose(double[][] matrix) {
        if (matrix == null || matrix.length <= 0) {
            return matrix;
        }


        double[][] result = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    public static <S extends Solution<?>> List<Pair<Integer, S>> findMinByObjective(List<S> solutionSet, int index) {
        if (solutionSet == null || solutionSet.isEmpty()) {
            throw new IllegalArgumentException("非法参数, 种群数不能为空");
        }

        int size = solutionSet.get(0).objectives().length;
        if (index < 0 || index >= size) {
            throw new JMetalException("非法参数，索引超出优化目标维度");
        }

        double min = Double.MAX_VALUE;

        for (S s : solutionSet) {
            double[] objectives = s.objectives();
            if (objectives[index] < min) {
                min = objectives[index];
            }
        }

        List<Pair<Integer, S>> pairs = new ArrayList<>();
        for (int i = 0; i < solutionSet.size(); i++) {
            S s = solutionSet.get(i);
            if (s.objectives()[index] == min) {
                Pair<Integer, S> pair = Pair.of(i, s);
                pairs.add(pair);
            }
        }

        return pairs;
    }

    public static double manhattanDistance(double[] v1, double[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            throw new IllegalArgumentException("非法参数");
        }

        double distance = 0.0;
        for (int i = 0; i < v1.length; i++) {
            distance += Math.abs(v1[i] - v2[i]);
        }

        return distance;
    }

    public static double chebyshevDistance(double[] v1, double[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            throw new IllegalArgumentException("非法参数");
        }

        double[] diffs = new double[v1.length];
        for (int i = 0; i < v1.length; i++) {
            diffs[i] = Math.abs(v1[i] - v2[i]);
        }

        return Arrays.stream(diffs).max().orElse(0);
    }
}
