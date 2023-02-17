/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.js

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import junit.framework.AssertionFailedError
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsPropertySubType
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils.removeWhitespace
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.s2i.model.js.JsModel
import org.eclipse.scout.sdk.s2i.template.TemplateHelper

abstract class AbstractJsModelCompletionContributorTest : JavaCodeInsightFixtureTestCase() {

    companion object {
        const val NODE_MODULES_DIR = "node_modules"

        const val MODELS_DIR = "src/models"
        const val NAME_COMPLETION_FILE = "$MODELS_DIR/NameCompletion.js"
        const val NAME_COMPLETION_UNKNOWN_OBJECT_FILE = "$MODELS_DIR/NameCompletionUnknownObject.js"
        const val VALUE_COMPLETION_WIDGET_FILE = "$MODELS_DIR/ValueCompletionWidget.js"
        const val VALUE_COMPLETION_OBJECT_TYPE_FILE = "$MODELS_DIR/ValueCompletionObjectType.js"
        const val VALUE_COMPLETION_OBJECT_TYPE_STRING_LITERAL_FILE = "$MODELS_DIR/ValueCompletionObjectTypeStringLiteral.js"
        const val VALUE_COMPLETION_ENUM_FILE = "$MODELS_DIR/ValueCompletionEnum.js"
        const val VALUE_COMPLETION_WIDGET_ARRAY_FILE = "$MODELS_DIR/ValueCompletionWidgetArray.js"

        const val SCOUT_NAMESPACE = "scout"
        const val LAYER_NAMESPACE = "layer"
        const val PROJECT_NAMESPACE = "project"
        const val CONTRIBUTION_NAMESPACE = "contribution"

        const val SCOUT_MODULE_NAME = "@eclipse-scout/core"
        const val LAYER_MODULE_NAME = "@eclipse-scout/layer"

        const val SCOUT_WITHOUT_CLASS_REFERENCE_DIR = "scout_without_class_reference"

        const val WIDGET_STATE = "${ScoutJsModel.WIDGET_CLASS_NAME}.WidgetState"
        const val WIDGET_STATE_A = "$WIDGET_STATE.A"
        const val WIDGET_STATE_B = "$WIDGET_STATE.B"
        const val WIDGET_STATE_C = "$WIDGET_STATE.C"

        const val STRING_FIELD_NAME = "StringField"
        const val FIELD_STYLE_NAME = "$STRING_FIELD_NAME.FieldStyle"

        const val STRING_FIELD_EX_NAME = "StringFieldEx"
        const val STRING_FIELD_EX_QUALIFIED_NAME = "$LAYER_NAMESPACE.$STRING_FIELD_EX_NAME"

        const val TEMPLATE_COMPLETION_CONTENT = "a"
        const val LABEL_PROPERTY_NAME = "label"
        const val STATE_PROPERTY_NAME = "state"
        const val CHILD_PROPERTY_NAME = "child"
        const val FIELD_STYLE_PROPERTY_NAME = "fieldStyle"
        const val SELECTED_TAB_PROPERTY_NAME = "selectedTab"
        const val FIELDS_PROPERTY_NAME = "fields"
        const val MAX_LENGTH_PROPERTY_NAME = "maxLength"
        const val MIN_LENGTH_PROPERTY_NAME = "minLength"
        const val VISIBLE_PROPERTY_NAME = "visible"
        const val NAME_PROPERTY_NAME = "name"
        const val ONLY_HERE_PROPERTY_NAME = "onlyHere"
    }

