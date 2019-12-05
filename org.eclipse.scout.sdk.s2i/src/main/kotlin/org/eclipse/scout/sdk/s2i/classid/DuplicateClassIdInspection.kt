package org.eclipse.scout.sdk.s2i.classid

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInspection.*
import com.intellij.codeInspection.actions.RunInspectionIntention
import com.intellij.codeInspection.ex.GlobalInspectionContextUtil
import com.intellij.codeInspection.ex.InspectionManagerEx
import com.intellij.ide.PowerSaveMode
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiClass
import com.intellij.psi.search.SearchScope
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.findAllTypesAnnotatedWith
import org.eclipse.scout.sdk.s2i.isInstanceOf
import java.awt.FlowLayout
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel


open class DuplicateClassIdInspection : GlobalInspectionTool() {

    var ignoreGenerated = true

    override fun runInspection(scope: AnalysisScope, manager: InspectionManager, globalContext: GlobalInspectionContext, problemDescriptionsProcessor: ProblemDescriptionsProcessor) =
            runInspection(manager.project, scope.toSearchScope()) { message, duplicate ->
                val quickFix = ChangeClassIdValueQuickFix(duplicate)
                val problem = manager.createProblemDescriptor(duplicate.psiAnnotation, message, false, arrayOf(quickFix), ProblemHighlightType.ERROR)
                problemDescriptionsProcessor.addProblemElement(GlobalInspectionContextUtil.retrieveRefElement(duplicate.psiAnnotation, globalContext), problem)
            }


    fun runInspection(project: Project, scope: SearchScope, problemConsumer: (String, ClassIdAnnotation) -> Unit) =
            project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.ClassId, scope)
                    .filter { accept(it) }
                    .mapNotNull { ClassIdAnnotation.of(it) }
                    .filter { it.hasValue() }
                    .groupBy { it.value() }
                    .values
                    .filter { it.size > 1 }
                    .forEach { registerProblemFor(it, problemConsumer) }

    protected fun accept(type: PsiClass): Boolean =
            !ignoreGenerated || !type.isInstanceOf(
                    IScoutRuntimeTypes.AbstractValueFieldData,
                    IScoutRuntimeTypes.AbstractFormData,
                    IScoutRuntimeTypes.AbstractTablePageData,
                    IScoutRuntimeTypes.AbstractTableFieldBeanData,
                    IScoutRuntimeTypes.AbstractFormFieldData)

    protected fun registerProblemFor(duplicates: List<ClassIdAnnotation>, problemCollector: (String, ClassIdAnnotation) -> Unit) {
        for (duplicate in duplicates) {
            val othersWithSameValue = IdeaEnvironment.computeInReadAction(duplicate.psiClass.project) {
                duplicates
                        .filter { d -> d != duplicate }
                        .map { d -> d.psiClass }
                        .map { psi -> psi.qualifiedName }
                        .joinToString(prefix = "[", postfix = "]")
            }
            val message = EclipseScoutBundle.message("duplicate.classid.value", othersWithSameValue)
            problemCollector.invoke(message, duplicate)
        }
    }

    override fun createOptionsPanel(): JComponent? {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        val ignoreGeneratedCheckBox = JCheckBox()
        ignoreGeneratedCheckBox.putClientProperty("html.disable", true)
        ignoreGeneratedCheckBox.text = EclipseScoutBundle.message("ignore.generated.classes")
        ignoreGeneratedCheckBox.isSelected = ignoreGenerated
        ignoreGeneratedCheckBox.addItemListener {
            ignoreGenerated = ignoreGeneratedCheckBox.isSelected
        }
        panel.add(ignoreGeneratedCheckBox)
        return panel
    }

    override fun isGraphNeeded(): Boolean = false

    companion object {

        private const val SHORT_NAME = "DuplicateClassId"

        fun isEnabledFor(project: Project): Boolean = !project.isDisposed
                && InspectionProjectProfileManager.getInstance(project).currentProfile.isToolEnabled(HighlightDisplayKey.find(SHORT_NAME))

        fun scheduleIfEnabled(project: Project, delay: Long, unit: TimeUnit): ScheduledFuture<*> = AppExecutorUtil.getAppScheduledExecutorService().schedule({
            try {
                if (PowerSaveMode.isEnabled()) {
                    SdkLog.info("Duplicate @ClassId validation skipped because the power save mode is activated.")
                    return@schedule
                }
                if (!isEnabledFor(project)) {
                    SdkLog.info("Duplicate @ClassId validation skipped because the Inspection is disabled for the project.")
                    return@schedule
                }

                val currentProfile = InspectionProjectProfileManager.getInstance(project).currentProfile
                val wrapper = currentProfile.getInspectionTool(SHORT_NAME, project)
                        ?: throw Ensure.newFail("Inspection '{}' could not be found.", SHORT_NAME)
                val inspectionMgr = InspectionManager.getInstance(project) as InspectionManagerEx
                val scope = AnalysisScope(project)
                scope.setSearchInLibraries(false)
                scope.isIncludeTestSource = true

                DumbService.getInstance(project).smartInvokeLater {
                    RunInspectionIntention.rerunInspection(wrapper, inspectionMgr, scope, null)
                }
                SdkLog.info("Duplicate @ClassId validation scheduled.")
            } catch (e: Throwable) {
                SdkLog.error("Error scheduling automatic @ClassId value validation.", e)
            }
        }, delay, unit)
    }
}

