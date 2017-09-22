/*
 * ------------------------------------------------------------------ *
 * Copyright by
 * University of Konstanz, Germany.
 * Chair for Bioinformatics and Information Mining
 * Prof. Dr. Michael R. Berthold
 *
 * This file is part of the MEKA integration plugin for KNIME.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;

import meka.classifiers.multilabel.MultiLabelClassifier;

import org.apache.commons.codec.binary.Base64;
import org.knime.base.data.util.DataCellStringMapper;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.KNIMETimer;
import org.meka.knime.classifier.MekaClassifierNodeModel;
import org.meka.knime.ports.MekaClassifierModelPortObject;
import org.meka.knime.ports.MekaClassifierModelPortObjectSpec;
import org.meka.knime.utils.MekaClassifierCreationTask;
import org.meka.knime.utils.MekaConverter;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.misc.SerializedClassifier;
import weka.classifiers.trees.J48;
import weka.core.Capabilities;
import weka.core.Instances;

/**
 * NodeModel of the Weka Classifier node.
 *
 * @author cebron, University of Konstanz
 */
public class MekaClassifierNodeModel extends NodeModel {

    /* The node logger for this class. */
    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(MekaClassifierNodeModel.class);

    /**
     * Key to store the class column in the NodeSettings.
     */
    public static final String CLASSCOLS_KEY = "m_classcols";
     
    /**
     * Key to store the DataTableSpec in the NodeSettings.
     */
    public static final String SPEC_KEY = "tablespec";
    //Start
    protected static final String CFG_CLASSCOLS_COLUMNS = "m_classcols";

    private final SettingsModelFilterString m_sf_classcols =
        new SettingsModelFilterString(CFG_CLASSCOLS_COLUMNS);
    //End
    /**
     * Key to store the DataCellStringMapper.
     */
    public static final String MAPPER_KEY = "mapper";

    /**
     * Model info identifier.
     */
    public static final String MODEL_INFO = "model_info";

    /**
     * Key to store the weka settings in the NodeSettings.
     */
    public static final String MEKA_KEY = "Serialized MEKA-Classifier";

    private static final String MEKA_FILE = "MekaClassifier";


    /*
     * The classifier from Weka.
     */
    private MultiLabelClassifier m_classifier;

    /*
     * The classifier from Weka of the last execution or null if not executed.
     */
//    private Classifier m_lastClassifier = null;
    private MultiLabelClassifier m_lastClassifier = null;

