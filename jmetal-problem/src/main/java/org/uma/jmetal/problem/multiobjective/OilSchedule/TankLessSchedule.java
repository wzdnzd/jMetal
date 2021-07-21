package org.uma.jmetal.problem.multiobjective.OilSchedule;


import org.uma.jmetal.problem.multiobjective.OilSchedule.gante.PlotUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class TankLessSchedule {
    public static boolean _showGante = true;

    public static class KeyValue implements Serializable {
        private String type;
        private double volume;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }

        public KeyValue(String type, double volume) {
            this.type = type;
            this.volume = volume;
        }
    }

    static Queue<BackTrace> queueBack=new LinkedList<>();
    public static void main(String[] args) {
         int k=150;
         int popsize=100;
         double[][] pop=new double[popsize][k];
       // PaperCost();

         for(int i=0;i<popsize;i++){
             for(int j=0;j<k;j++){
                 pop[i][j]=Math.random();
             }
         }
         fat(pop,true);
    }
    static int RT=6;
    private static double[] DSFR=new double[]{333.3,291.7,625};
    private static double[] PIPEFR=new double[]{840, 1250, 1370 };

    public static List<List<Double>> fat(double[][]pop,boolean showGante){
        _showGante=showGante;
        List<List<Double>> eff=new ArrayList<>();
        double inf=-1;

        int popsize=pop.length;

        int[][] tankCost = new int[][]{

                { 0, 11, 12, 13, 10, 15},
                {11,  0, 11, 12, 13, 10},
                {12, 11,  0, 10, 12, 13},
                {13, 12, 10,  0, 11, 12},
                {10, 13, 12, 11,  0, 11},
                {15, 10, 13, 12, 11,  0}

        };// 罐底混合成本
        int[][] pipCost = new int[][]{
                { 0, 11, 12, 13,  7, 15},
                {10,  0,  9, 12, 13, 7},
                {13,  8,  0,  7, 12, 13},
                {13, 12,  7,  0, 11, 12},
                { 7, 13, 12, 11,  0, 11},
                {15,  7, 13, 12, 11, 0}
        };// 管道混合成本
        for(int p=0;p<pop.length;p++){
            queueBack.clear();
            double[] x=pop[p];
            List<List<Double>> schedulePlan=new ArrayList<>();
            Map<Integer, Queue<KeyValue>> feedingPackages=new HashMap<>();
            Queue<KeyValue> ds1=new LinkedList<>();
            ds1.add(new KeyValue("5",38000));
            ds1.add(new KeyValue("1",42000));

            feedingPackages.put(1,ds1);

            Queue<KeyValue> ds2=new LinkedList<>();
            ds2.add(new KeyValue("6",21000));
            ds2.add(new KeyValue("2",49008));
            feedingPackages.put(2,ds2);

            Queue<KeyValue> ds3=new LinkedList<>();
            ds3.add(new KeyValue("4",30000));
            ds3.add(new KeyValue("3",120000));
            feedingPackages.put(3,ds3);

       double[][] TKS=new double[][]{// 油罐编号 容量 已有油量  开始st 结束st 原油类型
               { 1, 16000,  8000, 0, 0, 5},//no
               { 2, 34000, 30000, 0, 0, 5},
               { 3, 34000, 30000, 0, 0, 4},
               { 4, 34000,     0, 0, 0, inf},
               { 5, 34000, 30000, 0, 0, 3},
               { 6, 16000, 16000, 0, 0, 1},
               { 7, 20000, 16000, 0, 0, 6},
               { 8, 16000,  5000, 0, 0, 6},//no
               { 9, 16000,     0, 0, 0, inf},
               {10, 30000,     0, 0, 0, inf}
//               {11, 34000, 28000, 0, 0, 7},
//               {12, 16000, 16000, 0, 0 ,7}
       };
       double[] DSFET=new double[]{0,0,0};
       //进行初始指派（后期可以考虑变换初始指派）
            for(int i=0; i<TKS.length; i++){
                if(TKS[i][2]>0 &&TKS[i][5]!=inf){
                    int type=(int)TKS[i][5];
                    for(Map.Entry<Integer, Queue<KeyValue>> entry: feedingPackages.entrySet()){
                        Queue<KeyValue> fp=entry.getValue();
                        for(KeyValue k:fp){
                            if(Integer.parseInt(k.type)==type && k.volume>0){
                                doODF(schedulePlan,DSFET,entry.getKey(),type, (int)TKS[i][0], TKS[i][2]);

                                //更新进料包
                                if((k.volume-TKS[i][2])<0.00001 && fp!=null){
                                    fp.poll();
                                }else{
                                    k.setVolume(k.volume-TKS[i][2]);
                                }
                            }
                        }

                    }

                }
            }

            BackTrace back=new BackTrace(x,0,0,DSFET,feedingPackages,TKS,schedulePlan);

            back=backSchedule(back);
//            if(back.getStep()>=30){
//                System.out.println("backStep:"+back.getStep());
//            }

            if(_showGante){
                PlotUtils.plotSchedule2(back.getSchedulePlan());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            double f_Tank, f_ds,f_pipe,f_mixT,f_energy;
            if(back.getFlag()){
                f_Tank= TestFun.gNum(back.getSchedulePlan());
                f_ds=TestFun.dsChange(back.getSchedulePlan());
                f_pipe=TestFun.pipeMix(back.getSchedulePlan(), pipCost);
                f_mixT=TestFun.tankMix(back.getSchedulePlan(),tankCost);
                f_energy=TestFun.gEnergyCost(back.getSchedulePlan(), new double[]{1,2,3}, PIPEFR);


            }else {
                f_Tank=inf;
                f_ds=inf;
                f_pipe=inf;
                f_mixT=inf;
                f_energy=inf;
            }

            if(_showGante){
                System.out.println("--------------------------"+(p+1)+"--------------------------");
                System.out.println("Tank_Number:       " + f_Tank);
                System.out.println("DS_Change:         " + f_ds);
                System.out.println("pipe_Cost:         " + f_pipe);
                System.out.println("Tank_Cost:         " + f_mixT);
                System.out.println("Energy_Cost:       " + f_energy);
                System.out.println("----------------------------------------------------");
            }

            List<Double> tl=new ArrayList<>();
            for(int i=0; i<back.getX().length; i++){
                tl.add(back.getX()[i]);
            }
            tl.add(f_Tank);
            tl.add(f_ds);
            tl.add(f_pipe);
            tl.add(f_mixT);
            tl.add(f_energy);
            eff.add(tl);
        }
        return eff;

    }

    private static BackTrace backSchedule(BackTrace backTrace){
       // System.out.println("Step:"+backTrace.getStep());
        List<Integer> ET=getET(backTrace);
        List<Integer> UD=getUD(backTrace);
        if (ET.isEmpty()) {
            backTrace.setFootprint(new int[1]);
        } else {
            backTrace.setFootprint(new int[UD.size()+1]);
        }
        if(!backTrace.getFlag() && backTrace.getStep()<50){
            while (!backTrace.allTested()){
                boolean ff=false;
                BackTrace back = CloneUtil.clone(backTrace);
                ET=getET(back);
                UD=getUD(back);
                int DS_NO= TestFun.getInt(back.getX()[3*back.getStep()+1],UD.size());
                double Speed=PIPEFR[TestFun.getInt(back.getX()[3*back.getStep()+2], PIPEFR.length-1)];
                if(back.notStoped() && (DS_NO==UD.size() || ET.size()<1)){

                    double StopTime=getStopTime(back);
                    double[] feedTimes=back.getFeedTime();
                    double tmp=Double.MAX_VALUE;
                    for(int i=0;i<feedTimes.length;i++){
                        if(tmp>feedTimes[i]){
                            tmp=feedTimes[i];
                        }
                    }
                    tmp-=RT;

                    if(StopTime>0 && StopTime<tmp ){
                        ff=stopPipe(back, StopTime);
                    }

                }else if(ET.size()>=1 && DS_NO<UD.size()){
                    int DS=UD.get(DS_NO);
                    int TK=ET.get(TestFun.getInt(back.getX()[3*back.getStep()], ET.size()-1));
                     if(TK>0){
                         boolean suc=trySchedule(back, TK, DS, Speed);

                         if(suc && isSchedule(back)){
                             ff=true;
                         }
                     }

                }
                //判断调度是否成功
                boolean badFlag=false;

//                if(ff){
////                    back.setStep(back.getStep()+1);
////                    if(!back.getFlag()){
////                        if(back.allTested()){
////                            badFlag=true;
////                        }else{
////                            return backSchedule(back);
////                        }
////                    }else{
////                        return back;
////                    }
////                }
                if(ff){
                    back.setStep(back.getStep()+1);
                    if(!back.getFlag()){
                        BackTrace newBackTrace=backSchedule(back);
                        if(newBackTrace.getFlag()){
                            // 绘制甘特图
                            return newBackTrace;
                        }
                        if(back.allTested()){
                            badFlag=true;
                        }

                    }else{
                        return back;
                    }
                }

                //调度失败，切换蒸馏塔

                if(!ff || badFlag){
                    for(int i=backTrace.getFootprint().length-1;i>=0;i--){
                        if(backTrace.getFootprint()[i]==0){
                            int speed_ind=TestFun.getInt(backTrace.getX()[3*backTrace.getStep()+2], PIPEFR.length-1);
                            if(speed_ind!=PIPEFR.length-1){
                                while(speed_ind!=PIPEFR.length-1)
                                {
                                    backTrace.getX()[3*backTrace.getStep()+2]=Math.random();
                                    speed_ind=TestFun.getInt(backTrace.getX()[3*backTrace.getStep()+2], PIPEFR.length-1);
                                }
                            }else{
                                if(ET.isEmpty()){
                                    backTrace.mark(0);
                                }else{
                                    backTrace.mark(DS_NO);
                                }
                                while (DS_NO!=i){
                                    backTrace.getX()[3*backTrace.getStep()+1]=Math.random();
                                    DS_NO=TestFun.getInt(backTrace.getX()[3*backTrace.getStep()+1], UD.size());
                                }
                            }
                            break;
                        }
                    }
                }
                //System.out.println("changeDS_NO:"+DS_NO);
            }
        }
    return backTrace;
    }

    public static boolean isSchedule(BackTrace back){
        //判断当前时间是否有炼油计划被延误

        double[] feedTime=back.getFeedTime();

        for(int i=0;i<feedTime.length;i++){
            if(back.getTime()+RT>feedTime[i]){
                return false;
            }
        }
        return true;
    }

    public static boolean trySchedule(BackTrace back, int TK, int DS, double Speed){
        Map<Integer, Queue<KeyValue>> FPs=back.getFP();

        Queue<KeyValue> FP=FPs.get(DS);

        int OilType=Integer.parseInt(FP.peek().type);
        double Volume=FP.peek().volume;
        double V=getSafeV(back, DS, Speed);

        //确定转运体积 1. 油罐约束 2.油包约束 3.安全体积约束
        if(V>=5000){
            V=Math.min(V,back.getTKS()[TK-1][1]);

            if(V>Volume){
                V=Volume;
            }
            if (Math.round(Math.abs(FP.peek().getVolume() - V)) < 1) {
                // 进料包转运结束
                FP.remove();
            } else {
                // 进料包递减
                FP.peek().setVolume(FP.peek().getVolume() - V);
            }

            //转运操作ODT

            List<Double> tran=new ArrayList<>();
            tran.add(4.0);
            tran.add((double)TK);
            tran.add((double) Math.round(back.getTime() * 100.0) / 100.0);
            tran.add((double) Math.round((back.getTime() + V/Speed) * 100.0) / 100.0);
            tran.add((double)OilType);
            tran.add(Speed);
            back.getSchedulePlan().add(tran);
            back.setTime(tran.get(3));
            //炼油操作ODF

            double endTime=getFeedEndTime(back.getSchedulePlan(), back.getTime(), DS);
            List<Double> refin=new ArrayList<>();
            refin.add((double)DS);
            refin.add((double)TK);
            refin.add((double) Math.round(endTime * 100.0) / 100.0);
            refin.add((double) Math.round((endTime + V/ DSFR[DS - 1]) * 100.0) / 100.0);
            refin.add((double)OilType);
            back.getSchedulePlan().add(refin);

            back.getFeedTime()[DS-1]=refin.get(3);

            if(isFinish(back)){
                back.setFlag(true);
            }

            return true;
        }

        return false;
    }

    public static boolean isFinish(BackTrace back){
        if(back.getFP().isEmpty() || (back.getFP().get(1).isEmpty() &&back.getFP().get(2).isEmpty() && back.getFP().get(3).isEmpty())){
            return true;
        }
        return false;
    }

    public static double getSafeV(BackTrace back, int DS, double PIPEFR){
        double curTime=back.getTime();

        double endTime=getFeedEndTime(back.getSchedulePlan(), back.getTime(), DS);

        double V=PIPEFR*(endTime-curTime-RT);
        //System.out.println("SafeV:"+V);
        return V;

    }

    public static double getFeedEndTime(List<List<Double>> schedulePlan, double curTime , double DS){
        //获得DS的炼油记录
        List<Double> list=schedulePlan.stream()
                .filter(e->e.get(0)==DS).map(e->e.get(3)).collect(Collectors.toList());
        double endTime=Double.MAX_VALUE;
        if(list!=null && !list.isEmpty()){
            endTime=Collections.max(list);
        }
        return endTime;
    }

    public static  boolean stopPipe(BackTrace back, double stopTime){
        List<Double> list=new ArrayList<>();
        list.add(4.0);
        list.add(0.0);
        list.add(back.getTime());
        list.add(stopTime);
        list.add(0.0);
        back.getSchedulePlan().add(list);
        back.setTime(list.get(3));

        return true;
    }
    public static double getStopTime(BackTrace backTrace){
        //ODF 蒸馏塔  油罐  开始st 结束st 原油类型
        double t=Double.MAX_VALUE;

        Map<Double,List<List<Double>>> collect=backTrace.getSchedulePlan().stream()
                .filter(e->e.get(0)<=3 && e.get(3)>backTrace.getTime())
                .collect(Collectors.groupingBy(e->e.get(1)));

        for(Double d:collect.keySet()){
            List<List<Double>> list=collect.get(d);
            double min = Collections.max(list, (e1, e2) -> (int) Math.ceil(e1.get(3) - e2.get(3))).get(3);
            if(t>min){
                t=min;
            }
        }
        return t;

    }

    public static List<Integer> getET(BackTrace backTrace) {
        List<Integer> ET = new ArrayList<>();

        int length = backTrace.getTKS().length;
        for (int i = 0; i < length; i++) {
            int tk = i + 1;
            List<List<Double>> ops = new ArrayList<>();
            for (int j = 0; j < backTrace.getSchedulePlan().size(); j++) {
                List<Double> op = backTrace.getSchedulePlan().get(j);
                if (op.get(1) == tk) {
                    ops.add(op);
                }
            }
            List<List<Double>> opCollections = ops.stream()
                    .sorted((e1, e2) -> (int) Math.ceil(Math.abs(e1.get(3) - e2.get(3))))
                    .filter(e -> e.get(3) > backTrace.getTime())    // 过滤结束时间大于当前时间的操作
                    .collect(Collectors.toList());

            if (opCollections.isEmpty()) {
                ET.add(tk);
            }
        }
        return ET;
    }

    public static List<Integer> getUD(BackTrace backTrace){
        List<Integer> UD=new ArrayList<>();

        Map<Integer,Queue<KeyValue>> FPs=backTrace.getFP();

        for(int i=0;i<FPs.size();i++){
            int ds=i+1;

            if(FPs.containsKey(ds) && !FPs.get(ds).isEmpty()){
                UD.add(ds);
            }
        }
        return UD;
    }

    private static void doODF(List<List<Double>> schedulePlan, double[] DSFET, int ds, int type, int TK, double tkv){
        double Temp=DSFET[ds-1];
        DSFET[ds-1]=Temp+tkv/DSFR[ds-1];
        List<Double> list=new ArrayList<>();
        list.add((double)ds);
        list.add((double)TK);
        list.add((double) Math.round(Temp * 100.0) / 100.0);            // 开始供油t
        list.add((double) Math.round(DSFET[ds - 1] * 100.0) / 100.0);
        list.add((double)type);
        schedulePlan.add(list);
    }

    public static void PaperCost(){
        //计算论文中的各个成本
        int[][] pipCost1= new int[][]{
                { 0,  11, 12, 13,  7,  15,  9, 10},
                {10,   0,  9, 12, 13,   7, 11, 11,},
                {13,   8,  0,  7, 12,  13, 12, 13},
                {13,  12,  7,  0, 11,  12, 13, 12},
                { 7,  13, 12, 11,  0,  11, 10, 15},
                {15,   7, 13, 12, 11,	0,  8,  9},
                { 9,  11, 12, 13, 10,	8,  0, 15},
                {10,  11, 13, 12, 15,	9, 15,  0}

        };// 管道混合成本
        int[][] tankCost1 = new int[][]{
                { 0, 11, 12, 13, 10, 15,  9, 14},
                {11,  0, 11, 12, 13, 10, 14, 12},
                {12, 11,  0, 10, 12, 13, 10, 13},
                {13, 12, 10,  0, 11, 12, 12, 11},
                {10, 13, 12, 11,  0, 11, 13,  9},
                {15, 10, 13, 12, 11,  0, 11, 15},
                { 9, 14, 10, 12, 13, 11,  0, 10},
                {14, 12, 13, 11,  9, 15, 10,  0}
        };// 罐底混合成本

        //1.生成DDF    DS  TK  st end OilType
        List<List<Double>> Plan=new ArrayList<>();
        List<List<Double>> ds1=new ArrayList<>();
        List<List<Double>> ds2=new ArrayList<>();
        List<List<Double>> ds3=new ArrayList<>();
        List<List<Double>> odt=new ArrayList<>();
        // type分割  type1 type2
        int[][] OilType=new int[][]{
                {2, 1, 5},
                {2, 6, 7},
                {1, 3, 4}
        };
        //各个蒸馏塔的炼油任务
        double[][] s1=new double[][]{
                {8000, 16000, 15504,  5496, 10008, 15504, 7008},
                {2, 3, 2, 3, 10, 2, 3}
        };

        double[][] s2=new double[][]{
                {16000,  5000, 14496, 14496, 14496, 7992},
                {6, 4, 5, 6, 5, 6}
        };

        double[][] s3=new double[][]{
                {30000, 30000, 30000, 30000, 30000},
                {7, 9, 7, 9, 7}
        };

        double[][] pipe=new double[][]{
                {30000, 14496, 15504, 30000, 14496, 5496, 10008, 30000, 14496, 15504, 30000, 7992, 7008}, //转运油量
                {9, 5, 2, 7, 6, 3, 10, 9, 5, 2, 7, 6, 3},//油罐
                {4, 7, 1, 4, 7, 1, 5, 4, 7, 5, 4, 7, 5} //原油类型
        };
        double t1=0, t2=0,t3=0, tp=0;

        for(int i=0;i<s1[0].length;i++){
            List<Double> list=new ArrayList<>();
            list.add(1.0);
            list.add(s1[1][i]);
            list.add(t1);
            list.add(Math.round((t1+s1[0][i]/DSFR[0])*100.0)/100.0);
            if(ds1.size()>=OilType[0][0]){
                list.add((double)OilType[0][2]);
            }else{
                list.add((double)OilType[0][1]);
            }
            t1=list.get(3);
            ds1.add(list);
            Plan.add(list);
        }

        for(int i=0;i<s2[0].length;i++){
            List<Double> list=new ArrayList<>();
            list.add(2.0);
            list.add(s2[1][i]);
            list.add(t2);
            list.add(Math.round((t2+s2[0][i]/DSFR[1])*100.0)/100.0);
            if(ds2.size()>=OilType[1][0]){
                list.add((double)OilType[1][2]);
            }else{
                list.add((double)OilType[1][1]);
            }
            t2=list.get(3);
            ds2.add(list);
            Plan.add(list);
        }

        for(int i=0;i<s3[0].length;i++){
            List<Double> list=new ArrayList<>();
            list.add(3.0);
            list.add(s3[1][i]);
            list.add(t3);
            list.add(Math.round((t3+s3[0][i]/DSFR[2])*100.0)/100.0);
            if(ds3.size()>=OilType[2][0]){
                list.add((double)OilType[2][2]);
            }else{
                list.add((double)OilType[2][1]);
            }
            t3=list.get(3);
            ds3.add(list);
            Plan.add(list);
        }

        // 2.生成ODT   DS  TK  st end OilType  speed
        for(int i=0;i<pipe[0].length;i++){
            List<Double> temp=new ArrayList<>();
            temp.add(4.0);
            temp.add(pipe[1][i]);
            temp.add(tp);
            temp.add(Math.round((tp+pipe[0][i]/1250)*100.0)/100.0);
            temp.add(pipe[2][i]);
            temp.add(1250.0);
            tp=temp.get(3);
            odt.add(temp);
            Plan.add(temp);
        }

        double  f1=TestFun.dsChange(Plan);
        double  f2=TestFun.pipeMix(Plan, pipCost1);
        double  f3=TestFun.tankMix(Plan,tankCost1);
        double  f4=TestFun.gEnergyCost(Plan, new double[]{1,2,3}, PIPEFR);
       // PlotUtils.plotSchedule2(Plan);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("------------PaperCost------------");
        System.out.println("DS_Change:         " + f1);
        System.out.println("pipe_Cost:         " + f2);
        System.out.println("Tank_Cost:         " + f3);
        System.out.println("Energy_Cost:       " + f4);
        System.out.println();

    }
}
