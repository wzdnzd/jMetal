/**
 * @Author : wzdnzd
 * @Time :  2021-07-26
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.strategy;

import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.CommonUtils;

import java.util.List;

public class DefaultDeleteStrategy extends DeleteStrategy {
    private final double gamma;

    public DefaultDeleteStrategy(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public List<WolfSolution> delete(List<WolfSolution> list, int extra) {
        if (list == null || list.isEmpty() || list.size() < extra) {
            return list;
        }

        List<WolfSolution> solutions = CommonUtils.deepCopy(list);
        for (int i = 0; i < extra; i++) {
            int index = CommonUtils.selectOneSolution(solutions, gamma);
            solutions.remove(index);
        }

        return solutions;
    }
}
