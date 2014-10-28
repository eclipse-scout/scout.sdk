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
package org.eclipse.scout.sdk.internal.workspace.dto.formdata;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.dto.DtoUtility;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link CompositeFormDataTypeSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public class CompositeFormDataTypeSourceBuilder extends FormDataTypeSourceBuilder {

  /**
   * @param modelType
   * @param elementName
   * @param formDataAnnotation
   */
  public CompositeFormDataTypeSourceBuilder(IType modelType, String elementName, FormDataAnnotation formDataAnnotation, ICompilationUnit derivedCu, IProgressMonitor monitor) {
    super(modelType, elementName, formDataAnnotation, derivedCu, monitor);
  }

  @Override
  protected void createContent(IProgressMonitor monitor) {
    super.createContent(monitor);
    try {
      createCompositeFieldFormData(getModelType(), getLocalTypeHierarchy(), monitor);
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not build form data for '" + getModelType().getElementName() + "'.", e);
    }
  }

  private void createCompositeFieldFormData(IType compositeType, ITypeHierarchy declaringTypeHierarchy, IProgressMonitor monitor) throws JavaModelException {
    for (IType formField : TypeUtility.getInnerTypes(compositeType, TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IFormField), declaringTypeHierarchy))) {
      if (monitor.isCanceled()) {
        return;
      }

      boolean fieldExtendsTemplateField = false;

      if (Flags.isPublic(formField.getFlags())) {
        FormDataAnnotation fieldAnnotation = ScoutTypeUtility.findFormDataAnnotation(formField, declaringTypeHierarchy);

        if (FormDataAnnotation.isCreate(fieldAnnotation)) {
          String formDataTypeSignature = fieldAnnotation.getFormDataTypeSignature();
          String formDataTypeName = null;
          if (StringUtility.isNullOrEmpty(formDataTypeSignature)) {
            formDataTypeName = ScoutUtility.removeFieldSuffix(formField.getElementName());
          }
          else {
            formDataTypeName = Signature.getSignatureSimpleName(formDataTypeSignature);
          }

          String formDataSuperTypeSignature = fieldAnnotation.getSuperTypeSignature();
          IType superType = TypeUtility.getTypeBySignature(formDataSuperTypeSignature);
          String typeErasure = Signature.getTypeErasure(formDataSuperTypeSignature);
          ITypeHierarchy superTypeHierarchy = TypeUtility.getSupertypeHierarchy(superType);
          ITypeSourceBuilder fieldSourceBuilder = null;
          if (SignatureUtility.isEqualSignature(typeErasure, SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableFieldData))) {
            fieldSourceBuilder = new TableFieldFormDataSourceBuilder(formField, formDataTypeName, fieldAnnotation, getDerivedCompilationUnit(), monitor);
          }
          else if (superTypeHierarchy != null && superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.AbstractTableFieldBeanData))) {
            // fill table bean
            fieldSourceBuilder = new TableFieldBeanFormDataSourceBuilder(formField, formDataTypeName, fieldAnnotation, getDerivedCompilationUnit(), monitor);
          }
          else if (declaringTypeHierarchy.isSubtype(TypeUtility.getType(IRuntimeClasses.ICompositeField), formField)
              && !declaringTypeHierarchy.isSubtype(TypeUtility.getType(IRuntimeClasses.IValueField), formField)) {
            // field extends a field template.
            fieldExtendsTemplateField = true;
            fieldSourceBuilder = new CompositeFormDataTypeSourceBuilder(formField, formDataTypeName, fieldAnnotation, getDerivedCompilationUnit(), monitor);
          }
          else {
            fieldSourceBuilder = new FormDataTypeSourceBuilder(formField, formDataTypeName, fieldAnnotation, getDerivedCompilationUnit(), monitor);

            // special case if a boolean (primitive!) property has the same name as a form field -> show warning
            for (IMethodSourceBuilder msb : getMethodSourceBuilders()) {
              if (SIG_FOR_IS_METHOD_NAME.equals(msb.getReturnTypeSignature()) && ("is" + formDataTypeName).equals(msb.getElementName())) {
                fieldSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("TODO [everyone] Duplicate names '" + formDataTypeName + "'. Rename property or form field."));
                break;
              }
            }
          }
          fieldSourceBuilder.setFlags(fieldSourceBuilder.getFlags() | Flags.AccStatic);
          addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormDataPropertyKey(fieldSourceBuilder), fieldSourceBuilder);

          // add interfaces specified on the formdata annotation
          DtoUtility.addFormDataAdditionalInterfaces(fieldAnnotation, fieldSourceBuilder, getDerivedCompilationUnit().getJavaProject());

          // getter for field
          String methodName = NamingUtility.ensureStartWithUpperCase(formDataTypeName); // Scout RT requires the first char to be upper-case for a getter. See org.eclipse.scout.commons.beans.FastBeanUtility.BEAN_METHOD_PAT.
          IMethodSourceBuilder getterBuilder = new MethodSourceBuilder("get" + methodName);
          getterBuilder.setFlags(Flags.AccPublic);
          getterBuilder.setReturnTypeSignature(Signature.createTypeSignature(formDataTypeName, false));
          getterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return getFieldByClass(" + formDataTypeName + ".class);"));
          addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(getterBuilder), getterBuilder);
        }
        else if (FormDataAnnotation.isIgnore(fieldAnnotation)) {
          continue;
        }

        if (declaringTypeHierarchy.isSubtype(TypeUtility.getType(IRuntimeClasses.ICompositeField), formField) && !fieldExtendsTemplateField) {
          createCompositeFieldFormData(formField, declaringTypeHierarchy, monitor);
        }
      }
    }
  }
}
