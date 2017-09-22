/* 
 * -------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 * 
 * History
 *   21.07.2006 (cebron): created
 */
package org.meka.knime.utils;

import weka.clusterers.Clusterer;
import weka.core.Instances;

/**
 * A Thread to create a WekaClusterer. This allows to check for
 * cancel-operations during the building of the classifier.
 * 
 * @author cebron, University of Konstanz
 */
public class MekaClustererCreationTask extends Thread {

    /*
     * The clusterer to be trained.
     */
    private Clusterer m_clusterer;

    /*
     * The instances to build the clusterer.
     */
    private Instances m_instances;

    /*
     * Any exception, if it has been thrown
     */
    private Throwable m_throwable;

    /*
     * Flag if an exception was thrown.
     */
    private boolean m_exceptionThrown;

    /**
     * Creates a new WekaClusterer-Thread with the given clusterer and
     * instances.
     * 
     * @param clusterer to built.
     * @param instances for building.
     */
    public MekaClustererCreationTask(final Clusterer clusterer,
            final Instances instances) {
        m_clusterer = clusterer;
        m_instances = instances;
        m_throwable = null;
        m_exceptionThrown = false;
    }

    /**
     * Starts the learning process.
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            m_clusterer.buildClusterer(m_instances);
        } catch (Throwable e) {
            m_throwable = e;
            m_exceptionThrown = true;
        }
    }

    /**
     * @return the trained weka-clusterer.
     */
    public Clusterer getClusterer() {
        return m_clusterer;
    }

    /**
     * @return if an exception was thrown during training.
     */
    public boolean exceptionThrown() {
        return m_exceptionThrown;
    }

    /**
     * @return the exception if it has been thrown.
     */
    public Throwable getThrowable() {
        return m_throwable;
    }
}
