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
package org.eclipse.scout.nls.sdk.services.model.ws.project;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.simple.model.ws.NlsType;
import org.eclipse.scout.nls.sdk.simple.model.ws.project.SimpleNlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 *
 */
public class ServiceNlsProject extends SimpleNlsProject {
  public ServiceNlsProject(NlsType t) {
    super(t);
  }

  @Override
  public void setParent(INlsProject newParent) {
    super.setParent(newParent);
  }

  @Override
  public IType getNlsAccessorType() {
    return TypeUtility.getType(IRuntimeClasses.TEXTS);
  }
}
