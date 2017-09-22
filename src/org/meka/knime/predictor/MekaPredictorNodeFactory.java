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
 *   10.09.2007 (cebron): created
 */
package org.meka.knime.predictor;

import org.knime.core.node.DynamicNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.meka.knime.classifier.MekaClassifierNodeModel;

/**
 * NodeFactory for the Meka Predictor Node.
 * 
 * @author cebron, University of Konstanz
 */
public class MekaPredictorNodeFactory extends
        NodeFactory<MekaPredictorNodeModel> {
	//DynamicNodeFactory<MekaPredictorNodeModel>{

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new MekaPredictorNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MekaPredictorNodeModel createNodeModel() {
        return new MekaPredictorNodeModel();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public MekaPredictorNodeView createNodeView(final int viewIndex,
            final MekaPredictorNodeModel nodeModel) {
        return new MekaPredictorNodeView((MekaPredictorNodeModel)nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDialog() {
        return true;
    }

}
