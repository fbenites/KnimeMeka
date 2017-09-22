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
 *   17.09.2007 (cebron): created
 */
package org.meka.knime.predictor;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

/**
 * NodeDialog for the Weka Predictor Node.
 * 
 * @author cebron, University of Konstanz
 */
public class MekaPredictorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * Constructor.
     */
    MekaPredictorNodeDialog() {
        this.addDialogComponent(new DialogComponentBoolean(
                new SettingsModelBoolean(MekaPredictorNodeModel.WINNERCOL_KEY,
                        true),
                "(If class attribute is nominal:) Append Winner Value"));
    }
}
