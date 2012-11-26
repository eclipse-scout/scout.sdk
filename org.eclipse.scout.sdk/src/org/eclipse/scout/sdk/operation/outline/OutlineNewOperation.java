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
package org.eclipse.scout.sdk.operation.outline;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.MethodUpdateContentOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.InsertEdit;

/**
 * <h3>{@link OutlineNewOperation}</h3> ...
 */
public class OutlineNewOperation implements IOperation {
  // in members
  private IScoutBundle m_clientBundle;
  private String m_typeName;
  private String m_superTypeSignature;
  private INlsEntry m_nlsEntry;
  private boolean m_addToDesktop;
  private IType m_desktopType;
  private boolean m_formatSource;
  // out members
  private IType m_createdOutline;

  public OutlineNewOperation() {
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name can not be null or emtpy");
    }
  }

  @Override
  public String getOperationName() {
    return "New Outline '" + getTypeName() + "'...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getTypeName(), getClientBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_DESKTOP_OUTLINES), getClientBundle());
    if (getSuperTypeSignature() == null) {
      setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IOutline, getClientBundle().getJavaProject()));
    }
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdOutline = newOp.getCreatedType();
    boolean addToDesktop = TypeUtility.exists(getDesktopType()) && isAddToDesktop();

    workingCopyManager.register(m_createdOutline.getCompilationUnit(), monitor);

    // nls text
    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(getCreatedOutline(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TITLE, false);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }

    // add to desktop
    if (addToDesktop) {
      addOutlineToDesktop(getCreatedOutline(), monitor, workingCopyManager);
      addOutlineButtonToDesktop(getCreatedOutline(), monitor, workingCopyManager);
    }

    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{m_createdOutline.getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);

    if (m_formatSource) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedOutline(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);

      if (addToDesktop) {
        JavaElementFormatOperation desktopFormatOp = new JavaElementFormatOperation(getDesktopType(), true);
        desktopFormatOp.validate();
        desktopFormatOp.run(monitor, workingCopyManager);
      }
    }
  }

  private void addOutlineToDesktop(final IType outlineType, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String methodName = "getConfiguredOutlines";
    IMethod method = TypeUtility.getMethod(getDesktopType(), methodName);
    if (TypeUtility.exists(method)) {
      MethodUpdateContentOperation updateContentOp = new MethodUpdateContentOperation(method) {
        @Override
        protected void updateMethodBody(Document methodBody, IImportValidator validator) throws CoreException {
          // try 'list.add(MyOutline.class)' pattern
          Matcher matcher = Pattern.compile("([a-zA-Z0-9\\_\\-]*)\\.add\\(\\s*[a-zA-Z0-9\\_\\-]*\\.class\\s*\\)\\;", Pattern.MULTILINE).matcher(methodBody.get());
          int index = -1;
          String listName = null;
          while (matcher.find()) {
            index = matcher.end();
            listName = matcher.group(1);
          }
          if (index > 0) {
            String addSource = listName + ".add(" + validator.getTypeName(SignatureCache.createTypeSignature(outlineType.getFullyQualifiedName())) + ".class);";
            if (methodBody.get().contains(addSource)) {
              return;
            }
            InsertEdit edit = new InsertEdit(index, "\n" + addSource);
            try {
              edit.apply(methodBody);
            }
            catch (Exception e) {
              ScoutSdk.logError("could not update method '" + getMethod().getElementName() + "' in type '" + getMethod().getDeclaringType().getFullyQualifiedName() + "'.", e);
            }
          }
          else {
            // try 'return new Class[]{}' pattern
            matcher = Pattern.compile("\\s*return\\s*new\\s*Class\\[\\]\\{([a-zA-Z0-9\\.\\,\\s\\_\\-]*)\\}\\s*\\;", Pattern.MULTILINE).matcher(methodBody.get());
            if (matcher.find()) {
              String list = matcher.group(1).trim();
              boolean appendComma = !list.endsWith(",");
              int pos = matcher.end(1);
              String addSource = validator.getTypeName(SignatureCache.createTypeSignature(outlineType.getFullyQualifiedName())) + ".class";
              if (methodBody.get().contains(addSource)) {
                return;
              }
              InsertEdit edit = new InsertEdit(pos, (appendComma ? ", " : "") + "\n" + addSource);
              try {
                edit.apply(methodBody);
              }
              catch (Exception e) {
                ScoutSdk.logError("could not update method '" + getMethod().getElementName() + "' in type '" + getMethod().getDeclaringType().getFullyQualifiedName() + "'.", e);
              }
            }
            else {
              ScoutSdk.logWarning("could find insert position for an additional outline in method '" + getMethod().getElementName() + "' in type '" + getMethod().getDeclaringType().getFullyQualifiedName() + "'.");
            }
          }
        }
      };
      updateContentOp.setFormatSource(true);
      updateContentOp.validate();
      updateContentOp.run(monitor, workingCopyManager);
    }
    else {
      MethodOverrideOperation overrideOp = new MethodOverrideOperation(getDesktopType(), methodName) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder builder = new StringBuilder();
          String arrayListRef = validator.getTypeName(SignatureCache.createTypeSignature(ArrayList.class.getName()));
          String outlineRef = validator.getTypeName(SignatureCache.createTypeSignature(outlineType.getFullyQualifiedName()));
          String iOutlineRef = validator.getTypeName(SignatureCache.createTypeSignature(RuntimeClasses.IOutline));
          builder.append(arrayListRef + "<Class<? extends " + iOutlineRef + ">> outlines = new " + arrayListRef + "<Class<? extends " + iOutlineRef + ">>();\n");
          builder.append("outlines.add(" + outlineRef + ".class);\n");
          builder.append("return outlines.toArray(new Class[outlines.size()]);");
          return builder.toString();
        }
      };
      overrideOp.setFormatSource(true);
      IStructuredType structuredType = ScoutTypeUtility.createStructuredType(getDesktopType());
      overrideOp.setSibling(structuredType.getSiblingMethodConfigGetConfigured(methodName));
      overrideOp.validate();
      overrideOp.run(monitor, workingCopyManager);

      AnnotationCreateOperation createSuppressWarning = new AnnotationCreateOperation(overrideOp.getCreatedMethod(), SignatureCache.createTypeSignature(SuppressWarnings.class.getName()));
      createSuppressWarning.addParameter("\"unchecked\"");
      createSuppressWarning.validate();
      createSuppressWarning.run(monitor, workingCopyManager);
    }
  }

  private void addOutlineButtonToDesktop(IType outlineType, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {

    final String className = getTypeName() + SdkProperties.SUFFIX_VIEW_BUTTON;
    for (IType innerType : getDesktopType().getTypes()) {
      if (className.equals(innerType.getElementName())) {
        return;
      }
    }

    OrderedInnerTypeNewOperation outlineButtonOp = new OrderedInnerTypeNewOperation(className, getDesktopType(), false) {
      @Override
      protected void createContent(StringBuilder source, IImportValidator validator) {
        source.append("public ");
        source.append(className);
        source.append("() { super(Desktop.this, ");
        source.append(validator.getTypeName(SignatureCache.createTypeSignature(OutlineNewOperation.this.getTypeName())));
        source.append(".class); }");
      }
    };
    outlineButtonOp.setOrderDefinitionType(TypeUtility.getType(RuntimeClasses.IViewButton));
    outlineButtonOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.AbstractOutlineViewButton, getDesktopType().getJavaProject()));
    outlineButtonOp.setTypeModifiers(Flags.AccPublic);
    outlineButtonOp.validate();
    outlineButtonOp.run(monitor, workingCopyManager);

    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(outlineButtonOp.getCreatedType(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TEXT, false);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }
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

  public IType getCreatedOutline() {
    return m_createdOutline;
  }

  public void setAddToDesktop(boolean addToDesktop) {
    m_addToDesktop = addToDesktop;
  }

  public boolean isAddToDesktop() {
    return m_addToDesktop;
  }

  public void setDesktopType(IType desktopType) {
    m_desktopType = desktopType;
  }

  public IType getDesktopType() {
    return m_desktopType;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

}
