/*
 * ------------------------------------------------------------------ *
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
 *   07.09.2007 (cebron): created
 */
package org.meka.knime.classifier;

import java.awt.BorderLayout;
import weka.classifiers.trees.J48;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;

import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.gui.goe.GenericObjectEditor;

import org.apache.commons.codec.binary.Base64;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.meka.knime.utils.MekaConverter;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.gui.PropertySheetPanel;

/**
 * The MekaClassifierNodeDialog shows the PropertySheetPanel of the underlying
 * meka-classifier. Additionally, the user can select the class column.
 * 
 * @author cebron, University of Konstanz
 */
public class MekaClassifierNodeDialog extends NodeDialogPane {
    /* The node logger for this class. */
    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(MekaClassifierNodeDialog.class);

    /* The Classifier to configure. */
    private MultiLabelClassifier m_classifier;
    /** the GOE for the classifier. */
    protected GenericObjectEditor m_GenericObjectEditor;
    
    
    
    //To return the instance of Multilabel Classifier
    public MultiLabelClassifier getM_classifier() {
		return m_classifier;
	}


	/* Holds the selected class column */
    //private final ColumnSelectionPanel m_colsel;

    /* The PropertySheetPanel from the meka-classifier. */
    private PropertySheetPanel m_mekaprops;

    /* Panel displaying the PropertySheetPanel. */
    private final JPanel m_mekapanel;

    /* Panel displaying attribute info */
    private final JScrollPane m_attrInfoPanel;

    /* DataTableSpec at the input port */
    private DataTableSpec m_spec;

    /* JPanel holding all dialog components */
    private final JPanel m_all;
    
    public static DialogComponentColumnFilter m_columnFilter;
    

	public static DialogComponentColumnFilter getM_columnFilter() {
		return m_columnFilter;
	}

	
	private final  
    SettingsModelFilterString smcs = new SettingsModelFilterString(MekaClassifierNodeModel.CFG_CLASSCOLS_COLUMNS);

    /**
     * Constructor.
     * 
     * @param wekaclassifier The classifier to configure.
     */
    @SuppressWarnings("unchecked")
    public MekaClassifierNodeDialog(final MultiLabelClassifier mekaclassifier) {
        super();
        
        
        m_all = new JPanel(new GridLayout(1, 1));
        try {
            m_classifier = (MultiLabelClassifier)AbstractClassifier.makeCopy(mekaclassifier); 
            //m_classifier.setClassifier(new J48());
        } catch (Exception e) {
            LOGGER.error("Could not load meka classifier", e);
        }

        m_mekaprops = new PropertySheetPanel();
        m_mekapanel = new JPanel(new GridLayout(1, 1));
        m_mekapanel.setBorder(BorderFactory.createTitledBorder("Filter"));

   // add(m_mekapanel, BorderLayout.NORTH);

    	//m_GenericObjectEditor = new GenericObjectEditor(true);
       // m_GenericObjectEditor.setClassType(MultiLabelClassifier.class);
        //m_GenericObjectEditor.setValue(m_classifier);
        
    //m_mekapanel.add(m_GenericObjectEditor.getCustomPanel(), BorderLayout.CENTER);
    
      //  m_mekapanel.add(m_mekaprops);
      //  m_mekaprops.setTarget(m_classifier);
//        m_colsel =

   
        m_mekapanel.add(m_mekaprops);
        m_mekaprops.setTarget(m_classifier);
        /*m_colsel =

                new ColumnSelectionPanel("Select target column",
                        DataValue.class);
        m_colsel.getSelectedColumn(); //test
*/        /*m_colsel.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(final ActionEvent e) {
                m_attrInfoPanel.getViewport().setView(createAttrInfo(m_spec));
            }
        });*/
        m_attrInfoPanel = new JScrollPane();
        Border border =
                BorderFactory.createTitledBorder("Preliminary Attribute check");
        m_attrInfoPanel.setBorder(border);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_mekapanel, BorderLayout.CENTER);
        //panel.add(m_colsel, BorderLayout.SOUTH);       
                
