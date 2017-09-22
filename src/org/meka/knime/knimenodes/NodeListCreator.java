/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   01.10.2013 (thor): created
 */
package org.meka.knime.knimenodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.meka.knime.knimenodes.NodeListCreator;

import weka.associations.Associator;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;

/**
 * Simple class that reads all jars files in a directory, checks whether they
 * are usable Weka classifiers, clusterers, or associators and then writes out a
 * properties file with the class names
 * 
 * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
 */
public class NodeListCreator {

    /**
     * The main methods
     * 
     * @param args command line arguments
     * @throws Exception if an error occurs
     */
    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: " + NodeListCreator.class
                    + " libDir output-file");
            System.exit(1);
        }

        StringBuilder classifiers = new StringBuilder();
        StringBuilder associators = new StringBuilder();
        StringBuilder clusterers = new StringBuilder();
        for (Class<?> c : searchClasspath(new File(args[0]),
                new ArrayList<Class<?>>())) {
            if (Clusterer.class.isAssignableFrom(c)) {
                if (isInstantiable(c)) {
                    clusterers.append(c.getName()).append(", ");
                }
            } else if (Classifier.class.isAssignableFrom(c)) {
                if (isInstantiable(c)) {
                    classifiers.append(c.getName()).append(", ");
                }
            } else if (Associator.class.isAssignableFrom(c)) {
                if (isInstantiable(c)) {
                    associators.append(c.getName()).append(", ");
                }
            }
        }

        Properties props = new Properties();
        if (clusterers.length() > 0) {
            props.put(Clusterer.class.getName(),
                    clusterers.substring(0, clusterers.length() - 2));
        }
        if (classifiers.length() > 0) {
            props.put(Classifier.class.getName(),
                    classifiers.substring(0, classifiers.length() - 2));
        }
        if (associators.length() > 0) {
            props.put(Associator.class.getName(),
                    associators.substring(0, associators.length() - 2));
        }
        props.store(new FileWriter(args[1]), "");
    }

    private static boolean isInstantiable(Class<?> clazz) {
        try {
            // instantiate the class mainly to test for the existence of an
            // empty public constructor
            clazz.newInstance();
            return true;
        } catch (InstantiationException e) {
            System.err.println("Could not instantiate class "
                    + clazz.getCanonicalName()
                    + ", has empty public constructor?");
        } catch (IllegalAccessException e) {
            // ignore
        }
        return false;
    }

    private static List<Class<?>> searchClasspath(final File libDir,
            List<Class<?>> classes) throws IOException {
        for (File f : libDir.listFiles()) {
            if (f.getName().endsWith(".jar")) {
                checkJarForClasses(f, classes);
            }
            if (f.isDirectory()) {
                searchClasspath(f, classes);
            }
        }
        return classes;
    }

    private static void checkJarForClasses(final File jarFile,
            final List<Class<?>> classNames) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry entry;

        while ((entry = zipIn.getNextEntry()) != null) {
            String name = entry.getName();
            if (name.endsWith(".class")) {
                String className = name.replace('/', '.');
                className =
                        className.substring(0,
                                className.length() - ".class".length());
                try {
                    Class<?> clazz = Class.forName(className);
                    if (!clazz.isInterface()
                            && ((clazz.getModifiers() & Modifier.ABSTRACT) == 0)) {
                        classNames.add(clazz);
                    }
                } catch (ClassNotFoundException ex) {
                    System.err.println("Could not load class " + className
                            + " from " + jarFile.getAbsolutePath()
                            + ", is it in the classpath?");
                } catch (NoClassDefFoundError err) {
                    // ignore
                }
            }
        }
        zipIn.close();
    }
}
