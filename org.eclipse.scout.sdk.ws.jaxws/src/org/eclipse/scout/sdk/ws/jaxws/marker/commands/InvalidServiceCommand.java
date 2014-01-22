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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.TypeSelectionDialog;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.text.edits.ReplaceEdit;

public class InvalidServiceCommand extends AbstractExecutableMarkerCommand {

  private IType m_type;
  private IFile m_stubJarFile;
  private IType m_serviceType;

  public InvalidServiceCommand(IType type, IFile stubJarFile) {
    super("Invalid service in super type generic parameter");
    m_type = type;
    m_stubJarFile = stubJarFile;
    setSolutionDescription("Use this task to fix service registration");
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    IType[] candidates = JaxWsSdkUtility.resolveServiceTypes(null, m_stubJarFile);
    switch (candidates.length) {
      case 0: {
        MessageBox messageBox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_INFORMATION | SWT.OK);
        messageBox.setText(Texts.get("Information"));
        messageBox.setMessage(Texts.get("NoServicesFound"));
        messageBox.open();
        break;
      }
      case 1: {
        m_serviceType = candidates[0];
        break;
      }
      default: {
        TypeSelectionDialog dialog = new TypeSelectionDialog(ScoutSdkUi.getShell(), Texts.get("Service"), Texts.get("PleaseChooseServiceType"));
        dialog.setElements(Arrays.asList(candidates));
        if (dialog.open() == Dialog.OK) {
          m_serviceType = dialog.getElement();
        }
        break;
      }
    }
    return m_serviceType != null;
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ISourceRange classNameRange = m_type.getNameRange();
    int positionAfterClassName = classNameRange.getOffset() + classNameRange.getLength();

    String source = m_type.getCompilationUnit().getBuffer().getContents();
    String sourceAfterClassName = source.substring(positionAfterClassName);

    int insertPosition = -1;

    ReplaceEdit edit = null;
    Pattern pattern = Pattern.compile(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient).getElementName() + "\\s*\\<\\s*([^\\,]*?)\\,([^\\>]*?)\\s*\\>");
    Matcher matcher = pattern.matcher(sourceAfterClassName);
    if (matcher.find()) {
      insertPosition = positionAfterClassName + matcher.start(1);
      ISourceRange insertionRange = new SourceRange(insertPosition, matcher.end(1) - matcher.start(1));
      edit = new ReplaceEdit(insertionRange.getOffset(), insertionRange.getLength(), m_serviceType.getElementName());
    }
    else {
      pattern = Pattern.compile(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient).getElementName() + "\\s*\\<\\s*([^\\>]*?)\\>");
      matcher = pattern.matcher(sourceAfterClassName);
      if (matcher.find()) {
        insertPosition = positionAfterClassName + matcher.start(1);
        ISourceRange insertionRange = new SourceRange(insertPosition, matcher.end(1) - matcher.start(1));
        edit = new ReplaceEdit(insertionRange.getOffset(), insertionRange.getLength(), m_serviceType.getElementName() + ", Object");
      }
      else {
        pattern = Pattern.compile("(" + TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient).getElementName() + ")");
        matcher = pattern.matcher(sourceAfterClassName);
        if (matcher.find()) {
          insertPosition = positionAfterClassName + matcher.end(1);
          ISourceRange insertionRange = new SourceRange(insertPosition, 0);
          edit = new ReplaceEdit(insertionRange.getOffset(), insertionRange.getLength(), "<" + m_serviceType.getElementName() + ", Object>");
        }
      }
    }
    if (edit == null) {
      throw new CoreException(new ScoutStatus("Could not determine insert position for service type in type '" + m_type.getElementName() + "'"));
    }

    Document icuDoc = new Document(source);
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
    JaxWsSdkUtility.createImportDirective(m_type, m_serviceType);

    // reconcilation
    workingCopyManager.reconcile(m_type.getCompilationUnit(), monitor);
    workingCopyManager.unregister(icu, monitor);
  }
}
