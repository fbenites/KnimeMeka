/*
 * ------------------------------------------------------------------
 * Copyright, 2003 - 2011
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
 *   10.09.2007 (cebron): created
 */
package org.meka.knime.predictor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLEvalUtils;
import meka.core.Result;

import org.knime.base.data.util.DataCellStringMapper;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.meka.knime.ports.MekaClassifierModelPortObject;
import org.meka.knime.ports.MekaClassifierModelPortObjectSpec;
import org.meka.knime.utils.MekaConverter;

//import weka.classifiers.Classifier;
//import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * 
 * @author cebron, University of Konstanz
 */
public class MekaPredictorNodeModel extends NodeModel {
    /* The node logger for this class. */
    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(MekaPredictorNodeModel.class);

    /**
     * Key to store whether to append a winner value column for nominal class
     * attributes in the NodeSettings.
     */
    public static final String WINNERCOL_KEY = "winnercol";

    private static final String MEKA_FILE = "MekaClassifier";
    protected static final String CFG_CLASSCOLS_COLUMNS = "m_classcols";
           
    /*
     * Flag indicating whether an evaluation is carried out.
     */
    private boolean m_doEval;

    /*
     * Evaluation based on training and test instances
     */
    private String m_eval = "";

    /*
     * Meka-Classifier used for classification.
     */
    private MultiLabelClassifier m_classifier;

    /*
     * The training instances (used for evaluation).
     */
    private Instances m_trainingInstances;

