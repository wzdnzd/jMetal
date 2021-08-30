/**
 * @Author : wzdnzd
 * @Time :  2021-08-17
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.espea2.leader;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.DistanceUtils;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.*;
import java.util.stream.Collectors;

public class BestLeaderSelector<S extends DoubleSolution> extends LeaderSelector<S> {
    // 头狼个数
    private final int capacity;

    // 记录优化目标索引
    private List<Integer> indies;

    public BestLeaderSelector(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public int selectOne(List<S> archive) {
        if (indies == null || indies.isEmpty()) {
            // 重新生成索引
            indies = selectTopSolutions(archive, capacity);
        }

        // 取出优化目标对应的索引
        Integer index = indies.iterator().next();

        // 该优化目标已经被优化过，移除
        indies.remove(index);

        return index;
    }

    private List<Integer> selectTopSolutions(List<S> archives, int topN) {
        if (archives == null || archives.isEmpty() || topN <= 0) {
            throw new JMetalException("非法参数，归档集不能为空且topN必须大于0");
        }

        int m = archives.size();
        int n = archives.get(0).objectives().length;
        double[][] matrix = new double[m][n];
        for (int i = 0; i < archives.size(); i++) {
            matrix[i] = archives.get(i).objectives();
        }

        matrix = DistanceUtils.transpose(matrix);
        double[] means = new double[n];
        double[] variances = new double[n];
        for (int i = 0; i < n; i++) {
            means[i] = DistanceUtils.mean(matrix[i]);
            variances[i] = DistanceUtils.variance(matrix[i], means[i]);
        }

        double[] lowers = DistanceUtils.zScoreNormalize(DistanceUtils.findMin(archives), means, variances);

        List<Pair<Integer, Double>> pairs = new ArrayList<>();
        for (int i = 0; i < archives.size(); i++) {
            S s = archives.get(i);
            // 标准化
            double[] objectives = DistanceUtils.zScoreNormalize(s.objectives(), means, variances);

            // 计算个体与平均值之间的欧氏距离
            double distance = DistanceUtils.distance(lowers, objectives);
            pairs.add(Pair.of(i, distance));
        }

        pairs = pairs.stream().sorted(Comparator.comparing(Pair::getRight))
                .collect(Collectors.toList());

        List<Integer> indies = new ArrayList<>(topN);
        int count = 0;
        while (count < pairs.size() && indies.size() < topN) {
            indies.add(pairs.get(count).getLeft());
            count += 1;
        }

        Random ran = new Random();
        while (indies.size() < capacity) {
            indies.add(ran.nextInt(archives.size()));
        }

        return indies;
    }
}
