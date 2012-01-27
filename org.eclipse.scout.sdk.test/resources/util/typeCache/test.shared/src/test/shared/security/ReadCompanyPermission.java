/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package test.shared.security;

import java.security.BasicPermission;

public class ReadCompanyPermission extends BasicPermission {

  private static final long serialVersionUID = 0L;

  public ReadCompanyPermission() {
    super("ReadCompany");
  }
}
