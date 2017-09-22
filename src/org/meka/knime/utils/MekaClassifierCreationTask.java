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

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.UnsupportedClassTypeException;

/**
 * A Thread to create a MekaClassifier. This allows to check for
 * cancel-operations during the building of the classifier.
 * 
 * @author cebron, University of Konstanz
 */
public class MekaClassifierCreationTask extends Thread {

    /*
     * The classifier to be trained.
     */
    private Classifier m_classifier;

    /*
     * The instances to train the classifier.
     */
    private Instances m_instances;

    /*
     * Flag for the UnsupportedClassTypeException, if it has been thrown.
     */
    private boolean m_classtypeexception;

    /*
     * Flag for the UnsupportedAttributeTypeException, if it was thrown.
     */
    private boolean m_unsupportedattributetypeexception;

    /*
     * The UnsupportedAttributeTypeException, if it was thrown.
     */
    private UnsupportedAttributeTypeException m_attributetypeException;

    /*
     * Any exception, if it has been thrown
     */
    private Throwable m_throwable;

    /*
     * Flag if an exception was thrown.
     */
    private boolean m_exceptionThrown;

    /**
     * Creates a new WekaClassifer-Thread with the given classifier and
     * instances.
     * 
     * @param classifier to train.
     * @param instances to train with.
     */
    public MekaClassifierCreationTask(final Classifier classifier,
            final Instances instances) {
        m_classifier = classifier;
        m_instances = instances;
        m_classtypeexception = false;
        m_attributetypeException = null;
        m_unsupportedattributetypeexception = false;
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
            m_classifier.buildClassifier(m_instances);
        } catch (UnsupportedClassTypeException e) {
            m_classtypeexception = true;
        } catch (UnsupportedAttributeTypeException e2) {
            m_attributetypeException = e2;
            m_unsupportedattributetypeexception = true;
        } catch (Throwable e) {
            m_throwable = e;
            m_exceptionThrown = true;
        }
    }

    /**
     * @return the trained weka-classifer.
     */
    public Classifier getClassifier() {
        return m_classifier;
    }

    /**
     * @return if a ClassTypeException was thrown during training.
     */
    public boolean classTypeExceptionThrown() {
        return m_classtypeexception;
    }

    /**
     * @return if a unsupported attribute type Exception was thrown during
     *         training.
     */
    public boolean unsupportedAttributeTypeExceptionThrown() {
        return m_unsupportedattributetypeexception;
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

    /**
     * 
     * @return the {@link UnsupportedAttributeTypeException}, if it has been
     *         thrown.
     */
    public UnsupportedAttributeTypeException getAttributeTypeException() {
        return m_attributetypeException;
    }

}
