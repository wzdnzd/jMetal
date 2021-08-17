/**
 * @Author : wzdnzd
 * @Time :  2021-07-26
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.strategy;

import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;

import java.util.List;

public abstract class DeleteStrategy {
    public abstract List<WolfSolution> delete(List<WolfSolution> list, int extra);
}
