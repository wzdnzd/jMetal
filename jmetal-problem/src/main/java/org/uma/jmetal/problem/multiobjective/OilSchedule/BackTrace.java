package org.uma.jmetal.problem.multiobjective.OilSchedule;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BackTrace implements Serializable {
    private boolean Flag;
    private int step;
    private double time;        // 当前时刻
    private double[] feedTime;  // 炼油结束时刻
    private int[] footprint;    // 足迹
    private double[] x;
    private Map<Integer, Queue<TankLessSchedule.KeyValue>> FP;
    private double[][] TKS;
    private List<List<Double>> schedulePlan;

    public BackTrace(double[] x, int step, double time, double[] feedTime,
                     Map<Integer, Queue<TankLessSchedule.KeyValue>> FP,
                     double[][] TKS,
                     List<List<Double>> schedulePlan) {
        this.Flag = false;
        this.step = step;
        this.x = x;
        this.FP = FP;
        this.time = time;
        this.feedTime = feedTime;
        this.TKS = TKS;
        this.schedulePlan = schedulePlan;
    }

    public boolean allTested() {
        for (int i = 0; i < footprint.length; i++) {
            if (footprint[i] == 0) {
                return false;
            }
        }
        return true;
    }

    public void mark(int point){
        footprint[point]=1;
    }

    /**
     * 判断是否已经停运过
     * @return
     */
    public boolean notStoped() {
        return footprint[footprint.length - 1] == 0;
    }

    public boolean getFlag() {
        return Flag;
    }

    public void setFlag(boolean flag) {
        Flag = flag;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double[] getFeedTime() {
        return feedTime;
    }

    public void setFeedTime(double[] feedTime) {
        this.feedTime = feedTime;
    }

    public int[] getFootprint() {
        return footprint;
    }

    public void setFootprint(int[] footprint) {
        this.footprint = footprint;
    }

    public double[] getX() {
        return x;
    }

    public void setX(double[] x) {
        this.x = x;
    }

    public Map<Integer, Queue<TankLessSchedule.KeyValue>> getFP() {
        return FP;
    }

    public void setFP(Map<Integer, Queue<TankLessSchedule.KeyValue>> FP) {
        this.FP = FP;
    }

    public double[][] getTKS() {
        return TKS;
    }

    public void setTKS(double[][] TKS) {
        this.TKS = TKS;
    }

    public List<List<Double>> getSchedulePlan() {
        return schedulePlan;
    }

    public void setSchedulePlan(List<List<Double>> schedulePlan) {
        this.schedulePlan = schedulePlan;
    }
}