        Dimension prefSize = panel.getPreferredSize();
        Dimension attrSize = new Dimension(prefSize.width / 2, prefSize.height);
        m_attrInfoPanel.setMaximumSize(attrSize);
        m_attrInfoPanel.setPreferredSize(attrSize);
        JSplitPane jsp =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel,
                        m_attrInfoPanel);       
        
        m_all.add(jsp);
        
        /*Added a DiaglogComponent in order to select the classes needs to be
        //classified accordingly*/     
       
        m_columnFilter = new DialogComponentColumnFilter(
        		smcs, 0, true,
                        StringValue.class);
        m_columnFilter.setIncludeTitle("Included Columns");
        m_columnFilter.setExcludeTitle("Not Included Columns");
        
        DefaultNodeSettingsPane dnsp = new DefaultNodeSettingsPane();
        dnsp.addDialogComponent(m_columnFilter);
        dnsp.setDefaultTabTitle("Columns to transform");
        JSplitPane jsp2 =
                new JSplitPane(JSplitPane.VERTICAL_SPLIT, dnsp.getPanel(),
                        m_attrInfoPanel);          
        m_all.add(jsp2);        
        super.addTab("Options", m_all);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
        
    	m_spec = (DataTableSpec)specs[0];
    	
        if (m_spec.getNumColumns() > 0) {
            /*String fallbackClasscol =
                    m_spec.getColumnSpec(m_spec.getNumColumns() - 1).getName();*/
            /*String classcol =
                    settings.getString(MekaClassifierNodeModel.CLASSCOLS_KEY,
                            fallbackClasscol);*/
            /*String classcol =
                    settings.getString(MekaClassifierNodeModel.CLASSCOLS_KEY,
                            "");*/
            /*if (m_spec.findColumnIndex(classcol) < 0) {
                classcol = fallbackClasscol;
            }*/
            //m_colsel.update(m_spec, classcol);
            //m_colsel.update(m_spec, "");
            //m_colsel.setSelectedIndex(0);
            /*Loading Class Names, since we need to use them into the dialog
              component in order to select the required classes to classify*/
            try {
	            m_columnFilter.loadSettingsFrom(settings, specs);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
            
            if (settings.containsKey(MekaClassifierNodeModel.MEKA_KEY)) {
                byte[] bytes = null;
                try {
                    String input =
                            settings.getString(MekaClassifierNodeModel.MEKA_KEY);
                    bytes = Base64.decodeBase64(input.getBytes());
                    
                } catch (InvalidSettingsException e) {
                    // we use the default classifier.
                	LOGGER.error(e);
                }
                if (bytes == null) {
                    LOGGER.error("Could not load settings"
                            + ", using default values");
                }
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(bis);
                    m_classifier = (MultiLabelClassifier)ois.readObject();
                    m_mekapanel.remove(m_mekaprops);
                    m_mekaprops = new PropertySheetPanel();
                    m_mekaprops.setTarget(m_classifier);
                    m_mekapanel.add(m_mekaprops);
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
            m_attrInfoPanel.getViewport().setView(createAttrInfo(m_spec));
        } else {
            throw new NotConfigurableException(
                    "Class column can not be set, no input data");
        }
        m_columnFilter.loadSettingsFrom(settings, new DataTableSpec[] {m_spec});
    }
    
    private JComponent createAttrInfo(final DataTableSpec spec) {
        //if (spec == null || m_colsel.getSelectedColumn() == null) {
    	if (spec == null ) {
            return new JPanel();
        }
        JPanel content = new JPanel();
        BoxLayout yaxis = new BoxLayout(content, BoxLayout.PAGE_AXIS);
        content.setLayout(yaxis);

        MekaConverter con = new MekaConverter(spec);
        String[] ok =
                //con.testAttributes(m_classifier.getClassifier(), m_classifier.m_colsel.getSelectedColumn());
        		//con.testAttributes(m_classifier.getClassifier(), m_columnFilter.getInvalidIncludeColumns());
        		con.testAttributes(m_classifier, m_columnFilter.getInvalidExcludeColumns().toString()); // Problem is that we do not have classifier so the test is not performed properly about the columns
        for (int i = 0; i < ok.length; i++) {
            JPanel colpanel = new JPanel();
            BoxLayout xaxis = new BoxLayout(colpanel, BoxLayout.X_AXIS);
            colpanel.setLayout(xaxis);
            JLabel colname = new JLabel(spec.getColumnSpec(i).getName() + ": ");
            JLabel oklabel = new JLabel();
            if (ok[i].equals("")) {
                oklabel.setForeground(new Color(0, 215, 0));
                oklabel.setText("ok");
            } else {
                oklabel.setForeground(Color.red);
                oklabel.setText(ok[i]);
            }
            colpanel.add(colname);
            colpanel.add(oklabel);
            content.add(colpanel);
        }
        return content;
    }
    
    /**
     * {@inheritDoc}
     */
    
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
    	/*Passing to setting the classes or attributes which are invalid and cannot be 
    	 * processed.
    	 */
    	final Set<String> invalidInclCols = m_columnFilter.getInvalidIncludeColumns();
    	if (invalidInclCols != null && !invalidInclCols.isEmpty()) {
            throw new InvalidSettingsException(invalidInclCols.size() + " invalid pivot columns found.");
        }
    	settings.addString(MekaClassifierNodeModel.CLASSCOLS_KEY,
                //m_colsel.getSelectedColumn());
    			m_columnFilter.getInvalidIncludeColumns().toString());
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = null;
        
    //    m_classifier.setClassifier(((MultiLabelClassifier)m_GenericObjectEditor.getValue()).getClassifier());
        
        try {
            oo = new ObjectOutputStream(bo);
            oo.writeObject(m_classifier);
            byte[] outputs = bo.toByteArray();
            // String output = new BASE64Encoder().encode(outputs);
            String output = new String(Base64.encodeBase64(outputs));
            settings.addString(MekaClassifierNodeModel.MEKA_KEY, output);
        } catch (IOException e) {
            LOGGER.error("Internal error: settings not saved.");
        } finally {
            if (oo != null) {
                try {
                    oo.close();
                } catch (Exception e) {
                    LOGGER.debug("Could not close stream", e);
                }
            }
        }
        // selected columns whixch are the target classes
        m_columnFilter.saveSettingsTo(settings);
    }

}
