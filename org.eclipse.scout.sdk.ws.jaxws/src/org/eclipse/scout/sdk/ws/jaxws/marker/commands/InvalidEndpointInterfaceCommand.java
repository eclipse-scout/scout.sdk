/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.marker.commands;

import javax.jws.WebService;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.TypeBrowseDialog;

public class InvalidEndpointInterfaceCommand extends AbstractExecutableMarkerCommand {

  private IType m_implType;
  private IType m_annotationType;
  private IType m_portTypeInterfaceType;

  public InvalidEndpointInterfaceCommand(IType implType) {
    super(Texts.get("InvalidEndpointInterfaceSpecified", WebService.class.getSimpleName(), implType.getElementName()));
    m_implType = implType;
    m_annotationType = TypeUtility.getType(WebService.class.getName());
    setSolutionDescription("By using this task, you can change the endpoint interface.");
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    TypeBrowseDialog dialog = new TypeBrowseDialog(ScoutSdkUi.getShell(), Texts.get("PortTypeInterface"), Texts.get("PleaseChoosePortTypeInterface1"));
    dialog.setTypeStyle(IJavaElementSearchConstants.CONSIDER_INTERFACES);
    if (dialog.open() == Dialog.OK) {
      m_portTypeInterfaceType = dialog.getType();
      return true;
    }
    return false;
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    AnnotationUpdateOperation op = new AnnotationUpdateOperation();
    op.setDeclaringType(m_implType);
    op.setAnnotationType(m_annotationType);
    op.addStringProperty("endpointInterface", m_portTypeInterfaceType.getFullyQualifiedName());
    new OperationJob(op).schedule();
  }
}
