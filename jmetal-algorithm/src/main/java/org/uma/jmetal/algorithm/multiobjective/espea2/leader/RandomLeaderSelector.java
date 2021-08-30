/**
 * @Author : wzdnzd
 * @Time :  2021-08-17
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.espea2.leader;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.DistanceUtils;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.*;
import java.util.stream.Collectors;

public class RandomLeaderSelector<S extends DoubleSolution> extends LeaderSelector<S> {
    // 头狼个数
    private final int capacity;

    // 记录优化目标索引
    private HashSet<Integer> indies;

    // 记录前面选过的狼
    private final HashSet<Integer> solutions = new HashSet<>();


    public RandomLeaderSelector(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public int selectOne(List<S> archive) {
        int objNum = archive.get(0).objectives().length;
        if (indies == null || indies.isEmpty()) {
            // 重新生成索引
            indies = generateIndies(objNum, capacity);

            // 清空前序已选狼群
            solutions.clear();
        }

        // 取出优化目标对应的索引
        Integer index = indies.iterator().next();

        // 找出该优化目标上所有极值点
        List<Pair<Integer, S>> pairs = DistanceUtils.findMinByObjective(archive, index);
        int idx;
        if (pairs.size() == 1) {
            idx = pairs.get(0).getLeft();
        } else {
            idx = selectOneFromMultiWolves(pairs, solutions);
        }

        solutions.add(idx);
        // 该优化目标已经被优化过，移除
        indies.remove(index);
        return idx;
    }

    private static HashSet<Integer> generateIndies(int objNum, int capacity) {
        Random ran = new Random();
        HashSet<Integer> rands = new HashSet<>(capacity);
        do {
            int index = ran.nextInt(objNum);
            rands.add(index);
        } while (rands.size() != capacity);

        return rands;
    }

    private int selectOneFromMultiWolves(List<Pair<Integer, S>> pairs, HashSet<Integer> solutions) {
        int m = pairs.size();
        int n = pairs.get(0).getRight().objectives().length;
        double[][] matrix = new double[m][n];
        for (int i = 0; i < pairs.size(); i++) {
            matrix[i] = pairs.get(i).getRight().objectives();
        }

        matrix = DistanceUtils.transpose(matrix);
        double[] means = new double[n];
        double[] variances = new double[n];
        for (int i = 0; i < n; i++) {
            means[i] = DistanceUtils.mean(matrix[i]);
            variances[i] = DistanceUtils.variance(matrix[i], means[i]);
        }

        List<S> archives = pairs.stream().map(Pair::getRight).collect(Collectors.toList());
        double[] lowers = DistanceUtils.zScoreNormalize(DistanceUtils.findMin(archives), means, variances);
        List<Pair<Integer, Double>> list = pairs.stream().map(p -> {
                    S s = p.getRight();
                    // 标准化
                    double[] objectives = DistanceUtils.zScoreNormalize(s.objectives(), means, variances);

                    // 计算个体与平均值之间的欧氏距离
                    double distance = DistanceUtils.distance(lowers, objectives);

                    return Pair.of(p.getLeft(), distance);
                }).sorted(Comparator.comparing(Pair::getRight))
                .collect(Collectors.toList());

        int index = 0;
        while (index < list.size() && solutions.contains(list.get(index).getLeft())) {
            index += 1;
        }


        return index == list.size() ? list.get(0).getLeft() : list.get(index).getLeft();
    }
}
