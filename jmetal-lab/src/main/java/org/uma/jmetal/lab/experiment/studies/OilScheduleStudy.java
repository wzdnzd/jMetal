package org.uma.jmetal.lab.experiment.studies;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEABuilder;
import org.uma.jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.smpso.SMPSOBuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.algorithm.multiobjective.spea2aga.SPEA2WithAGABuilder;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.*;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.lab.visualization.StudyVisualizer;
import org.uma.jmetal.operator.crossover.impl.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.problem.multiobjective.OilSchedule.OilScheduleProblem;
import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OilScheduleStudy {
    private static final int INDEPENDENT_RUNS = 30;
    private static final int[] PopsizeList = new int[]{100, 150,200};
    private static final int[] MaxEvalationList = new int[]{300, 500,1000};


    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new JMetalException("Missing argument: experimentBaseDirectory");
        }
        String experimentBaseDirectory = args[0];

        List<ExperimentProblem<DoubleSolution>> problemList = List.of(
                new ExperimentProblem<>(new OilScheduleProblem())
        );

        for (int i = 0; i < PopsizeList.length; i++) {
            for (int j = 0; j < MaxEvalationList.length; j++) {

                List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList =
                        configureAlgorithmList(problemList, i, j);

                ExperimentBuilder<DoubleSolution, List<DoubleSolution>> oilScheduleStudy =
                        new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>("OilScheduleStudy");

                oilScheduleStudy.setAlgorithmList(algorithmList);
                oilScheduleStudy.setProblemList(problemList);
                oilScheduleStudy.setExperimentBaseDirectory(experimentBaseDirectory);
                oilScheduleStudy.setOutputParetoFrontFileName("FUN");
                oilScheduleStudy.setOutputParetoSetFileName("VAR");
                oilScheduleStudy.setReferenceFrontDirectory(experimentBaseDirectory + "/OilScheduleStudy/referenceFronts");
                oilScheduleStudy.setIndicatorList(Arrays.asList(
                        new GenerationalDistance(),
                        new PISAHypervolume(),
                        new NormalizedHypervolume(),
                        new InvertedGenerationalDistance(),
                        new InvertedGenerationalDistancePlus()));
                oilScheduleStudy.setIndependentRuns(INDEPENDENT_RUNS);
                oilScheduleStudy.setNumberOfCores(8);
                Experiment<DoubleSolution, List<DoubleSolution>> experiment = oilScheduleStudy.build();

                new ExecuteAlgorithms<>(experiment).run();
                new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();
                new ComputeQualityIndicators<>(experiment).run();
                new GenerateLatexTablesWithStatistics(experiment).run();
                new GenerateFriedmanHolmTestTables<>(experiment).run();
                new GenerateWilcoxonTestTablesWithR<>(experiment).run();
                new GenerateBoxplotsWithR<>(experiment).setRows(1).setColumns(1).run();
                new GenerateHtmlPages<>(experiment, StudyVisualizer.TYPE_OF_FRONT_TO_SHOW.MEDIAN).run();

                File Oil = new File("dataTemp/OilScheduleStudy");
                File OilNext = new File("dataTemp/OilScheduleStudy" + "_Popsize" + PopsizeList[i] + "&Iteration_" + MaxEvalationList[j]);

                Oil.renameTo(OilNext);

            }
        }

