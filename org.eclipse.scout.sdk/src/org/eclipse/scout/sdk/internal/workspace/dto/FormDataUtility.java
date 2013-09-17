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
package org.eclipse.scout.sdk.internal.workspace.dto;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.CompositeFormDataTypeSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.TableFieldBeanFormDataSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.TableFieldFormDataSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.pagedata.PageDataSourceBuilder;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link FormDataUtility}</h3>
 * 
 * @author aho
 * @since 3.10.0 27.08.2013
 */
public final class FormDataUtility {

  private FormDataUtility() {
  }

  public static ITypeSourceBuilder createFormDataSourceBuilder(IType modelType, FormDataAnnotation formDataAnnotation) {
    String superTypeSignature = formDataAnnotation.getSuperTypeSignature();
    if (StringUtility.hasText(superTypeSignature)) {
      IType superType = TypeUtility.getTypeBySignature(superTypeSignature);
      String typeErasure = Signature.getTypeErasure(superTypeSignature);
      ITypeHierarchy superTypeHierarchy = TypeUtility.getSuperTypeHierarchy(superType);
      String formDataTypeSignature = formDataAnnotation.getFormDataTypeSignature();
      String formDataTypeName = Signature.getSignatureSimpleName(formDataTypeSignature);
      ITypeSourceBuilder formDataSourceBuilder = null;
      if (SignatureUtility.isEqualSignature(typeErasure, Signature.createTypeSignature(RuntimeClasses.AbstractTableFieldData, true))) {
        formDataSourceBuilder = new TableFieldFormDataSourceBuilder(modelType, formDataTypeName, formDataAnnotation);
      }
      else if (superTypeHierarchy != null && superTypeHierarchy.contains(TypeUtility.getType(RuntimeClasses.AbstractTableFieldBeanData))) {
        // fill table bean
        formDataSourceBuilder = new TableFieldBeanFormDataSourceBuilder(modelType, formDataTypeName, formDataAnnotation);
      }
      else {
        formDataSourceBuilder = new CompositeFormDataTypeSourceBuilder(modelType, formDataTypeName, formDataAnnotation);
      }
      formDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("<b>NOTE:</b><br>This class is auto generated, no manual modifications recommended.\n\n@generated"));
      return formDataSourceBuilder;
    }
    return null;
  }

  public static ITypeSourceBuilder createPageDataSourceBuilder(IType modelType, PageDataAnnotation pageDataAnnotation) {
    String pageDataSignature = pageDataAnnotation.getPageDataTypeSignature();
    ITypeSourceBuilder pageDataSourceBuilder = new PageDataSourceBuilder(modelType, Signature.getSignatureSimpleName(pageDataSignature), pageDataAnnotation);
    pageDataSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("<b>NOTE:</b><br>This class is auto generated, no manual modifications recommended.\n\n@generated"));
    return pageDataSourceBuilder;
  }

  public static String getFormDataName(String typeName) {
    String formDataName = typeName;
    if (typeName.endsWith("Field")) {
      formDataName = typeName.replaceAll("Field$", "");
    }
    else if (typeName.endsWith("Button")) {
      formDataName = typeName.replaceAll("Button$", "");
    }
    else if (typeName.endsWith("Column")) {
      formDataName = typeName.replaceAll("Column$", "");
    }
    String resultName = formDataName;
//    int i = 0;
//    if (operation != null) {
//      while (operation.getTypeNewOperation(resultName) != null) {
//        resultName = formDataName + i;
//        i++;
//      }
//    }
    return resultName;
  }

  public static IType findTable(IType tableOwner, ITypeHierarchy hierarchy) {

    if (TypeUtility.exists(tableOwner)) {
      if (hierarchy == null) {
        hierarchy = TypeUtility.getLocalTypeHierarchy(tableOwner);
      }
      IType[] tables = TypeUtility.getInnerTypes(tableOwner, TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.ITable), hierarchy), null);
      if (tables.length > 0) {
        if (tables.length > 1) {
          ScoutSdk.logWarning("table field '" + tableOwner.getFullyQualifiedName() + "' contains more than one table! Taking first for dto creation.");
        }
        return tables[0];
      }
      else {
        IType superclass = hierarchy.getSuperclass(tableOwner);

        return findTable(superclass, null);
      }
    }
    return null;
  }

  public static String computeSuperTypeSignatureForFormData(IType formField, FormDataAnnotation formDataAnnotation, ITypeHierarchy localHierarchy) {
    String superTypeSignature = formDataAnnotation.getSuperTypeSignature();
    if (formDataAnnotation.getGenericOrdinal() >= 0) {
      IType superType = TypeUtility.getTypeBySignature(superTypeSignature);
      if (TypeUtility.isGenericType(superType)) {
        try {
          String genericTypeSig = ScoutTypeUtility.computeFormFieldGenericType(formField, localHierarchy);
          if (genericTypeSig != null) {
            superTypeSignature = superTypeSignature.replaceAll("\\;$", "<" + genericTypeSig + ">;");
          }
        }
        catch (CoreException e) {
          ScoutSdk.logError("could not find generic type for form data of type '" + formField.getFullyQualifiedName() + "'.");
        }
      }

    }
    return superTypeSignature;
  }

}
