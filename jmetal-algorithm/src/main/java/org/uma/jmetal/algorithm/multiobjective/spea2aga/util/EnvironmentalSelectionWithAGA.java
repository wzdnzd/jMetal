package org.uma.jmetal.algorithm.multiobjective.spea2aga.util;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.algorithm.multiobjective.spea2.util.EnvironmentalSelection;
import org.uma.jmetal.algorithm.multiobjective.spea2aga.model.CosineDistance;
import org.uma.jmetal.algorithm.multiobjective.spea2aga.model.GridAlgoBound;
import org.uma.jmetal.algorithm.multiobjective.spea2aga.model.GridStore;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.densityestimator.impl.StrenghtRawFitnessDensityEstimator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @param <S>
 * @author Juanjo Durillo
 */
public class EnvironmentalSelectionWithAGA<S extends Solution<?>> extends EnvironmentalSelection<S> {

    private int solutionsToSelect;
    private StrenghtRawFitnessDensityEstimator<S> densityEstimator = new StrenghtRawFitnessDensityEstimator<>(1);

    public EnvironmentalSelectionWithAGA(int solutionsToSelect) {
        super(solutionsToSelect);
        this.solutionsToSelect = solutionsToSelect;
    }

    public EnvironmentalSelectionWithAGA(int solutionsToSelect, int k) {
        super(solutionsToSelect, k);
        this.solutionsToSelect = solutionsToSelect;
    }

    @Override
    public List<S> execute(List<S> source2) {
        int size;
        List<S> source = new ArrayList<>(source2.size());
        source.addAll(source2);
        if (source2.size() < this.solutionsToSelect) {
            size = source.size();
        } else {
            size = this.solutionsToSelect;
        }

        List<S> aux = new ArrayList<>(source.size());

        int i = 0;
        while (i < source.size()) {
            double fitness = densityEstimator.getValue(source.get(i));
            if (fitness < 1.0) {
                aux.add(source.get(i));
                source.remove(i);
            } else {
                i++;
            }
        }

        if (aux.size() < size) {
            Comparator<S> comparator = densityEstimator.getComparator();
            source.sort(comparator);
            int remain = size - aux.size();
            for (i = 0; i < remain; i++) {
                aux.add(source.get(i));
            }
            return aux;
        } else if (aux.size() == size) {
            return aux;
        }

        // calculate cosine and remove if |Q| > N
        GridAlgoBound<S> bounds = AGAUtils.findBounds(aux, size, densityEstimator);
        double[] widths = bounds.getWidths();
        double[] bottoms = bounds.getMin();
        List<GridStore<S>> gridStores = new ArrayList<>();
        for (S next : aux) {
            GridStore<S> gs = new GridStore<>();
            List<Integer> arrayList = new ArrayList<>();
            double[] centers = new double[widths.length];
            for (int j = 0; j < widths.length; j++) {
                int index = (int) Math.ceil((next.objectives()[j] - bottoms[j]) / widths[j]);
                arrayList.add(index);

                centers[j] = bottoms[j] + widths[j] * (index - 0.5);
            }
            gs.setSolution(next);
            gs.setObjectGrid(arrayList);
            gs.setCenters(centers);

            gridStores.add(gs);
        }

        List<S> candidates = bounds.getBoundPoints();
        Map<String, List<GridStore<S>>> map = gridStores.stream().collect(Collectors.groupingBy(gs -> {
            List<Integer> lists = gs.getObjectGrid();
            List<String> tmp = new ArrayList<>(lists.size());
            for (Integer value : lists) {
                tmp.add(String.valueOf(value));
            }

            return String.join("#", tmp);
        }));

        for (S candidate : candidates) {
            boolean found = false;
            for (List<GridStore<S>> list : map.values()) {
                Iterator<GridStore<S>> iterator = list.iterator();
                while (iterator.hasNext()) {
                    GridStore<S> gridStore = iterator.next();
                    S solution = gridStore.getSolution();
                    if (candidate == solution) {
                        iterator.remove();
                        found = true;
                        break;
                    }
                }

                if (found) {
                    break;
                }
            }
        }

        int total = count(map);
        while (total > this.solutionsToSelect - candidates.size()) {
            Pair<String, CosineDistance<S>> pair = map.entrySet().stream().filter(e -> {
                List<GridStore<S>> stores = e.getValue();
                return stores != null && stores.size() >= 2;
            }).map(entry -> {
                String key = entry.getKey();
                List<GridStore<S>> stores = entry.getValue();
                double minDistance = Double.MIN_VALUE;
                Pair<S, S> p = null;
                for (int j = 0; j < stores.size() - 1; j++) {
                    GridStore<S> s1 = stores.get(j);
                    for (int k = j + 1; k < stores.size(); k++) {
                        GridStore<S> s2 = stores.get(k);
                        double distance = AGAUtils.cosineDistance(s1.getSolution(), s2.getSolution());
                        if (distance > minDistance) {
                            minDistance = distance;
                            p = Pair.of(s1.getSolution(), s2.getSolution());
                        }
                    }
                }

                CosineDistance<S> cosineDistance = new CosineDistance<>(p, minDistance);
                return Pair.of(key, cosineDistance);
            }).max(Comparator.comparingDouble(o -> o.getRight().getMinDistance()))
                    .orElse(null);

            // 所有网格均只有一个个体，移除距离最近的个体
            if (pair == null) {
                List<S> list = new ArrayList<>(map.size());
                map.values().stream().filter(l -> l != null && !l.isEmpty()).forEach(l -> list.add(l.get(0).getSolution()));
                double minDistance = Double.MIN_VALUE;
                Pair<S, S> solutions = null;
                for (int j = 0; j < list.size() - 1; j++) {
                    S s1 = list.get(j);
                    for (int k = j + 1; k < list.size(); k++) {
                        S s2 = list.get(k);
                        double distance = AGAUtils.cosineDistance(s1, s2);
                        if (distance > minDistance) {
                            minDistance = distance;
                            solutions = Pair.of(s1, s2);
                        }
                    }
                }

                for (Map.Entry<String, List<GridStore<S>>> entry : map.entrySet()) {
                    List<GridStore<S>> stores = entry.getValue();
                    if (stores == null || stores.isEmpty()) {
                        continue;
                    }

                    S solution = stores.get(0).getSolution();
                    S removed = densityEstimator.getValue(solutions.getLeft()) > densityEstimator.getValue(solutions.getRight()) ? solutions.getLeft() : solutions.getRight();
                    if (solutionEquals(removed, solution)){
                        stores.clear();
                    }
                }
            } else {
                Pair<S, S> solutions = pair.getRight().getSolutions();
                S removed = densityEstimator.getValue(solutions.getLeft()) > densityEstimator.getValue(solutions.getRight()) ? solutions.getLeft() : solutions.getRight();
                Iterator<GridStore<S>> iterator = map.get(pair.getLeft()).iterator();
                S s = iterator.next().getSolution();
                while (!solutionEquals(removed, s) && iterator.hasNext()) {
                    iterator.next();
                }

                iterator.remove();
            }

            total = count(map);
        }

        for (List<GridStore<S>> stores : map.values()) {
            for (GridStore<S> store : stores) {
                S s = store.getSolution();
                if (!candidates.contains(s)) {
                    candidates.add(s);
                }
            }
        }

        return candidates;
    }

