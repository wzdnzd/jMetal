package org.uma.jmetal.algorithm.multiobjective.espea2.util;

import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.DistanceUtils;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUtils implements Serializable {
    @SuppressWarnings("unchecked")
    public static <T> List<T> deepCopy(List<T> src) {
        List<T> dest = null;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);
            out.close();

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            dest = (List<T>) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dest;
    }

    public static double getRand(double lower, double upper) {
        double value = Math.random();
        if (Math.random() > 0.5) {
            value = -value;
        }

        return Math.min(Math.max(value, lower), upper);
    }

    public static <S extends Solution<?>> int dominated(S s1, S s2) {
        if (s1 == null || s2 == null) {
            throw new JMetalException("非法参数，解不能为空");
        }

        return dominated(s1.objectives(), s2.objectives());
    }

    public static int dominated(double[] arr1, double[] arr2) {
        if (arr1 == null || arr1.length <= 0 || arr2 == null || arr1.length != arr2.length) {
            throw new JMetalException("维度不匹配，无法判断支配关系");
        }

        int bestIsOne = 0;
        int bestIsTwo = 0;
        for (int i = 0; i < arr1.length; i++) {
            double v1 = arr1[i];
            double v2 = arr2[i];
            if (v1 != v2) {
                if (v1 < v2) {
                    bestIsOne = 1;
                } else {
                    bestIsTwo = 1;
                }
            }
        }

        return Integer.compare(bestIsTwo, bestIsOne);
    }

    public static boolean betterThan(double[] arr1, double[] arr2) {
        if (arr1 == null || arr1.length <= 0 || arr2 == null || arr1.length != arr2.length) {
            throw new JMetalException("维度不匹配，无法判断优劣");
        }

        double r1 = 0.0;
        double r2 = 0.0;
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] < arr2[i]) {
                r1 += (arr2[i] - arr1[i]) / arr2[i];
            } else {
                r2 += (arr1[i] - arr2[i]) / arr1[i];
            }
        }

        return r1 > r2;
    }

    public static List<Double> boundCheck(List<Double> pos, double[] lowers, double[] uppers) {
        List<Double> bound = new ArrayList<>();
        for (int i = 0; i < pos.size(); i++) {
            double t = Math.min(Math.max(pos.get(i), lowers[i]), uppers[i]);
            bound.add(t);
        }
        return bound;
    }

    public static List<Double> mean(List<Double> l1, List<Double> l2, List<Double> l3) {
        if (l1 == null || l2 == null || l3 == null || l1.size() != l2.size() || l2.size() != l3.size()) {
            throw new JMetalException("Invalid arguments, position dim not equals");
        }

        List<Double> pos = new ArrayList<>();
        for (int i = 0; i < l1.size(); i++) {
            pos.add(l1.get(i) + l2.get(i) + l3.get(i));
        }

        return pos.stream().map(e -> e / 3).collect(Collectors.toList());
    }

    public static List<Double> nearPrey(List<Double> pos, double weight, List<Double> other) {
        if (pos == null || other == null || pos.size() != other.size()) {
            return pos;
        }

        List<Double> list = new ArrayList<>();
        for (int i = 0; i < pos.size(); i++) {
            double p = pos.get(i) - weight * Math.abs(other.get(i));
            list.add(p);
        }
        return list;
    }

    public static List<Double> nearPrey(List<Double> pos, List<Double> vec, List<Double> other) {
        if (pos == null || other == null || pos.size() != other.size()) {
            return pos;
        }

        List<Double> list = new ArrayList<>();
        for (int i = 0; i < pos.size(); i++) {
            double p = pos.get(i) - vec.get(i) * Math.abs(other.get(i));
            list.add(p);
        }
        return list;
    }

    public static List<Double> updateD(List<Double> vec, List<Double> position, List<Double> otherPosition) {
        if (vec == null || position == null || otherPosition == null
                || vec.size() != position.size() || vec.size() != otherPosition.size()) {
            throw new JMetalException("Invalid arguments, size not equals");
        }

        List<Double> list = new ArrayList<>();
        for (int i = 0; i < position.size(); i++) {
            double p = Math.abs((position.get(i) * vec.get(i)) - otherPosition.get(i));
            list.add(p);
        }
        return list;
    }

    public static List<Double> updateC(int dim) {
        List<Double> rand = new ArrayList<>();
        for (int i = 0; i < dim; i++) {
            rand.add(Math.random() * 2);
        }
        return rand;
    }

    /**
     * 轮盘赌选择
     *
     * @param p 存储个体的选择概率
     * @return 对应的下标
     */
    public static int rouletteWheelSelect(List<Double> p) {
        double r = Math.random();
        double sum = 0;
        int index = 0;
        List<Double> cum = new ArrayList<>();
        for (Double v : p) {
            sum = sum + v;
            cum.add(sum);
        }

        for (int j = 0; j < cum.size(); j++) {
            if (r <= cum.get(j)) {
                index = j;
                break;
            }
        }

        return index;
    }

    public static <S extends Solution<?>> double[][] cosineDistanceMatrix(List<S> solutionSet) {
        double[][] distance = new double[solutionSet.size()][solutionSet.size()];
        for (int i = 0; i < solutionSet.size(); i++) {
            distance[i][i] = 0.0;
            for (int j = i + 1; j < solutionSet.size(); j++) {
                double v = Math.acos(DistanceUtils.cosineDistance(solutionSet.get(i), solutionSet.get(j)));
                distance[i][j] = v;
                distance[j][i] = v;
            }
        }

        return distance;
    }

    public static void computeFitness(List<WolfSolution> solutionList) {
        double[][] distance = cosineDistanceMatrix(solutionList);
        double[] strength = new double[solutionList.size()];
        double[] rawFitness = new double[solutionList.size()];
        double kDistance;

        // strength(i) = |{j | j <- SolutionSet and i dominate j}|
        for (int i = 0; i < solutionList.size(); i++) {
            for (WolfSolution wolfSolution : solutionList) {
                if (dominated(solutionList.get(i), wolfSolution) == -1) {
                    strength[i] += 1.0;
                }
            }
        }

        // Calculate the raw fitness
        // rawFitness(i) = |{sum strenght(j) | j <- SolutionSet and j dominate i}|
        for (int i = 0; i < solutionList.size(); i++) {
            for (int j = 0; j < solutionList.size(); j++) {
                if (dominated(solutionList.get(i), solutionList.get(j)) == 1) {
                    rawFitness[i] += strength[j];
                }
            }
        }

        // Add the distance to the k-th individual. In the reference paper of SPEA2,
        // k = sqrt(population.size()), but a value of k = 1 is recommended. See
        // http://www.tik.ee.ethz.ch/pisa/selectors/spea2/spea2_documentation.txt
        for (int i = 0; i < distance.length; i++) {
            Arrays.sort(distance[i]);
            kDistance = 1.0 / (distance[i][1] + 2.0);
            solutionList.get(i).attributes().put("fitness", rawFitness[i] + kDistance);
        }
    }

    private static double[] generateVec(int length, double num) {
        if (length < 0) {
            throw new JMetalException("无效的参数，数组长度必须大于等于0");
        }

        double[] arr = new double[length];
        for (int i = 0; i < length; i++) {
            arr[i] = num;
        }

        return arr;
    }

    public static double[] zeros(int length) {
        return generateVec(length, 0);
    }

    public static double[] ones(int length) {
        return generateVec(length, 1);
    }
}
