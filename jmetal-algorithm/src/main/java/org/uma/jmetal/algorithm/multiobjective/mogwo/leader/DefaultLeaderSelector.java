/**
 * @Author : wzdnzd
 * @Time :  2021-07-26
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.leader;

import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.CommonUtils;

import java.util.List;

public class DefaultLeaderSelector extends LeaderSelector {
    private final double beta;

    public DefaultLeaderSelector(double beta) {
        this.beta = beta;
    }

    @Override
    public int selectOne(List<WolfSolution> archive) {
        return CommonUtils.selectOneSolution(archive, beta);
    }
}
