package org.uma.jmetal.problem.multiobjective.OilSchedule.gante;

import org.ejml.data.DenseMatrix64F;
import org.uma.jmetal.problem.multiobjective.OilSchedule.TestFun;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CanvasGante extends Canvas {
    private static final long serialVersionUID = 1L;

    private final static int margin_left = 20;// 图像左边距
    private final static int margin_top = 50;// 图像上边距
    private final static int margin_right = 120;// 图像右边距
    private final static int margin_buttom = 40;// 图像下边距

    private final static int label_width = 40;// 任务标签
    private final static int block_width = 40;// 块高度
    private final static int block_height = 25;// 块高度
    private final static int num_x_divide = 10;// x轴刻度个数

    private double[][] data;

    public double[][] getData() {
        return data;
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    // 颜色数组，以区分不同的原油类型
//    private final static Color[] colors = {
//            new Color(204, 204, 204),
//            new Color(192, 255, 32),
//            new Color(153, 0, 102),
//            new Color(50, 205, 50),
//            new Color(51, 51, 51),
//            new Color(0, 153, 255),
//            new Color(51, 51, 153),
//            new Color(0, 0, 255),
//            new Color(255,165,0),
//            new Color(21, 116, 168),
//            new Color(255, 0, 0)
//    };

    private final static Color[] colors = {
            new Color(220, 220, 220),
            new Color(135, 206, 235),
            new Color(255, 165, 0),
            new Color(255, 182, 193),
            new Color(160, 32, 240),
            new Color(255, 250, 173),
            new Color(144,238,0144),
            new Color(0, 191, 255),
            new Color(205, 197, 191),
            new Color(21, 116, 168),
            new Color(0, 153, 255)
    };

    /**
     * 重量级组件：    重写update方法
     * 轻量级组件：    尽快调用paint方法
     * canvas为重量级组件
     *
     * @param g
     */
    public void update(Graphics g) {
        super.update(g);
        drawGante(g);
    }

    /**
     * 绘制甘特图
     *
     * @param g
     */
    public void drawGante(Graphics g) {
        if (data != null) {
            // 切记，这里要获取Graphics的长和宽，而不是Form的长和宽
            Rectangle rectangle = g.getClipBounds();
            int width = rectangle.width;
            int height = rectangle.height;

            // 设置字体和线的宽度
            g.setFont(new Font("Times new roman", Font.PLAIN, 14));

            // 设置背景色为白色
            g.setColor(Color.white);
            g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

            DenseMatrix64F dataMatrix = new DenseMatrix64F(data);
            double[] col_max = MatrixHelper.getColMax(dataMatrix).data;
            int numOfOilType = (int) col_max[4];// 原油种类数
            int maxTime = (int) col_max[3];// 最后的任务的完成时间
            int numOfTasks = (int) col_max[0];// 任务的种类数

            double scale_x = MathUtil.round(1.0 * (width - margin_left - margin_right - label_width) / maxTime, 3);// 图像大小变化尺度
            double scale_y = MathUtil.round(1.0 * (height - margin_top - margin_buttom - label_width) / 600 * 100, 3);// 图像大小变化尺度【参考原始比例】

            Map<Integer, Double> flags = new HashMap<>();

            // 绘制详细的调度数据
            for (int i = 0; i < data.length; i++) {
                // 使用不同的颜色填充封闭的矩形区域
                int ds = (int) (data[i][0] - 1);
                double start = data[i][2];
                double end = data[i][3];
                int color = (int) data[i][4];
                //int numberOfPumpGroups = (int) data[i][5];

                // 计算矩形区域所在位置和宽度
                int data_x = (int) MathUtil.round(margin_left + label_width + MathUtil.multiply(start, scale_x), 0).doubleValue();
                int data_y = (int) MathUtil.round(margin_top + MathUtil.multiply(ds, scale_y), 0).doubleValue();
                int data_width = (int) MathUtil.round(MathUtil.multiply(MathUtil.subtract(end, start), scale_x), 0).doubleValue();
                // 不显示停运
                if(color>0){
                    if (ds<3) {
                        //填充矩形区域
                        MyFillRect(g, data_x, data_y, data_width, block_height, Color.white, colors[color-1], 10, 0);
                    }else{
                        int ind= TestFun.getIndex(new double[]{840, 1250, 1370},data[i][5]);
                        MyFillRect(g, data_x, data_y, data_width, block_height, Color.black, colors[color-1], 10, ind+1);
                    }
                }
            }


            // 绘制详细的调度数据的边界和标签
            for (int i = 0; i < data.length; i++) {
                // 使用不同的颜色填充封闭的矩形区域
                int color = (int) data[i][4] - 1;
                int tank = (int) data[i][1];
                int ds = (int) (data[i][0] - 1);
                double start = data[i][2];
                double end = data[i][3];
                // 计算矩形区域所在位置和宽度
                int data_x = (int) MathUtil.round(margin_left + label_width + MathUtil.multiply(start, scale_x), 0).doubleValue();
                int data_y = (int) MathUtil.round(margin_top + MathUtil.multiply(ds, scale_y), 0).doubleValue();
                int data_width = (int) MathUtil.round(MathUtil.multiply(MathUtil.subtract(end, start), scale_x), 0).doubleValue();
                // 不显示停运
                if (color >= 0) {
                    g.setColor(Color.black);

                    // 绘制边框
                    if (!flags.containsKey(ds) || Math.abs(flags.get(ds) - start) > 1) {
                        g.drawLine(data_x, data_y, data_x, data_y + block_height);
                    }

                    g.drawLine(data_x + data_width, data_y, data_x + data_width, data_y + block_height);
                    flags.put(ds, end);

                    g.drawLine(data_x, data_y, data_x + data_width, data_y);
                    g.drawLine(data_x, data_y + block_height, data_x + data_width, data_y + block_height);

                    // 绘制标签
                    g.setFont(new Font("Times new Roman", Font.BOLD, 14));
                    g.drawString("TK" + tank + "", data_x, data_y);
                }
            }

            // 标识x轴刻度
            for (int i = 0; i < num_x_divide; i++) {
                int x1 = (int) (margin_left + label_width
                        + (i + 1) * (width - margin_left - margin_right - label_width) / num_x_divide);
                int y1 = (int) (margin_top + numOfTasks * scale_y - 10);
                int x2 = (int) (margin_left + label_width
                        + (i + 1) * (width - margin_left - margin_right - label_width) / num_x_divide);
                int y2 = (int) (margin_top + numOfTasks * scale_y);

                g.setColor(Color.black);
                g.drawLine(x1, y1, x2, y2);
                g.drawString((i + 1) * maxTime / 10.0 + "", x2 - 15, y2 + 15);
            }

            // 标识y轴刻度
            for (int i = 0; i < numOfTasks; i++) {
                int x = margin_left;
                int y = (int) (margin_top + 1.0 * i * scale_y + block_height / 1.5);
                if (i < 3) {
                    g.drawString("DS" + (i + 1) + "", x, y);
                } else {
                    g.drawString("PIPE" + "", x, y);
                }
            }

            // 绘制坐标轴
            int x1 = margin_left + label_width;
            int y1 = (int) (margin_top + numOfTasks * scale_y);
            int x2 = width - margin_right;
            int y2 = (int) (margin_top + numOfTasks * scale_y);
            g.setColor(Color.black);
            g.drawLine(x1, y1, x2, y2);// x轴
            int x3 = margin_left + label_width;
            int y3 = margin_top;
            int x4 = margin_left + label_width;
            int y4 = (int) (margin_top + numOfTasks * scale_y);
            g.drawLine(x3, y3, x4, y4);// y轴

            // 绘制legend
            // 计算矩形区域所在位置和宽度
            int x = width - margin_right + 20;
            int y = margin_top;
            MyColorLegend(g, x, y, 11);
            x = margin_left;
            y = height - margin_buttom;
            MyNumberOfpumpGroupsLegend(g, x, y, 3);
        }
    }

    /**
     * 绘制legend
     * @param g
     * @param x
     * @param y
     */
    public void MyColorLegend(Graphics g, int x, int y, int oilTypes){

        int betweenDistance = 4;
        int block_offset_top = -9;
        int text_offset_top = 10;
        // 绘制颜色
        for (int i = 0; i < oilTypes; i++) {

            // 使用不同的颜色填充封闭的矩形区域
            g.setColor(colors[i]);
            g.fillRect(x + 20, y + i * (block_height + betweenDistance) + block_offset_top, block_width, block_height);
            g.setColor(Color.black);
            g.drawRect(x + 20, y + i * (block_height + betweenDistance) + block_offset_top, block_width, block_height);

            // 设置标签
            int oilType = i + 1;
            g.setColor(Color.black);
            g.drawString("#" + oilType, x + block_width + 30, y + i * (block_height + betweenDistance) + text_offset_top);
        }
    }

    /**
     * 绘制legend
     * @param g
     * @param x
     * @param y
     * @param numberOfPumpGroups
     */
    public void MyNumberOfpumpGroupsLegend(Graphics g, int x, int y, int numberOfPumpGroups){
        // 绘制开启的泵的组数
        int x_offset = 25;
        int y_offset = 20;
        int betweenDistance = 100;
        for (int i = 0; i < numberOfPumpGroups; i++) {

            int xb = x + i * betweenDistance;
            // 设置标签
            g.setColor(Color.black);
            g.setFont(new Font("Times new roman", Font.BOLD, 14));
            g.drawString("V" + (i+1), xb, y + y_offset);

            // 使用不同的颜色填充封闭的矩形区域
            MyFillRect(g, xb + x_offset, y, block_width, block_height, Color.BLACK, Color.GRAY, 10, i+1);
        }
    }

    /**
     * 功能：在矩形的中间画条纹
     *
     * @param g          绘图对象
     * @param x          起点x
     * @param y          起点y
     * @param width      矩形宽度
     * @param height     矩形高度
     * @param front_color           前景色
     * @param background_color      背景色
     * @param line_width 条纹宽度
     * @param style      条纹类型，style==0时，不会绘制底纹
     */
    public void MyFillRect(Graphics g, int x, int y, int width, int height, Color front_color, Color background_color, int line_width, int style) {
        // 填充矩形区域
        g.setColor(background_color);
        g.fillRect(x, y, width, height);
        g.setColor(front_color);
        // 画底纹
        // style==0时，不会绘制底纹
        if (style == 1) {
            for (int i = 0; i < width - 2; i++) {
                for (int j = 0; j < height - 2; j++) {
                    if ((i + j) % line_width == 0){
                        g.fillOval(x + i, y + j, 2, 2);
                    }
                }
            }
        } else if (style == 2) {
            for (int i = 1; i < width - 2; i++) {
                for (int j = 1; j < height - 2; j++) {
                    if (i % (line_width / 2) == 0 || j % (line_width / 2) == 0){
                        g.fillOval(x + i, y + j, 2, 2);
                    }
                }
            }
        } else if (style == 3) {
            for (int i = 0; i < width - 2; i++) {
                for (int j = 0; j < height - 2; j++) {
                    if ((i + j) % line_width == 0){
                        //设置条纹宽度
                        g.fillOval(x + i, y + height - j - 2, 2, 2);
                    }
                }
            }
        }
    }
}