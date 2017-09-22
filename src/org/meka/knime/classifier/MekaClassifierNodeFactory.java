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
 *   19.09.2017 fbenites: changed to multilabel suppor for meka
 */
package org.meka.knime.classifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.namespace.QName;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import org.apache.xmlbeans.XmlCursor;
import org.knime.core.node.DynamicNodeFactory;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.meka.knime.MekaTestNodeSetFactory;
import org.meka.knime.classifier.MekaClassifierNodeDialog;
import org.meka.knime.classifier.MekaClassifierNodeModel;
import org.meka.knime.classifier.MekaClassifierNodeView;
import org.meka.knime.utils.MekaConverter;
//import org.knime.node.v212.KnimeNode;
//import org.knime.node.v31.KnimeNodeDocument;

import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.InPortDocument.InPort;
import org.knime.node2012.IntroDocument.Intro;
import org.knime.node2012.KnimeNodeDocument;
import org.knime.node2012.KnimeNodeDocument.KnimeNode;
import org.knime.node2012.OptionDocument.Option;
import org.knime.node2012.OutPortDocument.OutPort;
import org.knime.node2012.PDocument.P;
import org.knime.node2012.PortsDocument.Ports;
import org.knime.node2012.ViewDocument.View;
import org.knime.node2012.ViewsDocument.Views;

import org.knime.core.node.InvalidSettingsException;

import meka.classifiers.multilabel.BR;

import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.classifiers.AbstractClassifier;
import weka.core.Utils;
import weka.gui.beans.KnowledgeFlowApp;

/**
 * Factory for the Weka Classifier Node.
 *
 * @author cebron, University of Konstanz
 */
