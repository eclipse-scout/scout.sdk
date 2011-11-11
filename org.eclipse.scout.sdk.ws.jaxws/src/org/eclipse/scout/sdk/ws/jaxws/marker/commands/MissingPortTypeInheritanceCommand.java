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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.jdt.SourceRange;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.text.edits.ReplaceEdit;

public class MissingPortTypeInheritanceCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private IType m_type;
  private IType m_portTypeInterfaceType;
  private SunJaxWsBean m_sunJaxWsBean;
  private String m_markerGroupUUID;

  public MissingPortTypeInheritanceCommand(IScoutBundle bundle, String markerGroupUUID, IType type, IType portTypeInterfaceType, SunJaxWsBean sunJaxWsBean) {
    super("Port type must implement service interface");
    m_bundle = bundle;
    m_type = type;
    m_portTypeInterfaceType = portTypeInterfaceType;
    m_sunJaxWsBean = sunJaxWsBean;
    m_markerGroupUUID = markerGroupUUID;
    setSolutionDescription("By using this task, the port type '" + m_type.getElementName() + "' is changed to inherit from the service interface '" + m_portTypeInterfaceType.getElementName() + "'.");
  }

  @Override
  public void execute(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    boolean superInterfaceAvailable = false;

    ISourceRange classNameRange = m_type.getNameRange();
    int positionAfterClassName = classNameRange.getOffset() + classNameRange.getLength();

    String source = m_type.getCompilationUnit().getBuffer().getContents();
    String sourceAfterClassName = source.substring(positionAfterClassName);

    int insertPosition = -1;
    // try to find 'implements' keyword
    Pattern pattern = Pattern.compile("(implements)\\s+(.*?)\\s+\\{");
    Matcher matcher = pattern.matcher(sourceAfterClassName);
    if (matcher.find()) {
      insertPosition = matcher.end(1);
      superInterfaceAvailable = true;
    }
    else {
      // try to find end of class declaration
      pattern = Pattern.compile("\\s+\\{");
      matcher = pattern.matcher(sourceAfterClassName);
      if (matcher.find()) {
        insertPosition = matcher.end() - 1;
      }
    }

    if (insertPosition == -1) {
      throw new CoreException(new ScoutStatus("Could not determine insert position for interface declaration in type '" + m_type.getElementName() + "'"));
    }
    insertPosition = positionAfterClassName + insertPosition;

    ISourceRange interfaceInsertionRange = new SourceRange(insertPosition, 0);

    Document icuDoc = new Document(source);
    String implementsKeyword = null;
    if (!superInterfaceAvailable) {
      implementsKeyword = "implements";
    }
    String replacement = StringUtility.join(" ", implementsKeyword, m_portTypeInterfaceType.getElementName());
    if (superInterfaceAvailable) {
      replacement += ", ";
    }
    replacement = " " + replacement + " ";

    ReplaceEdit edit = new ReplaceEdit(interfaceInsertionRange.getOffset(), interfaceInsertionRange.getLength(), replacement);
    try {
      edit.apply(icuDoc);
    }
    catch (BadLocationException e) {
      throw new CoreException(new ScoutStatus("Failed to update interface declaration in type '" + m_type.getElementName() + "'", e));
    }

    ICompilationUnit icu = m_type.getCompilationUnit();
    workingCopyManager.register(icu, monitor);

    // format icu
    SourceFormatOperation sourceFormatOp = new SourceFormatOperation(m_type.getJavaProject(), icuDoc, null);
    sourceFormatOp.run(monitor, workingCopyManager);

    // write document back
    icu.getBuffer().setContents(ScoutUtility.cleanLineSeparator(icuDoc.get(), icuDoc));

    // create import directive of Activator if required
    JaxWsSdkUtility.createImportDirective(m_type, m_portTypeInterfaceType);

    // reconcilation
    workingCopyManager.reconcile(m_type.getCompilationUnit(), monitor);
    workingCopyManager.unregister(icu, monitor);

    JaxWsSdkUtility.overrideUnimplementedMethodsAsync(m_type);

    JaxWsSdk.getDefault().notifyPageReload(WebServiceProviderNodePage.class, m_markerGroupUUID, WebServiceProviderNodePage.DATA_JDT_TYPE);
  }
}
