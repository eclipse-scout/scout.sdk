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
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class IntegerFromToTemplate extends AbstractFormFieldTemplate {

  @Override
  public String getName() {
    return "Integer FROM-TO";
  }

  @Override
  public void apply(ITypeSourceBuilder sourceBuilder, IType declaringType, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    apply(sourceBuilder, declaringType, RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IIntegerField, declaringType.getJavaProject()), monitor, manager);
  }
}
