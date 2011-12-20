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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.BindingFileCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.SelectionDialog;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SchemaBean;
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
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    // determine schema to use
    SchemaBean schema = null;

    SchemaBean[] schemas = JaxWsSdkUtility.getAllSchemas(m_bundle, m_wsdlResource);
    if (schemas.length == 0) {
      JaxWsSdk.logWarning("No XML schema found to apply customized binding");
    }
    else if (schemas.length == 1) {
      schema = schemas[0];
    }
    else {
      // let user choose which schema to apply custom bindings
      P_SelectionDialog dialog = new P_SelectionDialog(ScoutSdkUi.getShell());
      dialog.setElements(Arrays.asList(schemas));
      if (dialog.open() == Window.OK) {
        schema = dialog.getElement();
      }
      else {
        return null;
      }
    }

    String schemaTargetNamespace = null;
    if (schema != null && schemas.length > 1) {
      schemaTargetNamespace = schema.getTargetNamespace();
    }

    String bindingFileName = JaxWsSdkUtility.createUniqueBindingFileNamePath(m_bundle, m_buildJaxWsBean.getAlias(), schemaTargetNamespace);

    Map<String, List<String>> properties = m_buildJaxWsBean.getPropertiers();
    JaxWsSdkUtility.addBuildProperty(properties, JaxWsConstants.OPTION_BINDING_FILE, bindingFileName);

    BindingFileCreateOperation op = new BindingFileCreateOperation();
    op.setBundle(m_bundle);
    op.setWsdlDestinationFolder(JaxWsSdkUtility.getParentFolder(m_bundle, m_wsdlResource.getFile()));
    op.setProjectRelativeFilePath(new Path(bindingFileName));
    if (schema != null) {
      op.setSchemaTargetNamespace(schemaTargetNamespace);
      if (!schema.isRootWsdlFile()) {
        op.setSchemaDefiningFile(schema.getWsdlFile());
      }
    }
    op.setCreateGlobalBindingSection(!JaxWsSdkUtility.containsGlobalBindingSection(m_bundle, properties, false));
    new OperationJob(op).schedule();

    m_buildJaxWsBean.setProperties(properties);
    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXmlAsync(m_buildJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_BUILDJAXWS_PROPERTIES_CHANGED, m_buildJaxWsBean.getAlias());
    return null;
  }

  private class P_SelectionDialog extends SelectionDialog<SchemaBean> {

    public P_SelectionDialog(Shell shell) {
      super(shell, "XML schema selection", "Which XML schema should be customized?");
    }

    @Override
    protected String getConfiguredNameColumnText() {
      return "TargetNamespace";
    }

    @Override
    protected String getConfiguredDescriptionColumnText() {
      return "Schema defining WSDL file";
    }

    @Override
    protected boolean getConfiguredIsDescriptionColumnVisible() {
      return true;
    }

    @Override
    protected void execDecorateElement(SchemaBean schemaBean, ViewerCell cell) {
      if (cell.getColumnIndex() == 0) {
        cell.setText(StringUtility.nvl(schemaBean.getTargetNamespace(), "?"));
        cell.setImage(JaxWsSdk.getImage(JaxWsIcons.WsdlFile));
      }
      else {
        String text;
        IFile schemaDefiningFile = schemaBean.getWsdlFile();
        if (schemaDefiningFile != null) {
          text = schemaDefiningFile.getName();
        }
        else {
          text = "?";
        }

        if (schemaBean.isRootWsdlFile()) {
          text += " (Root WSDL file)";
        }
        cell.setText(text);
      }
    }
  }
}
