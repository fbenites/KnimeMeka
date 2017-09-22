/*
 * ------------------------------------------------------------------
 * Copyright by
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
package org.meka.knime.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import org.knime.base.data.util.DataCellStringMapper;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;

import weka.associations.Associator;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This utility class covers all operations necessary to convert the data from
 * Knime to weka.
 *
 * @author cebron, University of Konstanz
 */
public class MekaConverter {

    /*
     * DataTableSpec.
     */
    private DataTableSpec m_spec;

    /*
     * Mapping between DataCells and corresponding strings.
     */
    private DataCellStringMapper m_mapper;

    /**
     * Constructor.
     *
     * @param spec DataTableSpec of the table to convert.
     */
    public MekaConverter(final DataTableSpec spec) {
        m_spec = spec;
        m_mapper = new DataCellStringMapper();
    }

    /**
     * Tests the classifier-capabilities on the given attributes.
     *
     * @param classifier weka-classifier to check.
     * @param classattribute the class attribute
     * @return String array containing error messages if the attribute test
     *         failed, otherwise an empty string.
     */
    public String[] testAttributes(final Classifier classifier,
            final String classattribute) {
        Attribute[] attributes = createWekaAttributes();
        String[] fail = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            Capabilities cap = classifier.getCapabilities();
            if (attributes[i].name().equals(
                    m_mapper.origStringToString(classattribute))) {
                cap.test(attributes[i], true);
                Exception e = cap.getFailReason();
                fail[i] = (e == null) ? "" : e.getMessage();

            } else {
                cap.test(attributes[i]);
                Exception e = cap.getFailReason();
                fail[i] = (e == null) ? "" : e.getMessage();
            }
        }
        return fail;
    }
    public String[] testAttributes(final Clusterer clusterer) {
        Attribute[] attributes = createWekaAttributes();
        String[] fail = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            Capabilities cap = clusterer.getCapabilities();
            cap.test(attributes[i]);
            Exception e = cap.getFailReason();
            fail[i] = (e == null) ? "" : e.getMessage();
        }
        return fail;
    }
    /**
     * Tests the associator-capabilities on the given attributes.
     *
     * @param associator weka-associator to check.
     * @param classattribute the class attribute
     * @return String array containing error messages if the attribute test
     *         failed, otherwise an empty string.
     */
    public String[] testAttributes(final Associator associator,
            final String classattribute) {
        Attribute[] attributes = createWekaAttributes();
        String[] fail = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            Capabilities cap = associator.getCapabilities();
            if (attributes[i].name().equals(
                    m_mapper.origStringToString(classattribute))) {
                cap.test(attributes[i], true);
                Exception e = cap.getFailReason();
                fail[i] = (e == null) ? "" : e.getMessage();

            } else {
                cap.test(attributes[i]);
                Exception e = cap.getFailReason();
                fail[i] = (e == null) ? "" : e.getMessage();
            }
        }
        return fail;
    }

    /**
     * Tests the clusterer-capabilities on the given attributes.
     *
     * @param clusterer weka-clusterer to check.
     * @param classattributes the class attribute
     * @return String array containing error messages if the attribute test
     *         failed, otherwise an empty string.
     */
    
    public String[] testAttributes(Classifier classifier,
			Set<String> invalidIncludeColumns) {
    	Attribute[] attributes = createWekaAttributes();
    	String[] fail = new String[attributes.length];
    	@SuppressWarnings("rawtypes")
		Iterator it = invalidIncludeColumns.iterator();
    	for (int i = 0; i < attributes.length; i++) {
            Capabilities cap = classifier.getCapabilities();
            if (attributes[i].name().equals(
                    m_mapper.origStringToString(it.next().toString()))) {
                cap.test(attributes[i], true);
                Exception e = cap.getFailReason();
                fail[i] = (e == null) ? "" : e.getMessage();

            } else {
                cap.test(attributes[i]);
                Exception e = cap.getFailReason();
                fail[i] = (e == null) ? "" : e.getMessage();
            }
        }
        return fail;
	}

    /*
     * Creates the weka attributes.
     */
    private Attribute[] createWekaAttributes() {
        int numCols = m_spec.getNumColumns();
        Attribute[] attInfo = new Attribute[numCols];

        for (int c = 0; c < numCols; c++) {
            DataColumnSpec colspec = m_spec.getColumnSpec(c);
            DataType colType = colspec.getType();
            // after pre-checking, the colType should be one of these
            assert (colType.isCompatible(DoubleValue.class)
                    || colType.isCompatible(IntValue.class) || colType
                        .isCompatible(StringValue.class));

            if (!colType.isCompatible(IntValue.class)
                    && !colType.isCompatible(DoubleValue.class)
                    && !colType.isCompatible(StringValue.class)) {
                throw new IllegalStateException("Can only parse Double, Int,"
                        + " and String columns for WEKA-processing");
            }

            if ((colType.isCompatible(DoubleValue.class))
                    || (colType.isCompatible(IntValue.class))) {
                String attributename =
                        m_mapper.origStringToString(colspec.getName());
                attInfo[c] = new Attribute(attributename);
            }

            if (colType.isCompatible(StringValue.class)) {
                String attributename =
                        m_mapper.origStringToString(colspec.getName());
                // check for nominal values
                FastVector myNominalValues = null;
                if (colspec.getDomain().hasValues()) {
                    myNominalValues = new FastVector();
                    // domain values are sorted to guarantee correct order
                    Set<DataCell> values =
                            m_spec.getColumnSpec(c).getDomain().getValues();
                    DataCell[] valuesarr = new DataCell[values.size()];
                    valuesarr = values.toArray(valuesarr);
                    DataValueComparator comp =
                            m_spec.getColumnSpec(c).getType().getComparator();
                    Arrays.sort(valuesarr, comp);

                    for (int n = 0; n < valuesarr.length; n++) {
                        myNominalValues.addElement(m_mapper
                                .dataCellToString(valuesarr[n]));
                    }
                    attInfo[c] = new Attribute(attributename, myNominalValues);
                } else {
                    attInfo[c] = new Attribute(attributename, myNominalValues);
                }
            }
        }
        return attInfo;
    }

    /**
     * Converts the {@link BufferedDataTable} to weka-Instances. Note: works
     * only with columns of type {@link IntValue}, {@link DoubleValue} and
     * {@link StringValue}, otherwise an {@link IllegalStateException} is
     * thrown.
     *
     * @param bdt the {@link BufferedDataTable} to convert.
     * @param exec ExecutionMonitor to show progress
     * @return Instances from weka.
     * @throws CanceledExecutionException if the conversion is canceled by the
     *             user.
     * @throws IllegalStateException if the DataTable to convert contains
     *             illegal column types.
     */
    public Instances convertToMeka(final BufferedDataTable bdt,
            final ExecutionMonitor exec) throws CanceledExecutionException,
            IllegalStateException {

        DataTableSpec inSpec = bdt.getDataTableSpec();
        int numOfCols = inSpec.getNumColumns();

        // check that all column types are compatible
        for (int c = 0; c < numOfCols; c++) {
            DataColumnSpec colspec = inSpec.getColumnSpec(c);
            DataType colType = colspec.getType();
            if (!colType.isCompatible(IntValue.class)
                    && !colType.isCompatible(DoubleValue.class)
                    && !colType.isCompatible(StringValue.class)) {
                throw new IllegalStateException("Can only parse Double, Int,"
                        + " and String columns for WEKA-processing");
            }
        }

        // make list of attributes
        FastVector attInfo = new FastVector();
        for (Attribute att : createWekaAttributes()) {
            attInfo.addElement(att);
        }
        Instances wekaInstances = new Instances("Weka-Instances", attInfo, 0);

        // create a Weka-Instance from each DataRow, add it to Instances
        int nrRows = bdt.getRowCount();
        int count = 0;
        for (DataRow row : bdt) {
            Instance tempinstance =
                    convertToInstance(row, wekaInstances, m_mapper);
            wekaInstances.add(tempinstance);
            count++;
            exec.setProgress((double)count / (double)nrRows,
                    "Converting Instances: " + count + " of " + nrRows);
            exec.checkCanceled();
        }
        return wekaInstances;
    }

    /**
     * During the conversion, a mapping between DataCells and Strings is
     * created, which can be obtained here.
     *
     * @return the mapping from DataCells to Strings.
     */
    public DataCellStringMapper getMapping() {
        return m_mapper;
    }

    /**
     * Converts a DataRow into a Weka Instance with the given Instances as a
     * kind of DataTableSpec.
     *
     * @param row the {@link DataRow} to convert.
     * @param inst the weka-instances associated with the produced instance
     * @param mapper a {@link DataCellStringMapper} mapping DataCell values to
     *            weka-strings.
     * @return new weka instance.
     */
    public static Instance convertToInstance(final DataRow row,
            final Instances inst, final DataCellStringMapper mapper) {
        Instance tempinstance = new DenseInstance(inst.numAttributes());
        tempinstance.setDataset(inst);
        for (int i = 0; i < inst.numAttributes(); i++) {
            if (i < row.getNumCells() && !row.getCell(i).isMissing()) {
                DataCell cell = row.getCell(i);
                if (cell instanceof DoubleValue) {
                    tempinstance.setValue(i,
                            ((DoubleValue)cell).getDoubleValue());
                }
                if (cell instanceof StringValue) {
                    tempinstance.setValue(i, mapper.dataCellToString(cell));
                }
            } else {
                // missing cell
                tempinstance.setMissing(i);
            }
        }
        return tempinstance;
    }

    /**
     * Converts a DataRow into a Weka Instance with the given Instances as a
     * kind of DataTableSpec.
     *
     * @param row the {@link DataRow} to convert.
     * @param inst the weka-instances associated with the produced instance
     * @param mapper a {@link DataCellStringMapper} mapping DataCell values to
     *            weka-strings.
     * @param order the order in which the {@link DataCell}s of the
     *            {@link DataRow} should be processed.
     * @return new weka instance.
     */
    public static Instance convertToInstance(final DataRow row,
            final Instances inst, final DataCellStringMapper mapper,
            final int[] order) {
        Instance tempinstance = new DenseInstance(inst.numAttributes());
        tempinstance.setDataset(inst);
        for (int i = 0; i < order.length; i++) {
            if (order[i] != -1 && !row.getCell(order[i]).isMissing()) {
                DataCell cell = row.getCell(order[i]);
                if (cell instanceof DoubleValue) {
                    tempinstance.setValue(i,
                            ((DoubleValue)cell).getDoubleValue());
                }
                if (cell instanceof StringValue) {
                    tempinstance.setValue(i, mapper.dataCellToString(cell));
                }
            } else {
                // missing cell
                tempinstance.setMissing(i);
            }
        }
        return tempinstance;
    }

    /**
     * Light-weight html parser to get rid of html-tags in the weka classifier
     * description
     *
     * @param html text with html-tags
     *
     * @return a list of paragraphs with html-tags stripped. A new paragraph is
     *         assumed, if two <br>
     *         -tags are found in series.
     */
    public static List<String> extractText(final String html) {
        if (html == null) {
            return new ArrayList<String>();
        }
        final StringBuffer text = new StringBuffer();
        final ArrayList<String> res = new ArrayList<String>();

        ParserDelegator parserDelegator = new ParserDelegator();
        ParserCallback parserCallback = new ParserCallback() {

            private int numBRs = 0;

            /**
             * {@inheritDoc}
             */
            @Override
            public void handleText(final char[] data, final int pos) {
                text.append(data);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void handleSimpleTag(final Tag t, final MutableAttributeSet a, final int pos) {

                if (t.equals(Tag.BR)) {
                    numBRs++;
                    if (numBRs > 1) {
                        res.add(text.toString());
                        text.setLength(0);
                        numBRs = 0;
                    }
                }
            }

        };
        try {
            parserDelegator.parse(new StringReader(html), parserCallback, true);
        } catch (IOException e) {
            // ignore, should not happen
        }

        res.add(text.toString());
        return res;
    }


    /**
     * Creates a deep copy of the given object. This method first serialized the object and then immediately
     * de-serializes it. This creates a deep copy.
     *
     * @param o any serializable object
     * @return a deep copy of the input object
     * @throws IOException if an I/O error occurs (should not happen)
     * @throws ClassNotFoundException if a class could not be found (should not happen)
     * @since 2.10
     */
    public static <T> T deepCopy(final T o) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(o);
        os.close();
        ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        @SuppressWarnings("unchecked")
        T newObject = (T)is.readObject();
        is.close();
        return newObject;
    }

	
}