    private double manhattanDistance(List<Integer> v1, List<Integer> v2) {
        if (v1 == null || v2 == null || v1.size() != v2.size()) {
            throw new IllegalArgumentException("非法参数");
        }

        double distance = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            distance += Math.abs(v1.get(i) - v2.get(i));
        }

        return distance;
    }

    private List<Integer> convert2Grid(String key) {
        if (key == null || "".equals(key)) {
            throw new IllegalArgumentException("空字符串无法转换成网格坐标");
        }

        return Arrays.stream(key.split("#"))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    private int count(Map<String, List<GridStore<S>>> map) {
        int total = 0;

        if (map == null || map.isEmpty()) {
            return total;
        }

        for (List<GridStore<S>> list : map.values()) {
            if (list != null && !list.isEmpty()) {
                total += list.size();
            }
        }

        return total;
    }

    private String generateKey(GridStore<S> gridStore) {
        String key = "";
        if (gridStore == null) {
            return key;
        }

        List<Integer> lists = gridStore.getObjectGrid();
        List<String> list = new ArrayList<>(lists.size());
        for (Integer value : lists) {
            list.add(String.valueOf(value));
        }

        key = String.join("#", list);
        return key;
    }

    private boolean solutionEquals(S s1, S s2) {
        if (s1 == s2) {
            return true;
        }

        if (s1 == null || s2 == null) {
            return false;
        }

        double[] o1 = s1.objectives();
        double[] o2 = s2.objectives();

        for (int i = 0; i < o1.length; i++) {
            if (o1[i] != o2[i]) {
                return false;
            }
        }

        return true;
    }
}
