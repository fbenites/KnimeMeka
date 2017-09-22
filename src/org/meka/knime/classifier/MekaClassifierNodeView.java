/*
 * ------------------------------------------------------------------ *
 * Copyright by 
 * University of Konstanz, Germany.
 * Chair for Bioinformatics and Information Mining
 * Prof. Dr. Michael R. Berthold
 *
 * This file is part of the WEKA integration plugin for KNIME.
 *
 * The MEKA integration plugin is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St., Fifth Floor, Boston, MA 02110-1301, USA.
 * Or contact us: contact@knime.org.
 * ---------------------------------------------------------------------
 *
 * History
 *   07.09.2007 (cebron): created
 */
package org.meka.knime.classifier;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.StringReader;
import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeView;
import org.meka.knime.classifier.MekaClassifierNodeModel;
import org.meka.knime.classifier.MekaClassifierNodeView;

import weka.classifiers.Classifier;
import weka.classifiers.Sourcable;
import weka.core.AdditionalMeasureProducer;
import weka.core.Drawable;
import weka.core.Summarizable;
import weka.gui.graphvisualizer.BIFFormatException;
import weka.gui.graphvisualizer.GraphVisualizer;
import weka.gui.treevisualizer.Node;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeBuild;
import weka.gui.treevisualizer.TreeVisualizer;

/**
 *
 * @author cebron, University of Konstanz
 */
public class MekaClassifierNodeView extends NodeView<MekaClassifierNodeModel> {

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(MekaClassifierNodeView.class);

    private final JTabbedPane m_showpanel;

    /**
     *
     * @param model The underlying {@link MekaClassifierNodeModel}
     */
    public MekaClassifierNodeView(final MekaClassifierNodeModel model) {
        super(model);
        m_showpanel = new JTabbedPane();
        super.setComponent(m_showpanel);
    }

    /*
     * Draws the summary from the classifier into a JTextArea.
     */
    private JScrollPane drawSummary(final Classifier classifier) {

        JPanel drawpanel = new JPanel();
        JTextArea summary = new JTextArea(20, 40);
        if (classifier != null) {
            try {
                summary.setText("WEKA-Summary: \n"
                        + ((Summarizable)classifier).toSummaryString());
            } catch (NullPointerException npe) {
                summary.setText("");
            }

        }
        drawpanel.add(summary);
        return new JScrollPane(drawpanel);
    }

    /*
     * Draws the SourceCode obtained from the classifier to a JTextArea.
     */
    private JScrollPane drawSource(final Classifier classifier) {
        JPanel drawpanel = new JPanel();
        JTextArea source = new JTextArea(20, 40);
        try {
            source.setText("MEKA-Source: \n"
                    + ((Sourcable)classifier).toSource("MekaSource"));
        } catch (Exception e) {
            LOGGER.info("Could not convert to source");
        }
        drawpanel.add(source);
        return new JScrollPane(drawpanel);
    }

    /** Draws additional measures to a JTextArea. */
    private JScrollPane drawAdditionalMeasures(final Classifier classifier) {
        Enumeration<String> e =
                ((AdditionalMeasureProducer)classifier).enumerateMeasures();
        StringBuilder sb = new StringBuilder();
        while (e.hasMoreElements()) {
            String measurename = e.nextElement();
            double measure = 0;
            try {
                measure =
                        ((AdditionalMeasureProducer)classifier)
                                .getMeasure(measurename);
                sb.append(measurename + ": " + measure);
                sb.append("\n");
            } catch (NullPointerException npe) {
                /*
                 * Since this code is copied from weka this bug could be replicated into Meka
                 * this is a weka-bug. We ignore the NullPointerException that
                 * is thrown if you try to get a measure and the weka-classifier
                 * is not yet executed
                 */
            }

        }
        return drawText(sb.toString());
    }

    /*
     * Draws a given text to a JTextArea.
     */
    private JScrollPane drawText(final String text) {
        JPanel drawpanel = new JPanel();
        JTextArea source = new JTextArea(20, 40);
        source.setText(text);
        drawpanel.add(source);
        return new JScrollPane(drawpanel);
    }

    /*
     * Draws a graph obtained from the classifier to a JPanel.
     */
    private JScrollPane drawGraph(final Classifier classifier) {
        String strGraph = "";
        try {
            strGraph = ((Drawable)classifier).graph();
            if (strGraph.equals("<!--No model built yet-->")) {
                return new JScrollPane();
            }
        } catch (Exception e) {
            LOGGER.info("Could not get graph");
            return new JScrollPane();
        }
        if (((Drawable)classifier).graphType() == Drawable.TREE) {
            TreeBuild tb = new TreeBuild();
            Node n = tb.create(new StringReader(strGraph));
            TreeVisualizer tv = new TreeVisualizer(null, n, new PlaceNode2());
            tv.setPreferredSize(new Dimension(500, 300));
            return new JScrollPane(tv);
        }
        if (((Drawable)classifier).graphType() == Drawable.BayesNet) {
            GraphVisualizer gv = new GraphVisualizer();
            try {
                gv.readBIF(strGraph);
            } catch (BIFFormatException e) {
                LOGGER.error("", e);
            }
            gv.layoutGraph();
            return new JScrollPane(gv);
        }
        return new JScrollPane();
    }

    /** {@inheritDoc} */
    @Override
    protected void onOpen() {
    }

    /** {@inheritDoc} */
    @Override
    protected void modelChanged() {
        Classifier classifier = getNodeModel().getClassifier();
        if (classifier != null) {
            m_showpanel.removeAll();
            // there always seems to be something useful in the toString method
            m_showpanel.addTab("Meka Output", drawText(classifier.toString()));

            /* classifier can draw their result as a graph */
            if (classifier instanceof Drawable) {
                m_showpanel.addTab("Graph", drawGraph(classifier));
            }
            /*
             * Most classifiers have at least one view that contains the summary
             * string.
             */
            if (classifier instanceof Summarizable) {
                m_showpanel.addTab("Summary", drawSummary(classifier));
            }
            /*
             * Some classifiers are able to produce Java Source of their
             * results.
             */
            if (classifier instanceof Sourcable) {
                m_showpanel.addTab("Source", drawSource(classifier));
            }
            /*
             * Some classifiers can produce measures other than those calculated
             * by evaluation modules.
             */
            if (classifier instanceof AdditionalMeasureProducer) {
                m_showpanel.addTab("Additional Measures",
                        drawAdditionalMeasures(classifier));
            }

            // bug 3154: fix too large view with weka panels
            final Dimension screenSize =
                    Toolkit.getDefaultToolkit().getScreenSize();
            final Dimension panelSize;
            Component selectedComp = m_showpanel.getSelectedComponent();
            if (selectedComp != null) {
                panelSize = selectedComp.getPreferredSize();
            } else {
                panelSize = m_showpanel.getPreferredSize();
            }
            m_showpanel.setPreferredSize(new Dimension(Math.min(
                    panelSize.width + 25, screenSize.width), Math.min(
                    panelSize.height + 30, screenSize.height - 100)));
            super.setComponent(m_showpanel);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        m_showpanel.removeAll();
    }

}
