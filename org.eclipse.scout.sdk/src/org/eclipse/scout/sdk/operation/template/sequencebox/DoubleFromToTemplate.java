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
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.form.field.FormFieldNewOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.template.IContentTemplate;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public class DoubleFromToTemplate implements IContentTemplate {
  @Override
  public String getName() {
    return "Double FROM-TO";
  }

  @Override
  public void apply(IType type, IWorkingCopyManager manager, IProgressMonitor monitor) throws CoreException {
    monitor.beginTask("apply " + getName() + " template.", IProgressMonitor.UNKNOWN);

    String superTypeSignature = RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IDoubleField, type.getJavaProject());

    String parentName = type.getElementName();
    int lastBoxIndex = parentName.lastIndexOf(SdkProperties.SUFFIX_BOX);
    if (lastBoxIndex > 0) {
      parentName = parentName.substring(0, lastBoxIndex);
    }
    FormFieldNewOperation fromOp = new FormFieldNewOperation(type, false);
    fromOp.setTypeName(parentName + SdkProperties.SUFFIX_FROM);
    fromOp.setSiblingField(null);
    fromOp.setSuperTypeSignature(superTypeSignature);
    fromOp.validate();
    fromOp.run(monitor, manager);
    IType fromField = fromOp.getCreatedFormField();

    FormFieldNewOperation toOp = new FormFieldNewOperation(type, false);
    toOp.setTypeName(parentName + SdkProperties.SUFFIX_TO);
    toOp.setSiblingField(null);
    toOp.setSuperTypeSignature(superTypeSignature);
    toOp.validate();
    toOp.run(monitor, manager);
    IType toField = toOp.getCreatedFormField();

    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(type);
    if (nlsProject != null) {
      INlsEntry fromEntry = nlsProject.getEntry("from");
      if (fromEntry != null) {
        NlsTextMethodUpdateOperation fromNlsOp = new NlsTextMethodUpdateOperation(fromField, "getConfiguredLabel");
        fromNlsOp.setNlsEntry(fromEntry);
        fromNlsOp.validate();
        fromNlsOp.run(monitor, manager);
      }
      INlsEntry toEntry = nlsProject.getEntry("to");
      if (toEntry != null) {
        NlsTextMethodUpdateOperation toNlsOp = new NlsTextMethodUpdateOperation(toField, "getConfiguredLabel");
        toNlsOp.setNlsEntry(toEntry);
        toNlsOp.validate();
        toNlsOp.run(monitor, manager);
      }

    }
    monitor.done();
  }
}
