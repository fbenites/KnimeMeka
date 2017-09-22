package org.meka.knime.ranking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLEvalUtils;
import meka.core.Result;
import meka.core.ThresholdUtils;

import org.knime.base.data.util.DataCellStringMapper;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.meka.knime.ports.MekaClassifierModelPortObject;
import org.meka.knime.ports.MekaClassifierModelPortObjectSpec;
import org.meka.knime.utils.MekaConverter;

import com.sun.istack.internal.logging.Logger;

import weka.core.Instances;


/**
 * This is the model implementation of MekaPerformance.
 * 
 *
 * @author Waqar
 */
public class MekaRankingNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(MekaRankingNodeModel.class);
        
    /** the settings key which is used to retrieve and 
        store the settings (from the dialog or from a settings file)    
       (package visibility to be usable from the dialog). */
	static final String CFGKEY_COUNT = "Count";
	public static String STRSEL = "type";
	
	private final SettingsModelString m_selStr =
            new SettingsModelString(STRSEL, null);
	
	private static final String MEKA_FILE = "MekaClassifier";
	
	private Instances m_trainingInstances;
	private MultiLabelClassifier m_classifier;
	private String m_eval = "";
	private boolean m_doEval;

    /** initial default count value. */
    static final int DEFAULT_COUNT = 100;
    
    private List<String> m_classcols;

    // example value: the models count variable filled from the dialog 
    // and used in the models execution method. The default components of the
    // dialog work with "SettingsModels".
    /*private final SettingsModelIntegerBounded m_count =
        new SettingsModelIntegerBounded(MekaRankingNodeModel.CFGKEY_COUNT,
                    MekaRankingNodeModel.DEFAULT_COUNT,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);*/
    

        
    /**
     * Constructor for the node model.
     */
    /*protected MekaRankingNodeModel() {
    
        // TODO one incoming port and one outgoing port is assumed
        super(2, 1);
    }*/
    
    
    /**
     * Constructor.
     */
    public MekaRankingNodeModel() {
        super(new PortType[]{MekaClassifierModelPortObject.TYPE,
                BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
        
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        logger.info("Node Model Stub... this is not yet implemented !");
        BufferedDataTable testdata = (BufferedDataTable)inData[1];
        
        //BufferedDataTable bdt = (BufferedDataTable)inData[1];
        //DataTableSpec spec = bdt.getDataTableSpec();
        //ExecutionMonitor convertExecMonitor = exec.createSubProgress(.5);
        //MekaConverter mekacon = new MekaConverter(spec);
        //Instances trainingInstances =
                //mekacon.convertToMeka(bdt, convertExecMonitor);
        //logger.info(trainingInstances.toSummaryString());
                      
        //DataCellStringMapper mapper = mekacon.getMapping();

        //trainingInstances.setClassIndex(m_classcols.size());
        
       
        
        PortObject specPort = inData[0];
        MekaClassifierModelPortObject mekaout = (MekaClassifierModelPortObject) specPort;
        MekaClassifierModelPortObjectSpec out = mekaout.getSpec();
        
        MekaClassifierModelPortObject model =
                (MekaClassifierModelPortObject)inData[0];
        //DataTableSpec trainingspec = model.getSpec().getSpec();
        //isSubSpec(testdata.getDataTableSpec(), trainingspec);
        //List<String> classcolname = model.getSpec().getClassCols();
        //DataTableSpec testspec = testdata.getDataTableSpec();
        //Since we have multiple classes names so checking all
        
               
        m_classifier = (MultiLabelClassifier) model.getClassifier();
        m_trainingInstances = model.getTrainingInstances();
        
        //DataTableSpec dts = out.getSpec();
        //BufferedDataTable spec = inData[0];
        //PortObject pO = spec;
        
        
        //MekaClassifierModelPortObject mekaout = (MekaClassifierModelPortObject) pO;
        
        /*MekaClassifierModelPortObjectSpec mekaout =
                new MekaClassifierModelPortObjectSpec(m_classcols, spec);*/
        //MekaClassifierModelPortObjectSpec out = mekaout.getSpec();
        //DataTableSpec dts = mekaout.getSpec();
        //
        
                
        //MekaClassifierModelPortObject model = (MekaClassifierModelPortObject) modelport;
        DataTableSpec trainingspec =  model.getSpec().getSpec();
        //isSubSpec(testdata.getDataTableSpec(), trainingspec);
        List<String> classcolname = model.getSpec().getClassCols();
        //DataTableSpec testspec = testdata.getDataTableSpec();
        //Since we have multiple classes names so checking all
        /*for(int i = 0; i < classcolname.size(); i ++){
        	if (testspec.findColumnIndex(classcolname.get(i)) >= 0) {
                m_doEval = true;
            } else {
                m_doEval = false;
            }
        }*/
               
        //m_classifier = (MultiLabelClassifier) model.getClassifier();
        //m_trainingInstances = model.getTrainingInstances();
        
        /*ColumnRearranger colre =
                new ColumnRearranger(testdata.getDataTableSpec());*/
        //Getting column specifications with respect to class column names
        DataColumnSpec[] classcolspec = new DataColumnSpec[classcolname.size()];
        for(int i = 0; i < classcolname.size(); i ++){
        	classcolspec[i] = trainingspec.getColumnSpec(classcolname.get(i));
        }        		
        
        /*
        Vector<Integer> posVec = new Vector<Integer>();
        for (int i = 0; i < trainingspec.getNumColumns(); i++) {
            DataColumnSpec colspec = trainingspec.getColumnSpec(i);
            if (!m_doEval && colspec.equalStructure(classcolspec[i])) {
                // do not add
            } else {
                //int position = testspec.findColumnIndex(colspec.getName());
                //assert (position >= 0);
                //posVec.add(position);

            }
        }
        int[] positions = new int[posVec.size()];
        for (int j = 0; j < positions.length; j++) {
            positions[j] = posVec.get(j);
        }*/
        /*colre.permute(positions);
        colre.keepOnly(positions);
        testdata = exec.createColumnRearrangeTable(testdata, colre, exec);*/

        //DataCellStringMapper mapper = model.getMapper();
        //Instances testInstances = null;
        //ExecutionMonitor trainingConvert = exec.createSubProgress(.4);
        m_doEval = true;
        Result res = null;
        String voption = "1";
        if (m_doEval) {
	        // merge class column of training and test table
	        DataTableSpec oldTestSpec = testdata.getDataTableSpec();
	        //taking class indexes an array since we have multiple classes
	        Integer[] classIndex = new Integer[classcolname.size()];	        
        	
            /*MekaConverter mekacon =
                    new MekaConverter(testdata.getDataTableSpec());*/
           //testInstances = mekacon.convertToMeka(bdt, trainingConvert);
            //logger.debug(testInstances.toSummaryString());
            
            int newClassIndex = out.getClassCols().size();
            	
            m_trainingInstances.setClassIndex(newClassIndex);
            //res=Evaluation.testClassifier(m_classifier, testInstances);
            res=Evaluation.testClassifier(m_classifier, m_trainingInstances);
			res.setInfo("Type","ML");
            m_eval = res.toString();
            // TODO: assert that m_selStr is set or get stnd
            // TODO: implement Read Threshold method
            if(m_selStr.getStringValue().equals("PCut1"))
            	res.setInfo("Threshold",MLEvalUtils.getThreshold(res.predictions,m_trainingInstances,"PCut1"));
            else if(m_selStr.getStringValue().equals("PCutL"))
            	res.setInfo("Threshold",MLEvalUtils.getThreshold(res.predictions,m_trainingInstances,"PCutL"));
            
            res.setInfo("Verbosity",voption);
            res.output = Result.getStats(res, voption);
            
            
            int Y[][] = res.allTrueValues();
            double ts[] = ThresholdUtils.thresholdStringToArray(res.getInfo("Threshold"),Y[0].length);
                        
            
            //All Predictions
            int Ypred[][] = ThresholdUtils.threshold(res.allPredictions(),ts);
              //To be used for new node (Ranking to classes)
            m_eval = Result.getStats(res, "2").toString();
            //List<double[]> preds=res.predictions;
            
            DataColumnSpec[] newcolspecs = 
                    new DataColumnSpec[newClassIndex];
            for(int index = 0; index < classcolname.size(); index ++){
            	classIndex[index] = oldTestSpec.findColumnIndex(classcolname.get(index));
            
            	DataColumnSpecCreator newclassColumn =
                    new DataColumnSpecCreator(
                            oldTestSpec.getColumnSpec(classIndex[index]));
	            //newclassColumn.merge(classcolspec[index]);
	            newclassColumn.setType(DoubleCell.TYPE);
            
                    newcolspecs[index] = newclassColumn.createSpec();                    
            }
            DataTableSpec newspec = new DataTableSpec(newcolspecs);

            BufferedDataContainer buf = exec.createDataContainer(newspec);

            for (int j = 0; j < Ypred.length; j++) {
            	
            	
            	int[] pred= Ypred[j];
            	
            	
                DataCell[] cells = new DataCell[pred.length];
                for (int i=0;i<pred.length;i++){
                	IntCell dc=new IntCell(pred[i]);
                	cells[i]=dc;
                }                	
                
                DataRow row = new DefaultRow(
                "Row" + j, cells);
                buf.addRowToTable(row);
            }
            buf.close();
            BufferedDataTable classified = buf.getTable();

            return new BufferedDataTable[]{classified};                		
                
        }
		return null;       
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
    protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {        
        
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        
        //m_count.saveSettingsTo(settings);
    	m_selStr.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
                
        //m_count.loadSettingsFrom(settings);
    	m_selStr.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            

        //m_count.validateSettings(settings);
    	m_selStr.validateSettings(settings);

    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
    
        File f = new File(internDir, MEKA_FILE);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(f));
            m_classifier = (MultiLabelClassifier)in.readObject();
            m_eval = in.readUTF();
        } catch (ClassNotFoundException e) {
        	logger.error("Could not read meka classifier", e);
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                	logger.debug("Could not close stream", e);
                }
            }
            exec.setProgress(1.0);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        File f = new File(internDir, MEKA_FILE);
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
     * 
     * @return meka-Classifier
     */
    public final MultiLabelClassifier getClassifier() {
        return m_classifier;
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

}

