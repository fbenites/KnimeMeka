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
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.config.Config;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

/**
 * 
 * @author cebron, University of Konstanz
 */
public class MekaClustererModelPortObjectSpec implements PortObjectSpec {

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(MekaClustererModelPortObjectSpec.class);

    /*
     * Model info identifier.
     */
    private static final String MODEL_INFO = "model_info";

    /**
     * Key to store the DataTableSpec.
     */
    private static final String SPEC_KEY = "tablespec";

    /**
     * Key to store the number of clusters.
     */
    private static final String NRCLUSTERS_KEY = "nrclusters";

    /**
     * @return Serializer for the {@link MekaClustererModelPortObjectSpec}.
     */
    public static PortObjectSpecSerializer<MekaClustererModelPortObjectSpec> getPortObjectSpecSerializer() {
        return new PortObjectSpecSerializer<MekaClustererModelPortObjectSpec>() {
            /** {@inheritDoc} */
            @Override
            public void savePortObjectSpec(
                    final MekaClustererModelPortObjectSpec portObject,
                    final PortObjectSpecZipOutputStream out) throws IOException {
                portObject.save(out);

            }

            /** {@inheritDoc} */
            @Override
            public MekaClustererModelPortObjectSpec loadPortObjectSpec(
                    final PortObjectSpecZipInputStream in) throws IOException {
                return load(in);
            }
        };
    }

    private void save(final PortObjectSpecZipOutputStream out) {
        ModelContent model = new ModelContent(MODEL_INFO);
        Config specconf = model.addConfig(SPEC_KEY);
        m_spec.save(specconf);
        model.addInt(NRCLUSTERS_KEY, m_nrClusters);
        try {
            out.putNextEntry(new ZipEntry("mapper.xmlout"));
            model.saveToXML(out);
        } catch (IOException ioe) {
            LOGGER.error("Internal error: Could not save settings", ioe);
        }
    }

    private static MekaClustererModelPortObjectSpec load(
            final PortObjectSpecZipInputStream in) {
        ModelContentRO model = null;
        try {
            ZipEntry zentry = in.getNextEntry();
            assert zentry.getName().equals("mapper.xmlout");
            model = ModelContent.loadFromXML(in);
        } catch (IOException ioe) {
            LOGGER.error("Internal error: Could not load settings", ioe);
        }
        int nrClusters = 0;
        DataTableSpec spec = null;
        try {
            nrClusters = model.getInt(NRCLUSTERS_KEY);
            spec = DataTableSpec.load(model.getConfig(SPEC_KEY));
        } catch (InvalidSettingsException ise) {
            LOGGER.error("Internal error: Could not load settings", ise);
        }
        return new MekaClustererModelPortObjectSpec(spec, nrClusters);
    }

    private DataTableSpec m_spec;

    private int m_nrClusters;

    /**
     * The {@link MekaClustererModelPortObjectSpec} holds the columns of the
     * training data.
     * 
     * @param spec {@link DataTableSpec} of training data.
     * @param nrClusters the number of clusters.
     */
    public MekaClustererModelPortObjectSpec(final DataTableSpec spec,
            final int nrClusters) {
        m_spec = spec;
        m_nrClusters = nrClusters;
    }

    /**
     * @return the training {@link DataTableSpec}.
     */
    public DataTableSpec getSpec() {
        return m_spec;
    }

    /**
     * @return the number of clusters.
     */
    public int getNrClusters() {
        return m_nrClusters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent[] getViews() {
        return new JComponent[]{};
    }
}
