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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodNewOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodUpdateContentOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.InsertEdit;

/**
 * <h3>{@link OutlineNewOperation}</h3>
 */
public class OutlineNewOperation extends PrimaryTypeNewOperation {

  public static final String GET_CONFIGURED_OUTLINES = "getConfiguredOutlines";

  // in members
  private INlsEntry m_nlsEntry;
  private IType m_desktopType;

  public OutlineNewOperation(String outlineName, String packageName, IJavaProject javaProject) throws JavaModelException {
    super(outlineName, packageName, javaProject);

    // defaults
    setFlags(Flags.AccPublic);
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IOutline, getJavaProject()));
    getCompilationUnitNewOp().setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    setPackageExportPolicy(ExportPolicy.ADD_PACKAGE);
    setFormatSource(true);
  }

  @Override
  public String getOperationName() {
    return "New Outline '" + getElementName() + "'...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // nls method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsTextMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TITLE);
      nlsTextMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsTextMethodBuilder), nlsTextMethodBuilder);
    }

    super.run(monitor, workingCopyManager);

    // add to desktop
    if (TypeUtility.exists(getDesktopType())) {
      addOutlineToDesktop(getCreatedType(), monitor, workingCopyManager);
      addOutlineButtonToDesktop(getCreatedType(), monitor, workingCopyManager);
      // format desktop type
      JavaElementFormatOperation desktopFormatOp = new JavaElementFormatOperation(getDesktopType(), true);
      desktopFormatOp.validate();
      desktopFormatOp.run(monitor, workingCopyManager);
    }
  }

  private void addOutlineToDesktop(final IType outlineType, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IMethod method = TypeUtility.getMethod(getDesktopType(), GET_CONFIGURED_OUTLINES);
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
        }
      };
      updateContentOp.setFormatSource(true);
      updateContentOp.validate();
      updateContentOp.run(monitor, workingCopyManager);
    }
    else {
      IMethodSourceBuilder overrideBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(GET_CONFIGURED_OUTLINES, getDesktopType());
      overrideBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append(getOutlinesMethodBody(CollectionUtility.arrayList(outlineType), validator, lineDelimiter));
        }
      });
      MethodNewOperation overrideOp = new MethodNewOperation(overrideBuilder, getDesktopType());
      overrideOp.setFormatSource(true);
      IStructuredType structuredType = ScoutTypeUtility.createStructuredType(getDesktopType());
      overrideOp.setSibling(structuredType.getSiblingMethodConfigGetConfigured(GET_CONFIGURED_OUTLINES));
      overrideOp.validate();
      overrideOp.run(monitor, workingCopyManager);

    }
  }

  public static String getOutlinesMethodBody(List<IType> outlineTypes, IImportValidator validator, String lineDelimiter) {
    StringBuilder source = new StringBuilder();
    String listRef = validator.getTypeName(SignatureCache.createTypeSignature(List.class.getName()));
    String arrayListRef = validator.getTypeName(SignatureCache.createTypeSignature(ArrayList.class.getName()));
    String classRef = validator.getTypeName(SignatureCache.createTypeSignature(Class.class.getName()));
    String iOutlineRef = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.IOutline));

    StringBuilder genericPart = new StringBuilder().append('<').append(classRef).append("<? extends ").append(iOutlineRef).append(">>");

    source.append(listRef).append(genericPart).append(" outlines = ").append("new ").append(arrayListRef).append(genericPart).append("();").append(lineDelimiter);
    if (outlineTypes != null && outlineTypes.size() > 0) {
      for (IType t : outlineTypes) {
        if (TypeUtility.exists(t)) {
          String outlineRef = validator.getTypeName(SignatureCache.createTypeSignature(t.getFullyQualifiedName()));
          source.append("outlines.add(").append(outlineRef).append(".class);").append(lineDelimiter);
        }
      }
    }
    source.append("return outlines;");
    return source.toString();
  }

  private void addOutlineButtonToDesktop(IType outlineType, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {

    final String className = getElementName() + SdkProperties.SUFFIX_VIEW_BUTTON;
    for (IType innerType : getDesktopType().getTypes()) {
      if (className.equals(innerType.getElementName())) {
        return;
      }
    }

    workingCopyManager.reconcile(getDesktopType().getCompilationUnit(), monitor);
    ITypeHierarchy desktopSuperHierarchy = TypeUtility.getSupertypeHierarchy(getDesktopType());
    final boolean isExtension = desktopSuperHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IDesktopExtension));

    OrderedInnerTypeNewOperation outlineButtonOp = new OrderedInnerTypeNewOperation(className, getDesktopType());
    outlineButtonOp.setOrderDefinitionType(TypeUtility.getType(IRuntimeClasses.IViewButton));
    outlineButtonOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.AbstractOutlineViewButton, getDesktopType().getJavaProject()));
    outlineButtonOp.setFlags(Flags.AccPublic);

    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(className);
    constructorBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    constructorBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("super(");
        if (isExtension) {
          source.append("getCoreDesktop()");
        }
        else {
          source.append("Desktop.this");
        }
        source.append(",").append(validator.getTypeName(SignatureCache.createTypeSignature(OutlineNewOperation.this.getElementName()))).append(".class);");
      }
    });
    outlineButtonOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
    outlineButtonOp.validate();
    outlineButtonOp.run(monitor, workingCopyManager);
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public void setDesktopType(IType desktopType) {
    m_desktopType = desktopType;
  }

  public IType getDesktopType() {
    return m_desktopType;
  }

}
