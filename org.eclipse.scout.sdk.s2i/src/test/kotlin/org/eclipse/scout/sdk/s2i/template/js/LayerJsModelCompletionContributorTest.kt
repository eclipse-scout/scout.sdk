/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.template.js

import org.eclipse.scout.sdk.s2i.model.js.JsModel

class LayerJsModelCompletionContributorTest : AbstractJsModelCompletionContributorTest() {

    override fun getDirectory(): String = LAYER_NAMESPACE

    override fun getDependencies(): List<Pair<String, String>> = listOf(SCOUT_WITHOUT_CLASS_REFERENCE_DIR to SCOUT_MODULE_NAME)

    override fun getNameCompletionWidgetExpectedFileContents(finishLookupName: String) =
        arrayOf(", $CHILD_PROPERTY_NAME: { ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'}")

    override fun getNameCompletionWidgetArrayExpectedFileContents(finishLookupName: String) =
        arrayOf(", $FIELDS_PROPERTY_NAME: [{ ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'}]")

    override fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(objectType: String, importName: String?, importModule: String?): Array<String?> =
        arrayOf(
            "$FIELDS_PROPERTY_NAME: [{${JsModel.ID_PROPERTY_NAME}: 'FirstInnerField',${JsModel.OBJECT_TYPE_PROPERTY_NAME}:'${JsModel.WIDGET_CLASS_NAME}'}," + // the existing widget in the array
                    "{${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}',${JsModel.OBJECT_TYPE_PROPERTY_NAME}:'$objectType'}]"  // the inserted StringField at the end of the array
        )

    override fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringFieldEx() = getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_EX_QUALIFIED_NAME)

    override fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringFieldExQualified() = getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_EX_QUALIFIED_NAME)

    override fun getValueCompletionWidgetExpectedFileContents(): Array<String?> =
        arrayOf("$ONLY_HERE_PROPERTY_NAME: { ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '$STRING_FIELD_NAME' }")

    override fun getValueCompletionObjectTypeExpectedFileContents(objectType: String, importName: String?, importModule: String?) = super.getValueCompletionObjectTypeExpectedFileContents(objectType, null, null)
}
