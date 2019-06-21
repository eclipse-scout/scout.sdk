package org.eclipse.scout.sdk.s2i.settings

import com.intellij.openapi.ui.ComboBox
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.IdeaLogger
import java.awt.Dimension
import java.awt.Insets
import javax.swing.DefaultComboBoxModel
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel

class ScoutSettingsForm : JPanel() {

    private val m_htmlDisable = "html.disable"
    private val m_autoUpdateDerivedResources: JCheckBox
    private val m_autoCreateClassIdAnnotations: JCheckBox
    private val m_cboLogLevel: ComboBox<IdeaLogger.LogLevel>
    private val m_lblLogLevel: JLabel

    init {
        putClientProperty(m_htmlDisable, true)

        layout = GridLayoutManager(4, 2, Insets(0, 0, 0, 0), -1, -1)

        m_autoUpdateDerivedResources = JCheckBox()
        m_autoUpdateDerivedResources.text = EclipseScoutBundle.message("automatically.update.generated.classes")
        m_autoUpdateDerivedResources.putClientProperty(m_htmlDisable, true)

        m_autoCreateClassIdAnnotations = JCheckBox()
        m_autoCreateClassIdAnnotations.text = EclipseScoutBundle.message("automatically.create.classid.annotation")
        m_autoCreateClassIdAnnotations.putClientProperty(m_htmlDisable, true)

        m_lblLogLevel = JLabel()
        m_lblLogLevel.text = EclipseScoutBundle.message("log.level")
        m_lblLogLevel.putClientProperty(m_htmlDisable, true)

        val logLevelModel = DefaultComboBoxModel<IdeaLogger.LogLevel>()
        logLevelModel.addElement(IdeaLogger.LogLevel.ERROR)
        logLevelModel.addElement(IdeaLogger.LogLevel.WARNING)
        logLevelModel.addElement(IdeaLogger.LogLevel.INFO)
        logLevelModel.addElement(IdeaLogger.LogLevel.DEBUG)
        m_cboLogLevel = ComboBox()
        m_cboLogLevel.model = logLevelModel
        m_cboLogLevel.putClientProperty(m_htmlDisable, true)


        add(
            m_autoUpdateDerivedResources,
            GridConstraints(
                0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false
            )
        )
        add(
            m_autoCreateClassIdAnnotations,
            GridConstraints(
                1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false
            )
        )
        add(
            m_lblLogLevel,
            GridConstraints(
                2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, Dimension(82, 16), null, 0, false
            )
        )
        add(
            m_cboLogLevel,
            GridConstraints(
                2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false
            )
        )
        add(
            Spacer(),
            GridConstraints(
                3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null, 0, false
            )
        )
    }

    var isAutoUpdateDerivedResources
        get() = m_autoUpdateDerivedResources.isSelected
        set(value) {
            m_autoUpdateDerivedResources.isSelected = value
        }

    var isAutoCreateClassId
        get() = m_autoCreateClassIdAnnotations.isSelected
        set(value) {
            m_autoCreateClassIdAnnotations.isSelected = value
        }

    var logLevel
        get() = m_cboLogLevel.selectedItem as IdeaLogger.LogLevel
        set(value) {
            m_cboLogLevel.selectedItem = value
        }
}
