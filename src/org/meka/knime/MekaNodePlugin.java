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
 *   17.10.2005 (cebron): created
 */
package org.meka.knime;

import java.util.HashSet;
import java.util.Hashtable;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import weka.classifiers.Classifier;
import weka.core.Environment;
//import weka.gui.GenericObjectEditor;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.gui.goe.GenericObjectEditor;

/**
 * This is the eclipse bundle activator. Note: KNIME node vendors probably won't
 * have to do anything here, as this class is only needed by the eclipse
 * platform/plugin mechanism.
 * 
 * @author cebron, University of Konstanz
 */
public class MekaNodePlugin extends Plugin {
    // The shared instance.
    private static MekaNodePlugin plugin;

    /**
     * The constructor.
     */
    public MekaNodePlugin() {
        super();
        plugin = this;


        
        meka.gui.goe.GenericObjectEditor.registerAllEditors();        


    }

    /**
     * This method is called when the plug-in is stopped.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be stopped
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     * 
     * @return Singleton instance of the Plugin
     */
    public static MekaNodePlugin getDefault() {
        return plugin;
    }
}
