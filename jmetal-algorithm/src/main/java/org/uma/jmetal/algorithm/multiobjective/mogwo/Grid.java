/**
 * @Author : wzdnzd
 * @Time :  2021-07-25
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo;

import java.io.Serializable;
import java.util.List;

public class Grid implements Serializable {
    // 下界
    private final List<Double> lower;

    // 上界
    private final List<Double> upper;

    private List<WolfSolution> wolves;

    public Grid(List<Double> lower, List<Double> upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public List<Double> getLower() {
        return lower;
    }

    public List<Double> getUpper() {
        return upper;
    }

    public List<WolfSolution> getWolves() {
        return wolves;
    }

    public void setWolves(List<WolfSolution> wolves) {
        this.wolves = wolves;
    }
}
