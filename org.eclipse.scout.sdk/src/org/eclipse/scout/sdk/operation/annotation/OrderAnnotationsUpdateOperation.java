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
package org.eclipse.scout.sdk.operation.annotation;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 *
 */
public class OrderAnnotationsUpdateOperation implements IOperation {

  private HashMap<IType, OrderAnnotation> m_orderAnnotations = new HashMap<IType, OrderAnnotation>();
  private final IType m_declaringType;

  public OrderAnnotationsUpdateOperation(IType declaringType) {
    m_declaringType = declaringType;

  }

  @Override
  public String getOperationName() {
    return "Update order annotation...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    workingCopyManager.register(getDeclaringType().getCompilationUnit(), monitor);
    IBuffer buffer = getDeclaringType().getCompilationUnit().getBuffer();
    Document sourceDoc = new Document(buffer.getContents());
    MultiTextEdit multiEdit = new MultiTextEdit();
    IImportValidator validator = new CompilationUnitImportValidator(getDeclaringType().getCompilationUnit());
    for (OrderAnnotation orderAnnotation : m_orderAnnotations.values()) {
      if (orderAnnotation.getType() != null) {
        String orderSignature = SignatureCache.createTypeSignature(RuntimeClasses.Order);
        AnnotationCreateOperation op = new AnnotationCreateOperation(orderAnnotation.getType(), orderSignature);
        op.addParameter("" + orderAnnotation.getOrderNr());
        TextEdit edit = op.createEdit(validator, sourceDoc.getDefaultLineDelimiter());
        if (edit != null) {
          multiEdit.addChild(edit);
        }
      }
    }
    try {
      multiEdit.apply(sourceDoc);
      buffer.setContents(sourceDoc.get());
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not update order annotations.", e);
    }
  }

  public void addOrderAnnotation(IType type, double orderNr) {
    if (!type.getDeclaringType().getFullyQualifiedName().equals(m_declaringType.getFullyQualifiedName())) {
      throw new IllegalArgumentException("type must be a direct inner type of '" + getDeclaringType() + "'.");
    }
    m_orderAnnotations.put(type, new OrderAnnotation(type, orderNr));
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public class OrderAnnotation {
    private final IType m_scoutType;
    private final double m_orderNr;

    public OrderAnnotation(IType type, double orderNr) {
      m_scoutType = type;
      m_orderNr = orderNr;
    }

    public double getOrderNr() {
      return m_orderNr;
    }

    public IType getType() {
      return m_scoutType;
    }
  }
}