    override fun getTestDataPath() = "src/test/resources/template/js"

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("./${getDirectory()}", ".")
        getDependencies().forEach { myFixture.copyDirectoryToProject("./${it.first}", "./$NODE_MODULES_DIR/${it.second}") }
        myFixture.copyDirectoryToProject("./models", "./$MODELS_DIR")
    }

    protected abstract fun getDirectory(): String

    protected open fun getNamespace() = getDirectory()

    protected open fun getDependencies(): List<Pair<String /* dir name */, String /* module name */>> = listOf()

    protected fun hasLayerModule() = getNamespace() == LAYER_NAMESPACE || getDependencies().map { it.second }.any { LAYER_MODULE_NAME == it }

    fun testJsModel() {
        val scoutJsModel = scoutJsModel() ?: throw newFail("ScoutJsModel for module {} not found.", myFixture.module)
        testJsModel(scoutJsModel)
    }

    protected open fun scoutJsModel() = JsModel.getOrCreateModule(myFixture.module)

    protected open fun testJsModel(scoutJsModel: ScoutJsModel) {
        // FIXME model: add enum support
        assertEquals(if (hasLayerModule()) 3 else 2, scoutJsModel.scoutObjects().count())
//        assertEquals(if (hasLayerModule()) 5 else 4, scoutJsModel.scoutObjects().count())

        val scoutWidgetClass = scoutJsModel.scoutWidgetClass().orElse(null)
        assertNotNull(scoutWidgetClass)

        // validate model of WidgetState enum
        // FIXME model: add enum support
//        assertEquals("$JS_MODEL_ENUM_SIMPLE_NAME $WIDGET_STATE [A=$UNKNOWN_DATA_TYPE, B=$UNKNOWN_DATA_TYPE, C=$UNKNOWN_DATA_TYPE]", scoutJsModel.scoutObject(WIDGET_STATE).toString())

        // validate model of Widget class
        val widget = scoutJsModel.scoutObject(ScoutJsModel.WIDGET_CLASS_NAME)
        assertNotNull(widget)
        // FIXME model: add enum support, remove excludedProperties
//        assertEquals(10, widget.properties().count())
        val idProperty = widget.property(ScoutJsModel.ID_PROPERTY_NAME)
        assertNotNull(idProperty)
        // FIXME model: parser does not detect a dataType here (maybe use "any")
//        assertEquals(TypeScriptTypes._object, idProperty.type.toString())
        val objectTypeProperty = widget.property(ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME)
        assertNotNull(objectTypeProperty)
        // FIXME model: parser does not detect a dataType here (maybe use "any")
//        assertEquals(TypeScriptTypes._object, objectTypeProperty.type.toString())
        val visibleProperty = widget.property(VISIBLE_PROPERTY_NAME)
        assertNotNull(visibleProperty)
        assertEquals(TypeScriptTypes._boolean, visibleProperty.type.toString())
        val nameProperty = widget.property(NAME_PROPERTY_NAME)
        assertNotNull(nameProperty)
        assertEquals(TypeScriptTypes._string, nameProperty.type.toString())
        val fieldsProperty = widget.property(FIELDS_PROPERTY_NAME)
        assertNotNull(fieldsProperty)
        assertTrue(fieldsProperty.isWidget(scoutJsModel))
        // FIXME model: add array support
//        assertTrue(fieldsProperty.isArray)
        val childProperty = widget.property(CHILD_PROPERTY_NAME)
        assertNotNull(childProperty)
        assertTrue(childProperty.isWidget(scoutJsModel))
        val stateProperty = widget.property(STATE_PROPERTY_NAME)
        assertNotNull(stateProperty)
        // FIXME model: add enum support
//        assertEquals("$SCOUT_NAMESPACE.$WIDGET_STATE", stateProperty.type.toString())
        val labelProperty = widget.property(LABEL_PROPERTY_NAME)
        assertNotNull(labelProperty)
        assertEquals("${TypeScriptTypes._string} (sub-type=${ScoutJsPropertySubType.TEXT_KEY})", labelProperty.type.toString())
        val selectedTabProperty = widget.property(SELECTED_TAB_PROPERTY_NAME)
        assertNotNull(selectedTabProperty)
        assertEquals(TypeScriptTypes._string, selectedTabProperty.type.toString())
        val onlyHereProperty = widget.property(ONLY_HERE_PROPERTY_NAME)
        assertNotNull(onlyHereProperty)
        assertEquals(ScoutJsModel.WIDGET_CLASS_NAME, onlyHereProperty.type.toString())

        // test that Widget is recognized as Widget
        assertTrue(widget.declaringClass().isInstanceOf(scoutWidgetClass))

        // validate model of StringField.FieldState enum
        // FIXME model: add enum support
//        assertEquals("$JS_MODEL_ENUM_SIMPLE_NAME $FIELD_STYLE_NAME [CLASSIC=$UNKNOWN_DATA_TYPE, ALTERNATIVE=$UNKNOWN_DATA_TYPE]", scoutJsModel.scoutObject(FIELD_STYLE_NAME).toString())

        // validate model of StringField class. this includes the test that the fieldStyle property is recognized as enum
        val stringField = scoutJsModel.scoutObject(STRING_FIELD_NAME)
        assertNotNull(stringField)
        val maxLengthProperty = stringField.property(MAX_LENGTH_PROPERTY_NAME)
        assertNotNull(maxLengthProperty)
        // tests that the datatype is recognized if a reference to a variable is used
        assertEquals(TypeScriptTypes._number, maxLengthProperty.type.toString())
        val fieldStyleProperty = stringField.property(FIELD_STYLE_PROPERTY_NAME)
        assertNotNull(fieldStyleProperty)
        // FIXME model: add enum support
//        assertEquals("$SCOUT_NAMESPACE.$FIELD_STYLE_NAME", fieldStyleProperty.type.toString())

        // test that StringField is recognized as Widget
        assertTrue(stringField.declaringClass().isInstanceOf(scoutWidgetClass))

        // validate valuesForProperty results
        assertEquals(
            setOfNotNull(ScoutJsModel.WIDGET_CLASS_NAME, STRING_FIELD_NAME, if (hasLayerModule()) STRING_FIELD_EX_QUALIFIED_NAME else null),
            childProperty.values(scoutJsModel).map { it.name() }.toSet()
        )
        assertEquals(setOf(true.toString(), false.toString()), visibleProperty.values(scoutJsModel).map { it.name() }.toSet())
        // FIXME model: add enum support
//        assertEquals(setOf(WIDGET_STATE_A, WIDGET_STATE_B, WIDGET_STATE_C), stateProperty.values(scoutJsModel).map { it.name() }.toSet())
        assertEquals(
            setOfNotNull(ScoutJsModel.WIDGET_CLASS_NAME, STRING_FIELD_NAME, if (hasLayerModule()) STRING_FIELD_EX_QUALIFIED_NAME else null),
            objectTypeProperty.values(scoutJsModel).map { it.name() }.toSet()
        )

        // test that StringField inherits properties from Widget
        assertEquals(
            setOf(
                ScoutJsModel.ID_PROPERTY_NAME, ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME, VISIBLE_PROPERTY_NAME, NAME_PROPERTY_NAME, FIELDS_PROPERTY_NAME, CHILD_PROPERTY_NAME,
                STATE_PROPERTY_NAME, LABEL_PROPERTY_NAME, ONLY_HERE_PROPERTY_NAME, MAX_LENGTH_PROPERTY_NAME, FIELD_STYLE_PROPERTY_NAME, SELECTED_TAB_PROPERTY_NAME
                // FIXME model: add enum support
                , "WidgetState", "FieldStyle"
                // FIXME model: remove excludedProperties
                , "children", "enabledComputed"
            ), stringField.properties().keys
        )

        if (!hasLayerModule()) {
            return
        }

        // validate model of StringFieldEx class
        val stringFieldEx = scoutJsModel.scoutObject(STRING_FIELD_EX_QUALIFIED_NAME)
        assertNotNull(stringFieldEx)
        val minLengthProperty = stringFieldEx.property(MIN_LENGTH_PROPERTY_NAME)
        assertNotNull(minLengthProperty)
        // tests that the datatype is recognized if a reference to a variable is used
        assertEquals(TypeScriptTypes._number, minLengthProperty.type.toString())

        // test that StringFieldEx is recognized as Widget
        assertTrue(stringFieldEx.declaringClass().isInstanceOf(scoutWidgetClass))

        // test that StringFieldEx inherits properties from StringField, Widget
        assertEquals(
            setOf(
                ScoutJsModel.ID_PROPERTY_NAME, ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME, VISIBLE_PROPERTY_NAME, NAME_PROPERTY_NAME, FIELDS_PROPERTY_NAME, CHILD_PROPERTY_NAME,
                STATE_PROPERTY_NAME, LABEL_PROPERTY_NAME, ONLY_HERE_PROPERTY_NAME, MAX_LENGTH_PROPERTY_NAME, MIN_LENGTH_PROPERTY_NAME, FIELD_STYLE_PROPERTY_NAME, SELECTED_TAB_PROPERTY_NAME
                // FIXME model: add enum support
                , "WidgetState", "FieldStyle"
                // FIXME model: remove excludedProperties
                , "children", "enabledComputed"
            ), scoutJsModel.scoutObject(STRING_FIELD_EX_QUALIFIED_NAME).properties().keys
        )
    }

    fun testNameCompletionWidget() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, CHILD_PROPERTY_NAME, *getNameCompletionWidgetExpectedFileContents(CHILD_PROPERTY_NAME))
        assertTrue((selectedElement as? JsModelCompletionHelper.ScoutJsPropertyLookupElement)?.isWidget(scoutJsModel()) ?: false)
    }

    protected open fun getNameCompletionWidgetExpectedFileContents(finishLookupName: String) =
        arrayOf(", $CHILD_PROPERTY_NAME: { ${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: $TEMPLATE_COMPLETION_CONTENT}")

    fun testNameCompletionWidgetArray() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, FIELDS_PROPERTY_NAME, *getNameCompletionWidgetArrayExpectedFileContents(FIELDS_PROPERTY_NAME))
        val prop = selectedElement as JsModelCompletionHelper.ScoutJsPropertyLookupElement
        assertTrue(prop.isWidget(scoutJsModel()))
        // FIXME model: add array support