public class MekaClassifierNodeFactory extends
        DynamicNodeFactory<MekaClassifierNodeModel> {
    /** The underlying classifier. */
    private MultiLabelClassifier m_MekaClassifier;

    /** The WEKA version this classifier is based on. */
    private String m_version;

    /**
     * Creates a new Weka-Node.
     */
    public MekaClassifierNodeFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MekaClassifierNodeModel createNodeModel() {
        return new MekaClassifierNodeModel(m_MekaClassifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MekaClassifierNodeView createNodeView(final int viewIndex,
            final MekaClassifierNodeModel nodeModel) {
        /*
         * Check if viewIndex is out of range
         */
        if (viewIndex < 0 && viewIndex >= getNrNodeViews()) {
            throw new IllegalStateException();
        }
        return new MekaClassifierNodeView(nodeModel);
    }

    /**
     * All Weka-Nodes have a dialog for configuration.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MekaClassifierNodeDialog createNodeDialogPane() {
        return new MekaClassifierNodeDialog(m_MekaClassifier);
    }

    /**
     * {@inheritDoc}
     */
   protected void addNodeDescription(final KnimeNodeDocument doc) {
  //  protected NodeDescription createNodeDescription(){
        KnimeNode node = doc.addNewKnimeNode();
        node.setIcon("../weka.png");
        node.setType(KnimeNode.Type.LEARNER);
        node.setName(m_MekaClassifier.getClass().getSimpleName() + " ("
                + m_version + ")");

        // Create the short description
        List<String> description =
                MekaConverter.extractText(KnowledgeFlowApp
                        .getGlobalInfo(m_MekaClassifier));
        if (description.size() == 0) {
            description.add("Weka Classifier " + node.getName());
        }
        node.setShortDescription(description.get(0));
        // Create the full description
        FullDescription fullDesc = node.addNewFullDescription();
        Intro intro = fullDesc.addNewIntro();
        P p;
        for (String s : description) {
            p = intro.addNewP();
            p.newCursor().setTextValue(s);
        }
        p = intro.addNewP();
        p.newCursor().setTextValue("(based on WEKA " + m_version + ")");
        p = intro.addNewP();
        p.newCursor().setTextValue(
                "For further options, click the 'More' - button in "
                        + "the dialog.");
        p = intro.addNewP();
        p.newCursor().setTextValue(
                "All weka dialogs have a panel where you "
                        + "can specify classifier-specific parameters.");

        // Add an option
        Option option = fullDesc.addNewOption();
        option.setName("Class column");
        option.newCursor().setTextValue(
                "Choose the column that contains the target variable.");
        option = fullDesc.addNewOption();
        option.setName("Preliminary Attribute Check");
        p = option.addNewP();
        p.newCursor()
                .setTextValue(
                        "The Preliminary Attribute Check tests "
                                + "the underlying classifier against the DataTable "
                                + "specification at the inport of the node. Columns that are "
                                + "compatible with the classifier are marked with a green 'ok'."
                                + " Columns which are potentially not compatible are assigned "
                                + "a red error message.");
        p = option.addNewP();
        p.newCursor()
                .setTextValue(
                        " If a column is marked as 'incompatible', "
                                + "it does not necessarily mean that the classifier cannot be "
                                + "executed! Sometimes, the error message 'Cannot handle "
                                + "String class' simply means that no nominal values are "
                                + "available (yet). This may change during execution of the "
                                + "predecessor nodes.");
        XmlCursor pCursor = p.newCursor();
        pCursor.toNextToken();
        pCursor.insertElementWithText(new QName(node.getDomNode().getNamespaceURI(), "b"), "Important: ");
        p = option.addNewP();
        p.newCursor().setTextValue(
                m_MekaClassifier.getCapabilities().toString());

        // add the classifier options
        option = fullDesc.addNewOption();
        option.setName("Classifier Options");
        @SuppressWarnings("unchecked")
        Enumeration<weka.core.Option> wekaOptions =
                m_MekaClassifier.listOptions();
        List<P> paragraphs = new ArrayList<P>();
        while (wekaOptions.hasMoreElements()) {
            P optionP = P.Factory.newInstance();
            weka.core.Option o = wekaOptions.nextElement();
            optionP.newCursor().setTextValue(o.name() + ": " + o.description());
            paragraphs.add(optionP);
        }
        option.setPArray(paragraphs.toArray(new P[0]));

        // Create the port descriptions
        Ports ports = node.addNewPorts();
        InPort inPort = ports.addNewInPort();
        inPort.setIndex(0);
        inPort.setName("Training data");
        inPort.newCursor().setTextValue("Training data");
        OutPort outPort = ports.addNewOutPort();
        outPort.setName("Trained classifier");
        outPort.setIndex(0);
        outPort.newCursor().setTextValue("Trained classifier");

        // Create the views
        Views views = node.addNewViews();
        View view = views.addNewView();
        view.setIndex(0);
        view.setName("Weka Node View");
        view.newCursor()
                .setTextValue(
                        "Each Weka node provides a summary view that provides "
                                + "information about the classification. If the test data "
                                + "contains a class column, an evaluation is generated.");       
    }

    /**
     * {@inheritDoc}
     * @throws InvalidSettingsException 
     */
    @Override
    public void loadAdditionalFactorySettings(final ConfigRO config) throws InvalidSettingsException
            {
    	/*Loading configuration key for Meka Class
    	 * */
    	String clazz="" ;
        try {
        	 clazz= config.getString(MekaTestNodeSetFactory.MEKA_CLASS_KEY);
            
        	m_MekaClassifier=(MultiLabelClassifier)new BR();
    		String t1=AbstractClassifier.class.getName();
    		m_MekaClassifier =                        
            		(MultiLabelClassifier)Class.forName(clazz).newInstance();
        } catch (Exception e) {
            throw new InvalidSettingsException(
                    "Could not initialize WEKA classifier " + clazz + ".", e);
        }
        m_version = config.getString(MekaTestNodeSetFactory.MEKA_VERSION_KEY);
        super.loadAdditionalFactorySettings(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalFactorySettings(final ConfigWO config) {
        config.addString(MekaTestNodeSetFactory.MEKA_CLASS_KEY, m_MekaClassifier
                .getClass().getName());
        config.addString(MekaTestNodeSetFactory.MEKA_VERSION_KEY, m_version);
        super.saveAdditionalFactorySettings(config);
    }
}
