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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.operation.BindingFileCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper.SchemaCandidate;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact.TypeEnum;

public class CorruptBindingFileCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private IFile m_bindingFile;
  private WsdlResource m_wsdlResource;

  private SchemaCandidate m_schemaCandidate;

  public CorruptBindingFileCommand(IScoutBundle bundle, IFile bindingFile, WsdlResource wsdlResource) {
    super("Corrupt or missing binding file");
    m_bundle = bundle;
    m_bindingFile = bindingFile;
    m_wsdlResource = wsdlResource;
    setSolutionDescription("By using this task, a new binding file '" + m_bindingFile.getProjectRelativePath() + "' is created.");
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    try {
      m_schemaCandidate = GlobalBindingRegistrationHelper.popupForSchema(m_wsdlResource.getFile());
    }
    catch (CoreException e) {
      if (e.getStatus() != null && e.getStatus().getCode() == Status.CANCEL_STATUS.getCode()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IPath bindingFilePath = m_bindingFile.getProjectRelativePath();
    IFolder wsdlFolder = JaxWsSdkUtility.getParentFolder(m_bundle, m_wsdlResource.getFile());

    BindingFileCreateOperation op = new BindingFileCreateOperation();
    op.setBundle(m_bundle);
    op.setProjectRelativePath(bindingFilePath);
    op.setWsdlDestinationFolder(wsdlFolder);

    if (m_schemaCandidate != null) {
      WsdlArtefact<IFile> wsdlArtefact = m_schemaCandidate.getWsdlArtefact();
      if (wsdlArtefact.getInlineSchemas().length > 1) {
        op.setSchemaTargetNamespace(SchemaUtility.getSchemaTargetNamespace(m_schemaCandidate.getSchema()));
      }
      if (wsdlArtefact.getTypeEnum() == TypeEnum.ReferencedWsdl) {
        op.setWsdlLocation(wsdlArtefact.getFileHandle().getFile());
      }
    }
    op.run(monitor, workingCopyManager);
  }
}
