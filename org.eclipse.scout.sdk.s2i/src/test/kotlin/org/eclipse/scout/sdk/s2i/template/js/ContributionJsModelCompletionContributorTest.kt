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

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants

class ContributionJsModelCompletionContributorTest : AbstractJsModelCompletionContributorTest() {

    override fun getDirectory(): String = CONTRIBUTION_NAMESPACE

    override fun getDependencies(): List<Pair<String, String>> = listOf(SCOUT_NAMESPACE to SCOUT_MODULE_NAME, LAYER_NAMESPACE to LAYER_MODULE_NAME)

    override fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringField() = getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_NAME)

    override fun getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents_StringFieldEx() = getNameCompletionAdditionalWidgetInExistingArrayExpectedFileContents(STRING_FIELD_EX_NAME)

    override fun getValueCompletionWidgetExpectedFileContents(): Array<String?> =
        arrayOf("$ONLY_HERE_PROPERTY_NAME: { ${ScoutJsCoreConstants.PROPERTY_NAME_ID}: '${JsModelCompletionHelper.ID_DEFAULT_TEXT}', ${ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE}: $STRING_FIELD_NAME }")

    override fun getValueCompletionObjectTypeExpectedFileContents_Widget() = getValueCompletionObjectTypeExpectedFileContents(ScoutJsCoreConstants.CLASS_NAME_WIDGET)

    override fun getValueCompletionObjectTypeExpectedFileContents_StringFieldEx() = getValueCompletionObjectTypeExpectedFileContents(STRING_FIELD_EX_NAME)

    override fun getValueCompletionEnumExpectedFileContents(): Array<String?> = arrayOf("$STATE_PROPERTY_NAME: ${ScoutJsCoreConstants.CLASS_NAME_WIDGET}.$WIDGET_STATE_NAME.$WIDGET_STATE_B ")
}
