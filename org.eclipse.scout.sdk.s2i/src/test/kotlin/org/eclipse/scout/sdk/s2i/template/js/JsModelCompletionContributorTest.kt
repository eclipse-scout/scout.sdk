/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.scout.sdk.s2i.model.js.*
import org.eclipse.scout.sdk.s2i.template.TemplateHelper

class JsModelCompletionContributorTest : JavaCodeInsightFixtureTestCase() {

    companion object {
        const val MODELS_DIR = "src/models"
        const val UNKNOWN_OBJECT_FILE = "$MODELS_DIR/NameCompletionUnknownObject.js"
        const val NAME_COMPLETION_FILE = "$MODELS_DIR/NameCompletionModel.js"
        const val VALUE_COMPLETION_WIDGET_FILE = "$MODELS_DIR/ValueCompletionWidget.js"
        const val VALUE_COMPLETION_OBJECT_TYPE_FILE = "$MODELS_DIR/ValueCompletionObjectType.js"
        const val VALUE_COMPLETION_ENUM_FILE = "$MODELS_DIR/ValueCompletionEnum.js"
        const val VALUE_COMPLETION_WIDGET_ARRAY_FILE = "$MODELS_DIR/ValueCompletionWidgetArray.js"

        const val NAMESPACE = "scout" // as defined in the index.js
        const val STRING_FIELD_NAME = "StringField"
        const val FIELD_STYLE_NAME = "$STRING_FIELD_NAME.FieldStyle"
        const val WIDGET_STATE = "${JsModel.WIDGET_CLASS_NAME}.WidgetState"
        const val WIDGET_STATE_A = "$WIDGET_STATE.A"
        const val WIDGET_STATE_B = "$WIDGET_STATE.B"
        const val WIDGET_STATE_C = "$WIDGET_STATE.C"

        const val TEMPLATE_COMPLETION_CONTENT = "a"
        const val LABEL_PROPERTY_NAME = "label"
        const val STATE_PROPERTY_NAME = "state"
        const val CHILD_PROPERTY_NAME = "child"
        const val FIELD_STYLE_PROPERTY_NAME = "fieldStyle"
        const val SELECTED_TAB_PROPERTY_NAME = "selectedTab"
        const val FIELDS_PROPERTY_NAME = "fields"
        const val MAX_LENGTH_PROPERTY_NAME = "maxLength"
        const val VISIBLE_PROPERTY_NAME = "visible"
        const val NAME_PROPERTY_NAME = "name"
        const val ONLY_HERE_PROPERTY_NAME = "onlyHere"

        val UNKNOWN_DATA_TYPE = JsModelProperty.JsPropertyDataType.UNKNOWN.type
        val OBJECT_DATA_TYPE = JsModelProperty.JsPropertyDataType.OBJECT.type
        val BOOL_DATA_TYPE = JsModelProperty.JsPropertyDataType.BOOL.type
        val STRING_DATA_TYPE = JsModelProperty.JsPropertyDataType.STRING.type
        val WIDGET_DATA_TYPE = JsModelProperty.JsPropertyDataType.WIDGET.type
        val TEXT_KEY_DATA_TYPE = JsModelProperty.JsPropertyDataType.TEXT_KEY.type
        val NUMERIC_KEY_DATA_TYPE = JsModelProperty.JsPropertyDataType.NUMERIC.type
        val JS_MODEL_ENUM_SIMPLE_NAME: String = JsModelEnum::class.java.simpleName
        val JS_MODEL_CLASS_SIMPLE_NAME: String = JsModelClass::class.java.simpleName
    }

