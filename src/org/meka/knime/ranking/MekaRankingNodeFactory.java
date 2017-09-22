package org.meka.knime.ranking;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MekaPerformance" Node.
 * 
 *
 * @author Waqar
 */
public class MekaRankingNodeFactory 
        extends NodeFactory<MekaRankingNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MekaRankingNodeModel createNodeModel() {
        return new MekaRankingNodeModel();
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
    public NodeView<MekaRankingNodeModel> createNodeView(final int viewIndex,
            final MekaRankingNodeModel nodeModel) {
        return new MekaRankingNodeView(nodeModel);
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
        return new MekaRankingNodeDialog();
    }

}