    /**
     * Constructor.
     */
    public MekaPredictorNodeModel() {
        super(new PortType[]{MekaClassifierModelPortObject.TYPE,
                BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {      
    	return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        BufferedDataTable testdata = (BufferedDataTable)inData[1];
        
        //
        PortObject spec = inData[0];
        MekaClassifierModelPortObject mekaout = (MekaClassifierModelPortObject) spec;
        MekaClassifierModelPortObjectSpec out = mekaout.getSpec();
        DataTableSpec dts = out.getSpec();
        //
        
        MekaClassifierModelPortObject model =
                (MekaClassifierModelPortObject)inData[0];
        DataTableSpec trainingspec = model.getSpec().getSpec();
        isSubSpec(testdata.getDataTableSpec(), trainingspec);
        List<String> classcolname = model.getSpec().getClassCols();
        DataTableSpec testspec = testdata.getDataTableSpec();
        //Since we have multiple classes names so checking all
        for(int i = 0; i < classcolname.size(); i ++){
        	if (testspec.findColumnIndex(classcolname.get(i)) >= 0) {
                m_doEval = true;
            } else {
                m_doEval = false;
            }
        }
               
        m_classifier = (MultiLabelClassifier) model.getClassifier();
        m_trainingInstances = model.getTrainingInstances();
        

        ColumnRearranger colre =
                new ColumnRearranger(testdata.getDataTableSpec());
        //Getting column specifications with respect to class column names
        DataColumnSpec[] classcolspec = new DataColumnSpec[classcolname.size()];
        for(int i = 0; i < classcolname.size(); i ++){
        	classcolspec[i] = trainingspec.getColumnSpec(classcolname.get(i));
        }        		
        

        Vector<Integer> posVec = new Vector<Integer>();
        for (int i = 0; i < trainingspec.getNumColumns(); i++) {
            DataColumnSpec colspec = trainingspec.getColumnSpec(i);
            if (!m_doEval && colspec.equalStructure(classcolspec[i])) {
                // do not add
            } else {
                int position = testspec.findColumnIndex(colspec.getName());
                assert (position >= 0);
                posVec.add(position);

            }
        }
        int[] positions = new int[posVec.size()];
        for (int j = 0; j < positions.length; j++) {
            positions[j] = posVec.get(j);
        }
        colre.permute(positions);
        colre.keepOnly(positions);
        testdata = exec.createColumnRearrangeTable(testdata, colre, exec);

        DataCellStringMapper mapper = model.getMapper();
        Instances testInstances = null;
        ExecutionMonitor trainingConvert = exec.createSubProgress(.4);

        Result res = null;
        if (m_doEval) {
	        // merge class column of training and test table
	        DataTableSpec oldTestSpec = testdata.getDataTableSpec();
	        //taking class indexes an array since we have multiple classes
	        Integer[] classIndex = new Integer[classcolname.size()];	        
        	
            MekaConverter mekacon =
                    new MekaConverter(testdata.getDataTableSpec());
            testInstances = mekacon.convertToMeka(testdata, trainingConvert);
            LOGGER.debug(testInstances.toSummaryString());
            
            int newClassIndex = out.getClassCols().size();
            	
            testInstances.setClassIndex(newClassIndex); //Testing
            //testInstances.setClassIndex(newClassIndex-1);
            res=Evaluation.testClassifier(m_classifier, testInstances);
			res.setInfo("Type","ML");
            m_eval = res.toString();
            res.setInfo("Threshold",MLEvalUtils.getThreshold(res.predictions,m_trainingInstances,"PCut1"));  //To be used for new node (Ranking to classes)
            m_eval = Result.getStats(res, "2").toString();
            List<double[]> preds=res.predictions;
            DataColumnSpec[] newcolspecs =
                    new DataColumnSpec[newClassIndex];
            for(int index = 0; index < classcolname.size(); index ++){
            	classIndex[index] = oldTestSpec.findColumnIndex(classcolname.get(index));
            
            	DataColumnSpecCreator newclassColumn =
                    new DataColumnSpecCreator(
                            oldTestSpec.getColumnSpec(classIndex[index]));
            newclassColumn.merge(classcolspec[index]);
            newclassColumn.setType(DoubleCell.TYPE);
            
                    newcolspecs[index] = newclassColumn.createSpec();
                    
            }
            DataTableSpec newspec = new DataTableSpec(newcolspecs);

            BufferedDataContainer buf = exec.createDataContainer(newspec);

            for (int j = 0; j < preds.size(); j++) {
            	
            	double[] pred=m_classifier.distributionForInstance(testInstances.get(j));//preds.get(j);
                DataCell[] cells = new DataCell[pred.length];
            	//DataCell[] cells = new DataCell[pred.length+1]; //Test to be deleted
                for (int i=0;i<pred.length;i++){
                //for (int i=0;i<pred.length+1;i++){
                	DoubleCell dc=new DoubleCell(pred[i]);
                	cells[i]=dc;
                }                	
                
                DataRow row = new DefaultRow(
                "Row" + j, cells);
                buf.addRowToTable(row);
            }
            buf.close();
            BufferedDataTable classified = buf.getTable();

            return new PortObject[]{classified};                		
                
        }
		return null;       
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
            m_classifier = (MultiLabelClassifier)in.readObject();
            m_eval = in.readUTF();
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not read meka classifier", e);
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
    	    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_eval = "";
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
            out.writeObject(m_classifier);
            out.writeUTF(m_eval);
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
        //m_winnercol.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }

    /**
     * Returns the Evaluation-object based on the classifier and the test
     * instances.
     * 
     * @return Evaluation
     */
    public final String getEvaluation() {
        return m_eval;
    }

    /**
     * 
     * @return meka-Classifier
     */
    public final MultiLabelClassifier getClassifier() {
        return m_classifier;
    }

    /*
     * Tests that the DataTableSpec of the test data has not more possible
     * values than the DataTableSpec of the training data.
     */
    private void isSubSpec(final DataTableSpec testspec,
            final DataTableSpec trainingspec) throws InvalidSettingsException {
        for (DataColumnSpec testcolspec : testspec) {
            int colindex = trainingspec.findColumnIndex(testcolspec.getName());
            if (colindex >= 0) {
                DataColumnSpec trainingcolspec =
                        trainingspec.getColumnSpec(colindex);
                // check for possible values
                if (testcolspec.getDomain().hasValues()) {
                    if (trainingcolspec.getDomain().hasValues()) {
                        Set<DataCell> testpossvals =
                                testcolspec.getDomain().getValues();
                        Set<DataCell> trainingpossvals =
                                trainingcolspec.getDomain().getValues();
                        if (!trainingpossvals.containsAll(testpossvals)) {
                            throw new InvalidSettingsException(
                                    trainingcolspec.getName()
                                            + " column has more possible values in test"
                                            + " data than in the training data.");
                        }
                    } else {
                        throw new InvalidSettingsException(
                                trainingcolspec.getName()
                                        + " column in training data has no possible"
                                        + " values defined.");
                    }
                }
            }
        }
    }

}
