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

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel

class LayerJsModelCompletionContributorTest : AbstractJsModelCompletionContributorTest() {

    override fun getDirectory(): String = LAYER_NAMESPACE

    override fun getDependencies(): List<Pair<String, String>> = listOf(SCOUT_WITHOUT_CLASS_REFERENCE_DIR to SCOUT_MODULE_NAME)

    override fun getNameCompletionWidgetExpectedFileContents(finishLookupName: String) =
        arrayOf(", $CHILD_PROPERTY_NAME: { ${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'}")

    override fun getNameCompletionWidgetArrayExpectedFileContents(finishLookupName: String) =
        // FIXME model: add array support
        arrayOf(", $FIELDS_PROPERTY_NAME: { ${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'}")
//        arrayOf(", $FIELDS_PROPERTY_NAME: [{ ${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: '$TEMPLATE_COMPLETION_CONTENT'}]")

    override fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(objectType: String, importName: String?, importModule: String?): Array<String?> =
        arrayOf(
            "$FIELDS_PROPERTY_NAME: [{${ScoutJsModel.ID_PROPERTY_NAME}: 'FirstInnerField',${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}:'${ScoutJsModel.WIDGET_CLASS_NAME}'}," + // the existing widget in the array
                    "{${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}',${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}:'$objectType'}]"  // the inserted StringField at the end of the array
        )

    override fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringFieldEx() = getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_EX_QUALIFIED_NAME)

    override fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringFieldExQualified() = getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_EX_QUALIFIED_NAME)

    override fun getValueCompletionWidgetExpectedFileContents(): Array<String?> =
        arrayOf("$ONLY_HERE_PROPERTY_NAME: { ${ScoutJsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME}: '$STRING_FIELD_NAME' }")

    override fun getValueCompletionObjectTypeExpectedFileContents(objectType: String, importName: String?, importModule: String?) = super.getValueCompletionObjectTypeExpectedFileContents(objectType, null, null)
}
