/**
 * @Author : wzdnzd
 * @Time :  2021-07-13
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.spea2aga.util;

import org.uma.jmetal.algorithm.multiobjective.spea2aga.model.GridAlgoBound;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.densityestimator.impl.StrenghtRawFitnessDensityEstimator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AGAUtils {
    // 计算两个个体之间的余弦值
    public static <S extends Solution<?>> double cosineDistance(S solution, double[] ref) {
        double product = 0.0;
        double firstMode = 0.0;
        double secondMode = 0.0;

        for (int i = 0; i < solution.objectives().length; i++) {
            double div1 = solution.objectives()[i];
            double div2 = ref[i];

            product += div1 * div2;
            firstMode += Math.pow(div1, 2);
            secondMode += Math.pow(div2, 2);

        }

        double distance = product / (Math.sqrt(firstMode) * Math.sqrt(secondMode));
        distance = distance < 0 ? 1 - Math.abs(distance) : distance;
        return distance;
    }

    public static <S extends Solution<?>> double cosineDistance(S s1, S s2) {
        double product = 0.0;
        double firstMode = 0.0;
        double secondMode = 0.0;

        for (int i = 0; i < s1.objectives().length; i++) {
            double div1 = s1.objectives()[i];
            double div2 = s2.objectives()[i];

            product += div1 * div2;
            firstMode += Math.pow(div1, 2);
            secondMode += Math.pow(div2, 2);

        }

        double distance = product / (Math.sqrt(firstMode) * Math.sqrt(secondMode));
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

    public static  double distance(double[] v1, double[] v2) {
        double diff;
        double distance = 0.0;

        // euclidean distance
        for (int i = 0; i < v1.length; i++) {
            diff = v1[i] - v2[i];
            distance += Math.pow(diff, 2.0);
        }

        return Math.sqrt(distance);
    }

    public static <S extends Solution<?>> GridAlgoBound<S> findBounds(List<S> solutionSet, int popSize, StrenghtRawFitnessDensityEstimator<S> de) {
        if (solutionSet == null || solutionSet.isEmpty() || popSize < 0 || solutionSet.size() <= popSize) {
            throw new IllegalArgumentException("非法参数, 种群数必须小于候选集数");
        }

        int size = solutionSet.get(0).objectives().length;
        List<S> minPoints = new ArrayList<>(size);
        List<S> maxPoints = new ArrayList<>(size);
        double[] min = new double[size];
        double[] max = new double[size];

        for (int i = 0; i < size; i++) {
            min[i] = solutionSet.get(0).objectives()[i];
            max[i] = solutionSet.get(0).objectives()[i];
            minPoints.add(solutionSet.get(0));
            maxPoints.add(solutionSet.get(0));
        }

        for (S s : solutionSet) {
            double[] objectives = s.objectives();
            for (int i = 0; i < objectives.length; i++) {
                if (objectives[i] >= max[i]) {
                    S solution = objectives[i] != max[i] ? s : (de.getValue(s) <= de.getValue(maxPoints.get(i)) ? s : maxPoints.get(i));
                    max[i] = solution.objectives()[i];
                    maxPoints.set(i, solution);
                } else if (objectives[i] <= min[i]) {
                    S solution = objectives[i] != min[i] ? s : (de.getValue(s) <= de.getValue(minPoints.get(i)) ? s : minPoints.get(i));
                    min[i] = solution.objectives()[i];
                    minPoints.set(i, solution);
                }
            }
        }
        
        List<S> boundPoint = new ArrayList<>(minPoints.size() + maxPoints.size());
        boundPoint.addAll(minPoints);
        boundPoint.addAll(maxPoints);

        boundPoint = boundPoint.stream().distinct().collect(Collectors.toList());
        return new GridAlgoBound<>(boundPoint, min, max, popSize);
    }
}
