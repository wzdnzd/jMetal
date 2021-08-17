/**
 * @Author : wzdnzd
 * @Time :  2021-07-27
 * @Project : jMetal
 */

package org.uma.jmetal.algorithm.multiobjective.mogwo.plot;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ExitOnClose extends WindowAdapter {
    @Override
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
}