//        assertTrue(prop.isArray())
    }

    protected open fun getNameCompletionWidgetArrayExpectedFileContents(finishLookupName: String) =
        // FIXME model: add array support
        arrayOf(", $FIELDS_PROPERTY_NAME: { ${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: $TEMPLATE_COMPLETION_CONTENT}")
//        arrayOf(", $FIELDS_PROPERTY_NAME: [{ ${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: $TEMPLATE_COMPLETION_CONTENT}]")

    fun testNameCompletionAdditionalWidgetInExistingArray_StringField() {
        doCompleteAssertContent(VALUE_COMPLETION_WIDGET_ARRAY_FILE, STRING_FIELD_NAME, *getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringField())
    }

    fun testNameCompletionAdditionalWidgetInExistingArray_StringFieldEx() {
        if (!hasLayerModule()) {
            return
        }
        doCompleteAssertContent(VALUE_COMPLETION_WIDGET_ARRAY_FILE, STRING_FIELD_EX_NAME, *getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringFieldEx())
    }

    fun testNameCompletionAdditionalWidgetInExistingArray_StringFieldExQualified() {
        if (!hasLayerModule()) {
            return
        }
        doCompleteAssertContent(VALUE_COMPLETION_WIDGET_ARRAY_FILE, STRING_FIELD_EX_QUALIFIED_NAME, *getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringFieldExQualified())
    }

    protected fun getImportFileContent(importName: String?, importModule: String?): String? {
        importName ?: return null
        importModule ?: return null
        return "import {$importName} from '$importModule';"
    }

    protected open fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(objectType: String, importName: String? = null, importModule: String? = null) =
        arrayOf(
            "$FIELDS_PROPERTY_NAME: [{${ScoutJsModel.ID_PROPERTY_NAME}: 'FirstInnerField',${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}:'${ScoutJsModel.WIDGET_CLASS_NAME}'}," + // the existing widget in the array
                    "{${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}',${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}:$objectType}]",  // the inserted StringField at the end of the array
            getImportFileContent(importName, importModule)
        )

    protected open fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringField() = getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_NAME, STRING_FIELD_NAME, SCOUT_MODULE_NAME)

    protected open fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringFieldEx() =
        getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_EX_NAME, STRING_FIELD_EX_NAME, LAYER_MODULE_NAME)

    protected open fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringFieldExQualified() =
        getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_EX_NAME, STRING_FIELD_EX_NAME, LAYER_MODULE_NAME)

    fun testNameCompletionNumeric() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, MAX_LENGTH_PROPERTY_NAME, *getNameCompletionNumericExpectedFileContents())
        val type = (selectedElement as? JsModelCompletionHelper.ScoutJsPropertyLookupElement)?.scoutJsProperty?.type
        assertNotNull(type)
        assertEquals(TypeScriptTypes._number, type!!.dataType().map { it.name() }.orElse(null))
    }

    protected open fun getNameCompletionNumericExpectedFileContents() = arrayOf(", $MAX_LENGTH_PROPERTY_NAME: ")

    fun testNameCompletionTextKey() {
        val selectedElement = doCompleteAssertContent(NAME_COMPLETION_FILE, LABEL_PROPERTY_NAME, *getNameCompletionTextKeyExpectedFileContents())
        val type = (selectedElement as? JsModelCompletionHelper.ScoutJsPropertyLookupElement)?.scoutJsProperty?.type
        assertNotNull(type)
        assertEquals(TypeScriptTypes._string, type!!.dataType().map { it.name() }.orElse(null))
        assertEquals(ScoutJsPropertySubType.TEXT_KEY, type.subType())
    }

    protected open fun getNameCompletionTextKeyExpectedFileContents() =
        arrayOf(", $LABEL_PROPERTY_NAME: '${TranslationPatterns.JsModelTextKeyPattern.MODEL_TEXT_KEY_PREFIX}$TEMPLATE_COMPLETION_CONTENT${TranslationPatterns.JsModelTextKeyPattern.MODEL_TEXT_KEY_SUFFIX}'")

    fun testNameCompletionUnknownObject() {
        myFixture.configureByFile(NAME_COMPLETION_UNKNOWN_OBJECT_FILE)

        // on an empty object (no objectType) no completion should be done by Scout.
        // it must not be a Scout object! Ensure there are several options (defaults from IDE).
        assertTrue(computeLookupElements().size > 2)
    }

    fun testValueCompletionWidget() {
        doCompleteAssertContent(VALUE_COMPLETION_WIDGET_FILE, STRING_FIELD_NAME, *getValueCompletionWidgetExpectedFileContents())
    }

    protected open fun getValueCompletionWidgetExpectedFileContents() =
        arrayOf(
            "$ONLY_HERE_PROPERTY_NAME: { ${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: $STRING_FIELD_NAME }",
            getImportFileContent(STRING_FIELD_NAME, SCOUT_MODULE_NAME)
        )

    fun testValueCompletionObjectType_Widget() {
        doCompleteAssertContent(VALUE_COMPLETION_OBJECT_TYPE_FILE, ScoutJsModel.WIDGET_CLASS_NAME, *getValueCompletionObjectTypeExpectedFileContents_Widget())
    }

    fun testValueCompletionObjectType_StringFieldEx() {
        if (!hasLayerModule()) {
            return
        }
        doCompleteAssertContent(VALUE_COMPLETION_OBJECT_TYPE_FILE, STRING_FIELD_EX_NAME, *getValueCompletionObjectTypeExpectedFileContents_StringFieldEx())
    }

    protected open fun getValueCompletionObjectTypeExpectedFileContents(objectType: String, importName: String? = null, importModule: String? = null) =
        arrayOf("${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: $objectType ", getImportFileContent(importName, importModule))

    protected open fun getValueCompletionObjectTypeExpectedFileContents_Widget() = getValueCompletionObjectTypeExpectedFileContents(ScoutJsModel.WIDGET_CLASS_NAME, ScoutJsModel.WIDGET_CLASS_NAME, SCOUT_MODULE_NAME)

    protected open fun getValueCompletionObjectTypeExpectedFileContents_StringFieldEx() = getValueCompletionObjectTypeExpectedFileContents(STRING_FIELD_EX_NAME, STRING_FIELD_EX_NAME, LAYER_MODULE_NAME)

    fun testValueCompletionObjectTypeStringLiteral_Widget() {
        doCompleteAssertContent(VALUE_COMPLETION_OBJECT_TYPE_STRING_LITERAL_FILE, ScoutJsModel.WIDGET_CLASS_NAME, *getValueCompletionObjectTypeStringLiteralExpectedFileContents_Widget())
    }

    fun testValueCompletionObjectTypeStringLiteral_StringFieldEx() {
        if (!hasLayerModule()) {
            return
        }
        doCompleteAssertContent(VALUE_COMPLETION_OBJECT_TYPE_STRING_LITERAL_FILE, STRING_FIELD_EX_NAME, *getValueCompletionObjectTypeStringLiteralExpectedFileContents_StringFieldEx())
    }

    fun testValueCompletionObjectTypeStringLiteral_StringFieldExQualifiedName() {
        if (!hasLayerModule()) {
            return
        }
        doCompleteAssertContent(VALUE_COMPLETION_OBJECT_TYPE_STRING_LITERAL_FILE, STRING_FIELD_EX_QUALIFIED_NAME, *getValueCompletionObjectTypeStringLiteralExpectedFileContents_StringFieldExQualifiedName())
    }

    protected open fun getValueCompletionObjectTypeStringLiteralExpectedFileContents(objectType: String) = arrayOf("${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: '$objectType' ")

    protected open fun getValueCompletionObjectTypeStringLiteralExpectedFileContents_Widget() = getValueCompletionObjectTypeStringLiteralExpectedFileContents(ScoutJsModel.WIDGET_CLASS_NAME)

    protected open fun getValueCompletionObjectTypeStringLiteralExpectedFileContents_StringFieldEx() = getValueCompletionObjectTypeStringLiteralExpectedFileContents(STRING_FIELD_EX_QUALIFIED_NAME)

    protected open fun getValueCompletionObjectTypeStringLiteralExpectedFileContents_StringFieldExQualifiedName() = getValueCompletionObjectTypeStringLiteralExpectedFileContents(STRING_FIELD_EX_QUALIFIED_NAME)

    fun testValueCompletionEnum() {
        // FIXME model: add enum support
//        doCompleteAssertContent(VALUE_COMPLETION_ENUM_FILE, WIDGET_STATE_B, *getValueCompletionEnumExpectedFileContents())
    }

    protected open fun getValueCompletionEnumExpectedFileContents() = arrayOf("$STATE_PROPERTY_NAME: $WIDGET_STATE_B ", getImportFileContent(ScoutJsModel.WIDGET_CLASS_NAME, SCOUT_MODULE_NAME))

    private fun doCompleteAssertContent(filePath: String, finishLookupName: String, vararg expectedFileContent: String?): JsModelCompletionHelper.ScoutJsModelLookupElement? {
        val (file, modelElement) = doCompletion(filePath, finishLookupName)
        val fileContent = file.text
        val cleanFunc = { it: String -> removeWhitespace(it).replace('\"', '\'') }
        val fileContentClean = cleanFunc(fileContent)
        expectedFileContent
            .filterNotNull()
            .forEach {
                assertTrue("expected '$it' not found in:\n$fileContent", fileContentClean.contains(cleanFunc(it)))
            }
        return modelElement
    }

    private fun doCompletion(testClassName: String, finishLookupName: String): Pair<PsiFile, JsModelCompletionHelper.ScoutJsModelLookupElement?> {
        val psiFile = myFixture.configureByFile(testClassName)
        val lookupElements = computeLookupElements()
        val lookupElementToSelect = lookupElements
            .filter { it.getUserData(TemplateHelper.SCOUT_LOOKUP_ELEMENT_MARKER) ?: false }
            .firstOrNull { it.allLookupStrings.contains(finishLookupName) }
            ?: throw AssertionFailedError("No LookupElement with name '$finishLookupName' found in completion list. Available names: " + lookupElements.map { it.lookupString })
        val modelElement = lookupElementToSelect.getUserData(JsModelCompletionHelper.SELECTED_ELEMENT)
        val lookup = myFixture.lookup as LookupImpl
        lookup.finishLookup('\t', lookupElementToSelect)
        return psiFile to modelElement
    }

    private fun computeLookupElements(): List<LookupElement> {
        myFixture.complete(CompletionType.BASIC, 1)
        return myFixture.lookupElements?.asList() ?: emptyList()
    }
}
