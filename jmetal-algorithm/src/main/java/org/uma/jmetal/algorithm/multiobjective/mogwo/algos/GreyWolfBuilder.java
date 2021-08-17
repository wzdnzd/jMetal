/**
 * @Author : wzdnzd
 * @Time :  2021-07-26
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.algos;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.AlgorithmBuilder;
import org.uma.jmetal.algorithm.multiobjective.mogwo.leader.LeaderSelector;
import org.uma.jmetal.algorithm.multiobjective.mogwo.position.PositionInitializer;
import org.uma.jmetal.algorithm.multiobjective.mogwo.strategy.DeleteStrategy;
import org.uma.jmetal.algorithm.multiobjective.mogwo.velocity.VelocityInitializer;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.List;

public class GreyWolfBuilder<S extends Solution<?>> implements AlgorithmBuilder<Algorithm<List<DoubleSolution>>> {
    // 最大迭代次数
    private final int maxEvaluations;

    // 灰狼个数
    private final int wolvesNum;

    // 坐标初始化器
    private PositionInitializer positionInitializer;

    // 速度初始化器
    private VelocityInitializer velocityInitializer;

    // Leader选择器
    private LeaderSelector leaderSelector;

    private final Problem<DoubleSolution> problem;

    // 归档集大小
    private int archiveSize;

    // 网格数
    private int gridNum;

    private double alpha = 0.1;

    // 个体删除策略
    private DeleteStrategy deleteStrategy;

    // 算法名
    private final String name;

    // 描述信息
    private String description = "";

    public GreyWolfBuilder(int maxEvaluations, int wolvesNum, Problem<DoubleSolution> problem, String name) {
        this.maxEvaluations = maxEvaluations;
        this.wolvesNum = wolvesNum;
        this.problem = problem;
        this.name = name;
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public int getWolvesNum() {
        return wolvesNum;
    }

    public PositionInitializer getPositionInitializer() {
        return positionInitializer;
    }

    public GreyWolfBuilder<S> setPositionInitializer(PositionInitializer positionInitializer) {
        if (positionInitializer == null) {
            throw new JMetalException("positionInitializer is null");
        }

        this.positionInitializer = positionInitializer;
        return this;
    }

    public VelocityInitializer getVelocityInitializer() {
        return velocityInitializer;
    }

    public GreyWolfBuilder<S> setVelocityInitializer(VelocityInitializer velocityInitializer) {
        if (velocityInitializer == null) {
            throw new JMetalException("velocityInitializer is null");
        }

        this.velocityInitializer = velocityInitializer;
        return this;
    }

    public LeaderSelector getLeaderSelector() {
        return leaderSelector;
    }

    public GreyWolfBuilder<S> setLeaderSelector(LeaderSelector leaderSelector) {
        if (leaderSelector == null) {
            throw new JMetalException("leaderSelector is null");
        }

        this.leaderSelector = leaderSelector;
        return this;
    }

    public Problem<DoubleSolution> getProblem() {
        return problem;
    }

    public int getArchiveSize() {
        return archiveSize;
    }

    public GreyWolfBuilder<S> setArchiveSize(int archiveSize) {
        if (archiveSize <= 0) {
            throw new JMetalException("archiveSize less than 1");
        }

        this.archiveSize = archiveSize;
        return this;
    }

    public int getGridNum() {
        return gridNum;
    }

    public GreyWolfBuilder<S> setGridNum(int gridNum) {
        if (gridNum <= 0) {
            throw new JMetalException("gridNum less than 1");
        }

        this.gridNum = gridNum;
        return this;
    }

    public double getAlpha() {
        return alpha;
    }

    public GreyWolfBuilder<S> setAlpha(double alpha) {
        if (alpha < 0) {
            throw new JMetalException("alpha less than 0");
        }

        this.alpha = alpha;
        return this;
    }

    public DeleteStrategy getDeleteStrategy() {
        return deleteStrategy;
    }

    public GreyWolfBuilder<S> setDeleteStrategy(DeleteStrategy deleteStrategy) {
        if (deleteStrategy == null) {
            throw new JMetalException("deleteStrategy is null");
        }

        this.deleteStrategy = deleteStrategy;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public GreyWolfBuilder<S> setDescription(String description) {
        if (description != null && !"".equals(description.trim())) {
            this.description = description;
        }

        return this;
    }

    @Override
    public Algorithm<List<DoubleSolution>> build() {
        Pair<Boolean, String> result = valid();
        if (!result.getLeft()) {
            throw new IllegalArgumentException(result.getRight());
        }

        return new GreyWolfAlgorithm(maxEvaluations, wolvesNum, positionInitializer, velocityInitializer,
                leaderSelector, problem, archiveSize, gridNum, alpha, deleteStrategy, name, description);
    }

    protected Pair<Boolean, String> valid() {
        if (problem == null) {
            return Pair.of(false, "problem is null");
        }

        if (positionInitializer == null) {
            return Pair.of(false, "positionInitializer is null");
        }

        if (velocityInitializer == null) {
            return Pair.of(false, "velocityInitializer is null");
        }

        if (leaderSelector == null) {
            return Pair.of(false, "leaderSelector is null");
        }

        if (deleteStrategy == null) {
            return Pair.of(false, "deleteStrategy is null");
        }

        if (maxEvaluations <= 0) {
            return Pair.of(false, "maxEvaluations must great than 0");
        }

        if (wolvesNum <= 0) {
            return Pair.of(false, "wolvesNum must great than 0");
        }

        if (archiveSize <= 0) {
            return Pair.of(false, "archiveSize must great than 0");
        }

        if (gridNum <= 0) {
            return Pair.of(false, "gridNum must great than 0");
        }


        if (alpha <= 0) {
            return Pair.of(false, "alpha must great than 0");
        }


        if (name == null || "".equals(name.trim())) {
            return Pair.of(false, "algorithm name cannot be null");
        }

        return Pair.of(true, "");
    }
}
