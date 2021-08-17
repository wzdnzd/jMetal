package org.uma.jmetal.algorithm.multiobjective.mogwo.util;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.io.*;
import java.util.ArrayList;
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
        int bestIsOne = 0;
        int bestIsTwo = 0;
        for (int i = 0; i < s1.objectives().length; i++) {
            double v1 = s1.objectives()[i];
            double v2 = s2.objectives()[i];
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

    public static int diff(List<Double> l1, List<Double> l2) {
        if (l1 == l2) {
            return 0;
        }

        if (l1 == null) {
            return l2.size();
        }

        if (l2 == null) {
            return l1.size();
        }

        int count = 0;
        int min = Math.min(l1.size(), l2.size());
        int max = Math.max(l1.size(), l2.size());
        for (int i = 0; i < min; i++) {
            if (!l1.get(i).equals(l2.get(i))) {
                count++;
            }
        }

        return count + max - min;
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

    public static int selectOneSolution(List<WolfSolution> archive, double beta) {
        if (archive == null || archive.isEmpty() || beta < 0) {
            throw new IllegalArgumentException("归档集不能为空且beta必须大于0");
        }

        List<Pair<String, Integer>> pairs = GridProcess.getOccupiedCells(archive);

        //去重之后的网格下标
        List<String> occCellIndices = new ArrayList<>(pairs.size());

        // 网格包含的个体数量
        List<Integer> occMemberCount = new ArrayList<>(pairs.size());

        pairs.forEach(e -> {
            occCellIndices.add(e.getLeft());
            occMemberCount.add(e.getRight());
        });

        // 概率密度
        List<Double> pd = occMemberCount.stream()
                .map(e -> Math.pow(e, -beta))
                .collect(Collectors.toList());

        Double sump = pd.stream()
                .mapToDouble(x -> x)
                .summaryStatistics()
                .getSum();

        // 概率
        List<Double> p = pd.stream()
                .map(e -> e / sump)
                .collect(Collectors.toList());

        // 轮盘赌获得所选网格下标
        String key = occCellIndices.get(CommonUtils.rouletteWheelSelect(p));
        List<String> list = archive.stream()
                .map(WolfSolution::getIndex)
                .collect(Collectors.toList());

        // 被选的网格下标
        List<Integer> selectedGrids = GridProcess.findSelect(list, key);
        int n = selectedGrids.size();

        // [0,n)的整数
        int memberIndex = (int) (Math.random() * n);
        return selectedGrids.get(memberIndex);
    }
}
