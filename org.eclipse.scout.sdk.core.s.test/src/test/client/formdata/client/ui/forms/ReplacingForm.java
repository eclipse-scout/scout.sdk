/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
