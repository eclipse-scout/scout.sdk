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
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;

public class MissingWebServiceAnnotationCommand extends AbstractExecutableMarkerCommand {

  private IType m_implType;
  private IType m_annotationType;

  public MissingWebServiceAnnotationCommand(IType implType) {
    super(String.format("Missing @%s annotation", WebService.class.getSimpleName()));
    m_implType = implType;
    m_annotationType = TypeUtility.getType(WebService.class.getName());
    setSolutionDescription(String.format("By using this task, the @%s annotation is added.", WebService.class.getSimpleName()));
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    AnnotationUpdateOperation op = new AnnotationUpdateOperation();
    op.setDeclaringType(m_implType);
    op.setAnnotationType(m_annotationType);
    new OperationJob(op).schedule();
  }
}
