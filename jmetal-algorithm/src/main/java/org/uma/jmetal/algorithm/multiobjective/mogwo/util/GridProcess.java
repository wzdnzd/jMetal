/**
 * @Author : wzdnzd
 * @Time :  2021-07-25
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.util;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.algorithm.multiobjective.mogwo.Grid;
import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.*;
import java.util.stream.Collectors;

public class GridProcess {
    private static List<Double> linespace(double min, double max, int gridNum) {
        if (gridNum < 1) {
            throw new JMetalException("无效的参数，网格数必须大于1");
        }
        List<Double> list = new ArrayList<>();
        double current = min;
        double gap = (max - min) / (gridNum - 1);
        list.add(current);
        for (int i = 0; i < gridNum - 1; i++) {
            current = current + gap;
            list.add(current);
        }
        return list;
    }

    public static List<Integer> findSelect(List<String> gridIndices, String key) {
        List<Integer> list = new ArrayList<>();
        if (gridIndices == null || gridIndices.isEmpty()) {
            return list;
        }

        for (int i = 0; i < gridIndices.size(); i++) {
            if (gridIndices.get(i).equals(key)) {
                list.add(i);
            }
        }
        return list;
    }

    public static List<Grid> createHypercubes(List<WolfSolution> archive, int gridNum, double alpha) {
        if (archive == null) {
            return new ArrayList<>();
        }

        List<double[]> costs = archive.stream().map(WolfSolution::objectives).collect(Collectors.toList());

        //目标值个数
        int objNum = costs.get(0).length;

        //存放objNum个目标的最大最小值，size为目标值个数
        List<Grid> grids = new ArrayList<>();
        for (int i = 0; i < objNum; i++) {
            int index = i;
            double min = Collections.min(costs.stream().map(e -> e[index]).collect(Collectors.toList()));
            double max = Collections.max(costs.stream().map(e -> e[index]).collect(Collectors.toList()));
            double dcj = alpha * (max - min);
            min = min - dcj;
            max = max + dcj;
            List<Double> lowers = new ArrayList<>(gridNum + 1);
            List<Double> uppers = new ArrayList<>(gridNum + 1);
            lowers.add(Double.NEGATIVE_INFINITY);

            List<Double> points = linespace(min, max, gridNum - 1);
            lowers.addAll(points);
            uppers.addAll(points);
            uppers.add(Double.POSITIVE_INFINITY);

            grids.add(new Grid(lowers, uppers));
        }
        return grids;
    }

    public static List<Pair<String, Integer>> getOccupiedCells(List<WolfSolution> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<WolfSolution>> map = list.stream().collect(Collectors.groupingBy(w -> gridOccGenerate(w.getGridSubIndex())));
        return map.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().size()))
                .collect(Collectors.toList());
    }

    public static void setGridIndex(WolfSolution particle, List<Grid> grids) {
        if (particle == null || grids == null) {
            return;
        }

        //目标值个数
        int objNum = particle.objectives().length;
        int[] indices = new int[objNum];
        for (int i = 0; i < objNum; i++) {
            indices[i] = find(particle.objectives()[i], grids.get(i).getUpper());
        }

        particle.setIndex(gridOccGenerate(indices));
        particle.setGridSubIndex(indices);
    }

    private static int find(double cost, List<Double> list) {
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (cost < list.get(i)) {
                index = i + 1;
                break;
            }
        }
        return index;
    }

    private static boolean arrayEquals(int[] arr1, int[] arr2) {
        if (arr1 == arr2) {
            return true;
        }

        if (arr1 == null || arr2 == null || arr1.length != arr2.length) {
            return false;
        }

        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }

        return true;
    }

    public static String gridOccGenerate(int[] indies) {
        if (indies == null || indies.length <= 0) {
            return "";
        }

        List<String> list = new ArrayList<>(indies.length);
        for (int v : indies) {
            list.add(String.valueOf(v));
        }

        return String.join("#", list);
    }
}
