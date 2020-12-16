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
package org.eclipse.scout.sdk.s2i.template.js

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import junit.framework.AssertionFailedError
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils.removeWhitespace
import org.eclipse.scout.sdk.s2i.model.js.JsModel
import org.eclipse.scout.sdk.s2i.model.js.JsModelElement
import org.eclipse.scout.sdk.s2i.model.js.JsModelProperty
import org.eclipse.scout.sdk.s2i.template.TemplateHelper

class JsModelCompletionContributorTest : JavaCodeInsightFixtureTestCase() {

    companion object {
        const val UNKNOWN_OBJECT_FILE = "src/NameCompletionUnknownObject.js"
        const val NAME_COMPLETION_FILE = "src/NameCompletionModel.js"
        const val VALUE_COMPLETION_WIDGET = "src/ValueCompletionWidget.js"
        const val VALUE_COMPLETION_OBJECT_TYPE = "src/ValueCompletionObjectType.js"
        const val VALUE_COMPLETION_ENUM = "src/ValueCompletionEnum.js"

        const val STRING_FIELD_NAME = "StringField"
        const val WIDGET_STATE = "${JsModel.WIDGET_CLASS_NAME}.WidgetState"
        const val WIDGET_STATE_A = "$WIDGET_STATE.A"
        const val WIDGET_STATE_B = "$WIDGET_STATE.B"
        const val WIDGET_STATE_C = "$WIDGET_STATE.C"

        const val TEMPLATE_COMPLETION_CONTENT = "a"
        const val LABEL_PROPERTY_NAME = "label"
        const val STATE_PROPERTY_NAME = "state"
        const val CHILD_PROPERTY_NAME = "child"
        const val CHILDREN_PROPERTY_NAME = "children"
        const val MAX_LENGTH_PROPERTY_NAME = "maxLength"
        const val VISIBLE_PROPERTY_NAME = "visible"
        const val NAME_PROPERTY_NAME = "name"
        const val ONLY_HERE_PROPERTY_NAME = "onlyHere"
    }

