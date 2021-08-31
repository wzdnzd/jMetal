/**
 * @Author : wzdnzd
 * @Time :  2021-07-13
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.espea2.util;

import org.uma.jmetal.algorithm.multiobjective.espea2.model.GridAlgoBound;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AlgorithmUtils {
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

    public static <S extends Solution<?>> GridAlgoBound<S> findBounds(List<S> solutionSet, int gridNum, boolean filter) {
        if (solutionSet == null || solutionSet.isEmpty() || gridNum < 0 || solutionSet.size() <= gridNum) {
            throw new IllegalArgumentException("非法参数, 网格数必须不大于候选集数");
        }

        int size = solutionSet.get(0).objectives().length;
        Map<Integer, List<S>> relativeBest = new HashMap<>();
        double[] min = new double[size];
        double[] max = new double[size];

        for (int i = 0; i < size; i++) {
            min[i] = solutionSet.get(0).objectives()[i];
            max[i] = solutionSet.get(0).objectives()[i];
            relativeBest.put(i, new ArrayList<>());
        }

        for (S s : solutionSet) {
            double[] objectives = s.objectives();
            for (int i = 0; i < objectives.length; i++) {
                if (objectives[i] > max[i]) {
                    max[i] = objectives[i];
                } else if (objectives[i] < min[i]) {
                    min[i] = objectives[i];
                }
            }
        }

        for (S s : solutionSet) {
            double[] objectives = s.objectives();
            for (int i = 0; i < objectives.length; i++) {
                if (objectives[i] == min[i]) {
                    relativeBest.get(i).add(s);
                }
            }
        }

        List<S> minPoints = new ArrayList<>();
        if (filter) {
            for (List<S> list : relativeBest.values()) {
                if (list.size() == 1) {
                    minPoints.addAll(list);
                    continue;
                }

                list = list.stream().sorted((o1, o2) -> {
                    if (o1 == o2) {
                        return 0;
                    } else if (o1 == null) {
                        return 1;
                    } else if (o2 == null) {
                        return -1;
                    } else {
                        double c1 = chebyshevDistance(o1.objectives(), min);
                        double c2 = chebyshevDistance(o2.objectives(), min);


                        return Double.compare(c1, c2);
                    }
                }).collect(Collectors.toList());

                minPoints.add(list.get(0));
            }
        } else {
            minPoints = relativeBest.values().stream().flatMap(Collection::stream).distinct().collect(Collectors.toList());
        }

        minPoints = minPoints.stream().distinct().collect(Collectors.toList());
        return new GridAlgoBound<>(minPoints, min, max, gridNum);
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
                if (objectives[i] < lowers[i]) {
                    lowers[i] = objectives[i];
                }
            }
        }

        return lowers;
    }

    public static <S extends Solution<?>> double[] findMax(List<S> archives) {
        if (archives == null || archives.isEmpty()) {
            throw new JMetalException("archive cannot be null");
        }

        int size = archives.get(0).objectives().length;
        double[] uppers = new double[size];
        for (int i = 0; i < size; i++) {
            uppers[i] = Double.MIN_VALUE;
        }

        for (S s : archives) {
            double[] objectives = s.objectives();
            for (int i = 0; i < objectives.length; i++) {
                if (objectives[i] > uppers[i]) {
                    uppers[i] = objectives[i];
                }
            }
        }

        return uppers;
    }


    public static double[] normalize(double[] objectives, double[] min, double[] max) {
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

    public static double[] std(double[] arrays) {
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

    public static double[] std(double[] arrays, double[] means, double[] variances) {
        if (arrays == null || arrays.length <= 0
                || means == null || means.length != arrays.length
                || variances == null || variances.length != means.length) {
            return arrays;
        }

        double[] normalized = new double[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            normalized[i] = (arrays[i] - means[i]) / variances[i];
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

    public static <S extends Solution<?>> List<S> findMinBounds(List<S> solutionSet, boolean sort) {
        if (solutionSet == null || solutionSet.isEmpty()) {
            throw new IllegalArgumentException("非法参数, 种群数不能为空");
        }

        int size = solutionSet.get(0).objectives().length;
        Map<Integer, List<S>> map = new HashMap<>();
        double[] min = new double[size];


        for (int i = 0; i < size; i++) {
             int t=i;
            min[t]  = solutionSet.stream().map(s -> s.objectives()[t]).min(Comparator.comparing(Double::doubleValue)).get();
        }


        for(int i=0;i<min.length;i++){
            int t=i;
            List<S> temp=solutionSet.stream().filter(s->s.objectives()[t]==min[t]).collect(Collectors.toList());
            map.put(i, temp);
        }

        if (sort) {
            for (Map.Entry<Integer, List<S>> entry : map.entrySet()) {
                List<S> list = entry.getValue();
                for (S s : list) {
                    int count = 0;
                    for (int i = 0; i < min.length; i++) {
                        if (min[i] == s.objectives()[i]) {
                            count += 1;
                        }
                    }

                    s.attributes().put("extreme", count);
                    s.attributes().put("dim", entry.getKey());
                }

                list.sort((o1, o2) -> {
                    if (o1 == null && o2 == null) {
                        return 0;
                    } else if (o1 == null) {
                        return 1;
                    } else if (o2 == null) {
                        return -1;
                    }

                    double d1 = Math.acos(cosineDistance(o1.objectives(), min));
                    double d2 = Math.acos(cosineDistance(o2.objectives(), min));
                    return Double.compare(d1, d2);
                });
            }
        }

        return map.values().stream().flatMap(Collection::stream).filter(distinctByKey(S::objectives)).collect(Collectors.toList());
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

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
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

    public static <S extends DoubleSolution> List<Double> calculatePosition(double a, S s1, S s2, int posDim, boolean flag) {
        List<Double> c = CommonUtils.updateC(posDim);
        List<Double> list = CommonUtils.updateD(c, s1.variables(), s2.variables());

        if (flag) {
            List<Double> vec = CommonUtils.updateC(posDim);
            vec = vec.stream().map(i -> i - a).collect(Collectors.toList());
            return CommonUtils.nearPrey(s1.variables(), vec, list);
        }

        double v = 2 * Math.random() * a - a;
        return CommonUtils.nearPrey(s1.variables(), v, list);
    }

    public static <S extends Solution<?>> List<S> getNonDominatedParticles(List<S> solutions) {
        if (solutions == null || solutions.isEmpty()) {
            return solutions;
        }

        return solutions.stream().
                filter(s -> !(Boolean) (s.attributes().getOrDefault("dominated", false)))
                .collect(Collectors.toList());
    }


    public static <S extends Solution<?>> List<S> determineDomination(List<S> solutions) {
        if (solutions != null && !solutions.isEmpty()) {
            for (int i = 0; i < solutions.size(); i++) {
                S s1 = solutions.get(i);
                s1.attributes().put("dominated", false);
                for (int j = 0; j < i - 1; j++) {
                    S s2 = solutions.get(j);
                    if ((Boolean) (s2.attributes().getOrDefault("", false))) {
                        continue;
                    }

                    int dominated = CommonUtils.dominated(s1, s2);
                    if (dominated == -1) {
                        s2.attributes().put("dominated", true);
                    } else if (dominated == 1) {
                        s1.attributes().put("dominated", true);
                        break;
                    }
                }
            }
        }

        return solutions;
    }
}
