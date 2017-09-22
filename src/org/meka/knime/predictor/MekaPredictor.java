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
 *   15.09.2007 (cebron): created
 */
package org.meka.knime.predictor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import meka.classifiers.multilabel.MultiLabelClassifier;

import org.knime.base.data.util.DataCellStringMapper;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.DoubleBarRenderer;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.ext.weka37.utils.WekaConverter;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This class generates the appended column(s) with the classification from the
 * underlying weka classifier..
 * 
 * @author cebron, University of Konstanz
 */
public class MekaPredictor implements CellFactory {

    /*
     * NodeLogger for this class.
     */
    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(MekaPredictor.class);
    
    /*
     * Meka-Classifier
     */
    private MultiLabelClassifier m_classifier;

    /*
     * A kind of DataTableSpec for the meka-row.
     */
    private final Instances m_mekainst;

    /*
     * Flag indicating whether a winner value column for nominal class
     * attributes will be produced.
     */
    private boolean m_winnercol;

    /*
     * DataColumnSpec of the class column.
     */
    private DataColumnSpec[] m_classcolspec;

    /*
     * DataCellStringMapper that is able to convert DataCells to Strings and the
     * other way around.
     */
    private DataCellStringMapper m_mapper;

    /* Has a warning message been printed in getAppendedCell? */
    private boolean m_hasPrintedWarning = false;

    /* The positions / ordering of the input DataCells. */
    private int[] m_positions;

