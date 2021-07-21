/**
 * @Author : wzdnzd
 * @Time :  2021-07-12
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.spea2aga;

import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2;
import org.uma.jmetal.algorithm.multiobjective.spea2aga.util.EnvironmentalSelectionWithAGA;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.List;

public class SPEA2WithAGA<S extends Solution<?>> extends SPEA2<S> {
    public SPEA2WithAGA(Problem<S> problem, int maxIterations, int populationSize, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator, SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator, int k) {
        super(problem, maxIterations, populationSize, crossoverOperator, mutationOperator, selectionOperator, new EnvironmentalSelectionWithAGA<>(populationSize, k), evaluator, k);
    }

    @Override
    public String getName() {
        return "SPEA2AGA";
    }

    @Override
    public String getDescription() {
        return "Strength Pareto. Evolutionary Algorithm With AGA";
    }
}
