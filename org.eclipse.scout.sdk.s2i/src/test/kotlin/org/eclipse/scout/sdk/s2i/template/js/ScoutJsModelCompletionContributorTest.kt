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

import org.eclipse.scout.sdk.s2i.model.js.JsModel

class ScoutJsModelCompletionContributorTest : AbstractJsModelCompletionContributorTest() {

    override fun getDirectory(): String = SCOUT_NAMESPACE

    override fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringField() = getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_NAME, STRING_FIELD_NAME, "../index")

    override fun getValueCompletionWidgetExpectedFileContents() =
        arrayOf(
            "$ONLY_HERE_PROPERTY_NAME: { ${JsModel.ID_PROPERTY_NAME}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${JsModel.OBJECT_TYPE_PROPERTY_NAME}: $STRING_FIELD_NAME }",
            getImportFileContent(STRING_FIELD_NAME, "../index")
        )

    override fun getValueCompletionObjectTypeExpectedFileContents_Widget() = getValueCompletionObjectTypeExpectedFileContents(JsModel.WIDGET_CLASS_NAME, JsModel.WIDGET_CLASS_NAME, "../index")

    override fun getValueCompletionEnumExpectedFileContents() = arrayOf("$STATE_PROPERTY_NAME: $WIDGET_STATE_B ", getImportFileContent(JsModel.WIDGET_CLASS_NAME, "../index"))
}
