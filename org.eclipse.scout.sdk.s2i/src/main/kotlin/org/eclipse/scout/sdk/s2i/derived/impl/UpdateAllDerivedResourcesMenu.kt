package org.eclipse.scout.sdk.s2i.derived.impl

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.search.GlobalSearchScope
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager

class UpdateAllDerivedResourcesMenu : AnAction("Update all derived resources...") {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return
        val answer = Messages.showYesNoDialog("This will update all derived resources.\n" +
                "Depending on the size of your project this can take several minutes.\nDo you want to continue?",
                "Do you want to update all derived resources?", Messages.getQuestionIcon())
        if (answer == Messages.YES) {
            val manager = project.getComponent(DerivedResourceManager::class.java)
            manager.trigger(GlobalSearchScope.projectScope(project))
        }
    }
}