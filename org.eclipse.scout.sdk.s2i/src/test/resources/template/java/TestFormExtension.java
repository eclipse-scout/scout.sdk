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
import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm;

@SuppressWarnings("ALL")
public class TestFormExtension extends AbstractFormExtension<ScoutInfoForm> {
  public TestFormExtension(ScoutInfoForm ownerForm) {
    super(ownerForm);
  }

  <caret>
}
