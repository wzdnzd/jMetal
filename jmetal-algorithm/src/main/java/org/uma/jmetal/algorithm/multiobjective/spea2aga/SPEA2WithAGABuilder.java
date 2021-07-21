package org.uma.jmetal.algorithm.multiobjective.spea2aga;

import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

/**
 * @author Juan J. Durillo
 */
public class SPEA2WithAGABuilder<S extends Solution<?>> extends SPEA2Builder<S> {
    /**
     * SPEA2Builder constructor
     *
     * @param problem
     * @param crossoverOperator
     * @param mutationOperator
     */
    public SPEA2WithAGABuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator) {
        super(problem, crossoverOperator, mutationOperator);
    }

    @Override
    public SPEA2WithAGA<S> build() {
        return new SPEA2WithAGA<>(problem, maxIterations, populationSize, crossoverOperator, mutationOperator, selectionOperator, evaluator, k);
    }
}
