package org.uma.jmetal.algorithm.multiobjective.espea2.selection;

import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.densityestimator.impl.StrenghtRawFitnessDensityEstimator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @param <S>
 * @author Juanjo Durillo
 */
public class ExtremeEnvironmentalSelection<S extends Solution<?>> implements SelectionOperator<List<S>, List<S>> {

    private int solutionsToSelect;
    private StrenghtRawFitnessDensityEstimator<S> densityEstimator = new StrenghtRawFitnessDensityEstimator<>(1);

    public ExtremeEnvironmentalSelection(int solutionsToSelect) {
        this(solutionsToSelect, 1);
    }

    public ExtremeEnvironmentalSelection(int solutionsToSelect, int k) {
        this.solutionsToSelect = solutionsToSelect;
    }

    @Override
    public List<S> execute(List<S> source2) {
        List<S> sources = new ArrayList<>();

        for (S s : source2) {
            double fitness = densityEstimator.getValue(s);
            if (fitness < 1.0) {
                sources.add(s);
            }
        }

        if (sources.size() <= solutionsToSelect) {
            return sources;
        }

        List<S> aux = new ArrayList<>(solutionsToSelect);
        int count = sources.get(0).objectives().length;
        while (aux.size() <= solutionsToSelect && count > 1) {
            int tmp = count;
            List<S> list = sources.stream()
                    .filter(s -> (Integer) (s.attributes().get("extreme")) == tmp)
                    .collect(Collectors.toList());
            aux.addAll(list);
            count -= 1;
        }

        if (aux.size() < solutionsToSelect) {
            Map<Integer, List<S>> map = sources.stream()
                    .filter(s -> (Integer) (s.attributes().get("extreme")) == 1)
                    .collect(Collectors.groupingBy(s -> (Integer) (s.attributes().get("dim"))));

            while (aux.size() < solutionsToSelect) {
                boolean flag = false;

                for (Map.Entry<Integer, List<S>> entry : map.entrySet()) {
                    List<S> list = entry.getValue();
                    if (list == null || list.isEmpty()) {
                        continue;
                    }

                    aux.add(list.get(0));
                    list.remove(0);
                    flag = true;
                }

                // map里所有元素为空
                if (!flag) {
                    break;
                }
            }
        }

        return aux;
    }
}
