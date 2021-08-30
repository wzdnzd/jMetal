package org.uma.jmetal.algorithm.multiobjective.espea2;

import org.uma.jmetal.algorithm.AlgorithmBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;

/**
 * @author Juan J. Durillo
 */
public class ESPEA2Builder<S extends DoubleSolution> implements AlgorithmBuilder<ESPEA2<S>> {
    /**
     * SPEA2Builder class
     */
    protected final Problem<S> problem;
    protected int maxIterations;
    protected int populationSize;
    protected CrossoverOperator<S> crossoverOperator;
    protected MutationOperator<S> mutationOperator;
    protected SelectionOperator<List<S>, S> selectionOperator;
    protected SolutionListEvaluator<S> evaluator;
    protected int k;
    protected int wolvesNum;

    /**
     * SPEA2Builder constructor
     */
    public ESPEA2Builder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                         MutationOperator<S> mutationOperator) {
        this.problem = problem;
        maxIterations = 250;
        populationSize = 100;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        selectionOperator = new BinaryTournamentSelection<>();
        evaluator = new SequentialSolutionListEvaluator<>();
        k = 1;
        wolvesNum = 10;
    }

    public ESPEA2Builder<S> setMaxIterations(int maxIterations) {
        if (maxIterations < 0) {
            throw new JMetalException("maxIterations is negative: " + maxIterations);
        }
        this.maxIterations = maxIterations;

        return this;
    }

    public ESPEA2Builder<S> setPopulationSize(int populationSize) {
        if (populationSize < 0) {
            throw new JMetalException("Population size is negative: " + populationSize);
        }

        this.populationSize = populationSize;

        return this;
    }

    public ESPEA2Builder<S> setWolvesNum(int wolvesNum) {
        if (wolvesNum < 0) {
            throw new JMetalException("wolvesNum is negative: " + wolvesNum);
        }

        this.wolvesNum = wolvesNum;

        return this;
    }

    public ESPEA2Builder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        if (selectionOperator == null) {
            throw new JMetalException("selectionOperator is null");
        }
        this.selectionOperator = selectionOperator;

        return this;
    }

    public ESPEA2Builder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        if (evaluator == null) {
            throw new JMetalException("evaluator is null");
        }
        this.evaluator = evaluator;

        return this;
    }

    public ESPEA2Builder<S> setK(int k) {
        this.k = k;

        return this;
    }

    public ESPEA2<S> build() {
        return new ESPEA2<>(problem, maxIterations, populationSize, crossoverOperator,
                mutationOperator, selectionOperator, evaluator, k, wolvesNum);
    }

    /* Getters */
    public Problem<S> getProblem() {
        return problem;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public CrossoverOperator<S> getCrossoverOperator() {
        return crossoverOperator;
    }

    public MutationOperator<S> getMutationOperator() {
        return mutationOperator;
    }

    public SelectionOperator<List<S>, S> getSelectionOperator() {
        return selectionOperator;
    }

    public SolutionListEvaluator<S> getSolutionListEvaluator() {
        return evaluator;
    }
}