    /*
     * The class column.
     */
    private List<String> m_classcols;
    SettingsModelFilterString smc = null;
    /**
     *
     * @param mekaclassifier The classification algorithm from Meka
     */
    public MekaClassifierNodeModel(final MultiLabelClassifier m_MekaClassifier) {
        super(new PortType[]{BufferedDataTable.TYPE},
                new PortType[]{MekaClassifierModelPortObject.TYPE});
        m_classifier = m_MekaClassifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpec inSpec = (DataTableSpec)inSpecs[0];
        
        if (inSpec.getNumColumns() == 0) {
            return new PortObjectSpec[]{null};
        }
        for(int i = 0; i < m_classcols.size(); i ++){
        	if (m_classcols.get(i) == null) {
                m_classcols.set(i, inSpec.getColumnSpec(inSpec.getNumColumns() - 1).getName());
                        
                setWarningMessage("No target columns specified. Target column"
                        + " was set to " + m_classcols.get(i));
            } else if (inSpec.findColumnIndex(m_classcols.get(i)) == -1) {
                throw new InvalidSettingsException("Target column " + m_classcols
                        + " not found in DataTableSpec.");
            }
        }
        
        MekaClassifierModelPortObjectSpec out =
                new MekaClassifierModelPortObjectSpec(m_classcols, inSpec);
        
        
        return new PortObjectSpec[]{out};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        BufferedDataTable bdt = (BufferedDataTable)inData[0];
        DataTableSpec spec = bdt.getDataTableSpec();
        ExecutionMonitor convertExecMonitor = exec.createSubProgress(.5);
        MekaConverter mekacon = new MekaConverter(spec);
        Instances trainingInstances =
                mekacon.convertToMeka(bdt, convertExecMonitor);
        LOGGER.info(trainingInstances.toSummaryString());
                      
        DataCellStringMapper mapper = mekacon.getMapping();

        trainingInstances.setClassIndex(m_classcols.size());
        
        //Testing 
        //trainingInstances.setClassIndex(m_classcols.size()-1);
        /*System.out.println("List"+m_classcols);
        
        DataCell[] cells = new DataCell[m_classcols.size()+1];        
        cells[0] = new StringCell("Selected Columns");
        RowKey k = new RowKey("" + 0);
        DataRow row = new DefaultRow(k, cells[0]);
        System.out.println(""+spec.toString());*/
        
        //make a deep copy of the classifier
        MultiLabelClassifier classifier = MekaConverter.deepCopy(m_classifier);
        
        /*
         * before we start, let's fully check the capabilities with the
         * generated training instances. Except for the serialized classifier
         * which is a special case.
         */
        ExecutionContext trainingContext = exec.createSubExecutionContext(.5);
        trainingContext.setMessage("Training Weka-Classifier");

        // ((MultiLabelClassifier)classifier).setClassifier(new J48());
        final MekaClassifierCreationTask w =
                new MekaClassifierCreationTask(classifier, trainingInstances);

        KNIMETimer timer = KNIMETimer.getInstance();
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                try {
                    exec.checkCanceled();
                } catch (final CanceledExecutionException ce) {
                    w.stop();
                    super.cancel();
                }

            }
        };
        timer.scheduleAtFixedRate(t, 0, 3000);
        w.start();
        w.join();

        // check if a class type exception was thrown during training
        if (w.classTypeExceptionThrown()) {
            throw new InvalidSettingsException("Unsupported class type "
                    + "for Weka-Classifier in column: " + m_classcols.toString());
        }
        // check if a unsupported attribute type exception was thrown
        if (w.unsupportedAttributeTypeExceptionThrown()) {
            throw new InvalidSettingsException("Unsupported attribute"
                    + " type exception in Weka: "
                    + w.getAttributeTypeException().getMessage());
        }
        // check if any exception has been thrown
        if (w.exceptionThrown()) {
            Throwable th = w.getThrowable();
            String s =
                    th.getClass().getSimpleName() + " in Weka during "
                            + "training. Please verify your settings. ";
            String exc = th.getMessage();
            throw new InvalidSettingsException(s + ((exc != null) ? exc : ""),
                    th);
        }
        t.cancel();

        MekaClassifierModelPortObjectSpec out =
                new MekaClassifierModelPortObjectSpec(m_classcols, spec);
        MekaClassifierModelPortObject mekaout =
                new MekaClassifierModelPortObject(classifier,
                        trainingInstances, mapper, out);
        m_lastClassifier = (MultiLabelClassifier) classifier;
        return new PortObject[]{mekaout};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        File f = new File(nodeInternDir, MEKA_FILE);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(f));
            m_lastClassifier = (MultiLabelClassifier)in.readObject();
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not read weka classifier", e);
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    LOGGER.debug("Could not close stream", e);
                }
            }
            exec.setProgress(1.0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	

        if (settings.containsKey(MEKA_KEY)) {
            byte[] bytes = null;
            String input = settings.getString(MEKA_KEY);
            // try {
            bytes = Base64.decodeBase64(input.getBytes());
            // bytes = new BASE64Decoder().decodeBuffer(input);
            // } catch (IOException e) {
            // LOGGER.error("Unexpected end of "
            // + "settings, using default values", e);
            // }

            if (bytes == null) {
                LOGGER.error("Could not load settings"
                        + ", using default values");
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bis);
                m_classifier = (MultiLabelClassifier)ois.readObject();
            } catch (IOException ioe) {
                LOGGER.error("Unexpected end of "
                        + "settings, using default values", ioe);
            } catch (ClassNotFoundException cnf) {
                LOGGER.error("Could not load settings"
                        + ", using default values", cnf);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (Exception e) {
                        LOGGER.debug("Could not close stream", e);
                    }
                }
            }
        }
    	if (settings.containsKey(CLASSCOLS_KEY)) {
    		String[] colkeys=settings.getStringArray(CLASSCOLS_KEY);
    		if (colkeys!=null){
            for(int i = 0; i < colkeys.length; i ++){
    		
            m_classcols.set(i, colkeys[i]);
        }
    		}
    }
        m_sf_classcols.loadSettingsFrom(settings);
        m_classcols = m_sf_classcols.getIncludeList();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_lastClassifier = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        File f = new File(nodeInternDir, MEKA_FILE);
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(m_lastClassifier);
            exec.setProgress(.5);
            exec.checkCanceled();
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (out != null) {
                out.close();
            }
            exec.setProgress(1.0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
           	
    	settings.addStringArray(CLASSCOLS_KEY, m_classcols.toArray(new String[m_classcols.size()]));
    	
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = null;
        try {
            oo = new ObjectOutputStream(bo);
            oo.writeObject(m_classifier);
            byte[] outputs = bo.toByteArray();
            String output = new String(Base64.encodeBase64(outputs));
            // String output = new BASE64Encoder().encode(outputs);
            settings.addString(MEKA_KEY, output);
            
        } catch (IOException ioe) {
            LOGGER.error("Internal error: Could not save settings", ioe);
        } finally {
            if (oo != null) {
                try {
                    oo.close();
                } catch (Exception e) {
                    LOGGER.debug("Could not close stream", e);
                }
            }
        }
        m_sf_classcols.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_sf_classcols.validateSettings(settings);
    }

    /**
     *
     * @return the classifier from the last execution if available, null
     *         otherwise
     */
    public final Classifier getClassifier() {
        return m_lastClassifier;
    }

}
