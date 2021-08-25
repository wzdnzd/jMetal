/**
 * @Author : wzdnzd
 * @Time :  2021-08-20
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo;

public class Distance {
    // 下标
    private final WolfSolution raw;

    // 目标索引
    private final WolfSolution dest;

    // 距离
    private final double distance;

    public Distance(WolfSolution raw, WolfSolution dest, double distance) {
        this.raw = raw;
        this.dest = dest;
        this.distance = distance;
    }

    public WolfSolution getRaw() {
        return raw;
    }

    public WolfSolution getDest() {
        return dest;
    }

    public double getDistance() {
        return distance;
    }
}
