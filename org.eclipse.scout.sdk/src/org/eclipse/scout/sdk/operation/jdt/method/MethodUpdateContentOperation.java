/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.jdt.method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.icu.ImportsCreateOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <h3>MethodUpdateContentOperation</h3> This operation is used to update a content of an existing method. The method
 * must be
 * modifiable.
 */
public class MethodUpdateContentOperation implements IOperation {

  private final IMethod m_method;
  private boolean m_formatSource;
  private String m_simpleBody;

  public MethodUpdateContentOperation(IMethod method) {
    this(method, null);
  }

  public MethodUpdateContentOperation(IMethod method, String simpleBody) {
    this(method, simpleBody, false);
  }

  /**
   * @param method
   * @param formatSource
   *          true to force a source code formatting at the end of the operation.
   */
  public MethodUpdateContentOperation(IMethod method, String simpleBody, boolean formatSource) {
    m_method = method;
    m_simpleBody = simpleBody;
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return Texts.get("Process_deleteX", getMethod().getElementName());
  }

  @Override
  public void validate() {
    if (getMethod() == null || !getMethod().exists()) {
      throw new IllegalArgumentException("Update Content for method '" + getMethod().getElementName() + "' failed, method does not exit.");
    }
    if (getMethod().isReadOnly()) {
      throw new IllegalArgumentException("Update Content for method '" + getMethod().getElementName() + "' failed, method is read only.");
    }

  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ISourceRange contentRange = TypeUtility.getContentSourceRange(getMethod());
    if (contentRange != null) {
      try {
        ICompilationUnit icu = getMethod().getDeclaringType().getCompilationUnit();
        workingCopyManager.register(icu, monitor);
        ImportValidator validator = new ImportValidator(icu);
        Document methodBodyDocument = new Document(icu.getBuffer().getText(contentRange.getOffset(), contentRange.getLength()));
        int initialLenght = methodBodyDocument.getLength();
        updateMethodBody(methodBodyDocument, validator);
        int divLength = methodBodyDocument.getLength() - initialLenght;
        Document doc = new Document(icu.getSource());
        ReplaceEdit redit = new ReplaceEdit(contentRange.getOffset(), contentRange.getLength(), methodBodyDocument.get());
        redit.apply(doc);
        if (isFormatSource()) {
          ISourceRange methodSourceRange = getMethod().getSourceRange();
          int offset = methodSourceRange.getOffset();
          int length = methodSourceRange.getLength() + divLength;
          SourceFormatOperation sourceFormatOp = new SourceFormatOperation(getMethod().getJavaProject(), doc, new SourceRange(offset, length));
          sourceFormatOp.run(monitor, workingCopyManager);
        }
        icu.getBuffer().setContents(ScoutUtility.cleanLineSeparator(doc.get(), doc));
        workingCopyManager.reconcile(icu, monitor);

        new ImportsCreateOperation(icu, validator).run(monitor, workingCopyManager);
      }
      catch (BadLocationException e) {
        throw new CoreException(new Status(Status.ERROR, ScoutSdk.PLUGIN_ID, "could not update method: " + getMethod().getElementName(), e));
      }
    }
  }

  /**
   * @param methodBody
   *          the document of the method body -> everything between the method '{''}'.
   * @param validator
   *          validator can be used to determ class references (fully qualified vs. simple name).
   * @throws CoreException
   * @see {@link MethodUpdateContentOperation#createMethodBody(IImportValidator)}
   */
  protected void updateMethodBody(Document methodBody, IImportValidator validator) throws CoreException {
    StringBuilder sourceBuilder = new StringBuilder();
    createMethodBody(sourceBuilder, methodBody.getDefaultLineDelimiter(), validator, methodBody.get());
    ReplaceEdit edit = new ReplaceEdit(0, methodBody.getLength(), sourceBuilder.toString());
    try {
      edit.apply(methodBody);
    }
    catch (BadLocationException e) {
      throw new CoreException(new Status(Status.ERROR, ScoutSdk.PLUGIN_ID, "could not update method: " + getMethod().getElementName(), e));
    }
  }

  /**
   * can be overridden to provide a specific method body. The method body is defined as part between the method body{}.
   * Use {@link SignatureUtility#getTypeReference(String, IImportValidator)} to determ class references (fully
   * qualified vs. simple name).
   *
   * @param sourceBuilder
   * @param lineDelimiter
   * @param validator
   *          validator can be used to determ class references (fully qualified vs. simple name).
   * @param originalBody
   * @return
   * @throws JavaModelException
   */
  protected void createMethodBody(StringBuilder sourceBuilder, String lineDelimiter, IImportValidator validator, String originalBody) throws JavaModelException {
    if (!StringUtility.isNullOrEmpty(getSimpleBody())) {
      sourceBuilder.append(getSimpleBody());
    }
    else {
      sourceBuilder.append(ScoutUtility.getCommentAutoGeneratedMethodStub()).append(lineDelimiter);
    }
    String methodSignature = getMethod().getSignature();
    if (!Signature.getReturnType(methodSignature).equals(Signature.SIG_VOID)) {
      sourceBuilder.append("return ").append(ScoutUtility.getDefaultValueOf(Signature.getReturnType(methodSignature))).append(";").append(lineDelimiter);
    }
  }

  public IMethod getMethod() {
    return m_method;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setSimpleBody(String body) {
    m_simpleBody = body;
  }

  public String getSimpleBody() {
    return m_simpleBody;
  }
}
