package org.eclipse.scout.sdk.s2i

import com.intellij.ide.macro.Macro
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.PathManager

open class IdeaHomePathMacro : Macro() {

    override fun getName(): String = "IdeaHomePath"

    override fun getDescription(): String = "Installation Path of the running IntelliJ IDEA instance."

    override fun expand(dataContext: DataContext?): String? = PathManager.getHomePath()
}