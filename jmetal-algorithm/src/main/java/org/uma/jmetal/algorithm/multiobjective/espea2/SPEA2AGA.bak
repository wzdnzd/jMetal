package org.uma.jmetal.algorithm.multiobjective.spea2aga;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.CommonUtils;
import org.uma.jmetal.algorithm.multiobjective.spea2.util.EnvironmentalSelection;
import org.uma.jmetal.algorithm.multiobjective.spea2aga.leader.BestLeaderSelector;
import org.uma.jmetal.algorithm.multiobjective.spea2aga.leader.LeaderSelector;
import org.uma.jmetal.algorithm.multiobjective.spea2aga.util.AGAUtils;
import org.uma.jmetal.algorithm.multiobjective.spea2aga.util.ExternalEnvironmentalSelectionAGA;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.densityestimator.impl.StrenghtRawFitnessDensityEstimator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Juan J. Durillo
 **/
public class SPEA2AGA<S extends DoubleSolution> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final int maxIterations;
    protected final SolutionListEvaluator<S> evaluator;
    protected int iterations;
    protected List<S> archive;
    protected final StrenghtRawFitnessDensityEstimator<S> densityEstimator = new StrenghtRawFitnessDensityEstimator<S>(1);
    protected final EnvironmentalSelection<S> environmentalSelection;
    protected final EnvironmentalSelection<S> externalEnvironmentalSelection;
    protected final int k;
    protected List<S> externalArchive;
    private final LeaderSelector<S> leaderSelector = new BestLeaderSelector<>(3);
    private final static double[] LOWER_BOUND = CommonUtils.zeros(150);
    private final static double[] UPPER_BOUND = CommonUtils.ones(150);

    public SPEA2AGA(Problem<S> problem, int maxIterations, int populationSize,
                    CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                    SelectionOperator<List<S>, S> selectionOperator, EnvironmentalSelection<S> environmentalSelection,
                    SolutionListEvaluator<S> evaluator, int k) {
        super(problem);
        this.maxIterations = maxIterations;
        this.setMaxPopulationSize(populationSize);

        this.k = k;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.environmentalSelection = environmentalSelection;
        this.externalEnvironmentalSelection = new ExternalEnvironmentalSelectionAGA<>(populationSize, k);

        this.archive = new ArrayList<>(populationSize);
        this.externalArchive = new ArrayList<>(populationSize);

        this.evaluator = evaluator;
    }

    public SPEA2AGA(Problem<S> problem, int maxIterations, int populationSize,
                    CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                    SelectionOperator<List<S>, S> selectionOperator,
                    SolutionListEvaluator<S> evaluator, int k) {
        super(problem);
        this.maxIterations = maxIterations;
        this.setMaxPopulationSize(populationSize);

        this.k = k;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.environmentalSelection = new EnvironmentalSelection<>(populationSize, k);
        this.externalEnvironmentalSelection = new ExternalEnvironmentalSelectionAGA<>(populationSize, k);

        this.archive = new ArrayList<>(populationSize);
        this.externalArchive = new ArrayList<>(populationSize);

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
    protected List<S> replacement(List<S> population,
                                  List<S> offspringPopulation) {
        return offspringPopulation;
    }

    @Override
    public void run() {
        List<S> offspringPopulation;
        List<S> matingPopulation;
        List<S> externalMatingPopulation;

        population = createInitialPopulation();
        population = evaluatePopulation(population);

        initProgress();
        while (!isStoppingConditionReached()) {
            double a = 2 - (iterations + 1) * (2.0 / maxIterations);
            //  外部归档集更新
            externalMatingPopulation = selectionExternal(population);

            matingPopulation = selection(population);
            offspringPopulation = reproduction(matingPopulation);
            offspringPopulation = evaluatePopulation(offspringPopulation);
            population = replacement(population, offspringPopulation);

            // 外部归档集交叉变异
            List<S> offspringExternal = reproductionExternal(externalMatingPopulation, a);
            offspringExternal = evaluatePopulation(offspringExternal);
            population = replacementExternal(population, offspringExternal);

            updateProgress();
        }
    }

    private List<S> replacementExternal(List<S> population, List<S> offspring) {
        if (population == null || population.isEmpty()) {
            return offspring;
        }

        if (offspring == null || offspring.isEmpty()) {
            return population;
        }

        population.addAll(CommonUtils.deepCopy(offspring));
        population = population.stream().filter(distinctByKey(S::variables)).collect(Collectors.toList());
        densityEstimator.compute(population);
        population = environmentalSelection.execute(population);

        return population;
    }

    private List<S> reproductionExternal(List<S> population, double a) {
        population = AGAUtils.determineDomination(population);
        List<S> solutions = CommonUtils.deepCopy(AGAUtils.getNonDominatedParticles(population));

        S deltaWolf = leaderSelector.select(solutions);
        S betaWolf = leaderSelector.select(solutions);
        S alphaWolf = leaderSelector.select(solutions);

        for (S s : population) {
            //根据选择的三只狼更新其他狼的位置
            int posDim = s.variables().size();

            List<Double> p1 = AGAUtils.calculatePosition(a, deltaWolf, s, posDim, true);
            List<Double> p2 = AGAUtils.calculatePosition(a, betaWolf, s, posDim, false);
            List<Double> p3 = AGAUtils.calculatePosition(a, alphaWolf, s, posDim, false);

            //判断位置是否超出边界
            List<Double> variables = CommonUtils.boundCheck(CommonUtils.mean(p1, p2, p3), LOWER_BOUND, UPPER_BOUND);
            for (int j = 0; j < variables.size(); j++) {
                s.variables().set(j, variables.get(j));
            }

            problem.evaluate(s);
        }

        population = AGAUtils.determineDomination(population);
        List<S> nonDominatedParticles = AGAUtils.getNonDominatedParticles(population);

        solutions.addAll(CommonUtils.deepCopy(nonDominatedParticles));
        solutions = AGAUtils.getNonDominatedParticles(AGAUtils.determineDomination(solutions));

        // 根据position去重
        return solutions.stream().filter(distinctByKey(S::variables)).collect(Collectors.toList());
    }

    private List<S> selectionExternal(List<S> population) {
        if (population == null || population.isEmpty()) {
            return population;
        }

        List<S> sources = new ArrayList<>(population.size() + externalArchive.size());
        sources.addAll(population);
        sources.addAll(externalArchive);
        sources = sources.stream().filter(distinctByKey(S::variables)).collect(Collectors.toList());
        externalArchive = externalEnvironmentalSelection.execute(sources);
        return externalArchive;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public List<S> getResult() {
        return archive;
    }

    @Override
    public String getName() {
        return "SPEA2AGA";
    }

    @Override
    public String getDescription() {
        return "Improved Strength Pareto. Evolutionary Algorithm";
    }
}
