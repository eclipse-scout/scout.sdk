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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;

public class MissingScoutWebServiceAnnotationCommand extends AbstractExecutableMarkerCommand {

  private IType m_declaringType;
  private IType m_interfacePortType;

  public MissingScoutWebServiceAnnotationCommand(IType declaringType) {
    super("Missing annotation '" + JaxWsRuntimeClasses.ScoutWebService.getElementName() + "'");
    m_declaringType = declaringType;
    setSolutionDescription("By using this task, the port type '" + m_declaringType.getElementName() + "' is annotated with '" + JaxWsRuntimeClasses.ScoutWebService.getElementName() + "'.");
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    AnnotationUpdateOperation op = new AnnotationUpdateOperation();
    op.setDeclaringType(m_declaringType);
    op.setAnnotationType(JaxWsRuntimeClasses.ScoutWebService);
    new OperationJob(op).schedule();
  }
}
