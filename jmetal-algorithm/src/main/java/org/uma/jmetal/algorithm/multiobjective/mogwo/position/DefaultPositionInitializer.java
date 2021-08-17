/**
 * @Author : wzdnzd
 * @Time :  2021-07-26
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.position;

import org.uma.jmetal.algorithm.multiobjective.mogwo.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultPositionInitializer extends PositionInitializer {
    public DefaultPositionInitializer(double[] lowers, double[] uppers) {
        super(lowers, uppers);
    }

    @Override
    public List<Double> initialize() {
        int dim = getDim();
        List<Double> pos = new ArrayList<>(dim);
        pos.add(Math.random());
        for (int i = 1; i < dim; i++) {
            pos.add(CommonUtils.getRand(getLowers()[i], getUppers()[i]));
        }

        return pos;
    }

    @Override
    public List<Double> boundCheck(List<Double> pos) {
        return CommonUtils.boundCheck(pos, getLowers(), getUppers());
    }
}
