/*
 * ------------------------------------------------------------------
 * Copyright, 2003 - 2011
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
 *   17.09.2007 (cebron): created
 */
package org.meka.knime.predictor;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.node.ExecutionMonitor;

/**
 * This class generates an appended column with a fake classification in order
 * to get the meka-classifier to work.
 * 
 * @author cebron, University of Konstanz
 */

public class FakeClassFactory implements CellFactory {

    private DataColumnSpec m_classcolspec;

    /**
     * @param classcolspec The class column spec to append.
     */
    FakeClassFactory(final DataColumnSpec classcolspec) {
        m_classcolspec = classcolspec;
    }

    /**
     * {@inheritDoc}
     */
    public DataCell[] getCells(final DataRow row) {
        DataCell[] append = new DataCell[1];
        append[0] = DataType.getMissingCell();
        return append;
    }

    /**
     * {@inheritDoc}
     */
    public DataColumnSpec[] getColumnSpecs() {
        DataColumnSpecCreator colspecCreator =
                new DataColumnSpecCreator(m_classcolspec);
        return new DataColumnSpec[]{colspecCreator.createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    public void setProgress(final int curRowNr, final int rowCount,
            final RowKey lastKey, final ExecutionMonitor exec) {
        exec.setProgress((double)curRowNr / (double)rowCount);
    }
}
