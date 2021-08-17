package org.uma.jmetal.problem.multiobjective.OilSchedule;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestFun {
    /**
     * 根据塔分配油罐
     */

    public static int getTK(int ds, List<Integer> ET, BackTrace back) {
        List<Integer> ET1 = new ArrayList<>();
        int TK = 0;
        if (ds == 1) {
            for (int i = 0; i < ET.size(); i++) {
                if (ET.get(i) == 2 || ET.get(i) == 3 || ET.get(i) == 10) {
                    ET1.add(ET.get(i));
                }
            }

        } else if (ds == 2) {
            for (int i = 0; i < ET.size(); i++) {
                if (ET.get(i) == 6 || ET.get(i) == 5) {
                    ET1.add(ET.get(i));
                }
            }

        } else {
            for (int i = 0; i < ET.size(); i++) {
                if (ET.get(i) == 7 || ET.get(i) == 9) {
                    ET1.add(ET.get(i));
                }
            }

        }
        if (!ET1.isEmpty()) {
            TK = ET1.get(getInt(back.getX()[2 * back.getStep()], ET1.size() - 1));
        }

        return TK;

    }

    /**
     * 只返回本次蒸馏塔能使用对应的油罐集合
     */

    public static List<Integer> getTKDS(List<Integer> ET, int DS) {
        Map<Integer, List<Integer>> DS_TKs = new HashMap<>();
        List<Integer> fET = new ArrayList<>();
        for (int i = 0; i < ET.size(); i++) {
            fET.add(ET.get(i));
        }

        List<Integer> ds1 = new ArrayList<>();
        ds1.add(2);
        ds1.add(3);
        ds1.add(10);
        DS_TKs.put(1, ds1);

        List<Integer> ds2 = new ArrayList<>();
        ds2.add(5);
        ds2.add(6);
        ds2.add(10);
        DS_TKs.put(2, ds2);

        List<Integer> ds3 = new ArrayList<>();
        ds3.add(7);
        ds3.add(9);
        ds3.add(10);
        DS_TKs.put(3, ds3);

        List<Integer> TankNeed = DS_TKs.get(DS);
        fET.retainAll(TankNeed);

        return fET;

    }

    public static void sort(double[][] ob, int[] order) {
        Arrays.sort(ob, (o1, o2) -> {    //降序排列
            double[] one = (double[]) o1;
            double[] two = (double[]) o2;
            for (int i = 0; i < order.length; i++) {
                int k = order[i];
                if (one[k] > two[k]) return 1; //返回值大于0，将前一个目标值和后一个目标值交换
                else if (one[k] < two[k]) return -1;
                else continue;
            }
            return 0;
        });
    }

    /**
     * 返回一个0-len之间的整数
     *
     * @param a
     * @param len
     * @return
     */
    public static int getInt(double a, int len) {
        double[] sequence = getHDsequence(0.29583, 1000);
        int index = (int) (Math.ceil(a * 1000));
        if (index == 1000) {
            index = 0;
        }
        if (a != 0) {
            a = sequence[index];  //ceil向无穷大方向取整,将随机产生的数转换为混沌序列
        }
        if (a == 1) {
            return len;
        } else {
            return (int) (Math.floor((len + 1) * a)); //取整（舍掉小数）
        }
    }

    public static double[] getHDsequence(double x0, int num) {  //获取混沌序列
        double[] r = new double[num];
        double xn = x0;
        for (int i = 0; i < num; i++) {
            xn = 4 * xn * (1 - xn);
            r[i] = xn;
        }
        return r;
    }

    public static double getMax(double[] array) { //取得一维数组中的最大值
        double max = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * 计算供油罐个数
     *
     * @param a 调度计划
     * @return
     */
    public static double gNum(List<List<Double>> a) {
        // ODF格式：蒸馏塔号 | 油罐号 | 开始供油时间 | 结束供油时间 | 原油类型
        // ODT格式：蒸馏塔号 | 油罐号  | 开始供油时间 | 结束供油时间 | 原油类型 | 转运速度
        return a.stream().filter(e -> e.get(0) <= 3).map(e -> e.get(1)).distinct().collect(Collectors.toList()).size();
    }

    public static double gChange(Object[][] TKS, List<List<Double>> a) { //油罐切换次数
        // 初始装有的原油类型    TKS格式：容量  原油类型  已有容量 蒸馏塔  供油开始时间 供油结束时间  供油罐编号  混合原油类型集合
        Map<String, Integer> res = new HashMap<>();
        for (int i = 0; i < TKS.length; i++) {
            if (Integer.parseInt(TKS[i][2].toString()) > 0) {
                String key = "TK" + (i + 1);
                if (!res.containsKey(key)) {
                    res.put(key, 0);
                }
                res.put(key, res.get(key) + 1);
            }
        }

        // 后期转运的原油类型      ODT格式：蒸馏塔号 | 油罐号 | 开始供油时间 | 结束供油时间 | 原油类型
        List<List<Double>> lists = a.stream()
                .filter(e -> e.get(0) == 4 && e.get(4) != 0)        // 过滤出转运记录，排除停运
                .sorted((e1, e2) -> (int) (e1.get(2) - e2.get(2)))  // 按照转运开始时间排序
                .collect(Collectors.toList());
        for (int i = 0; i < lists.size(); i++) {
            int tk = (int) lists.get(i).get(1).doubleValue();
            String key = "TK" + tk;
            if (!res.containsKey(key)) {
                res.put(key, 0);
            }
            res.put(key, res.get(key) + 1);
        }

        // 统计各个油罐的使用次数
        int count = 0;
        for (String key : res.keySet()) {
            count += res.get(key);
        }

        return count;
    }

    public static int dsChange(List<List<Double>> schedule) {
        List<Double> TKS1 = schedule.stream().filter(e -> e.get(0) <= 3).map(e -> e.get(1)).collect(Collectors.toList());

        Map<String, Integer> res = new HashMap<>();

        for (int i = 0; i < TKS1.size(); i++) {
            String key = "TK" + (int) Math.floor(TKS1.get(i));
            if (!res.containsKey(key)) {
                res.put(key, 0);
            }
            res.put(key, res.get(key) + 1);
        }
        int count = 0;
        for (String key : res.keySet()) {
            count += res.get(key);
        }
        return count;
    }

    /**
     * 计算管道混合成本
     *
     * @param a
     * @param pipeMix
     * @return
     */
    public static double pipeMix(List<List<Double>> a, int[][] pipeMix) {
        int[][] m1 = new int[8][8];//存储混合次数
        double pipeCost = 0;
        List<Double> mix = a.stream()
                .filter(e -> e.get(0) > 3 && e.get(4) > 0)//过滤停运和塔的炼油记录
                .map(e -> e.get(4))
                .collect(Collectors.toList());//取出管道混合的一行

        for (int i = 0; i < mix.size() - 1; i++) {
            if (!(Double.toString(mix.get(i)).equals(Double.toString(mix.get(i + 1))))) {
                double m, n;
                m = mix.get(i);
                n = mix.get(i + 1);
                m1[(int) m - 1][(int) n - 1]++;
            }
        }
        for (int i = 0; i < pipeMix.length; i++) {
            for (int j = 0; j < pipeMix[0].length; j++) {
                pipeCost = pipeCost + pipeMix[i][j] * m1[i][j];
            }
        }
        return pipeCost;
    }

    /**
     * 计算罐底混合成本
     *
     * @param a
     * @param tankC
     * @return
     */
    public static double tankMix(List<List<Double>> a, int[][] tankC) {
        //思路：统计所有ODF按照油罐分组即可。  格式：DS(4)  Tank  st endt type
        double[][] tankN = new double[6][6]; //存放各个类型油的混合次数

        Map<Double, List<List<Double>>> tankk = a.stream().filter(e -> e.get(0) < 4).collect(Collectors.groupingBy(e -> e.get(1)));

        // 统计各个油罐的混合次数
        for (Double key : tankk.keySet()) {
            List<List<Double>> oilTypes = tankk.get(key);
            for (int i = 0; i < oilTypes.size() - 1; i++) {
                double preType = oilTypes.get(i).get(4);
                double nextType = oilTypes.get(i + 1).get(4);
                int preType1 = (int) preType;
                int nextType1 = (int) nextType;
                if (preType1 != nextType1) {
                    tankN[preType1 - 1][nextType1 - 1]++;
                }
            }
        }
        // 计算混合成本
        double sum = 0;
        for (int i = 0; i < tankN.length; i++) {
            for (int j = 0; j < tankN[0].length; j++) {
                sum += tankN[i][j] * tankC[i][j];
            }
        }
        return sum;
    }

    /**
     * 计算管道的转运能耗
     *
     * @param plan        调度计划
     * @param costPerHour 单位时间能耗
     * @param PIPEFR      管道转运速度
     * @return
     */
    public static double gEnergyCost(List<List<Double>> plan, double[] costPerHour, double[] PIPEFR) {

        double result = 0.0;

        // 后期转运的原油类型      ODT格式：蒸馏塔号(1,2,3,4) | 油罐号 | 开始供油时间 | 结束供油时间 | 原油类型  |  转运速度
        List<List<Double>> list = plan.stream()
                .filter(e -> e.get(0) > 3 && e.get(4) > 0)        // 过滤出转运记录&&排除停运
                .sorted((e1, e2) -> (int) (e1.get(2) - e2.get(2)))  // 按照转运开始时间排序
                .collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            double speed = list.get(i).get(5).doubleValue();
            int ind = getIndex(PIPEFR, speed);
            double cost = costPerHour[ind];
            double start = list.get(i).get(2).doubleValue();
            double end = list.get(i).get(3).doubleValue();
            // 成本 = 单位时间成本 * 总时间
            result += cost * (end - start);
        }

        return (int) result;
    }

    /**
     * 获取某一个元素所在数组的位置
     *
     * @param arr
     * @param value
     * @return
     */
    public static int getIndex(double[] arr, double value) {
        for (int i = 0; i < arr.length; i++) {
            if (Math.abs(arr[i] - value) < 0.0001) {
                return i;
            }
        }
        return -1;//如果未找到返回-1
    }

    public static double sum(double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum = sum + array[i];
        }
        return sum;
    }

    /**
     * 提取数字
     *
     * @param line
     * @return
     */
    public static List<Double> getNumber(String line) {
        String regEx = "([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])";
        Matcher matcher = Pattern.compile(regEx).matcher(line);
        List<String> res = new ArrayList<>();
        while (matcher.find()) {
            String tmp = matcher.group();//tmp为括号中的内容，您可以自己进行下一步的处理
            res.add(tmp);
        }
        return res.stream().mapToDouble(e -> Double.parseDouble(e)).boxed().collect(Collectors.toList());
    }

    /**
     * 提取字符串中的数字
     */
    public static double matchNumber(String a) {
        String regEX = "[^0-9]";
        Pattern p = Pattern.compile(regEX);
        Matcher m = p.matcher(a);
        String str = m.replaceAll("").trim();
        return Double.parseDouble(str);
    }

    public static void main(String[] args) {
        getNumber("This order was 234.35placed for QT3000! OK?").forEach(e -> System.out.println(e));
    }

    /**
     * 判断原油组合顺序是否可以交换
     *
     * @param text 组合
     * @return
     */
    private static boolean judge(String text) {
        if (text == null || "".equals(text.trim())) {
            return false;
        }

        String[] words = text.split("=");
        if (words.length != 2) {
            return false;
        }

        words = words[1].split(":");
        return words.length == 2;
    }

    /**
     * 原油组合交换实现
     *
     * @param text 原组合
     * @return 交换顺序后的组合
     */
    private static String reverse(String text) {
        String[] words = text.split("=");
        String[] metas = words[0].split(":");
        metas = swap(metas, 2, 3);
        String[] rates = words[1].split(":");
        rates = swap(rates, 0, 1);

        String prefix = String.join(":", metas);
        String suffix = String.join(":", rates);

        return prefix + "=" + suffix;
    }

    /**
     * 调换数组任意两个位置的值
     *
     * @param arrays 数组
     * @param i      索引
     * @param j      索引
     * @return
     */
    private static String[] swap(String[] arrays, int i, int j) {
        if (arrays == null
                || arrays.length < 2
                || i < 0 || j < 0
                || i >= arrays.length
                || j >= arrays.length) {
            throw new RuntimeException("invalid arguments");
        }

        String tmp = arrays[i];
        arrays[i] = arrays[j];
        arrays[j] = tmp;

        return arrays;
    }

    /**
     * 生成所有组合
     *
     * @param ctks  原始组合
     * @param index 数组索引
     * @param list  结果列表
     */
    public static void combine(Object[][] ctks, int index, List<Object[][]> list) {
        if (ctks == null || index < 0 || index >= ctks.length) {
            return;
        }

        if (list == null) {
            list = new ArrayList<>();
        }

        if (index == 0) {
            list.add(copyOf(ctks));
        }

        String text = String.valueOf(ctks[index][1]);
        boolean flag = judge(text);
        if (!flag) {
            combine(ctks, index + 1, list);
            return;
        }

        Object[][] copy = copyOf(ctks);//把原来的数组copy
        copy[index][1] = reverse(text);

        // 必须是深拷贝，否则list存的值是最终的数组，数据全部一样
        list.add(copyOf(copy));

        combine(ctks, index + 1, list);
        combine(copy, index + 1, list);
    }

    /**
     * 二维数组打印
     *
     * @param arrays 数组
     */
    public static void print(Object[][] arrays) {
        for (Object[] object : arrays) {
            int length = object.length;
            String[] contents = new String[length];
            for (int j = 0; j < length; j++) {
                contents[j] = String.valueOf(object[j]);
            }

            System.out.println(String.join(", ", contents));
        }
    }

    /**
     * 二维数组的深拷贝CTKS
     *
     * @param arrays 数组
     * @return
     */
    private static Object[][] copyOf(Object[][] arrays) {
        if (arrays == null || arrays.length == 0) {
            return null;
        }

        int m = arrays.length;
        int n = arrays[0].length;

        Object[][] objects = new Object[m][n];

        for (int i = 0; i < m; i++) {
            System.arraycopy(arrays[i], 0, objects[i], 0, n);
        }

        return objects;
    }


}
