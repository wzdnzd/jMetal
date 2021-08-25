/**
 * @Author : wzdnzd
 * @Time :  2021-08-20
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.strategy;

import org.uma.jmetal.algorithm.multiobjective.mogwo.Distance;
import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.DistanceUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ImprovedDeleteStrategy extends DeleteStrategy {
    @Override
    public List<WolfSolution> delete(List<WolfSolution> list, int extra) {
        if (list == null || list.size() < extra) {
            return list;
        }

        List<Distance> result = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++) {
            WolfSolution s1 = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                WolfSolution s2 = list.get(j);
                double v = Math.acos(DistanceUtils.cosineDistance(s1, s2));
                result.add(new Distance(s1, s2, v));
            }
        }

        result = result.stream()
                .sorted(Comparator.comparing(Distance::getDistance))
                .collect(Collectors.toList());

        for (int i = 0; i < extra; i++) {
            Distance distance = result.get(i);
            list.remove(distance.getDest());
        }

        return list;
    }
}
