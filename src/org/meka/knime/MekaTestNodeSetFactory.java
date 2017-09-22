package org.meka.knime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSetFactory;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeView;
import org.knime.core.node.config.ConfigRO;
import org.meka.knime.classifier.MekaClassifierNodeFactory;
import org.meka.knime.predictor.MekaPredictorNodeFactory;
import org.meka.knime.ranking.MekaRankingNodeFactory;


import weka.classifiers.Classifier;
import weka.classifiers.AbstractClassifier;
import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.gui.beans.PluginManager;
import org.knime.core.node.NodeDialogPane;

/**
 * <code>NodeFactory</code> for the "MekaTest" Node.
 * 
 *
 * @author Waqar
 */
public class MekaTestNodeSetFactory 
        //extends NodeFactory<MekaTestNodeModel> {
		implements NodeSetFactory {    
	/** The config key for the meka class. */
    public static final String MEKA_CLASS_KEY = "meka-class";

    /** The config key for the meka version. */
    public static final String MEKA_VERSION_KEY = "meka-version";


  
	
	private static final Map<String, Class<? extends NodeFactory<? extends NodeModel>>> MEKA_NODE_FACTORIES;
	
	private static final Collection<String> MEKA_CLASSIFIERS;

    private static final Collection<String> ALL_MEKA_CLASSES;

    private static final Map<String, String> MEKA_CATEGORIES;
    
    public static final String MEKA_VERSION = " 1.6";
    
    private static final String EXT_POINT_ID_MEKA_CLUSTERER =
            "org.knime.ext.meka_" + MEKA_VERSION + ".mekaclusterer";
    
    private static final String EXT_POINT_ID_MEKA_CLASSIFIER =
            "org.meka.knime.mekaclassifier";    


    /**
     * Loads the meka node factories.
     * 
     */
    static {
        Properties mekaProperties = new Properties();
        try {

            // load all meka classes from the meka library
            mekaProperties.load((MekaTestNodeSetFactory.class
                    .getResourceAsStream("meka-classes.props")));

            MEKA_CLASSIFIERS = 
                    getClassesFromProperties(mekaProperties, MultiLabelClassifier.class);
            ALL_MEKA_CLASSES = new ArrayList<String>();
            ALL_MEKA_CLASSES.addAll(MEKA_CLASSIFIERS);

            MEKA_CATEGORIES = new HashMap<String, String>();
            MEKA_NODE_FACTORIES =
                    new HashMap<String, Class<? extends NodeFactory<? extends NodeModel>>>();

            //automatically create the categories from the class paths
            for (String s : ALL_MEKA_CLASSES) {
                String category = s.substring(0, s.lastIndexOf('.'));
                MEKA_CATEGORIES.put(s, category.replace('.', '/'));
            }

            // load additional classes defined by the extension points
            Map<String, String> tmp =
                    getClassifierClassesFromExtensionsPoints();
            MEKA_CATEGORIES.putAll(tmp);
            ALL_MEKA_CLASSES.addAll(tmp.keySet());
            MEKA_CLASSIFIERS.addAll(tmp.keySet());


            Class<MekaClassifierNodeFactory> classifierClass =
            		MekaClassifierNodeFactory.class;
            for (String s : MEKA_CLASSIFIERS) {
                MEKA_NODE_FACTORIES.put(s, classifierClass);
            }
            //Hard Code
            Class<MekaPredictorNodeFactory> predictorClass =
            		MekaPredictorNodeFactory.class;
            
            MEKA_NODE_FACTORIES.put("meka.predictors.MekaPredictor",predictorClass);
            ALL_MEKA_CLASSES.add("meka.predictors.MekaPredictor");
            MEKA_CATEGORIES.put("meka.predictors.MekaPredictor", "meka/predictors/MekaPredictor");
            
            Class<MekaRankingNodeFactory> mekaRanking =
            		MekaRankingNodeFactory.class;
            
            MEKA_NODE_FACTORIES.put("meka.core.MLEvalUtils",mekaRanking);
            ALL_MEKA_CLASSES.add("meka.core.MLEvalUtils");
            MEKA_CATEGORIES.put("meka.core.MLEvalUtils", "meka/predictors/MekaRanking");
          //Hard Code

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Could not initialize the meka properties.", ex);
        }

        // make the classes available within meka
        try {
            // add the classifieres etc. from the meka-classes.props-file
            PluginManager.addFromProperties(mekaProperties);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize meka nodes!", e);
        }
        
    }
    /**
     * {@inheritDoc}
     */
    public MekaTestNodeSetFactory createNodeModel() {
        return new MekaTestNodeSetFactory();
    }
    
    private static Map<String, String> getClassifierClassesFromExtensionsPoints() {
        Map<String, String> classes = new HashMap<String, String>();
        //
        // Process the contributed classifiers
        //
        IExtension[] nodeExtensions =
        		MekaTestNodeSetFactory.getExtensions(EXT_POINT_ID_MEKA_CLASSIFIER);
        for (IExtension ext : nodeExtensions) {
            // iterate through the config elements and create 'NodeTemplate'
            // objects
            IConfigurationElement[] elements = ext.getConfigurationElements();
            for (IConfigurationElement elem : elements) {
                if (!Platform.isRunning()) { // shutdown was initiated
                    return null;
                }
                Classifier c;
                try {
                    c =
                            (Classifier)elem
                                    .createExecutableExtension("classifier-class");
                } catch (CoreException e) {
                    throw new IllegalArgumentException(
                            "Can't load classifier class "
                                    + elem.getAttribute("classifier-class"), e);
                }
                classes.put(c.getClass().getName(),
                        elem.getAttribute("category"));
            }
        }

        return classes;
    }
    
    private static boolean checkClassExistence(final String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private static IExtension[] getExtensions(final String pointID) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(pointID);
        if (point == null) {
            throw new IllegalStateException("Invalid extension point : "
                    + pointID);

        }
        return point.getExtensions();
    }
    
    private static List<String> getClassesFromProperties(
            final Properties props, final Class<?>... classNames) {
        List<String> classes = new ArrayList<String>();

        for (Class<?> clazz : classNames) {
            String tmp = props.getProperty(clazz.getName());
            StringTokenizer st = new StringTokenizer(tmp, ", ");
            while (st.hasMoreTokens()) {
                // filter some classifier classes (i.e. multi-instance learning)
                String nt = st.nextToken();
                if (!nt.contains(".mi.") && checkClassExistence(nt)) {
                    classes.add(nt);
                }
            }
        }
        return classes;

    }

	@Override
	public Collection<String> getNodeFactoryIds() {

		 return Collections.unmodifiableCollection(ALL_MEKA_CLASSES);

	}

	@Override
	public Class<? extends NodeFactory<? extends NodeModel>> getNodeFactory(
			String id) {

        return MEKA_NODE_FACTORIES.get(id);
	}

	@Override
	public String getCategoryPath(String id) {
        String string = MEKA_CATEGORIES.get(id);
        if (string != null) {
            string = string.replaceFirst("meka/", "meka/meka" + MEKA_VERSION + "/");
        }
        return string;
	}

	@Override
	public String getAfterID(String id) {
        return "";
	}

	@Override
	public ConfigRO getAdditionalSettings(String id) {
        NodeSettings settings = new NodeSettings("meka-factory");
        settings.addString(MEKA_CLASS_KEY, id);
        settings.addString(MEKA_VERSION_KEY, MEKA_VERSION);
        return settings;
	}

}