//        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList=
//                configureAlgorithmList(problemList,i,j);
//
//        ExperimentBuilder<DoubleSolution, List<DoubleSolution>> oilScheduleStudy =
//                new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>("OilScheduleStudy");
//
//        oilScheduleStudy.setAlgorithmList(algorithmList);
//        oilScheduleStudy.setProblemList(problemList);
//        oilScheduleStudy.setExperimentBaseDirectory(experimentBaseDirectory);
//        oilScheduleStudy.setOutputParetoFrontFileName("FUN");
//        oilScheduleStudy.setOutputParetoSetFileName("VAR");
//        oilScheduleStudy.setReferenceFrontDirectory(experimentBaseDirectory + "\\OilScheduleStudy\\referenceFronts");
//        oilScheduleStudy.setIndicatorList(Arrays.asList(
//                new GenerationalDistance(),
//                new PISAHypervolume(),
//                new NormalizedHypervolume(),
//                new InvertedGenerationalDistance(),
//                new InvertedGenerationalDistancePlus())) ;
//        oilScheduleStudy.setIndependentRuns(INDEPENDENT_RUNS);
//        oilScheduleStudy.setNumberOfCores(8);
//        Experiment<DoubleSolution, List<DoubleSolution>> experiment = oilScheduleStudy.build();
//
//        new ExecuteAlgorithms<>(experiment).run();
//        new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();
//        new ComputeQualityIndicators<>(experiment).run();
//        new GenerateLatexTablesWithStatistics(experiment).run();
//        new GenerateFriedmanHolmTestTables<>(experiment).run();
//        new GenerateWilcoxonTestTablesWithR<>(experiment).run();
//        new GenerateBoxplotsWithR<>(experiment).setRows(1).setColumns(1).run();
//
//        File Oil=new File("dataTemp/ZDTStudy");
//        File OilNext=new File("dataTemp/ZDTStudy2");
//
//        Oil.renameTo(OilNext);


    }


    static List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> configureAlgorithmList(
            List<ExperimentProblem<DoubleSolution>> problemList, int i, int j) {
        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();

        for (int run = 0; run < INDEPENDENT_RUNS; run++) {

            //IBEA算法
//            for (var experimentProblem : problemList) {
//                Algorithm<List<DoubleSolution>> algorithm = new IBEABuilder(experimentProblem.getProblem())
//                        .setPopulationSize(PopsizeList[i])
//                        .setMaxEvaluations(MaxEvalationList[j] * PopsizeList[i])
//                        .setArchiveSize(100)
//                        .setCrossover(new SBXCrossover(1.0, 20))
//                        .setMutation(new PolynomialMutation(1.0 / experimentProblem.getProblem().getNumberOfVariables(), 20.0))
//                        .build();
//                algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
//            }


            for (var experimentProblem : problemList) {
                Algorithm<List<DoubleSolution>> algorithm = new SPEA2WithAGABuilder<>(
                        experimentProblem.getProblem(),
                        new SBXCrossover(1.0, 20),
                        new PolynomialMutation(1.0 / experimentProblem.getProblem().getNumberOfVariables(), 20.0))
                        .setPopulationSize(PopsizeList[i])
                        .setMaxIterations(MaxEvalationList[j])
                        .setK(1)
                        .build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
            }

            //SPEA2算法

            for (var experimentProblem : problemList) {
                Algorithm<List<DoubleSolution>> algorithm = new SPEA2Builder<>(
                        experimentProblem.getProblem(),
                        new SBXCrossover(1.0, 20),
                        new PolynomialMutation(1.0 / experimentProblem.getProblem().getNumberOfVariables(), 20.0))
                        .setPopulationSize(PopsizeList[i])
                        .setMaxIterations(MaxEvalationList[j])
                        .setK(1)
                        .build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
            }

            //SMPSO算法

//            for (var experimentProblem : problemList) {
//                double mutationProbability = 1.0 / experimentProblem.getProblem().getNumberOfVariables();
//                double mutationDistributionIndex = 20.0;
//                Algorithm<List<DoubleSolution>> algorithm = new SMPSOBuilder(
//                        (DoubleProblem) experimentProblem.getProblem(),
//                        new CrowdingDistanceArchive<DoubleSolution>(100))
//                        .setMutation(new PolynomialMutation(mutationProbability, mutationDistributionIndex))
//                        .setMaxIterations(MaxEvalationList[j])
//                        .setSwarmSize(PopsizeList[i])
//                        .setSolutionListEvaluator(new SequentialSolutionListEvaluator<>())
//                        .build();
//                algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
//            }

            //NSGAII算法
//            for (var experimentProblem : problemList) {
//                Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<DoubleSolution>(
//                        experimentProblem.getProblem(),
//                        new SBXCrossover(1.0, 20.0),
//                        new PolynomialMutation(1.0 / experimentProblem.getProblem().getNumberOfVariables(),
//                                20.0),
//                        PopsizeList[i])
//                        .setMaxEvaluations(MaxEvalationList[j] * PopsizeList[i])
//                        .build();
//                algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
//            }

            //MOEAD算法
//            for (var experimentProblem : problemList) {
//                Algorithm<List<DoubleSolution>> algorithm = new MOEADBuilder(experimentProblem.getProblem(), MOEADBuilder.Variant.MOEAD)
//                        //.setCrossover(new SBXCrossover(1.0,20))
//                        .setCrossover(new DifferentialEvolutionCrossover(1.0, 0.5, DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN))
//                        .setMutation(new PolynomialMutation(1.0 / experimentProblem.getProblem().getNumberOfVariables(),
//                                20.0))
//                        .setMaxEvaluations(PopsizeList[i] * MaxEvalationList[j])
//                        .setPopulationSize(PopsizeList[i])
//                        .setResultPopulationSize(100)
//                        .setNeighborhoodSelectionProbability(0.9)
//                        .setMaximumNumberOfReplacedSolutions(2)
//                        .setNeighborSize(20)
//                        .setDataDirectory("resources/weightVectorFiles/moead")
//                        .setFunctionType(AbstractMOEAD.FunctionType.TCHE)
//                        .build();
//
//                algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
//            }
            //NSGAII算法
//            for (var experimentProblem : problemList) {
//                Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<DoubleSolution>(
//                        experimentProblem.getProblem(),
//                        new SBXCrossover(1.0, 20.0),
//                        new PolynomialMutation(1.0 / experimentProblem.getProblem().getNumberOfVariables(),
//                                20.0),
//                        PopsizeList[i])
//                        .setMaxEvaluations(MaxEvalationList[j] * PopsizeList[i])
//                        .build();
//                algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
//            }
            //NSGAIII算法
//            for (var experimentProblem : problemList) {
//                int nDiv = 0;
//                switch (PopsizeList[i]) {
//                    case 50:
//                        nDiv = 3;
//                        break;
//                    case 100:
//                        nDiv = 4;
//                        break;
//                    case 150:
//                        nDiv = 5;
//                        break;
//                    case 200:
//                        nDiv = 6;
//                        break;
//                }
//                Algorithm<List<DoubleSolution>> algorithm = new NSGAIIIBuilder<>(experimentProblem.getProblem())
//                        .setPopulationSize(PopsizeList[i])
//                        .setCrossoverOperator(new SBXCrossover(1.0, 20))
//                        .setMutationOperator(new PolynomialMutation(1.0 / experimentProblem.getProblem().getNumberOfVariables(), 20.0))
//                        .setMaxIterations(MaxEvalationList[j])
//                        .setNumberOfDivisions(nDiv)
//                        .setSelectionOperator(new BinaryTournamentSelection<>())
//                        .build();
//                algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
//            }
        }

        return algorithms;
    }
}
