package org.meka.knime.ranking;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "MekaPerformance" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Waqar
 */
public class MekaRankingNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring MekaPerformance node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected MekaRankingNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    MekaRankingNodeModel.CFGKEY_COUNT,
                    MekaRankingNodeModel.DEFAULT_COUNT,
                    Integer.MIN_VALUE, Integer.MAX_VALUE),
                    "Counter:", /*step*/ 1, /*componentwidth*/ 5));
        addDialogComponent(new DialogComponentStringSelection(
                new SettingsModelString(MekaRankingNodeModel.STRSEL, null),
                "Select Modes:","PCut1", "PCutL"));
                    
    }
}

