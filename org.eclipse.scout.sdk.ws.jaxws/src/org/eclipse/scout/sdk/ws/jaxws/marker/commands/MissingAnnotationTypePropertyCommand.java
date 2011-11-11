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
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;

public class MissingAnnotationTypePropertyCommand extends AbstractExecutableMarkerCommand {

  private IType m_declaringType;
  private IType m_annotationType;
  private String m_property;
  private IType m_propertyValue;

  public MissingAnnotationTypePropertyCommand(IType declaringType, IType annotationType, String property, IType propertyValue) {
    super("Invalid annotation declaration");
    m_declaringType = declaringType;
    m_annotationType = annotationType;
    m_property = property;
    m_propertyValue = propertyValue;
    setSolutionDescription("By using this task, the annotation declaration is updated.");
  }

  @Override
  public void execute(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    AnnotationUpdateOperation op = new AnnotationUpdateOperation();
    op.setDeclaringType(m_declaringType);
    op.setAnnotationType(m_annotationType);
    op.addTypeProperty(m_property, m_propertyValue);
    new OperationJob(op).schedule();
  }
}
