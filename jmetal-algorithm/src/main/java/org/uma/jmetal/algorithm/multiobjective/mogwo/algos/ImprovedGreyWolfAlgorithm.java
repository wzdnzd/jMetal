/**
 * @Author : wzdnzd
 * @Time :  2021-07-25
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.algos;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mogwo.Grid;
import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;
import org.uma.jmetal.algorithm.multiobjective.mogwo.leader.LeaderSelector;
import org.uma.jmetal.algorithm.multiobjective.mogwo.position.PositionInitializer;
import org.uma.jmetal.algorithm.multiobjective.mogwo.strategy.DeleteStrategy;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.CommonUtils;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.GridProcess;
import org.uma.jmetal.algorithm.multiobjective.mogwo.velocity.VelocityInitializer;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImprovedGreyWolfAlgorithm<S extends Solution<?>> implements Algorithm<List<WolfSolution>> {
    // 当前迭代次数
    private int evaluations;

    // 最大迭代次数
    private final int maxEvaluations;

    // 灰狼个数
    private final int wolvesNum;

    // 坐标初始化器
    private final PositionInitializer positionInitializer;

    // 速度初始化器
    private final VelocityInitializer velocityInitializer;

    // Leader选择器
    private final LeaderSelector leaderSelector;

    private final Problem<DoubleSolution> problem;

    // 归档集
    private List<WolfSolution> archive;

    // 归档集大小
    private final int archiveSize;

    // 网格数
    private final int gridNum;

    private final double alpha;

    // 个体删除策略
    private final DeleteStrategy deleteStrategy;

    // 算法名
    private final String name;

    // 描述信息
    private final String description;

    public ImprovedGreyWolfAlgorithm(int maxEvaluations, int wolvesNum, PositionInitializer positionInitializer,
                                     VelocityInitializer velocityInitializer, LeaderSelector leaderSelector,
                                     Problem<DoubleSolution> problem, int archiveSize, int gridNum, double alpha,
                                     DeleteStrategy deleteStrategy, String name, String description) {
        this.evaluations = 0;
        this.maxEvaluations = maxEvaluations;
        this.wolvesNum = wolvesNum;
        this.positionInitializer = positionInitializer;
        this.velocityInitializer = velocityInitializer;
        this.leaderSelector = leaderSelector;
        this.problem = problem;
        this.archiveSize = archiveSize;
        this.gridNum = gridNum;
        this.alpha = alpha;
        this.deleteStrategy = deleteStrategy;
        this.name = name;
        this.description = description;
        this.archive = new ArrayList<>();
    }

    public List<WolfSolution> initWolves(int greyWolvesNum, VelocityInitializer velocityInitializer) {
        List<WolfSolution> wolves = new ArrayList<>();

        for (int i = 0; i < greyWolvesNum; i++) {
            DoubleSolution solution = problem.createSolution();
            WolfSolution wolf = new WolfSolution();
            wolf.setPosition(solution.variables());
            wolf.setVelocity(velocityInitializer.initialize());
            wolf.setFitness(problem.evaluate(solution).objectives());
            wolf.setDominated(false);
            wolf.setBestPos(wolf.variables());
            wolf.setBestFitness(wolf.objectives());
            wolves.add(wolf);
        }

        return wolves;
    }

    @Override
    public void run() {
        List<WolfSolution> wolves = initWolves(wolvesNum, velocityInitializer);
        wolves = determineDomination(wolves);
        archive = CommonUtils.deepCopy(getNonDominatedParticles(wolves));
        List<Grid> grids = GridProcess.createHypercubes(archive, gridNum, alpha);
        setGridsIndices(archive, grids);
        initProgress();

        while (!isStoppingConditionReached()) {
            double a = 2 - (evaluations + 1) * (2.0 / maxEvaluations);
            for (int i = 0; i < wolvesNum; i++) {
                WolfSolution deltaWolf = leaderSelector.select(archive);
                WolfSolution betaWolf = leaderSelector.select(archive);
                WolfSolution alphaWolf = leaderSelector.select(archive);

                //根据选择的三只狼更新其他狼的位置
                WolfSolution wolf = wolves.get(i);
                int posDim = wolf.variables().size();

                List<Double> p1 = calculatePosition(a, deltaWolf, wolf, posDim, true);
                List<Double> p2 = calculatePosition(a, betaWolf, wolf, posDim, false);
                List<Double> p3 = calculatePosition(a, alphaWolf, wolf, posDim, false);

                //判断位置是否超出边界
                wolf.setPosition(positionInitializer.boundCheck(CommonUtils.mean(p1, p2, p3)));
                wolf.setFitness(problem.evaluate(wolf).objectives());
            }

            wolves = determineDomination(wolves);
            List<WolfSolution> nonDominatedParticles = getNonDominatedParticles(wolves);
            archive.addAll(CommonUtils.deepCopy(nonDominatedParticles));
            archive = determineDomination(archive);
            archive = getNonDominatedParticles(archive);

            // 根据position去重
            archive = archive.stream().distinct().collect(Collectors.toList());

            //归档集中每个个体找到对应的网格
            for (WolfSolution wolf : archive) {
                GridProcess.setGridIndex(wolf, grids);
            }

            if (archive.size() > archiveSize) {
                int extra = archive.size() - archiveSize;
                archive = deleteStrategy.delete(archive, extra);
                grids = GridProcess.createHypercubes(archive, gridNum, alpha);
            }

            updateProgress();
        }
    }

    @Override
    public List<WolfSolution> getResult() {
        return this.archive;
    }

    protected List<Double> calculatePosition(double a, WolfSolution wolf, WolfSolution otherWolf, int posDim, boolean flag) {
        List<Double> c = CommonUtils.updateC(posDim);
        List<Double> list = CommonUtils.updateD(c, wolf.variables(), otherWolf.variables());

        if (flag) {
            List<Double> vec = CommonUtils.updateC(posDim);
            vec = vec.stream().map(i -> i - a).collect(Collectors.toList());
            return CommonUtils.nearPrey(wolf.variables(), vec, list);
        }

        double v = 2 * Math.random() * a - a;
        return CommonUtils.nearPrey(wolf.variables(), v, list);
    }

    protected void setGridsIndices(List<WolfSolution> archive, List<Grid> grids) {
        if (archive == null || archive.isEmpty() || grids == null || grids.isEmpty()) {
            return;
        }

        archive.parallelStream().forEach(s -> GridProcess.setGridIndex(s, grids));
    }

    protected List<WolfSolution> getNonDominatedParticles(List<WolfSolution> wolves) {
        if (wolves == null || wolves.isEmpty()) {
            return wolves;
        }

        return wolves.stream().
                filter(w -> !w.isDominated())
                .collect(Collectors.toList());
    }


    protected List<WolfSolution> determineDomination(List<WolfSolution> wolves) {
        if (wolves != null && !wolves.isEmpty()) {
            for (int i = 0; i < wolves.size(); i++) {
                WolfSolution s1 = wolves.get(i);
                s1.setDominated(false);
                for (int j = 0; j < i - 1; j++) {
                    WolfSolution s2 = wolves.get(j);
                    if (s2.isDominated()) {
                        continue;
                    }

                    int dominated = CommonUtils.dominated(s1, s2);
                    if (dominated == -1) {
                        s2.setDominated(true);
                    } else if (dominated == 1) {
                        s1.setDominated(true);
                        break;
                    }
                }
            }
        }

        return wolves;
    }

    protected void updateProgress() {
        evaluations += 1;
    }

    protected void initProgress() {
        evaluations = 1;
    }

    protected boolean isStoppingConditionReached() {
        return evaluations >= maxEvaluations;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
