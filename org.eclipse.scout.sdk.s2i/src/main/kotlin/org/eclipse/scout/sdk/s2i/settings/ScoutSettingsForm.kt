package org.eclipse.scout.sdk.s2i.settings

import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import java.awt.Insets
import javax.swing.JCheckBox
import javax.swing.JPanel

class ScoutSettingsForm : JPanel() {

    private val m_htmlDisable = "html.disable"
    private val m_autoUpdateDerivedResources: JCheckBox
    private val m_autoCreateClassIdAnnotations: JCheckBox

    init {
        putClientProperty(m_htmlDisable, true)

        layout = GridLayoutManager(4, 2, Insets(0, 0, 0, 0), -1, -1)

        m_autoUpdateDerivedResources = JCheckBox()
        m_autoUpdateDerivedResources.text = EclipseScoutBundle.message("automatically.update.generated.classes")
        m_autoUpdateDerivedResources.putClientProperty(m_htmlDisable, true)

        m_autoCreateClassIdAnnotations = JCheckBox()
        m_autoCreateClassIdAnnotations.text = EclipseScoutBundle.message("automatically.create.classid.annotation")
        m_autoCreateClassIdAnnotations.putClientProperty(m_htmlDisable, true)

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
}
