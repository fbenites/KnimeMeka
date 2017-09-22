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
 *   15.02.2008 (cebron): created
 */
package org.meka.knime.ports;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.knime.base.data.util.DataCellStringMapper;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.config.Config;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import weka.clusterers.Clusterer;

/**
 * 
 * @author cebron, University of Konstanz
 */
public class MekaClustererModelPortObject implements PortObject {

    /**
     * The Port Type.
     */
    public static final PortType TYPE = new PortType(
            MekaClustererModelPortObject.class);

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(MekaClustererModelPortObject.class);

    /*
     * Model info identifier.
     */
    private static final String MODEL_INFO = "model_info";

    /**
     * Key to store the DataCellStringMapper.
     */
    private static final String MAPPER_KEY = "mapper";

    /**
     * @return Serializer for the {@link WekaClassifierModelPortObject}
     */
    public static PortObjectSerializer<MekaClustererModelPortObject> getPortObjectSerializer() {
        return new PortObjectSerializer<MekaClustererModelPortObject>() {

            /** {@inheritDoc} */
            @Override
            public void savePortObject(
                    final MekaClustererModelPortObject portObject,
                    final PortObjectZipOutputStream out,
                    final ExecutionMonitor exec) throws IOException,
                    CanceledExecutionException {
                portObject.save(out);

            }

            /** {@inheritDoc} */
            @Override
            public MekaClustererModelPortObject loadPortObject(
                    final PortObjectZipInputStream in,
                    final PortObjectSpec spec, final ExecutionMonitor exec)
                    throws IOException, CanceledExecutionException {
                return load(in, (MekaClustererModelPortObjectSpec)spec);
            }
        };
    }

    private void save(final PortObjectZipOutputStream out) {
        ObjectOutputStream oo = null;
        // save weka clusterer
        try {
            out.putNextEntry(new ZipEntry("clusterer.objectout"));
            oo = new ObjectOutputStream(new NonClosableOutputStream.Zip(out));
            oo.writeObject(m_clusterer);
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

        // save meta information
        ModelContent model = new ModelContent(MODEL_INFO);
        Config mapperconf = model.addConfig(MAPPER_KEY);
        m_mapper.save(mapperconf);
        try {
            out.putNextEntry(new ZipEntry("mapper.xmlout"));
            model.saveToXML(out);
        } catch (IOException ioe) {
            LOGGER.error("Internal error: Could not save settings", ioe);
        }
    }

    private static MekaClustererModelPortObject load(
            final PortObjectZipInputStream in,
            final MekaClustererModelPortObjectSpec spec) {
        ObjectInputStream oi = null;
        Clusterer clusterer = null;
        ModelContentRO model = null;
        // load weka clusterer
        try {
            ZipEntry zentry = in.getNextEntry();
            assert zentry.getName().equals("clusterer.objectout");
            oi = new ObjectInputStream(new NonClosableInputStream.Zip(in));
            clusterer = (Clusterer)oi.readObject();
        } catch (IOException ioe) {
            LOGGER.error("Internal error: Could not load settings", ioe);
        } catch (ClassNotFoundException cnf) {
            LOGGER.error("Internal error: Could not load settings", cnf);
        } finally {
            if (oi != null) {
                try {
                    oi.close();
                } catch (Exception e) {
                    LOGGER.debug("Could not close stream", e);
                }
            }
        }

        // load meta info
        try {
            ZipEntry zentry = in.getNextEntry();
            assert zentry.getName().equals("mapper.xmlout");
            model = ModelContent.loadFromXML(in);
        } catch (IOException ioe) {
            LOGGER.error("Internal error: Could not load settings", ioe);
        }
        assert (clusterer != null);
        assert (model != null);

        DataCellStringMapper mapper = null;
        try {
            mapper = DataCellStringMapper.load(model.getConfig(MAPPER_KEY));
        } catch (InvalidSettingsException ise) {
            LOGGER.error("Internal error: Could not load settings", ise);
        }
        return new MekaClustererModelPortObject(clusterer, mapper, spec);
    }

    private Clusterer m_clusterer;

    private MekaClustererModelPortObjectSpec m_modelspec;

    private DataCellStringMapper m_mapper;

    /**
     * The WekaClustererModelPort holds information about the used clusterer and
     * columns.
     * 
     * @param clusterer Clusterer from weka.
     * @param spec training {@link DataTableSpec}.
     * @param mapper mapping DataCells to Strings.
     */
    public MekaClustererModelPortObject(final Clusterer clusterer,
            final DataCellStringMapper mapper,
            final MekaClustererModelPortObjectSpec spec) {
        m_clusterer = clusterer;
        m_mapper = mapper;
        m_modelspec = spec;
    }

    /**
     * @return the clusterer
     */
    public Clusterer getClusterer() {
        return m_clusterer;
    }

    /**
     * @return the training {@link DataTableSpec}.
     */
    @Override
	public MekaClustererModelPortObjectSpec getSpec() {
        return m_modelspec;
    }

    /** {@inheritDoc} */
    @Override
    public String getSummary() {
        return m_clusterer.getClass().getSimpleName();
    }

    /**
     * @return the mapper, mapping DataCells to Strings.
     */
    public DataCellStringMapper getMapper() {
        return m_mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent[] getViews() {
        JPanel classifierInfoPanel = new JPanel();
        JTextArea field = new JTextArea();
        String text = m_clusterer.getClass().getSimpleName() + "\n";
        text += m_clusterer.toString();
        field.setText(text);
        classifierInfoPanel.add(field);
        JComponent comp = new JScrollPane(classifierInfoPanel);
        comp.setName("Weka Outport");
        return new JComponent[]{comp};
    }
}
