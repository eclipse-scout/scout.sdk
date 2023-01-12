/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm;

@SuppressWarnings("ALL")
public class TestFormExtension extends AbstractFormExtension<ScoutInfoForm> {
  public TestFormExtension(ScoutInfoForm ownerForm) {
    super(ownerForm);
  }

  <caret>
}
