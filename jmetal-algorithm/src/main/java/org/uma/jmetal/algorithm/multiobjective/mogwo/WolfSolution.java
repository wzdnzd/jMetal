/**
 * @Author : wzdnzd
 * @Time :  2021-07-25
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo;

import org.uma.jmetal.algorithm.multiobjective.mogwo.util.CommonUtils;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.io.Serializable;
import java.util.*;

public class WolfSolution implements Serializable, DoubleSolution {
    // 移动速度
    private double velocity;

    // 当前位置
    private List<Double> position;

    // 适应度
    private double[] fitness;

    // 最优位置
    private List<Double> bestPos;

    // 最佳适应度
    private double[] bestFitness;

    // 是否支配
    private boolean dominated = false;

    private String index;

    // 网格索引
    private int[] gridSubIndex;

    protected Map<Object, Object> attributes = new HashMap<>();

    public double getVelocity() {
        return velocity;
    }

    public WolfSolution() {
    }

    public WolfSolution(WolfSolution solution) {
        super();

        velocity = solution.getVelocity();
        attributes = new HashMap<>(solution.attributes);
        position = CommonUtils.deepCopy(solution.variables());
        bestPos = CommonUtils.deepCopy(solution.getBestPos());
        fitness = Arrays.copyOf(solution.objectives(), solution.objectives().length);
        bestFitness = Arrays.copyOf(solution.getBestFitness(), solution.getBestFitness().length);
        dominated = solution.isDominated();
        index = solution.getIndex();
        if (solution.getGridSubIndex() != null) {
            gridSubIndex = Arrays.copyOf(solution.getGridSubIndex(), solution.getGridSubIndex().length);
        }
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void setPosition(List<Double> position) {
        this.position = position;
    }

    public void setFitness(double[] fitness) {
        this.fitness = fitness;
    }

    public List<Double> getBestPos() {
        return bestPos;
    }

    public void setBestPos(List<Double> bestPos) {
        this.bestPos = bestPos;
    }

    public double[] getBestFitness() {
        return bestFitness;
    }

    public void setBestFitness(double[] bestFitness) {
        this.bestFitness = bestFitness;
    }

    public boolean isDominated() {
        return dominated;
    }

    public void setDominated(boolean dominated) {
        this.dominated = dominated;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int[] getGridSubIndex() {
        return gridSubIndex;
    }

    public void setGridSubIndex(int[] gridSubIndex) {
        this.gridSubIndex = gridSubIndex;
    }

    @Override
    public List<Double> variables() {
        return position;
    }

    @Override
    public double[] objectives() {
        return fitness;
    }

    @Override
    public double[] constraints() {
        return new double[0];
    }

    @Override
    public Map<Object, Object> attributes() {
        return attributes;
    }

    @Override
    public Solution<Double> copy() {
        return new WolfSolution(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WolfSolution that = (WolfSolution) o;
        return this.variables() == that.variables();
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    @Override
    public Double getLowerBound(int index) {
        return 0.0;
    }

    @Override
    public Double getUpperBound(int index) {
        return 1.0;
    }
}

