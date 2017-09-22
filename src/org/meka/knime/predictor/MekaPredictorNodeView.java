/*
 * ------------------------------------------------------------------
 * Copyright, 2003 - 2011
 * University of Konstanz, Germany.
 * Chair for Bioinformatics and Information Mining
 * Prof. Dr. Michael R. Berthold
 *
 * This file is part of the WEKA integration plugin for KNIME.
 *
 * The WEKA integration plugin is free software; you can redistribute
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
package org.meka.knime.predictor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeView;

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
 * NodeView for the Weka Predictor.
 *
 * @author cebron, University of Konstanz
 */
public class MekaPredictorNodeView extends NodeView<MekaPredictorNodeModel> {

    /**
     * Enumeration for all possible views that a weka classifier can provide.
     */
    public static enum ViewMode {
        /**
         * Field value indicating whether the underlying classifier is able to
         * provide an evaluation-view.
         */
        EVAL,
        /**
         * Field value indicating whether the underlying classifier is able to
         * provide a summary-view.
         */
        SUMMARY,
        /**
         * Field value indicating whether the underlying classifier is able to
         * provide a source code-view.
         */
        SOURCE,
        /**
         * Field value indicating whether the underlying classifier is able to
         * provide a graph-view.
         */
        DRAW,
        /**
         * Field value indicating whether the underlying classifier is able to
         * provide additional measures.
         */
        ADDITIONALMEASURES
    };

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(MekaPredictorNodeView.class);

    private MekaPredictorNodeModel m_model;

    private Classifier m_classifier;

    private JTabbedPane m_showpanel;

    private ViewMode[] m_viewmodes;

    /**
     *
     * @param model The underlying {@link MekaPredictorNodeModel}
     */
    public MekaPredictorNodeView(final MekaPredictorNodeModel model) {
        super(model);
        m_model = model;
        m_classifier = m_model.getClassifier();
        Vector<ViewMode> views = new Vector<ViewMode>();
        /*
         * Evaluation.
         */
        if (model.getEvaluation() != null) {
            views.add(ViewMode.EVAL);
        }
        /*
         * Some classifier can draw their result as a graph
         */
        if (m_classifier instanceof Drawable) {
            views.add(ViewMode.DRAW);
        }

        /*
         * Most classifiers have at least one view that contains the summary
         * string.
         */
        if (m_classifier instanceof Summarizable) {
            views.add(ViewMode.SUMMARY);
        }
        /*
         * Some classifiers are able to produce Java Source of their results.
         */
        if (m_classifier instanceof Sourcable) {
            views.add(ViewMode.SOURCE);
        }
        /*
         * Some classifiers can produce measures other than those calculated by
         * evaluation modules.
         */
        if (m_classifier instanceof AdditionalMeasureProducer) {
            views.add(ViewMode.ADDITIONALMEASURES);
        }
        m_viewmodes = new ViewMode[views.size()];
        m_viewmodes = views.toArray(m_viewmodes);

        m_showpanel = new JTabbedPane();
        super.setComponent(m_showpanel);
    }

    /** Draws the summary from the classifier into a JTextArea. */
    private JScrollPane drawSummary() {

        JPanel drawpanel = new JPanel();
        JTextArea summary = new JTextArea(20, 40);
        if (m_classifier != null) {
            try {
                summary.setText("MEKA-Summary: \n"
                        + ((Summarizable)m_classifier).toSummaryString());
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
    private JScrollPane drawSource() {
        JPanel drawpanel = new JPanel();
        JTextArea source = new JTextArea(20, 40);
        try {
            source.setText("WEKA-Source: \n"
                    + ((Sourcable)m_classifier).toSource("WekaSource"));
        } catch (Exception e) {
            LOGGER.info("Could not convert to source");
        }
        drawpanel.add(source);
        return new JScrollPane(drawpanel);
    }

    /** Draws additional measures to a JTextArea. */
    private JScrollPane drawAdditionalMeasures() {
        Enumeration<String> e =
                ((AdditionalMeasureProducer)m_classifier).enumerateMeasures();
        StringBuilder sb = new StringBuilder();
        while (e.hasMoreElements()) {
            String measurename = e.nextElement();
            double measure = 0;
            try {
                measure =
                        ((AdditionalMeasureProducer)m_classifier)
                                .getMeasure(measurename);
                sb.append(measurename + ": " + measure);
                sb.append("\n");
            } catch (NullPointerException npe) {
                /*
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
    private JScrollPane drawGraph() {
        String strGraph = "";
        try {
            strGraph = ((Drawable)m_classifier).graph();
            if (strGraph.equals("<!--No model built yet-->")) {
                return new JScrollPane();
            }
        } catch (Exception e) {
            LOGGER.info("Could not get graph");
            return new JScrollPane();
        }
        if (((Drawable)m_classifier).graphType() == Drawable.TREE) {
            TreeBuild tb = new TreeBuild();
            Node n = tb.create(new StringReader(strGraph));
            TreeVisualizer tv = new TreeVisualizer(null, n, new PlaceNode2());
            tv.setPreferredSize(new Dimension(500, 300));
            return new JScrollPane(tv);
        }
        if (((Drawable)m_classifier).graphType() == Drawable.BayesNet) {
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

    /*
     * Draws the Evaluation of the classifier into a JTextArea.
     */
    private JScrollPane drawEval() {
        JPanel drawpanel = new JPanel();
        JTextArea eval = new JTextArea(20, 40);
        if (m_model.getEvaluation() != null) {
        	String evaluation = formatEvaluation(m_model.getEvaluation());
            eval.setText("Evaluation: \n" + evaluation);
        } else {
            eval.setText("No Class column - no eval.");
        }
        drawpanel.add(eval);
        return new JScrollPane(drawpanel);
    }
    //This method formats the evaluation string for Node View
    private String formatEvaluation(String evaluation) {
    	String eval =  evaluation;
    	if (eval.contains(","))
            eval = eval.replace(",", " \n");
        
    	
		eval = eval.replace("{", "");
		eval = eval.replace("}", "");   			
    	
    	return eval;
	}
	/**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        m_model = super.getNodeModel();
        m_classifier = m_model.getClassifier();
        if (m_classifier == null) {
            // if there's no classifier - there's nothing to draw
            return;
        }
        m_showpanel = new JTabbedPane();
        for (ViewMode mode : m_viewmodes) {
            switch (mode) {
            case SUMMARY:
                m_showpanel.addTab("Summary", drawSummary());
                break;
            case SOURCE:
                m_showpanel.addTab("Source", drawSource());
                break;
            case DRAW:
                m_showpanel.addTab("Graph", drawGraph());
                break;
            case ADDITIONALMEASURES:
                m_showpanel.addTab("Additional Measures",
                        drawAdditionalMeasures());
                break;
            case EVAL:
                m_showpanel.addTab("Evaluation", drawEval());
                break;
            }
        }
        //there always seems to be something useful in the toString method...
        //m_showpanel.addTab("Meka Output", drawText(m_classifier.toString()));
        m_showpanel.addTab("Meka Output", drawText(m_classifier.toString()));
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
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        m_showpanel = new JTabbedPane();
        m_classifier = null;
    }

}