    override fun getTestDataPath() = "src/test/resources/template/js"

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject(".", ".")
    }

    fun testJsModel() {
        val model = JsModel().build(myFixture.module)
        assertEquals(3, model.elements().size)
        assertEquals("JsModelEnum $WIDGET_STATE [A=unknown, B=unknown, C=unknown]", model.element(WIDGET_STATE).toString())
        assertEquals("JsModelClass ${JsModel.WIDGET_CLASS_NAME} [${JsModel.ID_PROPERTY_NAME}=object, ${JsModel.OBJECT_TYPE_PROPERTY_NAME}=object, $VISIBLE_PROPERTY_NAME=boolean, $NAME_PROPERTY_NAME=string, $CHILDREN_PROPERTY_NAME=widget[]," +
                " $CHILD_PROPERTY_NAME=widget, $STATE_PROPERTY_NAME=scout.$WIDGET_STATE, $LABEL_PROPERTY_NAME=text-key, $ONLY_HERE_PROPERTY_NAME=widget]",
                model.element(JsModel.WIDGET_CLASS_NAME).toString())
        val stringField = model.element(STRING_FIELD_NAME)
        assertEquals("JsModelClass $STRING_FIELD_NAME [$MAX_LENGTH_PROPERTY_NAME=numeric]", stringField.toString())

        assertTrue(model.isWidget(stringField))

        assertEquals(setOf(JsModel.WIDGET_CLASS_NAME, STRING_FIELD_NAME), model.valuesForProperty(model.property(JsModel.WIDGET_CLASS_NAME, CHILD_PROPERTY_NAME)!!).map { it.displayText }.toSet())
        assertEquals(setOf(true.toString(), false.toString()), model.valuesForProperty(model.property(JsModel.WIDGET_CLASS_NAME, VISIBLE_PROPERTY_NAME)!!).map { it.displayText }.toSet())
        assertEquals(setOf(WIDGET_STATE_A, WIDGET_STATE_B, WIDGET_STATE_C), model.valuesForProperty(model.property(JsModel.WIDGET_CLASS_NAME, STATE_PROPERTY_NAME)!!).map { it.displayText }.toSet())
        assertEquals(setOf(JsModel.WIDGET_CLASS_NAME, STRING_FIELD_NAME), model.valuesForProperty(model.property(JsModel.WIDGET_CLASS_NAME, JsModel.OBJECT_TYPE_PROPERTY_NAME)!!).map { it.displayText }.toSet())

        assertEquals(setOf(JsModel.ID_PROPERTY_NAME, JsModel.OBJECT_TYPE_PROPERTY_NAME, VISIBLE_PROPERTY_NAME, NAME_PROPERTY_NAME, CHILDREN_PROPERTY_NAME, CHILD_PROPERTY_NAME,
                STATE_PROPERTY_NAME, LABEL_PROPERTY_NAME, ONLY_HERE_PROPERTY_NAME, MAX_LENGTH_PROPERTY_NAME), model.properties(STRING_FIELD_NAME).keys)
        assertEquals(JsModelProperty.JsPropertyDataType.NUMERIC, model.property(STRING_FIELD_NAME, MAX_LENGTH_PROPERTY_NAME)?.dataType)
    }

    fun testNameCompletionWidget() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, CHILD_PROPERTY_NAME,
                "$CHILD_PROPERTY_NAME: { ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'}")
        assertEquals(JsModelProperty.JsPropertyDataType.WIDGET, (selectedElement as? JsModelProperty)?.dataType)
    }

    fun testNameCompletionWidgetArray() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, CHILDREN_PROPERTY_NAME,
                "$CHILDREN_PROPERTY_NAME: [{ ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'}]")
        val prop = selectedElement as JsModelProperty
        assertEquals(JsModelProperty.JsPropertyDataType.WIDGET, prop.dataType)
        assertTrue(prop.isArray)
    }

    fun testNameCompletionNumeric() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, MAX_LENGTH_PROPERTY_NAME, "$MAX_LENGTH_PROPERTY_NAME: ")
        assertEquals(JsModelProperty.JsPropertyDataType.NUMERIC, (selectedElement as? JsModelProperty)?.dataType)
    }

    fun testNameCompletionTextKey() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, LABEL_PROPERTY_NAME,
                "$LABEL_PROPERTY_NAME: '${TranslationPatterns.JsonTextKeyPattern.JSON_TEXT_KEY_PREFIX}$TEMPLATE_COMPLETION_CONTENT${TranslationPatterns.JsonTextKeyPattern.JSON_TEXT_KEY_SUFFIX}'")
        assertEquals(JsModelProperty.JsPropertyDataType.TEXT_KEY, (selectedElement as? JsModelProperty)?.dataType)
    }

    fun testNameCompletionUnknownObject() {
        doCompleteAssertContent(UNKNOWN_OBJECT_FILE, JsModel.OBJECT_TYPE_PROPERTY_NAME, "${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'")
    }

    fun testValueCompletionWidget() {
        doCompleteAssertContent(VALUE_COMPLETION_WIDGET, STRING_FIELD_NAME,
                "$ONLY_HERE_PROPERTY_NAME: { ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$STRING_FIELD_NAME' }")
    }

    fun testValueCompletionObjectType() {
        doCompleteAssertContent(VALUE_COMPLETION_OBJECT_TYPE, JsModel.WIDGET_CLASS_NAME,
                "${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '${JsModel.WIDGET_CLASS_NAME}' ")
    }

    fun testValueCompletionEnum() {
        doCompleteAssertContent(VALUE_COMPLETION_ENUM, WIDGET_STATE_B,
                "$STATE_PROPERTY_NAME: $WIDGET_STATE_B ",
                "import {${JsModel.WIDGET_CLASS_NAME}} from 'index';")
    }

    private fun doCompleteAssertContent(filePath: String, finishLookupName: String, vararg expectedFileContent: String): JsModelElement? {
        val (file, modelElement) = doCompletion(filePath, finishLookupName)
        val fileContent = file.text
        val cleanFunc = { it: String -> removeWhitespace(it).replace('\"', '\'') }
        val fileContentClean = cleanFunc(fileContent)
        expectedFileContent.forEach {
            assertTrue("expected '$it' not found in:\n$fileContent", fileContentClean.contains(cleanFunc(it)))
        }
        return modelElement
    }

    private fun doCompletion(testClassName: String, finishLookupName: String): Pair<PsiFile, JsModelElement?> {
        val psiFile = myFixture.configureByFile(testClassName)
        myFixture.complete(CompletionType.BASIC, 1)
        val lookupElements = myFixture.lookupElements?.asList()
        val lookupElementToSelect = lookupElements
                ?.filter { it.getUserData(TemplateHelper.SCOUT_LOOKUP_ELEMENT_MARKER) ?: false }
                ?.firstOrNull { it.lookupString == finishLookupName }
                ?: throw AssertionFailedError("No LookupElement with name '$finishLookupName' found in completion list. Available names: " + lookupElements?.map { it.lookupString })
        val modelElement = lookupElementToSelect.getUserData(JsModelCompletionHelper.SELECTED_ELEMENT)
        val lookup = myFixture.lookup as LookupImpl
        lookup.finishLookup('\t', lookupElementToSelect)
        return psiFile to modelElement
    }
}
