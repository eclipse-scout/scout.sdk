/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.texts;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.text.AbstractDynamicNlsTextProviderService;

/**
 * <h3>{@link TestTextProviderService}</h3>
 *
 * @since 7.0.0
 */
@Order(11.2)
public class TestTextProviderService extends AbstractDynamicNlsTextProviderService {

  @Override
  public String getDynamicNlsBaseName() {
    return "formdata.shared.texts.Prefix";
  }
}
