package org.meka.knime.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.knime.core.node.NodeView;
import org.meka.knime.predictor.MekaPredictorNodeModel;

/**
 * <code>NodeView</code> for the "LabelsetStatistics" Node.
 * 
 *
 * @author Fernando Benites
 */
public class LabelsetStatisticsNodeView extends NodeView<LabelsetStatisticsNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link LabelsetStatisticsNodeModel})
     */
    public static enum ViewMode {
    	SUMMARY
    };
    private JTabbedPane m_showpanel;
    private ViewMode[] m_viewmodes;
    private LabelsetStatisticsNodeModel m_model;
    protected LabelsetStatisticsNodeView(final LabelsetStatisticsNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        m_model = super.getNodeModel();
        
        assert m_model != null;
        m_showpanel = new JTabbedPane();
        m_showpanel.addTab("Summary", drawSummary());
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
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

    private JScrollPane drawSummary() {

        JPanel drawpanel = new JPanel();
        JTextArea summary = new JTextArea(20, 40);
        if (m_model != null) {
            try {
                summary.setText("Dataset-Summary: \n"
                        + "Cardinality:"+m_model.LCard+"\n"+
                         "Cardinality train:"+m_model.LCardtr+"\n"+
                         "Cardinality test:"+m_model.LCardts+"\n"+
                                 "Unique label combinations :"+m_model.uniquelabels+"\n"+
                         "Unique label combinations train:"+m_model.uniquelabelstr+"\n"+
                         "Unique label combinations test:"+m_model.uniquelabelsts+"\n"+
                         "Label Density tr:"+m_model.LDensitytr+"\n"+
                         "Label Density ts:"+m_model.LDensityts+"\n"+
                         "Label Density:"+m_model.LDensity+"\n"+
                         "Number rows tr:"+m_model.nrrowstr+"\n"+
                         "Number rows ts:"+m_model.nrrowsts+"\n"+
                         "Number rows:"+m_model.nrrows+"\n"
                		);
            } catch (NullPointerException npe) {
                summary.setText("");
            }

        }
        drawpanel.add(summary);
        return new JScrollPane(drawpanel);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

