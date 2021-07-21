/**
 * @Author : wzdnzd
 * @Time :  2021-07-21
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.spea2aga.model;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.solution.Solution;

public class CosineDistance<S extends Solution<?>> {
    private final Pair<S, S> solutions;
    private final double minDistance;

    public CosineDistance(Pair<S, S> solutions, double minDistance) {
        this.solutions = solutions;
        this.minDistance = minDistance;
    }

    public Pair<S, S> getSolutions() {
        return solutions;
    }

    public double getMinDistance() {
        return minDistance;
    }
}
