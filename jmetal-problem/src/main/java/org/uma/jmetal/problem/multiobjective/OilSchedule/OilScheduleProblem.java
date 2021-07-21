package org.uma.jmetal.problem.multiobjective.OilSchedule;

import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class OilScheduleProblem extends AbstractDoubleProblem {
    public OilScheduleProblem()  {
        this(150);
    }

    public OilScheduleProblem(Integer numberOfVariables){
        setNumberOfVariables(numberOfVariables);
        setNumberOfObjectives(5);
        setName("OilScheduleProblem");
        List<Double> lowerLimit = new ArrayList<>(getNumberOfVariables()) ;
        List<Double> upperLimit = new ArrayList<>(getNumberOfVariables()) ;

        for (int i = 0; i < getNumberOfVariables(); i++) {
            lowerLimit.add(0.0);
            upperLimit.add(1.0);
        }

        setVariableBounds(lowerLimit, upperLimit);
    }

    public DoubleSolution evaluate(DoubleSolution solution){
        double[] x=new double[getNumberOfVariables()];
        for(int i=0;i<getNumberOfVariables();i++){
                x[i]=solution.variables().get(i);
        }

        double[][] pop=new double[1][x.length];

        for(int i=0;i<x.length;i++){
            pop[0][i]=x[i];
        }

        List<List<Double>> eff=TankLessSchedule.fat(pop,false);

        //更新变量 解码过程中会改变染色体
        for(int i=0;i<getNumberOfVariables();i++){
            solution.variables().set(i,eff.get(0).get(i));
        }
        solution.objectives()[0]=eff.get(0).get(getNumberOfVariables()+0);
        solution.objectives()[1]=eff.get(0).get(getNumberOfVariables()+1);
        solution.objectives()[2]=eff.get(0).get(getNumberOfVariables()+2);
        solution.objectives()[3]=eff.get(0).get(getNumberOfVariables()+3);
        solution.objectives()[4]=eff.get(0).get(getNumberOfVariables()+4);

        return solution;
    }
}
