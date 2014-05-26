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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.TypeSelectionDialog;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceConsumerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.text.edits.ReplaceEdit;
import org.osgi.framework.BundleActivator;

/**
 * Not used in this release
 */
public class ErrorProneWsdlLocatorCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private IInitializer m_initializer;
  private String m_projectRelativeWsdlPath;
  private String m_staticMemberName;
  private IType m_activator;
  private String m_markerGroupUUID;
  private WebserviceEnum m_webserviceEnum;

  public ErrorProneWsdlLocatorCommand(IScoutBundle bundle, IInitializer initializer, String projectRelativeWsdlPath, String staticMemberName, String markerGroupUUID, WebserviceEnum webserviceEnum) {
    super("Fix problem to load WSDL resource");
    m_bundle = bundle;
    m_initializer = initializer;
    m_projectRelativeWsdlPath = projectRelativeWsdlPath;
    m_staticMemberName = staticMemberName;
    m_markerGroupUUID = markerGroupUUID;
    m_webserviceEnum = webserviceEnum;
    setSolutionDescription("By using this task, the locator code is changed to use the bundle's class loader instead.");
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    Set<IType> activators = findActivator();

    if (activators.size() == 0) {
      MessageBox messageBox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_QUESTION | SWT.OK);
      messageBox.setText(Texts.get("Error"));
      messageBox.setMessage("Unable to find bundle's activator class");
      messageBox.open();
      return false;
    }
    else if (activators.size() == 1) {
      m_activator = CollectionUtility.firstElement(activators);
      return true;
    }
    else {
      TypeSelectionDialog dialog = new TypeSelectionDialog(ScoutSdkUi.getShell(), "Activator selection", "Which Activator should be used to search for resources with it's bundle's classloader?");
      dialog.setElements(activators);
      if (dialog.open() == Window.OK) {
        m_activator = dialog.getElement();
        return TypeUtility.exists(m_activator);
      }
      return false;
    }
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ICompilationUnit icu = m_initializer.getDeclaringType().getCompilationUnit();
    IType declaringType = m_initializer.getDeclaringType();
    workingCopyManager.register(icu, monitor);

    String resolvedActivatorName = JaxWsSdkUtility.resolveTypeName(declaringType, m_activator);

    // prepare new content of static initializer
    StringBuilder buf = new StringBuilder();

    try {
      String oldCode = m_initializer.getSource();
      buf.append("/*\n")
          .append(" *").append(" This is a fix by Eclipse Scout SDK to use bundle's classloader to locate WSDL file.\n")
          .append(" * The uncommented static initializer code originates by JAX-WS stub generation process.\n")
          .append(" */\n");
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new StringReader(oldCode));
        String line = null;
        while ((line = reader.readLine()) != null) {
          buf.append("// ").append(line).append("\n");
        }
      }
      finally {
        if (reader != null) {
          try {
            reader.close();
          }
          catch (Exception e) {
          }
        }
      }
    }
    catch (IOException e) {
      JaxWsSdk.logError(e);
    }

    buf.append("static {\n");
    buf.append("URL url = " + resolvedActivatorName + ".getDefault().getBundle().getResource(\"" + m_projectRelativeWsdlPath + "\");\n");
    buf.append("if (url == null) {\n");
    buf.append("  logger.warning(\"Failed to create URL for the wsdl Location: '" + m_projectRelativeWsdlPath + "', retrying as a local file\");\n");
    buf.append("}\n");
    buf.append(m_staticMemberName + " = url;\n");
    buf.append("}");

    // replace static initializer with new content
    Document icuDoc = new Document(declaringType.getCompilationUnit().getBuffer().getContents());
    ReplaceEdit edit = new ReplaceEdit(m_initializer.getSourceRange().getOffset(), m_initializer.getSourceRange().getLength(), buf.toString());
    try {
      edit.apply(icuDoc);
    }
    catch (BadLocationException e) {
      throw new CoreException(new ScoutStatus("Failed to update static initializer. [type=" + declaringType.getElementName() + "]", e));
    }

    // format icu
    SourceFormatOperation sourceFormatOp = new SourceFormatOperation(declaringType.getJavaProject(), icuDoc, null);
    sourceFormatOp.run(monitor, workingCopyManager);

    // write document back
    icu.getBuffer().setContents(ScoutUtility.cleanLineSeparator(icuDoc.get(), icuDoc));

    // create import directive of Activator if required
    JaxWsSdkUtility.createImportDirective(declaringType, m_activator);

    // reconcilation
    workingCopyManager.reconcile(declaringType.getCompilationUnit(), monitor);
    workingCopyManager.unregister(icu, monitor);

    if (m_webserviceEnum == WebserviceEnum.Provider) {
      JaxWsSdk.getDefault().notifyPageReload(WebServiceProviderNodePage.class, m_markerGroupUUID, WebServiceProviderNodePage.DATA_STUB_FILES);
    }
    else {
      JaxWsSdk.getDefault().notifyPageReload(WebServiceConsumerNodePage.class, m_markerGroupUUID, WebServiceConsumerNodePage.DATA_STUB_FILES);
    }
  }

  public Set<IType> findActivator() {
    // try to find Activator to use it's bundle to load resources
    IType bundleActivator = TypeUtility.getType(BundleActivator.class.getName());
    ICachedTypeHierarchy hierarchy = TypeUtility.getPrimaryTypeHierarchy(bundleActivator);
    Set<IType> types = hierarchy.getAllSubtypes(bundleActivator, new ITypeFilter() {

      @Override
      public boolean accept(IType type) {
        try {
          if (!TypeUtility.exists(type)) {
            return false;
          }
          // must be concrete type
          if (Flags.isAbstract(type.getFlags())) {
            return false;
          }

          // must be in same project
          if (!m_bundle.contains(type)) {
            return false;
          }

          return true;
        }
        catch (JavaModelException e) {
          JaxWsSdk.logError("could not determine bundle's activator properly", e);
        }
        return false;
      }
    });

    return types;
  }

  public IType getActivator() {
    return m_activator;
  }

  public void setActivator(IType activator) {
    m_activator = activator;
  }
}
