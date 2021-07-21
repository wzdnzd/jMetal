//

//

package org.uma.jmetal.lab.experiment.studies;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.gde3.GDE3Builder;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.multiobjective.mochc.MOCHCBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.smpso.SMPSOBuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.*;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.crossover.impl.HUXCrossover;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.DifferentialEvolutionSelection;
import org.uma.jmetal.operator.selection.impl.RandomSelection;
import org.uma.jmetal.operator.selection.impl.RankingAndCrowdingSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.binaryproblem.BinaryProblem;
import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.problem.multiobjective.*;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT5;
import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Example of experimental study based on solving two binary problems with four algorithms: NSGAII,
 * SPEA2, MOCell, and MOCHC
 *
 * This org.uma.jmetal.experiment assumes that the reference Pareto front are not known, so the must be produced.
 *
 * Six quality indicators are used for performance assessment.
 *
 * The steps to carry out the org.uma.jmetal.experiment are: 1. Configure the org.uma.jmetal.experiment 2. Execute the algorithms
 * 3. Generate the reference Pareto fronts 4. Compute que quality indicators 5. Generate Latex
 * tables reporting means and medians 6. Generate Latex tables with the result of applying the
 * Wilcoxon Rank Sum Test 7. Generate Latex tables with the ranking obtained by applying the
 * Friedman test 8. Generate R scripts to obtain boxplots
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class BinaryProblemsStudy {

  private static final int INDEPENDENT_RUNS = 25;

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      throw new JMetalException("Needed arguments: experimentBaseDirectory");
    }
    String experimentBaseDirectory = args[0];

    List<ExperimentProblem<BinarySolution>> problemList = new ArrayList<>();
    problemList.add(new ExperimentProblem<>(new ZDT5()));
    problemList.add(new ExperimentProblem<>(new OneZeroMax(512)));

    List<ExperimentAlgorithm<BinarySolution, List<BinarySolution>>> algorithmList =
        configureAlgorithmList(problemList);

    Experiment<BinarySolution, List<BinarySolution>> experiment;
    experiment = new ExperimentBuilder<BinarySolution, List<BinarySolution>>("BinaryProblemsStudy")
        .setAlgorithmList(algorithmList)
        .setProblemList(problemList)
        .setExperimentBaseDirectory(experimentBaseDirectory)
        .setOutputParetoFrontFileName("FUN")
        .setOutputParetoSetFileName("VAR")
        .setReferenceFrontDirectory(experimentBaseDirectory + "/BinaryProblemsStudy/referenceFronts")
        .setIndicatorList(Arrays.asList(
            new Epsilon(),
            new Spread(),
            new GenerationalDistance(),
            new PISAHypervolume(),
                new NormalizedHypervolume(),
                new InvertedGenerationalDistance(),
            new InvertedGenerationalDistancePlus())
        )
        .setIndependentRuns(INDEPENDENT_RUNS)
        .setNumberOfCores(8)
        .build();

    new ExecuteAlgorithms<>(experiment).run();
    new GenerateReferenceParetoFront(experiment).run();
    new ComputeQualityIndicators<>(experiment).run();
    new GenerateLatexTablesWithStatistics(experiment).run();
    new GenerateWilcoxonTestTablesWithR<>(experiment).run();
    new GenerateFriedmanTestTables<>(experiment).run();
    new GenerateBoxplotsWithR<>(experiment).setRows(1).setColumns(2).setDisplayNotch().run();
  }

  /**
   * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
   * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}.
   */

  static List<ExperimentAlgorithm<BinarySolution, List<BinarySolution>>> configureAlgorithmList(
      List<ExperimentProblem<BinarySolution>> problemList) {
    List<ExperimentAlgorithm<BinarySolution, List<BinarySolution>>> algorithms = new ArrayList<>();
    for (int run = 0; run < INDEPENDENT_RUNS; run++) {

      for (ExperimentProblem<BinarySolution> problem : problemList) {
        Algorithm<List<BinarySolution>> algorithm = new NSGAIIBuilder<>(
                problem.getProblem(),
                new SinglePointCrossover(1.0),
                new BitFlipMutation(
                        1.0 / ((BinaryProblem) problem.getProblem()).getBitsFromVariable(0)),
                100)
                .setMaxEvaluations(25000)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

      for (ExperimentProblem<BinarySolution> problem : problemList) {
        Algorithm<List<BinarySolution>> algorithm = new SPEA2Builder<>(
                problem.getProblem(),
                new SinglePointCrossover(1.0),
                new BitFlipMutation(
                        1.0 / ((BinaryProblem) problem.getProblem()).getBitsFromVariable(0)))
                .setMaxIterations(250)
                .setPopulationSize(100)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

      for (ExperimentProblem<BinarySolution> problem : problemList) {
        Algorithm<List<BinarySolution>> algorithm = new MOCellBuilder<>(
                problem.getProblem(),
                new SinglePointCrossover(1.0),
                new BitFlipMutation(
                        1.0 / ((BinaryProblem) problem.getProblem()).getBitsFromVariable(0)))
                .setMaxEvaluations(25000)
                .setPopulationSize(100)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

      for (ExperimentProblem<BinarySolution> problem : problemList) {
        CrossoverOperator<BinarySolution> crossoverOperator;
        MutationOperator<BinarySolution> mutationOperator;
        SelectionOperator<List<BinarySolution>, BinarySolution> parentsSelection;
        SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection;

        crossoverOperator = new HUXCrossover(1.0);
        parentsSelection = new RandomSelection<>();
        newGenerationSelection = new RankingAndCrowdingSelection<>(100);
        mutationOperator = new BitFlipMutation(0.35);
        Algorithm<List<BinarySolution>> algorithm = new MOCHCBuilder(
                (BinaryProblem) problem.getProblem())
                .setInitialConvergenceCount(0.25)
                .setConvergenceValue(3)
                .setPreservedPopulation(0.05)
                .setPopulationSize(100)
                .setMaxEvaluations(25000)
                .setCrossover(crossoverOperator)
                .setNewGenerationSelection(newGenerationSelection)
                .setCataclysmicMutation(mutationOperator)
                .setParentSelection(parentsSelection)
                .setEvaluator(new SequentialSolutionListEvaluator<>())
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }
    }
    return algorithms;
  }

    /**
     * Example of experimental study based on solving the unconstrained problems included in jMetal.
     * <p>
     * This org.uma.jmetal.experiment assumes that the reference Pareto front are known and that, given a problem named
     * P, there is a corresponding file called P.pf containing its corresponding Pareto front. If this
     * is not the case, please refer to class {@link DTLZStudy} to see an example of how to explicitly
     * indicate the name of those files.
     * <p>
     * Six quality indicators are used for performance assessment.
     * <p>
     * The steps to carry out the org.uma.jmetal.experiment are:
     * 1. Configure the org.uma.jmetal.experiment
     * 2. Execute the algorithms
     * 3. Generate the reference Pareto fronts
     * 4. Compute the quality indicators
     * 5. Generate Latex tables reporting means and medians
     * 6. Generate Latex tables with the result of applying the Wilcoxon Rank Sum Test
     * 7. Generate Latex tables with the ranking obtained by applying the Friedman test
     * 8. Generate R scripts to obtain boxplots
     *
     * @author Antonio J. Nebro <antonio@lcc.uma.es>
     */
    public static class ConstraintProblemsStudy {
      private static final int INDEPENDENT_RUNS = 25;

      public static void main(String[] args) throws IOException {
        if (args.length != 1) {
          throw new JMetalException("Needed arguments: experimentBaseDirectory");
        }
        String experimentBaseDirectory = args[0];

        List<ExperimentProblem<DoubleSolution>> problemList = new ArrayList<>();
        problemList.add(new ExperimentProblem<>(new Binh2()));
        problemList.add(new ExperimentProblem<>(new ConstrEx()));
        problemList.add(new ExperimentProblem<>(new Golinski()));
        problemList.add(new ExperimentProblem<>(new Srinivas()));
        problemList.add(new ExperimentProblem<>(new Tanaka()));
        problemList.add(new ExperimentProblem<>(new Water()));

        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList =
                configureAlgorithmList(problemList);

        Experiment<DoubleSolution, List<DoubleSolution>> experiment =
                new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>("ConstrainedProblemsStudy")
                        .setAlgorithmList(algorithmList)
                        .setProblemList(problemList)
                        .setExperimentBaseDirectory(experimentBaseDirectory)
                        .setOutputParetoFrontFileName("FUN")
                        .setOutputParetoSetFileName("VAR")
                        .setReferenceFrontDirectory(experimentBaseDirectory + "/ConstrainedProblemsStudy/referenceFronts")
                        .setIndicatorList(Arrays.asList(
                                new Epsilon(),
                                new PISAHypervolume(),
                                new NormalizedHypervolume(),
                                new InvertedGenerationalDistancePlus()))
                        .setIndependentRuns(INDEPENDENT_RUNS)
                        .setNumberOfCores(8)
                        .build();

        new ExecuteAlgorithms<>(experiment).run();
        new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();
        new ComputeQualityIndicators<>(experiment).run();
        new GenerateLatexTablesWithStatistics(experiment).run();
        new GenerateWilcoxonTestTablesWithR<>(experiment).run();
        new GenerateFriedmanTestTables<>(experiment).run();
        new GenerateBoxplotsWithR<>(experiment).setRows(3).setColumns(3).run();
      }

      /**
       * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
       * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}. The {@link
       * ExperimentAlgorithm} has an optional tag component, that can be set as it is shown in this example,
       * where four variants of a same algorithm are defined.
       */
      static List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> configureAlgorithmList(
              List<ExperimentProblem<DoubleSolution>> problemList) {
        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();
        for (int run = 0; run < INDEPENDENT_RUNS; run++) {

          for (int i = 0; i < problemList.size(); i++) {
            Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<>(
                    problemList.get(i).getProblem(),
                    new SBXCrossover(1.0, 20),
                    new PolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 20.0),
                    100)
                    .setMaxEvaluations(25000)
                    .build();
            algorithms.add(new ExperimentAlgorithm<>(algorithm, problemList.get(i), run));
          }

          for (int i = 0; i < problemList.size(); i++) {
            Algorithm<List<DoubleSolution>> algorithm = new SPEA2Builder<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    new SBXCrossover(1.0, 10.0),
                    new PolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 20.0))
                    .build();
            algorithms.add(new ExperimentAlgorithm<>(algorithm, problemList.get(i), run));
          }

          for (int i = 0; i < problemList.size(); i++) {
            double mutationProbability = 1.0 / problemList.get(i).getProblem().getNumberOfVariables();
            double mutationDistributionIndex = 20.0;
            Algorithm<List<DoubleSolution>> algorithm = new SMPSOBuilder((DoubleProblem) problemList.get(i).getProblem(),
                    new CrowdingDistanceArchive<DoubleSolution>(100))
                    .setMutation(new PolynomialMutation(mutationProbability, mutationDistributionIndex))
                    .setMaxIterations(250)
                    .setSwarmSize(100)
                    .setSolutionListEvaluator(new SequentialSolutionListEvaluator<DoubleSolution>())
                    .build();
            algorithms.add(new ExperimentAlgorithm<>(algorithm, problemList.get(i), run));
          }
          for (int i = 0; i < problemList.size(); i++) {
            double cr = 0.5;
            double f = 0.5;

            Algorithm<List<DoubleSolution>> algorithm = new GDE3Builder((DoubleProblem) problemList.get(i).getProblem())
                    .setCrossover(new DifferentialEvolutionCrossover(cr, f, DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN))
                    .setSelection(new DifferentialEvolutionSelection())
                    .setMaxEvaluations(25000)
                    .setPopulationSize(100)
                    .setSolutionSetEvaluator(new SequentialSolutionListEvaluator<>())
                    .build();
            algorithms.add(new ExperimentAlgorithm<>(algorithm, problemList.get(i), run));
          }

          for (int i = 0; i < problemList.size(); i++) {
            Algorithm<List<DoubleSolution>> algorithm = new MOCellBuilder<DoubleSolution>(
                    (DoubleProblem) problemList.get(i).getProblem(),
                    new SBXCrossover(1.0, 20.0),
                    new PolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 20.0))
                    .setSelectionOperator(new BinaryTournamentSelection<>())
                    .setMaxEvaluations(25000)
                    .setPopulationSize(100)
                    .setArchive(new CrowdingDistanceArchive<DoubleSolution>(100))
                    .build();
            algorithms.add(new ExperimentAlgorithm<>(algorithm, problemList.get(i), run));
          }
        }
        return algorithms;
      }

    }
}
