package org.uma.jmetal.algorithm.multiobjective.spea2aga.model;

import org.uma.jmetal.solution.Solution;

import java.util.List;

public class GridStore<S extends Solution<?>> {
    private S solution;
    private List<Integer> objectGrid;
    private double[] centers;
    private CosineDistance<S> cosineDistance;

    public GridStore() {
    }

    public S getSolution() {
        return solution;
    }

    public void setSolution(S solution) {
        this.solution = solution;
    }

    public List<Integer> getObjectGrid() {
        return objectGrid;
    }

    public void setObjectGrid(List<Integer> objectGrid) {
        this.objectGrid = objectGrid;
    }

    public double[] getCenters() {
        return centers;
    }

    public void setCenters(double[] centers) {
        this.centers = centers;
    }

    public CosineDistance<S> getCosineDistance() {
        return cosineDistance;
    }

    public void setCosineDistance(CosineDistance<S> cosineDistance) {
        this.cosineDistance = cosineDistance;
    }
}
