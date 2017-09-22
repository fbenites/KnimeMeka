package org.meka.knime.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of LabelsetStatistics.
 * 
 *
 * @author Fernando Benites
 */
public class LabelsetStatisticsNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(LabelsetStatisticsNodeModel.class);
        
    /** the settings key which is used to retrieve and 
        store the settings (from the dialog or from a settings file)    
       (package visibility to be usable from the dialog). */
    public static final String CLASSCOL_KEY = "ClassColumn";

    /** initial default count value. */
    static final int DEFAULT_COUNT = 100;
    

    public int labelsnumber=0;
    public int uniquelabels=0;
    public double LCard=0;
    public double LDensity=0;
    public int labelsnumbertr=0;
    public int uniquelabelstr=0;
    public double LCardtr=0;
    public double LDensitytr=0;
    public int labelsnumberts=0;
    public int uniquelabelsts=0;
    public double LCardts=0;
    public double LDensityts=0;
    public int nrrows=0;
    public int nrrowstr=0;
    public int nrrowsts=0;
    public int maxtargetcol=0;
    

    private String m_classcol;

    // example value: the models count variable filled from the dialog 
    // and used in the models execution method. The default components of the
    // dialog work with "SettingsModels".
    

    /**
     * Constructor for the node model.
     */
    protected LabelsetStatisticsNodeModel() {
    
        // TODO one incoming port and one outgoing port is assumed
        super(2, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO do something here
        logger.info("Node Model Stub... this is not yet implemented !");


        BufferedDataTable traindata = (BufferedDataTable)inData[0];
        BufferedDataTable testdata = (BufferedDataTable)inData[1];
        DataTableSpec inSpec =traindata.getDataTableSpec();
        maxtargetcol=inSpec.findColumnIndex(m_classcol);
        
        //check cardinality
        
        
        // the data table spec of the single output table, 
//        // the table will have three columns:
//        DataColumnSpec[] allColSpecs = new DataColumnSpec[3];
//        allColSpecs[0] = 
//            new DataColumnSpecCreator("Column 0", StringCell.TYPE).createSpec();
//        allColSpecs[1] = 
//            new DataColumnSpecCreator("Column 1", DoubleCell.TYPE).createSpec();
//        allColSpecs[2] = 
//            new DataColumnSpecCreator("Column 2", IntCell.TYPE).createSpec();
//        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        // the execution context will provide us with storage capacity, in this
        // case a data container to which we will add rows sequentially
        // Note, this container can also handle arbitrary big data tables, it
        // will buffer to disc if necessary.
       // BufferedDataContainer container = exec.createDataContainer(outputSpec);
        // let's add m_count rows to it
        labelsnumbertr=0;
        labelsnumber=0;
        labelsnumberts=0;
        nrrows=0;
		HashMap<String,Integer> mapcu = new HashMap<String,Integer>();  
		HashMap<String,Integer> mapcutr = new HashMap<String,Integer>();  
		HashMap<String,Integer> mapcuts = new HashMap<String,Integer>();  

        for (DataRow row: traindata) {
        	
        	String hashu="";
        	for (int i=0; i< row.getNumCells(); i++){
        			DataCell c1=row.getCell(i);
        		if(c1.getClass().equals(StringCell.class) || i<=maxtargetcol){
        			String ts1=((StringCell) c1).getStringValue();
        			hashu+=ts1;
        			if (ts1.compareTo("0")==0){
        				
        			}else{
        				labelsnumbertr+=1;
        			}
        					
        		}
        	}
			Integer c = mapcu.get(hashu);
			mapcu.put(hashu,c == null ? 1 : c+1);
			c = mapcutr.get(hashu);
			mapcutr.put(hashu,c == null ? 1 : c+1);
        }

        nrrowstr=traindata.getRowCount();
        nrrows+=nrrowstr;
        labelsnumber+=labelsnumbertr;
        LCardtr=labelsnumbertr/((double)traindata.getRowCount());

        for (DataRow row: testdata) {
        	String hashu="";
        	

        	for (int i=0; i< row.getNumCells(); i++){
        			DataCell c1=row.getCell(i);
        		if(c1.getClass().equals(StringCell.class) || i<=maxtargetcol){
        			String ts1=((StringCell) c1).getStringValue();
        			hashu+=ts1;
        			if (ts1.compareTo("0")==0){
        				
        			}else{
        				labelsnumberts+=1;
        			}
        					
        		}
        	}
			Integer c = mapcu.get(hashu);
			mapcu.put(hashu,c == null ? 1 : c+1);
			c = mapcuts.get(hashu);
			mapcuts.put(hashu,c == null ? 1 : c+1);
        }
        nrrowsts=testdata.getRowCount();
        nrrows+=nrrowsts;
        labelsnumber+=labelsnumberts;
        LCardts=labelsnumberts/((double)testdata.getRowCount());
        LCard=labelsnumber/((double)nrrows);
        uniquelabels=mapcu.size();
        uniquelabelstr=mapcutr.size();
        uniquelabelsts=mapcuts.size();
        LDensitytr=LCardtr/((double)maxtargetcol+1);
        LDensityts=LCardts/((double)maxtargetcol+1);
        LDensity=LCard/((double)maxtargetcol+1);
        // once we are done, we close the container and return its table
       // container.close();
       // BufferedDataTable out = container.getTable();
        return new BufferedDataTable[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Code executed on reset.
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message

        return new DataTableSpec[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        // TODO save user settings to the config object.
        
        
        settings.addString(CLASSCOL_KEY, m_classcol);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO load (valid) settings from the config object.
        // It can be safely assumed that the settings are valided by the 
        // method below.

        if (settings.containsKey(CLASSCOL_KEY)) {
            m_classcol = settings.getString(CLASSCOL_KEY);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO check if the settings could be applied to our model
        // e.g. if the count is in a certain range (which is ensured by the
        // SettingsModel).
        // Do not actually set any values of any member variables.


    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
        // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).

    }

}