    /**
     * A new AppendedColumnFactory that uses the classification algorithm from
     * the <code>MekaNodeModel</code> to produce a new Meka Classification
     * column.
     * 
     * @param classifier the meka-classifier to use.
     * @param mekainst The 'meka-DataTableSpec'.
     * @param classcolspec the {@link DataColumnSpec} of the class column.
     * @param winnercol flag indicating whether a winner value column should be
     *            appended (for nominal class attributes).
     * @param mapper a {@link DataCellStringMapper} created during training to
     *            map DataCells to Strings and the other way around.
     * @param positions the ordering of the input columns.
     */
    MekaPredictor(final MultiLabelClassifier classifier, final Instances mekainst,
            final DataColumnSpec[] classcolspec, final boolean winnercol,
            final DataCellStringMapper mapper, final int[] positions) {
        m_classifier = (MultiLabelClassifier) classifier;
        m_mekainst = mekainst;
        m_classcolspec = classcolspec;
        m_winnercol = winnercol;
        m_mapper = mapper;
        m_positions = positions;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings("null")
    public DataCell[] getCells(final DataRow row) {
        Instance tempinstance =
                WekaConverter.convertToInstance(row, m_mekainst, m_mapper,
                        m_positions);
        DataCell[] append = null;
        /*Since we have multiple predictions for one attribute we are
         * having an array of data types
         * */
        DataType[] type = new DataType[m_classcolspec.length];        
        for(int index = 0; index < m_classcolspec.length; index ++){
        	try {        	
                type[index] = m_classcolspec[index].getType();
                if (type.equals(DoubleCell.TYPE) || type.equals(IntCell.TYPE)) {
                    // Regression
                    double[] classnr = m_classifier.distributionForInstance(tempinstance);
                    
                    append = new DataCell[classnr.length];
                    for(int i=0;i<classnr.length;i++){
                    	append[i]=new DoubleCell(classnr[i]);
                    }
                } else if (type.equals(StringCell.TYPE)) {
                    if (m_classcolspec[index].getDomain().hasValues()) {
                        // Classification with distributions
                        double[] distribution =
                                m_classifier.distributionForInstance(tempinstance);
                        int nrCells =
                                (m_winnercol) ? distribution.length + 1
                                        : distribution.length;
                        append = new DataCell[nrCells];
                        for (int d = 0; d < distribution.length; d++) {
                            append[d] = new DoubleCell(distribution[d]);
                        }
                        //Checking for winner column and if there is any appending it to the return string
                        if (m_winnercol) {
                            double classnr =
                                    m_classifier.classifyInstance(tempinstance);
                            Attribute att =
                                    m_mekainst.attribute(m_mekainst.classIndex());
                            String wekastr = att.value((int)classnr);
                            append[append.length - 1] =
                                    m_mapper.stringToDataCell(wekastr);
                        }
                    } else {
                        double classnr =
                                m_classifier.classifyInstance(tempinstance);
                        Attribute att =
                                m_mekainst.attribute(m_mekainst.classIndex());
                        String wekastr = att.value((int)classnr);
                        append = new DataCell[]{m_mapper.stringToDataCell(wekastr)};
                    }
                } else {
                    if (!m_hasPrintedWarning) {
                        m_hasPrintedWarning = true;
                        LOGGER.warn("Unknown type: " + type
                                + ", suppress further warnings");
                    }
                    append[0] = DataType.getMissingCell();
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }       
        
        return append;
    }

    /**
     * {@inheritDoc}
     */
    public DataColumnSpec[] getColumnSpecs() {
    	//Taking an array since we would have multiple predicted class types
    	DataType[] classtype = new DataType[m_classcolspec.length];
    	for(int i = 0; i < m_classcolspec.length; i ++){
        	classtype[i] = m_classcolspec[i].getType();
        }
    	
        DataColumnSpec[] colspecs = null;
        DataColumnSpecCreator colspecCreator = null;
        //Checking compatibility with loop as we have multiple classes 
        for(int i = 0; i < m_classcolspec.length; i ++){
        	if (classtype[i].isCompatible(DoubleValue.class)
                    || classtype[i].isCompatible(IntValue.class)) {
                colspecCreator =
                        new DataColumnSpecCreator("Meka Regression : "
                                + m_classcolspec[i].getName(), DoubleCell.TYPE);
                colspecs = new DataColumnSpec[]{colspecCreator.createSpec()};
            } else if (classtype[i].isCompatible(StringValue.class)) {
                DataColumnDomain domain = m_classcolspec[i].getDomain();
                if (domain.hasValues()) {
                    // domain values are sorted to guarantee correct order
                    Set<DataCell> values = domain.getValues();
                    DataCell[] valuesarr = new DataCell[values.size()];
                    valuesarr = values.toArray(valuesarr);
                    DataValueComparator comp = classtype[i].getComparator();
                    Arrays.sort(valuesarr, comp);
                    int nrNewColumns = valuesarr.length;
                    if (m_winnercol) {
                        nrNewColumns++;
                    }
                    colspecs = new DataColumnSpec[nrNewColumns];
                    for (int d = 0; d < valuesarr.length; d++) {
                        colspecCreator =
                                new DataColumnSpecCreator(valuesarr[d].toString(),
                                        DoubleCell.TYPE);
                        colspecCreator
                                .setProperties(new DataColumnProperties(
                                        Collections
                                                .singletonMap(
                                                        DataValueRenderer.PROPERTY_PREFERRED_RENDERER,
                                                        DoubleBarRenderer.DESCRIPTION)));
                        colspecCreator.setDomain(new DataColumnDomainCreator(
                                new DoubleCell(0.0), new DoubleCell(1.0))
                                .createDomain());
                        colspecs[d] = colspecCreator.createSpec();
                    }
                    if (m_winnercol) {
                        colspecCreator =
                                new DataColumnSpecCreator("Winner", StringCell.TYPE);
                        colspecs[colspecs.length - 1] = colspecCreator.createSpec();
                    }
                } else {
                    colspecCreator =
                            new DataColumnSpecCreator("Meka Classification: "
                                    + m_classcolspec[i].getName(), classtype[i]);
                    colspecs = new DataColumnSpec[]{colspecCreator.createSpec()};
                }
        }
        
        
        }
        return colspecs;
    }

    /**
     * {@inheritDoc}
     */
    public void setProgress(final int curRowNr, final int rowCount,
            final RowKey lastKey, final ExecutionMonitor exec) {
        exec.setProgress((double)curRowNr / (double)rowCount, "Classifying");
    }
}
