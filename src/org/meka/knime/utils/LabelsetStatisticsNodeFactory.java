package org.meka.knime.utils;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "LabelsetStatistics" Node.
 * 
 *
 * @author Fernando Benites
 */
public class LabelsetStatisticsNodeFactory 
        extends NodeFactory<LabelsetStatisticsNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LabelsetStatisticsNodeModel createNodeModel() {
        return new LabelsetStatisticsNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<LabelsetStatisticsNodeModel> createNodeView(final int viewIndex,
            final LabelsetStatisticsNodeModel nodeModel) {
        return new LabelsetStatisticsNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new LabelsetStatisticsNodeDialog();
    }

}

