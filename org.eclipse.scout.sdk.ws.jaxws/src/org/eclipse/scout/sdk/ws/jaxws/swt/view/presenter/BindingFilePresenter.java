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
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants.MarkerType;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.FileDeleteOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class BindingFilePresenter extends FilePresenter {

  private BuildJaxWsBean m_buildJaxWsBean;

  public BindingFilePresenter(Composite parent, FormToolkit toolkit) {
    super(parent, toolkit);
    setResetLinkVisible(true);
    setFileDirectory(JaxWsConstants.PATH_BUILD);
    setFileExtension("xml");
    setLabel(Texts.get("BindingFile"));
    setMarkerType(MarkerType.BindingFile);
    setResetTooltip(Texts.get("TooltipRemoveBindingFile"));
  }

  @Override
  protected IFile execBrowseAction() {
    IFile oldFile = getValue();
    IFile newFile = super.execBrowseAction();
    if (newFile == null || CompareUtility.equals(oldFile, newFile)) {
      return null;
    }

    Map<String, List<String>> propertiers = m_buildJaxWsBean.getPropertiers();
    String path = null;
    if (oldFile != null) {
      path = oldFile.getProjectRelativePath().toPortableString();
    }
    if (!removeBindingFileEntry(propertiers, path)) {
      return null;
    }
    JaxWsSdkUtility.addBuildProperty(propertiers, JaxWsConstants.OPTION_BINDING_FILE, newFile.getProjectRelativePath().toPortableString());
    m_buildJaxWsBean.setProperties(propertiers);

    // store property map
    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXmlAsync(m_buildJaxWsBean.getXml().getDocument(), m_buildJaxWsBean.getAlias(), IResourceListener.EVENT_BUILDJAXWS_PROPERTIES_CHANGED);
    return newFile;
  }

  @Override
  protected void execResetAction() throws CoreException {
    IFile file = getValue();

    String bindingFileRaw = null;
    if (file != null) {
      bindingFileRaw = file.getProjectRelativePath().toPortableString();
    }

    Map<String, List<String>> propertiers = m_buildJaxWsBean.getPropertiers();
    if (!removeBindingFileEntry(propertiers, bindingFileRaw)) {
      return; // used canceled operation
    }

    // store property map
    m_buildJaxWsBean.setProperties(propertiers);
    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXmlAsync(m_buildJaxWsBean.getXml().getDocument(), m_buildJaxWsBean.getAlias(), IResourceListener.EVENT_BUILDJAXWS_PROPERTIES_CHANGED);
  }

  private boolean removeBindingFileEntry(Map<String, List<String>> properties, String bindingFileRaw) {
    IFile bindingFile = JaxWsSdkUtility.getFile(m_bundle, bindingFileRaw, false);
    if (bindingFile != null && bindingFile.exists()) {
      MessageBox messageBox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
      messageBox.setMessage(Texts.get("QuestionShouldFileXAlsoBeDeletedFromDisk", bindingFileRaw));
      int status = messageBox.open();
      if (status == SWT.YES) {
        FileDeleteOperation op = new FileDeleteOperation();
        op.setFile(bindingFile);
        new OperationJob(op).schedule();
      }
      else if (status == SWT.CANCEL) {
        return false;
      }
    }

    List<String> bindingFiles = properties.get(JaxWsConstants.OPTION_BINDING_FILE);

    if (bindingFiles != null && bindingFiles.size() > 0) {
      while (bindingFiles.remove(bindingFileRaw)) {
        // nop -> in case multiple entries have null as their value <property name="b" />
      }

      if (bindingFiles.size() == 0) {
        properties.remove(JaxWsConstants.OPTION_BINDING_FILE);
      }
    }

    return true;
  }

  public BuildJaxWsBean getBuildJaxWsBean() {
    return m_buildJaxWsBean;
  }

  public void setBuildJaxWsBean(BuildJaxWsBean buildJaxWsBean) {
    m_buildJaxWsBean = buildJaxWsBean;
  }
}
