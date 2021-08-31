/**
 * @Author : wzdnzd
 * @Time :  2021-07-25
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.espea2.leader;

import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.List;

public abstract class LeaderSelector<S extends DoubleSolution> {
    public S select(List<S> archives) {
        if (archives == null || archives.isEmpty()) {
            throw new JMetalException("cannot select because archives is empty");
        }

        int index = selectOne(archives);
        S s = archives.get(index);
        if (s == null) {
            throw new JMetalException("solution select failed because selected wolf is null");
        }

        return s;
    }

    public abstract int selectOne(List<S> archives);
}
