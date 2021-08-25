/**
 * @Author : wzdnzd
 * @Time :  2021-07-25
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.spea2aga.leader;

import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.List;

public abstract class LeaderSelector<S extends DoubleSolution> {
    public S select(List<S> archive) {
        if (archive == null || archive.isEmpty()) {
            throw new JMetalException("cannot select because archive is empty");
        }

        int index = selectOne(archive);
        S s = archive.get(index);
        if (s == null) {
            throw new JMetalException("solution select failed");
        }

        return s;
    }

    public abstract int selectOne(List<S> archive);
}
