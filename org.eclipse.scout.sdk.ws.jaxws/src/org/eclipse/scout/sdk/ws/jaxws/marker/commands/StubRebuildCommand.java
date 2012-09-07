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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsStubGenerationOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ErrorDialog;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IOperationFinishedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

public class StubRebuildCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;

  private String m_alias;
  private Map<String, List<String>> m_properties;
  private WsdlResource m_wsdlResource;

  public StubRebuildCommand(IScoutBundle bundle) {
    super("Webservice stub might be out of date");
    m_bundle = bundle;
    setSolutionDescription("By using this task, the webservice stub is rebuilt.");
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    WsStubGenerationOperation op = new WsStubGenerationOperation();
    op.setBundle(m_bundle);
    op.setAlias(m_alias);
    op.setProperties(m_properties);
    op.addOperationFinishedListener(new P_OperationFinishedListener());
    op.setWsdlFileName(m_wsdlResource.getFile().getName());
    IPath wsdlFolderPath = m_wsdlResource.getFile().getProjectRelativePath().removeLastSegments(1); // get folder of WSDL file
    op.setWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, wsdlFolderPath.toPortableString(), false));
    op.run(monitor, workingCopyManager);
  }

  private class P_OperationFinishedListener implements IOperationFinishedListener {

    @Override
    public void operationFinished(final boolean success, final Throwable e) {
      m_wsdlResource.notifyStubRebuilt(m_alias);
      ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          if (success) {
            MessageBox messageBox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_INFORMATION | SWT.OK);
            messageBox.setMessage(Texts.get("WsStubSuccessfullyGenerated"));
            messageBox.open();
          }
          else {
            ErrorDialog dialog = new ErrorDialog(Texts.get("StubGenerationFailed"));
            dialog.setError(Texts.get("WsStubGenerationFailed"), e);
            dialog.open();
          }
        }
      });
    }
  }

  private String getStacktrace(Throwable t) {
    if (t == null) {
      return null;
    }
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public String getAlias() {
    return m_alias;
  }

  public void setAlias(String alias) {
    m_alias = alias;
  }

  public Map<String, List<String>> getProperties() {
    return m_properties;
  }

  public void setProperties(Map<String, List<String>> properties) {
    m_properties = properties;
  }

  public WsdlResource getWsdlResource() {
    return m_wsdlResource;
  }

  public void setWsdlResource(WsdlResource wsdlResource) {
    m_wsdlResource = wsdlResource;
  }
}
