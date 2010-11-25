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
package org.eclipse.scout.sdk.operation.page;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.annotation.OrderAnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.text.edits.InsertEdit;

/**
 * <h3> {@link PageNewOperation}</h3> ...
 */
public class PageNewOperation extends AbstractPageOperation {

  final IType iPageWithTable = ScoutSdk.getType(RuntimeClasses.IPageWithTable);

  private IScoutBundle m_clientBundle;
  private String m_typeName;
  private String m_superTypeSignature;
  private INlsEntry m_nlsEntry;
  private IType m_createdPage;
  private boolean m_formatSource;

  public PageNewOperation() {
    this(false);
  }

  public PageNewOperation(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public String getOperationName() {
    return "New page '" + getTypeName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getSuperTypeSignature())) {
      throw new IllegalArgumentException("super type can not be null.");
    }
    if (getClientBundle() == null) {
      throw new IllegalArgumentException("client bundle can not be null.");
    }
    if (getTypeName() == null) {
      throw new IllegalArgumentException("type name can not be null.");
    }
  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getTypeName(), getClientBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_DESKTOP_OUTLINES_PAGES), getClientBundle());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.run(monitor, workingCopyManager);
    m_createdPage = newOp.getCreatedType();
    workingCopyManager.register(getCreatedPage().getCompilationUnit(), monitor);
    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(getCreatedPage(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TITLE, false);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }
    ITypeHierarchy superTypeHierarchy = getCreatedPage().newSupertypeHierarchy(monitor);
    if (superTypeHierarchy.contains(iPageWithTable)) {
      // create table
      InnerTypeNewOperation tableOp = new InnerTypeNewOperation(ScoutIdeProperties.TYPE_NAME_OUTLINE_WITH_TABLE_TABLE, getCreatedPage());
      tableOp.addAnnotation(new OrderAnnotationCreateOperation(null, 10.0));
      tableOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractTable, true));
      tableOp.run(monitor, workingCopyManager);

      // generic type
      Pattern p = Pattern.compile("extends\\s*" + superTypeHierarchy.getSuperclass(getCreatedPage()).getElementName(), Pattern.MULTILINE);
      Matcher matcher = p.matcher(getCreatedPage().getSource());
      if (matcher.find()) {
        Document doc = new Document(getCreatedPage().getSource());
        InsertEdit genericEdit = new InsertEdit(matcher.end(), "<" + getCreatedPage().getElementName() + "." + ScoutIdeProperties.TYPE_NAME_TABLEFIELD_TABLE + ">");
        try {
          genericEdit.apply(doc);
          TypeUtility.setSource(getCreatedPage(), doc.get(), workingCopyManager, monitor);
        }
        catch (Exception e) {
          ScoutSdk.logWarning("could not set the generic type of the table field.", e);
        }
      }
    }

    addToHolder(getCreatedPage(), monitor, workingCopyManager);

    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{getCreatedPage().getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);

    if (m_formatSource) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedPage(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedPage() {
    return m_createdPage;
  }

  public void setClientBundle(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }
}