    override fun getTestDataPath() = "src/test/resources/template/js"

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject(".", ".")
    }

    fun testJsModel() {
        val model = JsModel().build(myFixture.module)
        assertEquals(4, model.elements().count())

        // validate model of WidgetState enum
        assertEquals("$JS_MODEL_ENUM_SIMPLE_NAME $WIDGET_STATE [A=$UNKNOWN_DATA_TYPE, B=$UNKNOWN_DATA_TYPE, C=$UNKNOWN_DATA_TYPE]", model.element(WIDGET_STATE).toString())

        // validate model of Widget class
        assertEquals("$JS_MODEL_CLASS_SIMPLE_NAME ${JsModel.WIDGET_CLASS_NAME} [${JsModel.ID_PROPERTY_NAME}=$OBJECT_DATA_TYPE," +
                " ${JsModel.OBJECT_TYPE_PROPERTY_NAME}=$OBJECT_DATA_TYPE," +
                " $VISIBLE_PROPERTY_NAME=$BOOL_DATA_TYPE," +
                " $NAME_PROPERTY_NAME=$STRING_DATA_TYPE," +
                " $FIELDS_PROPERTY_NAME=$WIDGET_DATA_TYPE[]," +
                " $CHILD_PROPERTY_NAME=$WIDGET_DATA_TYPE," +
                " $STATE_PROPERTY_NAME=$NAMESPACE.$WIDGET_STATE," +
                " $LABEL_PROPERTY_NAME=$TEXT_KEY_DATA_TYPE," +
                " $SELECTED_TAB_PROPERTY_NAME=$STRING_DATA_TYPE," +
                " $ONLY_HERE_PROPERTY_NAME=$WIDGET_DATA_TYPE]",
                model.element(JsModel.WIDGET_CLASS_NAME).toString())

        // validate model of StringField.FieldState enum
        assertEquals("$JS_MODEL_ENUM_SIMPLE_NAME $FIELD_STYLE_NAME [CLASSIC=$UNKNOWN_DATA_TYPE, ALTERNATIVE=$UNKNOWN_DATA_TYPE]", model.element(FIELD_STYLE_NAME).toString())

        // validate model of StringField class. this includes the test that the fieldStyle property is recognized as enum
        val stringField = model.element(STRING_FIELD_NAME)
        assertEquals("$JS_MODEL_CLASS_SIMPLE_NAME $STRING_FIELD_NAME [$MAX_LENGTH_PROPERTY_NAME=$NUMERIC_KEY_DATA_TYPE," +
                " $FIELD_STYLE_PROPERTY_NAME=$NAMESPACE.$FIELD_STYLE_NAME]", stringField.toString())

        // test that StringField is recognized as Widget
        assertTrue(model.isWidget(stringField))

        // validate valuesForProperty results
        assertEquals(setOf(JsModel.WIDGET_CLASS_NAME, STRING_FIELD_NAME), model.valuesForProperty(model.property(JsModel.WIDGET_CLASS_NAME, CHILD_PROPERTY_NAME)!!).map { it.displayText }.toSet())
        assertEquals(setOf(true.toString(), false.toString()), model.valuesForProperty(model.property(JsModel.WIDGET_CLASS_NAME, VISIBLE_PROPERTY_NAME)!!).map { it.displayText }.toSet())
        assertEquals(setOf(WIDGET_STATE_A, WIDGET_STATE_B, WIDGET_STATE_C), model.valuesForProperty(model.property(JsModel.WIDGET_CLASS_NAME, STATE_PROPERTY_NAME)!!).map { it.displayText }.toSet())
        assertEquals(setOf(JsModel.WIDGET_CLASS_NAME, STRING_FIELD_NAME), model.valuesForProperty(model.property(JsModel.WIDGET_CLASS_NAME, JsModel.OBJECT_TYPE_PROPERTY_NAME)!!).map { it.displayText }.toSet())

        // test that StringField inherits properties from Widget
        assertEquals(setOf(JsModel.ID_PROPERTY_NAME, JsModel.OBJECT_TYPE_PROPERTY_NAME, VISIBLE_PROPERTY_NAME, NAME_PROPERTY_NAME, FIELDS_PROPERTY_NAME, CHILD_PROPERTY_NAME,
                STATE_PROPERTY_NAME, LABEL_PROPERTY_NAME, ONLY_HERE_PROPERTY_NAME, MAX_LENGTH_PROPERTY_NAME, FIELD_STYLE_PROPERTY_NAME, SELECTED_TAB_PROPERTY_NAME), model.properties(STRING_FIELD_NAME).keys)

        // tests that the datatype is recognized if a reference to a variable is used
        assertEquals(JsModelProperty.JsPropertyDataType.NUMERIC, model.property(STRING_FIELD_NAME, MAX_LENGTH_PROPERTY_NAME)?.dataType)
    }

    fun testNameCompletionWidget() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, CHILD_PROPERTY_NAME,
                "$CHILD_PROPERTY_NAME: { ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'}")
        assertEquals(JsModelProperty.JsPropertyDataType.WIDGET, (selectedElement as? JsModelProperty)?.dataType)
    }

    fun testNameCompletionWidgetArray() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, FIELDS_PROPERTY_NAME,
                "$FIELDS_PROPERTY_NAME: [{ ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'}]")
        val prop = selectedElement as JsModelProperty
        assertEquals(JsModelProperty.JsPropertyDataType.WIDGET, prop.dataType)
        assertTrue(prop.isArray)
    }

    fun testNameCompletionAdditionalWidgetInExistingArray() {
        doCompleteAssertContent(VALUE_COMPLETION_WIDGET_ARRAY_FILE, STRING_FIELD_NAME,
                "$FIELDS_PROPERTY_NAME: [{${JsModel.ID_PROPERTY_NAME}: 'FirstInnerField',${JsModel.OBJECT_TYPE_PROPERTY_NAME}:'${JsModel.WIDGET_CLASS_NAME}'}," + // the existing widget in the array
                        "{${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}',${JsModel.OBJECT_TYPE_PROPERTY_NAME}:'$STRING_FIELD_NAME'}]") // the inserted StringField at the end of the array
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
        doCompleteAssertContent(VALUE_COMPLETION_WIDGET_FILE, STRING_FIELD_NAME,
                "$ONLY_HERE_PROPERTY_NAME: { ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$STRING_FIELD_NAME' }")
    }

    fun testValueCompletionObjectType() {
        doCompleteAssertContent(VALUE_COMPLETION_OBJECT_TYPE_FILE, JsModel.WIDGET_CLASS_NAME,
                "${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '${JsModel.WIDGET_CLASS_NAME}' ")
    }

    fun testValueCompletionEnum() {
        doCompleteAssertContent(VALUE_COMPLETION_ENUM_FILE, WIDGET_STATE_B,
                "$STATE_PROPERTY_NAME: $WIDGET_STATE_B ",
                "import {${JsModel.WIDGET_CLASS_NAME}} from '../index';")
    }

    private fun doCompleteAssertContent(filePath: String, finishLookupName: String, vararg expectedFileContent: String): AbstractJsModelElement? {
        val (file, modelElement) = doCompletion(filePath, finishLookupName)
        val fileContent = file.text
        val cleanFunc = { it: String -> removeWhitespace(it).replace('\"', '\'') }
        val fileContentClean = cleanFunc(fileContent)
        expectedFileContent.forEach {
            assertTrue("expected '$it' not found in:\n$fileContent", fileContentClean.contains(cleanFunc(it)))
        }
        return modelElement
    }

    private fun doCompletion(testClassName: String, finishLookupName: String): Pair<PsiFile, AbstractJsModelElement?> {
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
