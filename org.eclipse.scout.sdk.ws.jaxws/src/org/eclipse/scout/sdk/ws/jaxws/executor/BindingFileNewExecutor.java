/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.executor;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.operation.BindingFileCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceConsumerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper.SchemaCandidate;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact.TypeEnum;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link BindingFileNewExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class BindingFileNewExecutor extends AbstractExecutor {

  private IScoutBundle m_bundle;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_bundle = UiUtility.getScoutBundleFromSelection(selection);
    return isEditable(m_bundle);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    Object selected = selection.getFirstElement();
    SchemaCandidate schemaCandidate = null;
    BuildJaxWsBean buildJaxWsBean;
    WsdlResource wsdlResource;
    if (selected instanceof WebServiceConsumerNodePage) {
      WebServiceConsumerNodePage p = (WebServiceConsumerNodePage) selected;
      buildJaxWsBean = p.getBuildJaxWsBean();
      wsdlResource = p.getWsdlResource();
    }
    else if (selected instanceof WebServiceProviderNodePage) {
      WebServiceProviderNodePage p = (WebServiceProviderNodePage) selected;
      buildJaxWsBean = p.getBuildJaxWsBean();
      wsdlResource = p.getWsdlResource();
    }
    else {
      return null;
    }

    try {
      schemaCandidate = GlobalBindingRegistrationHelper.popupForSchema(wsdlResource.getFile());
    }
    catch (CoreException e) {
      if (e.getStatus() != null && e.getStatus().getCode() == Status.CANCEL_STATUS.getCode()) {
        return null;
      }
    }

    BindingFileCreateOperation op = new BindingFileCreateOperation();
    if (schemaCandidate != null) {
      WsdlArtifact<IFile> wsdlArtifact = schemaCandidate.getWsdlArtifact();
      if (wsdlArtifact.getInlineSchemas().length > 1) {
        op.setSchemaTargetNamespace(SchemaUtility.getSchemaTargetNamespace(schemaCandidate.getSchema()));
      }
      if (wsdlArtifact.getTypeEnum() == TypeEnum.REFERENCED_WSDL) {
        op.setWsdlLocation(wsdlArtifact.getFileHandle().getFile());
      }
    }

    IPath bindingFilePath = JaxWsSdkUtility.toUniqueProjectRelativeBindingFilePath(m_bundle, buildJaxWsBean.getAlias(), op.getSchemaTargetNamespace());
    Map<String, List<String>> properties = buildJaxWsBean.getPropertiers();
    JaxWsSdkUtility.addBuildProperty(properties, JaxWsConstants.OPTION_BINDING_FILE, bindingFilePath.toString());

    op.setBundle(m_bundle);
    op.setProjectRelativePath(bindingFilePath);
    op.setWsdlDestinationFolder(JaxWsSdkUtility.getParentFolder(m_bundle, wsdlResource.getFile()));
    op.setCreateGlobalBindingSection(!JaxWsSdkUtility.containsGlobalBindingSection(m_bundle, properties, false));

    new OperationJob(op).schedule();

    buildJaxWsBean.setProperties(properties);
    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXmlAsync(buildJaxWsBean.getXml().getOwnerDocument(), IResourceListener.EVENT_BUILDJAXWS_PROPERTIES_CHANGED, buildJaxWsBean.getAlias());

    return null;
  }

}
