/**
 * @Author : wzdnzd
 * @Time :  2021-07-27
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.plot;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.uma.jmetal.algorithm.multiobjective.mogwo.WolfSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.errorchecking.JMetalException;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ScatterPlot extends JFrame {
    private static final Dimension DEFAULT_CONTENT_SIZE = new Dimension(800, 480);
    private static volatile ChartPanel chartPanel;
    private static volatile JFreeChart chart;

    /**
     * Creates a new test app.
     *
     * @param title the frame title.
     */
    public ScatterPlot(String title, List<Pair<String, List<WolfSolution>>> archives) {
        super(title);
        addWindowListener(new ExitOnClose());
        getContentPane().add(createPanel(archives));
    }

    /**
     * Returns a panel containing the content for the demo.  This method is
     * used across all the individual demo applications to allow aggregation
     * into a single "umbrella" demo (OrsonChartsDemo).
     *
     * @return A panel containing the content for the demo.
     */
    public JPanel createPanel(List<Pair<String, List<WolfSolution>>> archives) {
        XYDataset dataset = createDataset(archives);
        chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(DEFAULT_CONTENT_SIZE);
        return chartPanel;
    }

    /**
     * Creates a scatter chart based on the supplied dataset.
     *
     * @param dataset the dataset.
     * @return A scatter chart.
     */
    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createScatterPlot("Solution Distribute", "f1", "f2", dataset);

        chart.setBackgroundPaint(Color.white);
        chart.setBorderPaint(Color.GREEN);
        chart.setBorderStroke(new BasicStroke(1.5f));
        XYPlot xyplot = (XYPlot) chart.getPlot();

        xyplot.setBackgroundPaint(new Color(255, 253, 246));
        ValueAxis axis1 = xyplot.getDomainAxis();
        axis1.setAxisLineStroke(new BasicStroke(1.5f));

        ValueAxis axis2 = xyplot.getDomainAxis(0);
        axis2.setAxisLineStroke(new BasicStroke(1.5f));

        axis2.setAxisLineStroke(new BasicStroke(1.5f));
        axis2.setAxisLinePaint(new Color(215, 215, 215));
        xyplot.setOutlineStroke(new BasicStroke(1.5f));
        axis2.setLabelPaint(new Color(10, 10, 10));
        axis2.setTickLabelPaint(new Color(102, 102, 102));
        ValueAxis axis3 = xyplot.getRangeAxis();
        axis3.setAxisLineStroke(new BasicStroke(1.5f));

        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot.getRenderer();
        xylineandshaperenderer.setSeriesOutlinePaint(0, Color.WHITE);
        xylineandshaperenderer.setUseOutlinePaint(true);
        NumberAxis numberaxis = (NumberAxis) xyplot.getDomainAxis();
        numberaxis.setAutoRangeIncludesZero(false);
        numberaxis.setTickMarkInsideLength(2.0F);
        numberaxis.setTickMarkOutsideLength(0.0F);
        numberaxis.setAxisLineStroke(new BasicStroke(1.5f));

        return chart;
    }

    /**
     * Creates a sample dataset (hard-coded for the purpose of keeping the
     * demo self-contained - in practice you would normally read your data
     * from a file, database or other source).
     *
     * @return A sample dataset.
     */
    private XYDataset createDataset(List<Pair<String, List<WolfSolution>>> archives) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        if (archives == null || archives.isEmpty()) {
            return dataset;
        }

        for (Pair<String, List<WolfSolution>> pair : archives) {
            if (pair == null || StringUtils.isBlank(pair.getLeft()) || pair.getRight() == null) {
                JMetalLogger.logger.warning("found invalid dataset, please recheck");
                continue;
            }

            String key = pair.getLeft();
            List<WolfSolution> solutions = pair.getRight();
            XYSeries series = createSeries(key, solutions);
            dataset.addSeries(series);
        }

        return dataset;
    }

    private XYSeries createSeries(String name, List<WolfSolution> wolves) {
        if (StringUtils.isBlank(name) || wolves == null || wolves.isEmpty()) {
            throw new JMetalException("invalid arguments, name and wolves cannot be null when create series");
        }

        XYSeries s = new XYSeries(name);
        int dim = wolves.get(0).objectives().length;
        if (dim != 2) {
            throw new JMetalException("the dimension of objectives must be 2");
        }

        for (WolfSolution wolf : wolves) {
            if (wolf == null || wolf.objectives() == null) {
                continue;
            }

            double[] objectives = wolf.objectives();
            s.add(objectives[0], objectives[1]);
        }

        return s;
    }

    public void draw() {
        setSize(800, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void refresh(List<Pair<String, List<WolfSolution>>> archives) {
        chart.getXYPlot().setDataset(createDataset(archives));
        chartPanel.repaint();
    }
}
