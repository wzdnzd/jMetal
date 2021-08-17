/**
 * @Author : wzdnzd
 * @Time :  2021-07-25
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.position;

import java.util.List;

public abstract class PositionInitializer {
    // 上界
    private final double[] lowers;

    // 下界
    private final double[] uppers;

    public PositionInitializer(double[] lowers, double[] uppers) {
        if (lowers == null || uppers == null || lowers.length != uppers.length) {
            throw new IllegalArgumentException("invalid lowerBound and upperBound");
        }

        this.lowers = lowers;
        this.uppers = uppers;
    }

    public int getDim() {
        return lowers.length;
    }

    public double[] getLowers() {
        return lowers;
    }

    public double[] getUppers() {
        return uppers;
    }

    public abstract List<Double> initialize();

    public abstract List<Double> boundCheck(List<Double> pos);
}
