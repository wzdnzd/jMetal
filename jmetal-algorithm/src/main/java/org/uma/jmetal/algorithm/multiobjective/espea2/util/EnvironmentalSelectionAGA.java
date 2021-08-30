package org.uma.jmetal.algorithm.multiobjective.espea2.util;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.algorithm.multiobjective.spea2.util.EnvironmentalSelection;
import org.uma.jmetal.algorithm.multiobjective.espea2.model.GridStore;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.densityestimator.impl.StrenghtRawFitnessDensityEstimator;
import org.uma.jmetal.util.solutionattribute.impl.LocationAttribute;

import java.util.*;

/**
 * @param <S>
 * @author Juanjo Durillo
 */
public class EnvironmentalSelectionAGA<S extends Solution<?>> extends EnvironmentalSelection<S> {

    private int solutionsToSelect;
    private StrenghtRawFitnessDensityEstimator<S> densityEstimator = new StrenghtRawFitnessDensityEstimator<>(1);

    public EnvironmentalSelectionAGA(int solutionsToSelect) {
        super(solutionsToSelect);
        this.solutionsToSelect = solutionsToSelect;
    }

    public EnvironmentalSelectionAGA(int solutionsToSelect, int k) {
        super(solutionsToSelect, k);
        this.solutionsToSelect = solutionsToSelect;
    }

