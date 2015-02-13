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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.XmlUtility;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h3>{@link CleanupPhantomJarFileExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class CleanupPhantomJarFileExecutor extends AbstractExecutor {

  @Override
  public boolean canRun(IStructuredSelection selection) {
    return isEditable(UiUtility.getScoutBundleFromSelection(selection));
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    IScoutBundle scoutBundle = UiUtility.getScoutBundleFromSelection(selection);

    Set<IFile> phantomJarFiles = getPhantomJarFiles(scoutBundle);
    if (phantomJarFiles.isEmpty()) {
      MessageBox messageBox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_INFORMATION | SWT.OK);
      messageBox.setText(Texts.get("Information"));
      messageBox.setMessage(Texts.get("NoPhantomJarFilesFound"));
      messageBox.open();
      return null;
    }

    IWizard wizard = new PhantomJarFilesDeleteWizard(scoutBundle, phantomJarFiles);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setHelpAvailable(false);
    wizardDialog.open();
    return null;
  }

  private Set<IFile> getPhantomJarFiles(IScoutBundle bundle) {
    IFolder folder = JaxWsSdkUtility.getFolder(bundle, JaxWsConstants.STUB_FOLDER, false);
    if (folder == null || !folder.exists()) {
      return CollectionUtility.hashSet();
    }

    // determine referenced JAR files
    Set<IFile> usedJarFiles = new HashSet<>();
    usedJarFiles.addAll(getProviderJarFiles(bundle));
    usedJarFiles.addAll(getConsumerJarFiles(bundle));

    // get all JAR files in stub folder
    Set<IFile> candidates = new HashSet<>();
    try {
      for (IResource resource : folder.members()) {
        if (!resource.exists() || resource.getType() != IResource.FILE) {
          continue;
        }
        IFile file = (IFile) resource;
        if ("jar".equalsIgnoreCase(file.getFileExtension())) {
          candidates.add(file);
        }
      }

      // determine phantom JAR files
      Set<IFile> phantomJarFiles = new HashSet<>();
      for (IFile candiate : candidates) {
        if (!usedJarFiles.contains(candiate)) {
          phantomJarFiles.add(candiate);
        }
      }
      return phantomJarFiles;
    }
    catch (CoreException e) {
      JaxWsSdk.logError(e);
      return CollectionUtility.hashSet();
    }
  }

  private Set<IFile> getProviderJarFiles(IScoutBundle bundle) {
    Set<IFile> jarFiles = new HashSet<>();

    XmlResource sunJaxWsResource = ResourceFactory.getSunJaxWsResource(bundle);
    Document sunJaxWsXmlDocument = sunJaxWsResource.loadXml();

    if (sunJaxWsXmlDocument == null || sunJaxWsXmlDocument.getDocumentElement() == null) {
      return CollectionUtility.hashSet();
    }

    String tagName = StringUtility.join(":", JaxWsSdkUtility.getXmlPrefix(sunJaxWsXmlDocument.getDocumentElement()), SunJaxWsBean.XML_ENDPOINT);
    List<Element> childElements = XmlUtility.getChildElements(sunJaxWsXmlDocument.getDocumentElement(), tagName);
    for (Element sunJaxWsXml : childElements) {
      SunJaxWsBean sunJaxWsBean = new SunJaxWsBean(sunJaxWsXml);
      BuildJaxWsBean buildJaxWsBean = BuildJaxWsBean.load(bundle, sunJaxWsBean.getAlias(), WebserviceEnum.PROVIDER);
      if (buildJaxWsBean == null) {
        // only consider by-contract providers
        continue;
      }

      IFile jarFile = JaxWsSdkUtility.getStubJarFile(bundle, buildJaxWsBean, sunJaxWsBean.getWsdl());
      if (jarFile != null && jarFile.exists()) {
        jarFiles.add(jarFile);
      }
    }
    return jarFiles;
  }

  private Set<IFile> getConsumerJarFiles(IScoutBundle bundle) {
    Set<IFile> jarFiles = new HashSet<>();

    ICachedTypeHierarchy hierarchy = TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient));
    Set<IType> wsConsumerTypes = hierarchy.getAllSubtypes(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient), ScoutTypeFilters.getClassesInScoutBundles(bundle));

    for (IType consumerType : wsConsumerTypes) {
      if (!TypeUtility.exists(consumerType)) {
        continue;
      }
      BuildJaxWsBean buildJaxWsBean = BuildJaxWsBean.load(bundle, consumerType.getElementName(), WebserviceEnum.CONSUMER);
      if (buildJaxWsBean == null) {
        continue;
      }
      IFile jarFile = JaxWsSdkUtility.getStubJarFile(bundle, buildJaxWsBean, buildJaxWsBean.getWsdl());
      if (jarFile != null && jarFile.exists()) {
        jarFiles.add(jarFile);
      }
    }

    return jarFiles;
  }
}
