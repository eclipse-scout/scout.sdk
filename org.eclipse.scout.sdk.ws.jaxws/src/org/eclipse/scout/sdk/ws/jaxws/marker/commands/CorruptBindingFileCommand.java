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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.operation.BindingFileCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.SelectionDialog;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SchemaBean;
import org.eclipse.swt.widgets.Shell;

public class CorruptBindingFileCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private IFile m_bindingFile;
  private WsdlResource m_wsdlResource;

  private SchemaBean m_schema;
  private boolean m_multipleSchemas;

  public CorruptBindingFileCommand(IScoutBundle bundle, IFile bindingFile, WsdlResource wsdlResource) {
    super("Corrupt or missing binding file");
    m_bundle = bundle;
    m_bindingFile = bindingFile;
    m_wsdlResource = wsdlResource;

    setSolutionDescription("By using this task, a new binding file '" + m_bindingFile.getProjectRelativePath() + "' is created.");
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    // determine schema to use
    SchemaBean[] schemas = JaxWsSdkUtility.getAllSchemas(m_bundle, m_wsdlResource);
    if (schemas.length == 0) {
      JaxWsSdk.logWarning("No XML schema found to apply customized binding");
      return true;
    }
    else if (schemas.length == 1) {
      m_schema = schemas[0];
      return true;
    }

    // let user choose which schema to apply custom bindings
    m_multipleSchemas = true;
    P_SelectionDialog dialog = new P_SelectionDialog(ScoutSdkUi.getShell());
    dialog.setElements(Arrays.asList(schemas));
    if (dialog.open() == Window.OK) {
      m_schema = dialog.getElement();
      return true;
    }
    return false;
  }

  @Override
  public void execute(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    BindingFileCreateOperation op = new BindingFileCreateOperation();
    op.setBundle(m_bundle);
    op.setProjectRelativeFilePath(m_bindingFile.getProjectRelativePath());

    if (m_schema != null) {
      if (m_multipleSchemas) {
        op.setSchemaTargetNamespace(m_schema.getTargetNamespace());
      }
      if (!m_schema.isRootWsdlFile()) {
        op.setSchemaDefiningFile(m_schema.getWsdlFile());
      }
    }
    op.run(monitor, workingCopyManager);
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
