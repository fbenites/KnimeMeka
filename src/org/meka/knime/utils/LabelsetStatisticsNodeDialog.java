package org.meka.knime.utils;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

/**
 * <code>NodeDialog</code> for the "LabelsetStatistics" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Fernando Benites
 */
public class LabelsetStatisticsNodeDialog extends NodeDialogPane {

    /* Holds the selected class column */
    private final ColumnSelectionPanel m_colsel;
    /* DataTableSpec at the input port */
    private DataTableSpec m_spec;
    private final JPanel m_all;
    public static final String CLASSCOL_KEY = "ClassColumn";
    /**
     * New pane for configuring LabelsetStatistics node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected LabelsetStatisticsNodeDialog() {
        super();
        m_all = new JPanel(new GridLayout(1, 1));


        JPanel panel = new JPanel(new BorderLayout());
                    
    
    m_colsel =
            new ColumnSelectionPanel("Select target column",
                    DataValue.class);
    //panel.add(m_colsel, BorderLayout.SOUTH);
    m_all.add(m_colsel);
    super.addTab("Options", m_all);
    
    }
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
        m_spec = (DataTableSpec)specs[0];
        if (m_spec.getNumColumns() > 0) {
            // class column
            String fallbackClasscol =
                    m_spec.getColumnSpec(m_spec.getNumColumns() - 1).getName();
            String classcol =
                    settings.getString(CLASSCOL_KEY,
                            fallbackClasscol);
            if (m_spec.findColumnIndex(classcol) < 0) {
                classcol = fallbackClasscol;
            }
            m_colsel.update(m_spec, classcol);
        }
}
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        settings.addString(CLASSCOL_KEY,
                m_colsel.getSelectedColumn());
    }
}

