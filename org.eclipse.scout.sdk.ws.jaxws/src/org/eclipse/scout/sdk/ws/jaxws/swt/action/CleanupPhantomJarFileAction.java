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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.PhantomJarFilesDeleteWizard;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class CleanupPhantomJarFileAction extends AbstractLinkAction {

  private IScoutBundle m_bundle;

  public CleanupPhantomJarFileAction() {
    super(Texts.get("CleanupUnreferencedJarFiles"), JaxWsSdk.getImageDescriptor(JaxWsIcons.Jar));
    setLeadingText(Texts.get("CleanupUnreferencedJarFilesByClicking"));
    setLinkText(Texts.get("here"));
    setToolTip(Texts.get("TooltipCleanupUnreferencedJarFiles"));
  }

  public void init(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    IFile[] phantomJarFiles = getPhantomJarFiles();
    if (phantomJarFiles.length == 0) {
      MessageBox messageBox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_INFORMATION | SWT.OK);
      messageBox.setText(Texts.get("Information"));
      messageBox.setMessage(Texts.get("NoPhantomJarFilesFound"));
      messageBox.open();
      return null;
    }

    IWizard wizard = new PhantomJarFilesDeleteWizard(m_bundle, phantomJarFiles);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setHelpAvailable(false);
    wizardDialog.open();
    return null;
  }

  private IFile[] getPhantomJarFiles() {
    IFolder folder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.STUB_FOLDER, false);
    if (folder == null || !folder.exists()) {
      return new IFile[0];
    }

    // determine referenced JAR files
    Set<IFile> usedJarFiles = new HashSet<IFile>();
    usedJarFiles.addAll(Arrays.asList(getProviderJarFiles()));
    usedJarFiles.addAll(Arrays.asList(getConsumerJarFiles()));

    // get all JAR files in stub folder
    Set<IFile> candidates = new HashSet<IFile>();
    try {
      for (IResource resource : folder.members()) {
        if (!resource.exists() || resource.getType() != IResource.FILE) {
          continue;
        }
        IFile file = (IFile) resource;
        if (file.getFileExtension() != null && file.getFileExtension().equalsIgnoreCase("jar")) {
          candidates.add(file);
        }
      }

      // determine phantom JAR files
      Set<IFile> phantomJarFiles = new HashSet<IFile>();
      for (IFile candiate : candidates) {
        if (!usedJarFiles.contains(candiate)) {
          phantomJarFiles.add(candiate);
        }
      }
      return phantomJarFiles.toArray(new IFile[phantomJarFiles.size()]);
    }
    catch (CoreException e) {
      JaxWsSdk.logError(e);
      return new IFile[0];
    }
  }

  private IFile[] getProviderJarFiles() {
    Set<IFile> jarFiles = new HashSet<IFile>();

    XmlResource sunJaxWsResource = ResourceFactory.getSunJaxWsResource(m_bundle);
    ScoutXmlDocument sunJaxWsXmlDocument = sunJaxWsResource.loadXml();

    if (sunJaxWsXmlDocument == null || sunJaxWsXmlDocument.getRoot() == null) {
      return new IFile[0];
    }

    for (ScoutXmlElement sunJaxWsXml : sunJaxWsXmlDocument.getRoot().getChildren(StringUtility.join(":", sunJaxWsXmlDocument.getRoot().getNamePrefix(), SunJaxWsBean.XML_ENDPOINT))) {
      SunJaxWsBean sunJaxWsBean = new SunJaxWsBean(sunJaxWsXml);
      BuildJaxWsBean buildJaxWsBean = BuildJaxWsBean.load(m_bundle, sunJaxWsBean.getAlias(), WebserviceEnum.Provider);
      if (buildJaxWsBean == null) {
        // only consider by-contract providers
        continue;
      }

      IFile jarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, buildJaxWsBean, sunJaxWsBean.getWsdl());
      if (jarFile != null && jarFile.exists()) {
        jarFiles.add(jarFile);
      }
    }
    return jarFiles.toArray(new IFile[jarFiles.size()]);
  }

  private IFile[] getConsumerJarFiles() {
    Set<IFile> jarFiles = new HashSet<IFile>();

    IPrimaryTypeTypeHierarchy hierarchy = TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient));
    IType[] wsConsumerTypes = hierarchy.getAllSubtypes(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient), TypeFilters.getTypesInProject(m_bundle.getJavaProject()));

    for (IType consumerType : wsConsumerTypes) {
      if (!TypeUtility.exists(consumerType)) {
        continue;
      }
      BuildJaxWsBean buildJaxWsBean = BuildJaxWsBean.load(m_bundle, consumerType.getElementName(), WebserviceEnum.Consumer);
      if (buildJaxWsBean == null) {
        continue;
      }
      IFile jarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, buildJaxWsBean, buildJaxWsBean.getWsdl());
      if (jarFile != null && jarFile.exists()) {
        jarFiles.add(jarFile);
      }
    }

    return jarFiles.toArray(new IFile[jarFiles.size()]);
  }
}
