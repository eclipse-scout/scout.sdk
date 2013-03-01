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
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.BindingFileCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper.SchemaCandidate;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact.TypeEnum;
import org.eclipse.swt.widgets.Shell;

public class BindingFileNewAction extends AbstractLinkAction {

  private BuildJaxWsBean m_buildJaxWsBean;
  private IScoutBundle m_bundle;
  private WsdlResource m_wsdlResource;

  public BindingFileNewAction() {
    super(Texts.get("AddBindingFile"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd));
    setLinkText(Texts.get("AddBindingFile"));
    setToolTip(Texts.get("TooltipBindingFileNew"));
  }

  public void init(IScoutBundle bundle, BuildJaxWsBean buildJaxWsBean, WsdlResource wsdlResource) {
    m_buildJaxWsBean = buildJaxWsBean;
    m_bundle = bundle;
    m_wsdlResource = wsdlResource;
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    SchemaCandidate schemaCandidate = null;
    try {
      schemaCandidate = GlobalBindingRegistrationHelper.popupForSchema(m_wsdlResource.getFile());
    }
    catch (CoreException e) {
      if (e.getStatus() != null && e.getStatus().getCode() == Status.CANCEL_STATUS.getCode()) {
        return null;
      }
    }

    BindingFileCreateOperation op = new BindingFileCreateOperation();
    if (schemaCandidate != null) {
      WsdlArtefact<IFile> wsdlArtefact = schemaCandidate.getWsdlArtefact();
      if (wsdlArtefact.getInlineSchemas().length > 1) {
        op.setSchemaTargetNamespace(SchemaUtility.getSchemaTargetNamespace(schemaCandidate.getSchema()));
      }
      if (wsdlArtefact.getTypeEnum() == TypeEnum.ReferencedWsdl) {
        op.setWsdlLocation(wsdlArtefact.getFileHandle().getFile());
      }
    }

    IPath bindingFilePath = JaxWsSdkUtility.toUniqueProjectRelativeBindingFilePath(m_bundle, m_buildJaxWsBean.getAlias(), op.getSchemaTargetNamespace());
    Map<String, List<String>> properties = m_buildJaxWsBean.getPropertiers();
    JaxWsSdkUtility.addBuildProperty(properties, JaxWsConstants.OPTION_BINDING_FILE, bindingFilePath.toString());

    op.setBundle(m_bundle);
    op.setProjectRelativePath(bindingFilePath);
    op.setWsdlDestinationFolder(JaxWsSdkUtility.getParentFolder(m_bundle, m_wsdlResource.getFile()));
    op.setCreateGlobalBindingSection(!JaxWsSdkUtility.containsGlobalBindingSection(m_bundle, properties, false));
    new OperationJob(op).schedule();

    m_buildJaxWsBean.setProperties(properties);
    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXmlAsync(m_buildJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_BUILDJAXWS_PROPERTIES_CHANGED, m_buildJaxWsBean.getAlias());
    return null;
  }
}
