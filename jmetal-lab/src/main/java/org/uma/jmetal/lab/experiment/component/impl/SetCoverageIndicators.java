/**
 * @Author : wzdnzd
 * @Time :  2021-08-05
 * @Project : jMetal
 */

package org.uma.jmetal.lab.experiment.component.impl;

import org.apache.commons.lang3.StringUtils;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.component.ExperimentComponent;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.qualityindicator.impl.SetCoverage;
import org.uma.jmetal.solution.AbstractSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.bounds.Bounds;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.legacy.front.Front;
import org.uma.jmetal.util.legacy.front.impl.ArrayFront;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;

public class SetCoverageIndicators implements ExperimentComponent {
    private final Experiment<?, ?> experiment;
    private final String algorithmName;

    public SetCoverageIndicators(Experiment<?, ?> experiment, String algorithmName) {
        this.experiment = experiment;
        this.algorithmName = algorithmName;
    }

    @Override
    public void run() throws IOException {
        if (experiment == null) {
            throw new JMetalException("Invalid arguments, experiment cannot be null");
        }

        String referenceFrontDirectory = experiment.getReferenceFrontDirectory();
        createOutputDirectory(referenceFrontDirectory);
        SetCoverage sc = new SetCoverage();
        List<? extends ExperimentProblem<?>> problemList = experiment.getProblemList();
        for (ExperimentProblem<?> problem : problemList) {
            Map<String, List<DummyDoubleSolution>> nonDominatedSolutions = getNonDominatedSolutions(problem);
            for (Map.Entry<String, List<DummyDoubleSolution>> entry : nonDominatedSolutions.entrySet()) {
                String path = referenceFrontDirectory +
                        "/" +
                        problem.getTag() +
                        "." +
                        entry.getKey() +
                        ".NonDominated.rf";
                writeReferenceFrontFile(path, entry.getValue());
            }

            List<DummyDoubleSolution> solutions = nonDominatedSolutions.getOrDefault(algorithmName, null);
            if (solutions == null || solutions.isEmpty()) {
                throw new JMetalException("Cannot found any solutions, please check algorithm name");
            }

            double[][] rf1 = getObjectives(solutions);
            for (Map.Entry<String, List<DummyDoubleSolution>> entry : nonDominatedSolutions.entrySet()) {
                String key = entry.getKey();
                if (key.equals(algorithmName)) {
                    continue;
                }

                double[][] rf2 = getObjectives(entry.getValue());
                double r1 = sc.compute(rf1, rf2);
                double r2 = sc.compute(rf2, rf1);

                List<String> contents = new ArrayList<>(2);
                String s1 = algorithmName + "," + key + "," + r1;
                String s2 = key + "," + algorithmName + "," + r2;

                contents.add(s1);
                contents.add(s2);
                String savedFile = referenceFrontDirectory +
                        "/" +
                        problem.getTag() +
                        "." +
                        algorithmName +
                        "." +
                        key +
                        ".Coverage.csv";
                printCoverageToFile(savedFile, contents);
            }

        }
    }

    private double[][] readReferenceFronts(String file) {
        if (StringUtils.isBlank(file)) {
            throw new JMetalException("Invalid argument, filename cannot be empty");
        }

        List<List<Double>> fronts = new ArrayList<>();

        try {
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);

            String line = br.readLine();
            while (line != null) {
                StringTokenizer st = new StringTokenizer(line, ",");
                List<Double> vec = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    vec.add(parseDouble(st.nextToken()));
                }

                fronts.add(vec);
                line = br.readLine();
            }
            br.close();

            if (fronts.isEmpty() || fronts.get(0).isEmpty()) {
                throw new JMetalException("loadReferenceFront: file is blank, file=" + file);
            }

            int m = fronts.size();
            int n = fronts.get(0).size();
            double[][] result = new double[m][n];
            for (int i = 0; i < m; i++) {
                List<Double> list = fronts.get(i);
                for (int j = 0; j < n; j++) {
                    result[i][j] = list.get(j);
                }
            }

