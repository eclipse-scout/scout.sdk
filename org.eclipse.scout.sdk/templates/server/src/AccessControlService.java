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
package @@BUNDLE_SERVER_NAME@@.services.custom.security;

import java.security.AllPermission;
import java.security.Permissions;

import org.eclipse.scout.rt.server.services.common.security.AbstractAccessControlService;

public class AccessControlService extends AbstractAccessControlService {

  @Override
  protected Permissions execLoadPermissions() {
    Permissions permissions = new Permissions();
    //TODO @@USER_NAME@@ fill access control service
    permissions.add(new AllPermission());
    return permissions;
  }

}