    @Override
    public List<S> execute(List<S> source2) {
//        int m = source2.size();
//        int n = source2.get(0).objectives().length;
//        double[][] matrix = new double[m][n];
//        for (int i = 0; i < source2.size(); i++) {
//            matrix[i] = source2.get(i).objectives();
//        }
//
//        matrix = AGAUtils.transpose(matrix);
//        double[] means = new double[n];
//        double[] variances = new double[n];
//        for (int i = 0; i < n; i++) {
//            means[i] = AGAUtils.mean(matrix[i]);
//            variances[i] = AGAUtils.variance(matrix[i], means[i]);
//        }

//        int size;
//        List<S> source = new ArrayList<>(source2.size());
//        source.addAll(source2);
//        if (source2.size() < this.solutionsToSelect) {
//            size = source.size();
//        } else {
//            size = this.solutionsToSelect;
//        }
//
//        List<S> aux = new ArrayList<>(source.size());
//
//        int i = 0;
//        while (i < source.size()) {
//            double fitness = densityEstimator.getValue(source.get(i));
//            if (fitness < 1.0) {
//                aux.add(source.get(i));
//                source.remove(i);
//            } else {
//                i++;
//            }
//        }
//
//        if (aux.size() < size) {
////            GridAlgoBound<S> bounds = AGAUtils.findBounds(source, 1, false);
////            List<S> points = bounds.getBoundPoints();
////            int remain = size - aux.size();
////            if (points.size() < remain) {
////                aux.addAll(points);
////                Comparator<S> comparator = densityEstimator.getComparator();
////                source.sort(comparator);
////
////                for (S s : source) {
////                    if (!aux.contains(s)) {
////                        aux.add(s);
////                    }
////
////                    if (aux.size() == size) {
////                        break;
////                    }
////                }
////            } else if (remain == points.size()) {
////                aux.addAll(points);
////            } else {
////                int m = source.size();
////                int n = source.get(0).objectives().length;
////                double[][] matrix = new double[m][n];
////                for (int j = 0; j < source.size(); j++) {
////                    matrix[j] = source.get(j).objectives();
////                }
////
////                matrix = AGAUtils.transpose(matrix);
////                double[] means = new double[n];
////                double[] variances = new double[n];
////                for (int j = 0; j < n; j++) {
////                    means[j] = AGAUtils.mean(matrix[j]);
////                    variances[j] = AGAUtils.variance(matrix[j], means[j]);
////                }
////
////                double[] lowers = AGAUtils.std(bounds.getMin(), means, variances);
////                List<Pair<S, Double>> pairs = points.stream().map(s -> {
////                            double[] objectives = AGAUtils.std(s.objectives(), means, variances);
////                            double distance = AGAUtils.distance(lowers, objectives);
////                            return Pair.of(s, distance);
////                        }).sorted(Comparator.comparing(Pair::getRight))
////                        .collect(Collectors.toList());
////                for (int j = 0; j < remain; j++) {
////                    aux.add(pairs.get(j).getLeft());
////                }
////            }
//
////            Ranking<S> ranking = new FastNonDominatedSortRanking<>();
////            ranking.compute(source);
////            int j = 0;
////            while (j < ranking.getNumberOfSubFronts() && aux.size() < size) {
////                List<S> front = ranking.getSubFront(j);
////                if ((size - aux.size()) >= front.size()) {
////                    aux.addAll(front);
////                    j += 1;
////                    continue;
////                }
////
////                double[] lowers = AGAUtils.std(AGAUtils.findMin(aux), means, variances);
////                List<Pair<S, Double>> pairs = front.stream().map(s -> {
////                            double[] objectives = AGAUtils.std(s.objectives(), means, variances);
////                            double distance = AGAUtils.distance(lowers, objectives);
////                            return Pair.of(s, distance);
////                        }).sorted(Comparator.comparing(Pair::getRight))
////                        .collect(Collectors.toList());
////
//////                GridAlgoBound<S> bounds = AGAUtils.findBounds(aux, 5, densityEstimator);
//////                double[] min = bounds.getMin();
//////                double[] max = bounds.getMax();
//////                double[] lowers = new double[min.length];
//////                List<Pair<S, Double>> pairs = source.stream().map(s -> {
//////                            double[] objs = AGAUtils.normalize(s.objectives(), min, max);
//////                            double distance = AGAUtils.distance(lowers, objs);
//////                            return Pair.of(s, distance);
//////                        }).sorted(Comparator.comparing(Pair::getRight))
//////                        .collect(Collectors.toList());
//////
//////                double[] lowers = AGAUtils.findMin(aux);
//////                List<S> list = front.stream().filter(s -> s.objectives()[0] <= 7.0).collect(Collectors.toList());
//////                List<Pair<S, Double>> pairs = front.stream().map(s -> {
//////                            double distance = AGAUtils.distance(lowers, s.objectives());
//////                            return Pair.of(s, distance);
//////                        }).sorted(Comparator.comparing(Pair::getRight))
//////                        .collect(Collectors.toList());
////
////                int remain = size - aux.size();
////                for (i = 0; i < remain; i++) {
////                    aux.add(pairs.get(i).getLeft());
////                }
////            }
//
//            Comparator<S> comparator = densityEstimator.getComparator();
//            source.sort(comparator);
//
//            int remain = size - aux.size();
//            for (i = 0; i < remain; i++) {
//                aux.add(source.get(i));
//            }
//
//            return aux;
//        } else if (aux.size() == size) {
//            return aux;
//        }
//
//        // calculate cosine and remove if |Q| > N
//        int gridNum = (int) Math.sqrt(solutionsToSelect);
//        GridAlgoBound<S> bounds = AGAUtils.findBounds(aux, gridNum, true);
//        double[] min = bounds.getMin();
//        double[] max = bounds.getMax();
//        int objNum = max.length;
//        double[] widths = new double[max.length];
//        for (int j = 0; j < max.length; j++) {
//            widths[j] = 1.0 / gridNum;
//        }
//
//        List<GridStore<S>> gridStores = new ArrayList<>();
//
//        for (S next : aux) {
//            double[] normalizedObj = AGAUtils.normalize(next.objectives(), min, max);
//            GridStore<S> gs = new GridStore<>();
//            List<Integer> arrayList = new ArrayList<>();
//            double[] centers = new double[objNum];
//            for (int j = 0; j < objNum; j++) {
//                int index = (int) Math.ceil(normalizedObj[j] / widths[j]);
//                arrayList.add(index);
//
//                centers[j] = normalizedObj[j] + widths[j] * (index - 0.5);
//            }
//            gs.setSolution(next);
//            gs.setObjectGrid(arrayList);
//            gs.setCenters(centers);
//            gs.setNormalizedObj(normalizedObj);
//
//            gridStores.add(gs);
//        }
//
//        List<S> candidates = bounds.getBoundPoints();
//        Map<String, List<GridStore<S>>> map = gridStores.stream().collect(Collectors.groupingBy(gs -> {
//            List<Integer> lists = gs.getObjectGrid();
//            List<String> tmp = new ArrayList<>(lists.size());
//            for (Integer value : lists) {
//                tmp.add(String.valueOf(value));
//            }
//
//            return String.join("#", tmp);
//        }));
//
//        for (S candidate : candidates) {
//            boolean found = false;
//            for (List<GridStore<S>> list : map.values()) {
//                Iterator<GridStore<S>> iterator = list.iterator();
//                while (iterator.hasNext()) {
//                    GridStore<S> gridStore = iterator.next();
//                    S solution = gridStore.getSolution();
//                    if (candidate == solution) {
//                        iterator.remove();
//                        found = true;
//                        break;
//                    }
//                }
//
//                if (found) {
//                    break;
//                }
//            }
//        }
//
//        int total = countSolution(map);
//        while (total > this.solutionsToSelect - candidates.size()) {
//            Pair<String, CosineDistance<S>> pair = map.entrySet().stream().filter(e -> {
//                List<GridStore<S>> stores = e.getValue();
//                return stores != null && stores.size() >= 2;
//            }).map(entry -> {
//                String key = entry.getKey();
//                List<GridStore<S>> stores = entry.getValue();
//                double minDistance = Double.MIN_VALUE;
//                int count = 1;
//                Pair<S, S> p = null;
//                for (int j = 0; j < stores.size() - 1; j++) {
//                    GridStore<S> s1 = stores.get(j);
//                    for (int k = j + 1; k < stores.size(); k++) {
//                        GridStore<S> s2 = stores.get(k);
//                        double distance = AGAUtils.cosineDistance(s1.getNormalizedObj(), s2.getNormalizedObj());
//                        if (distance == minDistance) {
//                            count += 1;
//                        }
//
//                        if (distance > minDistance) {
//                            minDistance = distance;
//                            p = Pair.of(s1.getSolution(), s2.getSolution());
//                        }
//                    }
//                }
//
//                CosineDistance<S> cosineDistance = new CosineDistance<>(p, minDistance, count);
//                return Pair.of(key, cosineDistance);
//            }).min((o1, o2) -> {
//                if (o1 == o2) {
//                    return 0;
//                } else if (o2 == null) {
//                    return -1;
//                }
//
//                int result = -Double.compare(o1.getRight().getMinDistance(), o2.getRight().getMinDistance());
//                return result != 0 ? result : o2.getRight().getCount() - o1.getRight().getCount();
//            }).orElse(null);
//
//            // 所有网格均只有一个个体，移除距离最近的个体
//            if (pair == null) {
//                List<S> list = new ArrayList<>(map.size());
//                map.values().stream().filter(l -> l != null && !l.isEmpty()).forEach(l -> list.add(l.get(0).getSolution()));
//                double minDistance = Double.MIN_VALUE;
//                Pair<S, S> solutions = null;
//                for (int j = 0; j < list.size() - 1; j++) {
//                    S s1 = list.get(j);
//                    double[] nv1 = AGAUtils.normalize(s1.objectives(), min, max);
//                    for (int k = j + 1; k < list.size(); k++) {
//                        S s2 = list.get(k);
//                        double[] nv2 = AGAUtils.normalize(s2.objectives(), min, max);
//                        double distance = AGAUtils.cosineDistance(nv1, nv2);
//                        if (distance > minDistance) {
//                            minDistance = distance;
//                            solutions = Pair.of(s1, s2);
//                        }
//                    }
//                }
//
//                for (Map.Entry<String, List<GridStore<S>>> entry : map.entrySet()) {
//                    List<GridStore<S>> stores = entry.getValue();
//                    if (stores == null || stores.isEmpty()) {
//                        continue;
//                    }
//
//                    S solution = stores.get(0).getSolution();
//                    S removed = densityEstimator.getValue(solutions.getLeft()) > densityEstimator.getValue(solutions.getRight()) ? solutions.getLeft() : solutions.getRight();
//                    if (solutionEquals(removed, solution)) {
//                        stores.clear();
//                    }
//                }
//            } else {
//                Pair<S, S> solutions = pair.getRight().getSolutions();
//                S removed = densityEstimator.getValue(solutions.getLeft()) > densityEstimator.getValue(solutions.getRight()) ? solutions.getLeft() : solutions.getRight();
//                Iterator<GridStore<S>> iterator = map.get(pair.getLeft()).iterator();
//                S s = iterator.next().getSolution();
//                while (!removed.equals(s) && iterator.hasNext()) {
//                    s = iterator.next().getSolution();
//                }
//
//                iterator.remove();
//            }
//
//            total = countSolution(map);
//        }
//
//        for (List<GridStore<S>> stores : map.values()) {
//            for (GridStore<S> store : stores) {
//                S s = store.getSolution();
//                candidates.add(s);
//            }
//        }
//
//        return candidates;

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
        while (i < source.size()){
            double fitness = densityEstimator.getValue(source.get(i)) ;
            if (fitness<1.0){
                aux.add(source.get(i));
                source.remove(i);
            } else {
                i++;
            }
        }

        if (aux.size() < size){
            Comparator<S> comparator = densityEstimator.getComparator() ;
            source.sort(comparator);
            int remain = size - aux.size();
            for (i = 0; i < remain; i++){
                aux.add(source.get(i));
            }
            return aux;
        } else if (aux.size() == size) {
            return aux;
        }

        double [][] distance = SolutionListUtils.distanceMatrix(aux);
        List<List<Pair<Integer, Double>> > distanceList = new ArrayList<>();
        LocationAttribute<S> location = new LocationAttribute<S>(aux);
        for (int pos = 0; pos < aux.size(); pos++) {
            List<Pair<Integer, Double>> distanceNodeList = new ArrayList<>();
            for (int ref = 0; ref < aux.size(); ref++) {
                if (pos != ref) {
                    distanceNodeList.add(Pair.of(ref, distance[pos][ref]));
                }
            }
            distanceList.add(distanceNodeList);
        }

        for (List<Pair<Integer, Double>> pairs : distanceList) {
            pairs.sort(Comparator.comparing(Pair::getRight));
        }

        while (aux.size() > size) {
            double minDistance = Double.MAX_VALUE;
            int toRemove = 0;
            i = 0;
            for (List<Pair<Integer, Double>> dist : distanceList) {
                if (dist.get(0).getRight() < minDistance) {
                    toRemove = i;
                    minDistance = dist.get(0).getRight();
                    //i y toRemove have the same distance to the first solution
                } else if (dist.get(0).getRight().equals(minDistance)) {
                    int k = 0;
                    while ((dist.get(k).getRight().equals(
                            distanceList.get(toRemove).get(k).getRight())) &&
                            k < (distanceList.get(i).size() - 1)) {
                        k++;
                    }

                    if (dist.get(k).getRight() <
                            distanceList.get(toRemove).get(k).getRight()) {
                        toRemove = i;
                    }
                }
                i++;
            }

            int tmp =  location.getAttribute(aux.get(toRemove));
            aux.remove(toRemove);
            distanceList.remove(toRemove);

            for (List<Pair<Integer, Double>> pairs : distanceList) {
                pairs.removeIf(integerDoublePair -> integerDoublePair.getLeft() == tmp);
            }
        }
        return aux;
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

    private int countSolution(Map<String, List<GridStore<S>>> map) {
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
}
