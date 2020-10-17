/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.template

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.impl.LookupImpl
import junit.framework.AssertionFailedError
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.s2i.AbstractTestCaseWithRunningClasspathModule
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message


class TemplateTest : AbstractTestCaseWithRunningClasspathModule() {

    override fun getTestDataPath() = "src/test/resources/template"
    private var m_oldAutoCreateClassId: Boolean = false

    override fun setUp() {
        super.setUp()
        m_oldAutoCreateClassId = ClassIds.isAutomaticallyCreateClassIdAnnotation()
        ClassIds.setAutomaticallyCreateClassIdAnnotation(true)
    }

    override fun tearDown() {
        ClassIds.setAutomaticallyCreateClassIdAnnotation(m_oldAutoCreateClassId)
        super.tearDown()
    }

    fun testCompletionStringField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.StringField"))
    }

    fun testCompletionBigDecimalField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.BigDecimalField"))
    }

    fun testCompletionBooleanField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.BooleanField"))
    }

    fun testCompletionButton() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.Button"))
    }

    fun testCompletionCalendarField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.CalendarField"))
    }

    fun testCompletionDateField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.DateField"))
    }

    fun testCompletionFileChooserField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.FileChooserField"))
    }

    fun testCompletionGroupBox() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.GroupBox"))
    }

    fun testCompletionHtmlField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.HtmlField"))
    }

    fun testCompletionLabelField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.LabelField"))
    }

    fun testCompletionListBox() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.ListBox"))
    }

    fun testCompletionProposalField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.ProposalField"))
    }

    fun testCompletionSmartField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.SmartField"))
    }

    fun testCompletionLongField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.LongField"))
    }

    fun testCompletionRadioButtonGroup() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.RadioButtonGroup"))
    }

    fun testCompletionSequenceBox() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.SequenceBox"))
    }

    fun testCompletionTabBox() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.TabBox"))
    }

    fun testCompletionTableField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.TableField"))
    }

    fun testCompletionTreeField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.TreeField"))
    }

    fun testCompletionRadioButton() {
        doCompletionAndAssertNoCompileErrors("TestRadioButtonGroupForm", message("template.RadioButton"))
    }

    fun testCompletionImageField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.ImageField"))
    }

    fun testCompletionFileChooserButton() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.FileChooserButton"))
    }

    fun testCompletionTagField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.TagField"))
    }

    fun testCompletionModeSelectorField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.ModeSelectorField"))
    }

    fun testCompletionBrowserField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.BrowserField"))
    }

    fun testCompletionTileField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.TileField"))
    }

    fun testCompletionAccordionField() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.AccordionField"))
    }

    fun testCompletionMenu() {
        doCompletionAndAssertNoCompileErrors("TestRadioButtonGroupForm", message("template.Menu"))
    }

    fun testCompletionMenuWithoutMenuTypes() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.Menu"))
    }

    fun testCompletionKeyStroke() {
        doCompletionAndAssertNoCompileErrors("TestForm", message("template.KeyStroke"))
    }

    fun testCompletionCode() {
        doCompletionAndAssertNoCompileErrors("TestCodeType", message("template.Code"))
    }

    fun testCompletionFormHandler() {
        doCompletionAndAssertNoCompileErrors("TestFormExtension", message("template.FormHandler"))
    }

    fun testCompletionColumn() {
        doCompletionAndAssertNoCompileErrors("TestTableFieldForm", message("template.Column"))
    }

    fun testCompletionTile() {
        doCompletionAndAssertNoCompileErrors("TestTileFieldForm", message("template.Tile"))
    }

    fun testCompletionGroup() {
        doCompletionAndAssertNoCompileErrors("TestAccordionFieldForm", message("template.Group"))
    }

    private fun doCompletionAndAssertNoCompileErrors(testClassName: String, finishLookupName: String) {
        val psiFile = myFixture.configureByFile(testClassName + JavaTypes.JAVA_FILE_SUFFIX)
        myFixture.complete(CompletionType.BASIC, 1)
        val lookupElementToSelect = myFixture.lookupElements
                ?.filter { it.getUserData(TemplateCompletionContributor.SCOUT_TEMPLATE_MARKER) ?: false }
                ?.first { it.lookupString == finishLookupName }
                ?: throw AssertionFailedError("No LookupElement with name '$finishLookupName' found in completion list.")
        val lookup = myFixture.lookup as LookupImpl
        lookup.finishLookup('\n', lookupElementToSelect)
        assertNoCompileErrors(testClassName, psiFile.text)
    }

    private fun assertNoCompileErrors(simpleName: String, source: String) {
        // exclude IntelliJ jars as these are not required
        JavaEnvironmentWithEcjBuilder.create()
                .withoutScoutSdk()
                .excludeIfContains("/com.jetbrains.intellij.idea/")
                .excludeIfContains("/org.jetbrains.kotlin/")
                .accept {
                    assertNoCompileErrors(it, null, simpleName, source)
                }
    }
}