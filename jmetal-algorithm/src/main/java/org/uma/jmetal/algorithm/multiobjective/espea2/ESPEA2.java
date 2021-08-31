package org.uma.jmetal.algorithm.multiobjective.espea2;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.espea2.leader.BestLeaderSelector;
import org.uma.jmetal.algorithm.multiobjective.espea2.leader.LeaderSelector;
import org.uma.jmetal.algorithm.multiobjective.espea2.selection.EnvironmentalSelection;
import org.uma.jmetal.algorithm.multiobjective.espea2.selection.ExtremeEnvironmentalSelection;
import org.uma.jmetal.algorithm.multiobjective.espea2.util.*;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.densityestimator.impl.StrenghtRawFitnessDensityEstimator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Juan J. Durillo
 **/
public class ESPEA2<S extends DoubleSolution> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final int maxIterations;
    protected final SolutionListEvaluator<S> evaluator;
    protected int iterations;
    protected List<S> archive;
    protected List<S> externalArchive;
    protected final ExtremeEnvironmentalSelection<S> externalEnvironmentSelection;

    protected final StrenghtRawFitnessDensityEstimator<S> densityEstimator = new StrenghtRawFitnessDensityEstimator<>(1);
    protected final EnvironmentalSelection<S> environmentalSelection;
    protected final int k;

    private final LeaderSelector<S> leaderSelector = new BestLeaderSelector<>(3);
    private final static double[] LOWER_BOUND = CommonUtils.zeros(150);
    private final static double[] UPPER_BOUND = CommonUtils.ones(150);

    public ESPEA2(Problem<S> problem, int maxIterations, int populationSize,
                  CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                  SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator,
                  int k) {
        super(problem);
        this.maxIterations = maxIterations;
        this.setMaxPopulationSize(populationSize);
        this.externalArchive = new ArrayList<>(populationSize);
        this.k = k;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.environmentalSelection = new EnvironmentalSelection<>(populationSize, k);
        this.externalEnvironmentSelection = new ExtremeEnvironmentalSelection<>(populationSize / 3);
        this.archive = new ArrayList<>(populationSize);
        this.evaluator = evaluator;
    }

    @Override
    protected void initProgress() {
        iterations = 1;
    }

    @Override
    protected void updateProgress() {
        iterations++;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return iterations >= maxIterations;
    }

    @Override
    public void run() {
        List<S> offspringPopulation;
        List<S> matingPopulation;

        List<S> externalMatingPopulation;
        List<S> externalOffspringPopulation;

        population = createInitialPopulation();
        population = evaluatePopulation(population);

        // List<S> nonDominatedPopulation = new ArrayList<>();

        initProgress();
        while (!isStoppingConditionReached()) {
            double a = 2 - (iterations + 1) * (2.0 / maxIterations);
            matingPopulation = selection(population);

            // 过滤非支配解集合
            // nonDominatedPopulation.addAll(population.stream().filter(s -> densityEstimator.getValue(s) < 1.0).collect(Collectors.toList()));
            List<S> nonDominatedPopulation = population.stream()
                    .filter(s -> densityEstimator.getValue(s) < 1.0)
                    .collect(Collectors.toList());

            // 对每一代的极值进行更新
            externalMatingPopulation = externalSelection(nonDominatedPopulation);
            externalOffspringPopulation = externalReproduction(externalMatingPopulation, a);
            externalOffspringPopulation = evaluatePopulation(externalOffspringPopulation);

            offspringPopulation = reproduction(matingPopulation);
            offspringPopulation = evaluatePopulation(offspringPopulation);
            population = replacementExternal(population, offspringPopulation, externalOffspringPopulation);

            updateProgress();
        }
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        population = evaluator.evaluate(population, getProblem());
        return population;
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> union = new ArrayList<>(2 * getMaxPopulationSize());
        union.addAll(archive);
        union.addAll(population);
        densityEstimator.compute(union);
        archive = environmentalSelection.execute(union);
        return archive;
    }

    protected List<S> externalSelection(List<S> population) {
        List<S> union = new ArrayList<>();
        union.addAll(externalArchive);
        union.addAll(AlgorithmUtils.findMinBounds(population, true));
        densityEstimator.compute(union);
        //外部非支配解集合生成
        externalArchive = externalEnvironmentSelection.execute(union);

        // union = AlgorithmUtils.determineDomination(union);
        // externalArchive = AlgorithmUtils.getNonDominatedParticles(union);

        return externalArchive;
    }

    protected List<S> externalReproduction(List<S> population, double a) {
        List<S> list = CommonUtils.deepCopy(population);

        for (S s : list) {
            S deltaWolf = leaderSelector.select(population);
            S betaWolf = leaderSelector.select(population);
            S alphaWolf = leaderSelector.select(population);

            //根据选择的三只狼更新其他狼的位置
            int posDim = s.variables().size();

            List<Double> p1 = AlgorithmUtils.calculatePosition(a, deltaWolf, s, posDim, true);
            List<Double> p2 = AlgorithmUtils.calculatePosition(a, betaWolf, s, posDim, false);
            List<Double> p3 = AlgorithmUtils.calculatePosition(a, alphaWolf, s, posDim, false);

            //判断位置是否超出边界
            List<Double> variables = CommonUtils.boundCheck(CommonUtils.mean(p1, p2, p3), LOWER_BOUND, UPPER_BOUND);
            for (int j = 0; j < variables.size(); j++) {
                s.variables().set(j, variables.get(j));
            }
        }

        return list;
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> offSpringPopulation = new ArrayList<>(getMaxPopulationSize());

        while (offSpringPopulation.size() < getMaxPopulationSize()) {
            List<S> parents = new ArrayList<>(2);
            S candidateFirstParent = selectionOperator.execute(population);
            parents.add(candidateFirstParent);
            S candidateSecondParent;
            candidateSecondParent = selectionOperator.execute(population);
            parents.add(candidateSecondParent);

            List<S> offspring = crossoverOperator.execute(parents);
            mutationOperator.execute(offspring.get(0));
            offSpringPopulation.add(offspring.get(0));
        }
        return offSpringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        return offspringPopulation;
    }

    private List<S> replacementExternal(List<S> population, List<S> offspring, List<S> externalOffSpring) {
        if (population == null || population.isEmpty()) {
            return offspring;
        }

        if (offspring == null || offspring.isEmpty()) {
            return population;
        }

        List<S> union = new ArrayList<>();
        union.addAll(population);
        union.addAll(offspring);
        union.addAll(externalOffSpring);

        union = union.stream()
                .filter(AlgorithmUtils.distinctByKey(S::variables))
                .collect(Collectors.toList());

        densityEstimator.compute(union);
        return environmentalSelection.execute(union);
    }

    @Override
    public List<S> getResult() {
        return archive;
    }

    @Override
    public String getName() {
        return "ESPEA2";
    }

    @Override
    public String getDescription() {
        return "Extreme Base Strength Pareto. Evolutionary Algorithm";
    }
}
