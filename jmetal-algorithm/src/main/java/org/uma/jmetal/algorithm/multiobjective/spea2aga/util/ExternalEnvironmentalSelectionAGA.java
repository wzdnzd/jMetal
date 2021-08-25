package org.uma.jmetal.algorithm.multiobjective.spea2aga.util;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.algorithm.multiobjective.spea2.util.EnvironmentalSelection;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.densityestimator.impl.StrenghtRawFitnessDensityEstimator;
import org.uma.jmetal.util.solutionattribute.impl.LocationAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @param <S>
 * @author Juanjo Durillo
 */
public class ExternalEnvironmentalSelectionAGA<S extends Solution<?>> extends EnvironmentalSelection<S> {

    private int solutionsToSelect;
    private StrenghtRawFitnessDensityEstimator<S> densityEstimator = new StrenghtRawFitnessDensityEstimator<>(1);

    public ExternalEnvironmentalSelectionAGA(int solutionsToSelect) {
        super(solutionsToSelect);
        this.solutionsToSelect = solutionsToSelect;
    }

    public ExternalEnvironmentalSelectionAGA(int solutionsToSelect, int k) {
        super(solutionsToSelect, k);
        this.solutionsToSelect = solutionsToSelect;
    }

    @Override
    public List<S> execute(List<S> sources) {
        if (sources.size() <= solutionsToSelect) {
            return sources;
        }

        int m = sources.size();
        int n = sources.get(0).objectives().length;
        double[][] matrix = new double[m][n];
        for (int i = 0; i < sources.size(); i++) {
            matrix[i] = sources.get(i).objectives();
        }

        matrix = AGAUtils.transpose(matrix);
        double[] means = new double[n];
        double[] variances = new double[n];
        for (int i = 0; i < n; i++) {
            means[i] = AGAUtils.mean(matrix[i]);
            variances[i] = AGAUtils.variance(matrix[i], means[i]);
        }

        double[] lowers = AGAUtils.std(AGAUtils.findMin(sources), means, variances);
        List<Pair<S, Double>> pairs = sources.stream().map(s -> {
                    double[] objectives = AGAUtils.std(s.objectives(), means, variances);
                    double distance = AGAUtils.distance(lowers, objectives);
                    return Pair.of(s, distance);
                }).sorted(Comparator.comparing(Pair::getRight))
                .collect(Collectors.toList());
        List<S> aux = new ArrayList<>(solutionsToSelect);
        int count = 0;
        while (aux.size() < solutionsToSelect && count < pairs.size()) {
            aux.add(pairs.get(count).getLeft());
            count += 1;
        }

        return aux;
    }
}