            return result;
        } catch (Exception e) {
            throw new JMetalException("loadReferenceFront: failed when reading for file: " + file, e);
        }
    }

    private static void printCoverageToFile(String savedFile, List<String> contents) {
        DefaultFileOutputContext context = new DefaultFileOutputContext(savedFile);
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            if (contents != null && !contents.isEmpty()) {
                for (String content : contents) {
                    bufferedWriter.write(content);
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
        } catch (IOException e) {
            throw new JMetalException("Error printing coverage to file: ", e);
        }
    }

    private static String splitFileName(String path) {
        if (StringUtils.isBlank(path)) {
            return "";
        }

        int index = path.lastIndexOf("/");
        if (index == -1) {
            return path;
        }

        path = path.substring(index + 1);
        String[] words = path.split("\\.");
        if (words.length < 2) {
            throw new JMetalException("Extract Algorithm Name Fail");
        }

        return words[words.length - 2];
    }

    private static String splitDirector(String path) {
        if (StringUtils.isBlank(path)) {
            return "";
        }

        int index = path.indexOf(".");
        if (index == -1) {
            return path;
        }

        return path.substring(0, index + 1);
    }

    private Map<String, List<DummyDoubleSolution>> getNonDominatedSolutions(ExperimentProblem<?> problem) throws FileNotFoundException {
        Map<String, List<DummyDoubleSolution>> algorithmSolutionMap = new HashMap<>();
        ArrayList<? extends ExperimentAlgorithm<?, ?>> algorithms = experiment.getAlgorithmList().stream()
                .filter(s -> s.getProblemTag().equals(problem.getTag()))
                .collect(Collectors.toCollection(ArrayList::new));
        Map<String, ? extends List<? extends ExperimentAlgorithm<?, ?>>> algorithmMap = algorithms.stream()
                .collect(Collectors.groupingBy(ExperimentAlgorithm::getAlgorithmTag));
        for (Map.Entry<String, ? extends List<? extends ExperimentAlgorithm<?, ?>>> entry : algorithmMap.entrySet()) {
            String key = entry.getKey();
            NonDominatedSolutionListArchive<DummyDoubleSolution> nonDominatedSolutionArchive = new NonDominatedSolutionListArchive<>();
            List<? extends ExperimentAlgorithm<?, ?>> algoList = entry.getValue();
            for (ExperimentAlgorithm<?, ?> algorithm : algoList) {
                String problemDirectory =
                        experiment.getExperimentBaseDirectory()
                                + "/data/"
                                + algorithm.getAlgorithmTag()
                                + "/"
                                + problem.getTag();

                String frontFileName =
                        problemDirectory
                                + "/"
                                + experiment.getOutputParetoFrontFileName()
                                + algorithm.getRunId()
                                + ".csv";
                String paretoSetFileName =
                        problemDirectory
                                + "/"
                                + experiment.getOutputParetoSetFileName()
                                + algorithm.getRunId()
                                + ".csv";

                Front frontWithObjectiveValues = new ArrayFront(frontFileName, ",");
                Front frontWithVariableValues = new ArrayFront(paretoSetFileName, ",");
                List<DummyDoubleSolution> solutionList =
                        createSolutionListFrontFiles(frontWithVariableValues, frontWithObjectiveValues);
                for (DummyDoubleSolution solution : solutionList) {
                    nonDominatedSolutionArchive.add(solution);
                }
            }

            algorithmSolutionMap.put(key, nonDominatedSolutionArchive.getSolutionList());
        }

        return algorithmSolutionMap;
    }

    private double[][] getObjectives(List<DummyDoubleSolution> solutions) {
        if (solutions == null || solutions.isEmpty()) {
            throw new JMetalException("Invalid arguments");
        }

        int m = solutions.size();
        int n = solutions.get(0).objectives().length;
        double[][] objectives = new double[m][n];
        for (int i = 0; i < m; i++) {
            objectives[i] = solutions.get(i).objectives();
        }

        return objectives;
    }

    private File createOutputDirectory(String outputDirectoryName) {
        File outputDirectory;
        outputDirectory = new File(outputDirectoryName);
        if (!outputDirectory.exists()) {
            boolean result = new File(outputDirectoryName).mkdir();
            JMetalLogger.logger.info("Creating " + outputDirectoryName + ". Status = " + result);
        }

        return outputDirectory;
    }

    private List<DummyDoubleSolution> createSolutionListFrontFiles(Front frontWithVariableValues, Front frontWithObjectiveValues) {
        if (frontWithVariableValues.getNumberOfPoints() != frontWithObjectiveValues.getNumberOfPoints()) {
            throw new JMetalException(
                    "The number of solutions in the variable and objective fronts are not equal");
        } else if (frontWithObjectiveValues.getNumberOfPoints() == 0) {
            throw new JMetalException("The front of solutions is empty");
        }

        int numberOfVariables = frontWithVariableValues.getPointDimensions();
        int numberOfObjectives = frontWithObjectiveValues.getPointDimensions();

        List<DummyDoubleSolution> solutionList = new ArrayList<>();
        for (int i = 0; i < frontWithVariableValues.getNumberOfPoints(); i++) {
            DummyDoubleSolution solution = new DummyDoubleSolution(numberOfVariables, numberOfObjectives);
            for (int vars = 0; vars < numberOfVariables; vars++) {
                solution.variables().set(vars, frontWithVariableValues.getPoint(i).getValues()[vars]);
            }
            for (int objs = 0; objs < numberOfObjectives; objs++) {
                solution.objectives()[objs] = frontWithObjectiveValues.getPoint(i).getValues()[objs];
            }

            solutionList.add(solution);
        }

        return solutionList;
    }

    private void writeReferenceFrontFile(String path, List<DummyDoubleSolution> nonDominatedSolutions) {
        new SolutionListOutput(nonDominatedSolutions).printObjectivesToFile(path, ",");
    }

    private static class DummyDoubleSolution extends AbstractSolution<Double>
            implements DoubleSolution {

        public DummyDoubleSolution(int numberOfVariables, int numberOfObjectives) {
            super(numberOfVariables, numberOfObjectives);
        }

        @Override
        public Solution<Double> copy() {
            return null;
        }

        /**
         * @deprecated Use {@link #getBounds(int)}{@link Bounds#getLowerBound()
         * .getLowerBound()} instead.
         */
        @Deprecated
        @Override
        public Double getLowerBound(int index) {
            return null;
        }

        /**
         * @deprecated Use {@link #getBounds(int)}{@link Bounds#getUpperBound()
         * .getUpperBound()} instead.
         */
        @Deprecated
        @Override
        public Double getUpperBound(int index) {
            return null;
        }

        @Override
        public Bounds<Double> getBounds(int index) {
            return null;
        }
    }
}
