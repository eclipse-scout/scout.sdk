/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.forms;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.platform.Replace;

import formdata.shared.ui.forms.ReplacingFormData;

/**
 * <h3>{@link ReplacingForm}</h3>
 */
@Replace
@FormData(value = ReplacingFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ReplacingForm extends AnnotationCopyTestForm {
}
