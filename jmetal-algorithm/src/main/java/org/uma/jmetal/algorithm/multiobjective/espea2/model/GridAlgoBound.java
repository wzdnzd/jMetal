/**
 * @Author : wzdnzd
 * @Time :  2021-07-13
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.espea2.model;

import org.uma.jmetal.solution.Solution;

import java.util.List;

public class GridAlgoBound<S extends Solution<?>> {
    // 各维度边界点列表
    private final List<S> boundPoints;

    // 网格宽度
    private final double[] widths;

    private final double[] min;

    private final double[] max;

    public GridAlgoBound(List<S> boundPoints, double[] min, double[] max, int num) {
        if (min == null || max == null || min.length != max.length) {
            throw new IllegalArgumentException("上下界维度必须一致");
        }

        this.boundPoints = boundPoints;
        this.min = min;
        this.max = max;
        this.widths = generateGrids(min, max, num);
    }

    public List<S> getBoundPoints() {
        return boundPoints;
    }


    public double[] getMin() {
        return min;
    }

    public double[] getMax() {
        return max;
    }

    public double[] getWidths() {
        return widths;
    }

    // 生成网格的参考点
    private double[] generateGrids(double[] min, double[] max, int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("num必须大于0");
        }

        // 生成网格的上下边界坐标及每一维的宽度
        int size = min.length;
        double[] widths = new double[size];
        for (int i = 0; i < size; i++) {
            widths[i] = (max[i] - min[i]) / num;
        }

        return widths;
    }
}
