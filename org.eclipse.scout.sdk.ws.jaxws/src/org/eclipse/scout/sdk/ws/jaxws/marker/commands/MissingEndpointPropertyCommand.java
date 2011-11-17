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

import java.util.Arrays;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.TypeSelectionDialog;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class MissingEndpointPropertyCommand extends AbstractExecutableMarkerCommand {

  private IType m_implType;
  private IType m_annotationType;
  private String m_property;
  private IType m_portTypeInterfaceType;
  private IFile m_stubJarFile;
  private QName m_portTypeQName;

  public MissingEndpointPropertyCommand(IType implType) {
    super("Missing or invalid " + WebService.class.getSimpleName() + " annotation declaration");
    m_implType = implType;
    m_annotationType = TypeUtility.getType(WebService.class.getName());
    m_property = "endpointInterface";
    setSolutionDescription("By using this task, the annotation declaration is updated.");
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    if (m_portTypeQName != null) {
      m_portTypeInterfaceType = JaxWsSdkUtility.resolvePortTypeInterfaceType(m_portTypeQName, m_stubJarFile);
    }
    if (m_portTypeInterfaceType != null) {
      return true;
    }

    IType[] candidates = JaxWsSdkUtility.resolvePortTypeInterfaceTypes(null, m_stubJarFile);
    TypeSelectionDialog dialog = new TypeSelectionDialog(ScoutSdkUi.getShell(), Texts.get("PortTypeInterface"), Texts.get("PleaseChoosePortTypeInterface"));
    dialog.setElements(Arrays.asList(candidates));
    if (dialog.open() == Dialog.OK) {
      m_portTypeInterfaceType = dialog.getElement();
      return true;
    }
    return false;
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    AnnotationUpdateOperation op = new AnnotationUpdateOperation();
    op.setDeclaringType(m_implType);
    op.setAnnotationType(m_annotationType);
    op.addStringProperty(m_property, m_portTypeInterfaceType.getFullyQualifiedName());
    new OperationJob(op).schedule();
  }

  public IFile getStubJarFile() {
    return m_stubJarFile;
  }

  public void setStubJarFile(IFile stubJarFile) {
    m_stubJarFile = stubJarFile;
  }

  public QName getPortTypeQName() {
    return m_portTypeQName;
  }

  public void setPortTypeQName(QName portTypeQName) {
    m_portTypeQName = portTypeQName;
  }

  public IType getPortTypeInterfaceType() {
    return m_portTypeInterfaceType;
  }
}
