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
package org.eclipse.scout.sdk.operation.template.sequencebox;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class DateTimeFromToTemplate extends DateFromToTemplate {

  @Override
  public String getName() {
    return "Date-Time FROM-TO";
  }

  @Override
  public void apply(IType type, IWorkingCopyManager manager, IProgressMonitor monitor) throws CoreException {
    super.apply(type, manager, monitor);
    MethodOverrideOperation fromHasTimeOp = new MethodOverrideOperation(getFromType(), "getConfiguredHasTime");
    fromHasTimeOp.setSimpleBody("return true;");
    fromHasTimeOp.validate();
    fromHasTimeOp.run(monitor, manager);

    MethodOverrideOperation toHasTimeOp = new MethodOverrideOperation(getToType(), "getConfiguredHasTime");
    toHasTimeOp.setSimpleBody("return true;");
    toHasTimeOp.validate();
    toHasTimeOp.run(monitor, manager);
  }

}
