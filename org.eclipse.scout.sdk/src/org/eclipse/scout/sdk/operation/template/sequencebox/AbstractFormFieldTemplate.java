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
package org.eclipse.scout.sdk.operation.template.sequencebox;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.method.MethodNewOperation;
import org.eclipse.scout.sdk.operation.template.IContentTemplate;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractFormFieldTemplate}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.9.0 12.04.2013
 */
public abstract class AbstractFormFieldTemplate implements IContentTemplate {

  public void apply(ITypeSourceBuilder sourceBuilder, IType declaringType, String superTypeSignature, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    String parentName = sourceBuilder.getElementName();
    int lastBoxIndex = parentName.lastIndexOf(SdkProperties.SUFFIX_BOX);
    if (lastBoxIndex > 0) {
      parentName = parentName.substring(0, lastBoxIndex);
    }
    String sequenceBoxFqn = SignatureUtility.DOLLAR_REPLACEMENT_REGEX.matcher(declaringType.getFullyQualifiedName() + "." + sourceBuilder.getElementName()).replaceAll(".");
    double order = 10;

    // from
    String fromFieldName = parentName + SdkProperties.SUFFIX_FROM;
    ITypeSourceBuilder fromFieldBuilder = new TypeSourceBuilder(fromFieldName);
    fromFieldBuilder.setSuperTypeSignature(superTypeSignature);
    fromFieldBuilder.setFlags(Flags.AccPublic);
    fromFieldBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(order));
    fillFromFieldBuilder(fromFieldBuilder);
    sourceBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(fromFieldBuilder, order), fromFieldBuilder);
    // create from getter
    createFormFieldGetter(SignatureCache.createTypeSignature(sequenceBoxFqn + "." + fromFieldBuilder.getElementName()), declaringType, monitor, manager);
    order += 10;

    // to
    String toFieldName = parentName + SdkProperties.SUFFIX_TO;
    ITypeSourceBuilder toFieldBuilder = new TypeSourceBuilder(toFieldName);
    toFieldBuilder.setSuperTypeSignature(superTypeSignature);
    toFieldBuilder.setFlags(Flags.AccPublic);
    toFieldBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(order));
    fillToFieldBuilder(toFieldBuilder);
    sourceBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(toFieldBuilder, order), toFieldBuilder);
    // create to getter
    createFormFieldGetter(SignatureCache.createTypeSignature(sequenceBoxFqn + "." + toFieldBuilder.getElementName()), declaringType, monitor, manager);

    // nls text methods
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(declaringType.getJavaProject());
    if (nlsProject != null) {
      INlsEntry fromEntry = nlsProject.getEntry("from");
      if (fromEntry != null) {
        IMethodSourceBuilder getConfiguredLabelFromBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(fromFieldBuilder, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
        getConfiguredLabelFromBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(fromEntry));
        fromFieldBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLabelFromBuilder), getConfiguredLabelFromBuilder);
      }
      INlsEntry toEntry = nlsProject.getEntry("to");
      if (toEntry != null) {
        IMethodSourceBuilder getConfiguredLabelToBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(fromFieldBuilder, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
        getConfiguredLabelToBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(toEntry));
        toFieldBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLabelToBuilder), getConfiguredLabelToBuilder);
      }
    }

  }

  protected void fillFromFieldBuilder(ITypeSourceBuilder fromFieldBuilder) throws CoreException {

  }

  protected void fillToFieldBuilder(ITypeSourceBuilder toFieldBuilder) throws CoreException {

  }

  protected void createFormFieldGetter(String formFieldSignature, IType declaringType, IProgressMonitor monitor, IWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    // find form
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(declaringType.getCompilationUnit());
    IType form = TypeUtility.getAncestor(declaringType, TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IForm), hierarchy),
        TypeFilters.getTopLevelTypeFilter()));

    if (TypeUtility.exists(form)) {
      String formFieldSimpleName = Signature.getSignatureSimpleName(formFieldSignature);
      MethodNewOperation getterOp = new MethodNewOperation(MethodSourceBuilderFactory.createFieldGetterSourceBuilder(formFieldSignature), form);
      IStructuredType sourceHelper = ScoutTypeUtility.createStructuredForm(form);
      getterOp.setSibling(sourceHelper.getSiblingMethodFieldGetter("get" + formFieldSimpleName));
      getterOp.setFormatSource(false);
      getterOp.validate();
      getterOp.run(monitor, manager);
    }
  }
}
