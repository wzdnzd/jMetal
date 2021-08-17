/**
 * @Author : wzdnzd
 * @Time :  2021-07-25
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.leader;

import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;
import org.uma.jmetal.algorithm.multiobjective.mogwo.util.CommonUtils;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.List;

public abstract class LeaderSelector {
    public WolfSolution select(List<WolfSolution> archive) {
        if (archive == null || archive.isEmpty()) {
            throw new JMetalException("cannot select because archive is empty");
        }

        int index = selectOne(archive);
        WolfSolution wolfSolution = archive.get(index);
        if (wolfSolution == null) {
            throw new JMetalException("wolf select failed because selected wolf is null");
        }

        return wolfSolution;
    }

    public abstract int selectOne(List<WolfSolution> archive);
}
