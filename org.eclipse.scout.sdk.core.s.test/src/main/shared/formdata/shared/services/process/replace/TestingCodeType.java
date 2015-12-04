/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.services.process.replace;

import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class TestingCodeType extends AbstractCodeType<Long, Long> {

  private static final long serialVersionUID = 1L;

  @Override
  public Long getId() {
    return 42L;
  }
}
