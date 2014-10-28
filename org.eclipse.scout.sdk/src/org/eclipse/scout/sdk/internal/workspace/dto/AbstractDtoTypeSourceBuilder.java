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

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.PropertyBeanComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutPropertyBeanFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractDtoTypeSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public abstract class AbstractDtoTypeSourceBuilder extends TypeSourceBuilder {

  private IType m_modelType;
  private ITypeHierarchy m_localTypeHierarchy;
  private final ICompilationUnit m_derivedCu;
  protected static final String SIG_FOR_IS_METHOD_NAME = Signature.SIG_BOOLEAN;

  public AbstractDtoTypeSourceBuilder(IType modelType, String elementName, ICompilationUnit derivedCu, IProgressMonitor monitor) {
    this(modelType, elementName, true, derivedCu, monitor);
  }

  /**
   * @param elementName
   */
  public AbstractDtoTypeSourceBuilder(IType modelType, String elementName, boolean setup, ICompilationUnit derivedCu, IProgressMonitor monitor) {
    super(elementName);
    m_modelType = modelType;
    m_derivedCu = derivedCu;
    m_localTypeHierarchy = TypeUtility.getLocalTypeHierarchy(modelType);
    if (setup) {
      setup(monitor);
    }
  }

  protected void setup(IProgressMonitor monitor) {
    setupBuilder();
    createContent(monitor);
  }

  /**
   *
   */
  protected void setupBuilder() {
    // flags
    int flags = Flags.AccPublic;
    try {
      if (Flags.isAbstract(getModelType().getFlags())) {
        flags |= Flags.AccAbstract;
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logWarning("could not determ abstract flag of '" + getModelType().getFullyQualifiedName() + "'.", e);
    }
    setFlags(flags);
    try {
      setSuperTypeSignature(computeSuperTypeSignature());
    }
    catch (CoreException e) {
      ScoutSdk.logError("could not calculate super type for '" + getElementName() + "'.", e);
    }
  }

  protected void createContent(IProgressMonitor monitor) {
    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(getElementName());
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);

    // serial version uid
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);

    // copy annotations over to the DTO
    copyAnnotations(getModelType(), getModelType(), this);
  }

  /**
   * @return
   */
  protected abstract String computeSuperTypeSignature() throws CoreException;

  protected static void copyAnnotations(IAnnotatable annotationOwner, final IType declaringType, ITypeSourceBuilder sourceBuilder) {
    IAnnotation[] annotations;
    try {
      annotations = annotationOwner.getAnnotations();
    }
    catch (JavaModelException e1) {
      ScoutSdk.logError("Unable to retrieve annotations of element '" + annotationOwner.toString() + "'.", e1);
      return;
    }

    for (IAnnotation a : annotations) {
      try {
        final IAnnotation annotation = a;
        String elementName = annotation.getElementName();

        if (!IRuntimeClasses.FormData.equals(elementName) && !IRuntimeClasses.Order.equals(elementName) && !IRuntimeClasses.PageData.equals(elementName)
            && !Signature.getSimpleName(IRuntimeClasses.FormData).equals(elementName) && !Signature.getSimpleName(IRuntimeClasses.Order).equals(elementName)
            && !Signature.getSimpleName(IRuntimeClasses.PageData).equals(elementName)) {
          if (!NamingUtility.isFullyQualifiedName(elementName)) {
            elementName = TypeUtility.getReferencedTypeFqn(declaringType, elementName, true);
          }

          final IType annotationDeclarationType = TypeUtility.getType(elementName);
          if (isAnnotationDtoRelevant(annotationDeclarationType)) {
            AnnotationSourceBuilder asb = new AnnotationSourceBuilder(SignatureCache.createTypeSignature(elementName)) {
              @Override
              public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
                // check if annotation is accessible
                if (!TypeUtility.isOnClasspath(annotationDeclarationType, ownerProject)) {
                  ScoutSdk.logInfo("DTO relevant annotation '" + annotationDeclarationType.getFullyQualifiedName() + "' is not accessible from DTO project '" + ownerProject.getElementName() + "'. Annotation will be skipped.");
                  return;
                }

                // check if there is source available
                String copySrc = annotation.getSource();

                if (!StringUtility.hasText(copySrc)) {
                  ScoutSdk.logInfo("DTO relevant annotation '" + annotationDeclarationType.getFullyQualifiedName() + "' in type '" + declaringType.getFullyQualifiedName() + "' has no source code. Annotation will be skipped.");
                  return;
                }

                // check if all referenced types are accessible
                List<IType> typeOccurenceInAnnotation = ScoutTypeUtility.getTypeOccurenceInAnnotation(annotation, declaringType);
                for (IType t : typeOccurenceInAnnotation) {
                  if (!TypeUtility.isOnClasspath(t, ownerProject)) {
                    ScoutSdk.logInfo("Type '" + t.getFullyQualifiedName() + "' referenced in DTO relevant annotation '" + annotationDeclarationType.getFullyQualifiedName() + "' in type '" + declaringType.getFullyQualifiedName() + "' is not accessible from DTO project '" + ownerProject.getElementName() + "'. Annotation will be skipped.");
                    return;
                  }
                }
                IType classIdType = TypeUtility.getType(IRuntimeClasses.ClassId);
                if (CompareUtility.equals(annotationDeclarationType, classIdType)) {
                  // classid append formdata
                  IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
                  if (memberValuePairs != null && memberValuePairs.length == 1 && "value".equals(memberValuePairs[0].getMemberName())) {
                    copySrc = copySrc.replace(memberValuePairs[0].getValue().toString(), memberValuePairs[0].getValue().toString() + "-formdata");
                  }
                }

                // add imports to referenced types
                for (IType t : typeOccurenceInAnnotation) {
                  validator.addImport(t.getFullyQualifiedName());
                  IType surroundingType = t.getDeclaringType();
                  while (surroundingType != null) {
                    validator.addImport(surroundingType.getFullyQualifiedName());
                    surroundingType = surroundingType.getDeclaringType();
                  }
                }

                // add annotation source
                source.append(copySrc);
              }
            };
            sourceBuilder.addAnnotationSourceBuilder(asb);
          }
        }
      }
      catch (JavaModelException e) {
        ScoutSdk.logError("Error copying the annotation '" + a.getElementName() + "' in type '" + declaringType.getFullyQualifiedName() + "' to the form data.", e);
      }
    }
  }

  protected static boolean isAnnotationDtoRelevant(IType annotation) {
    if (!TypeUtility.exists(annotation)) {
      return false;
    }
    IAnnotation dtoReleventAnnotation = JdtUtility.getAnnotation(annotation, IRuntimeClasses.DtoRelevant);
    return TypeUtility.exists(dtoReleventAnnotation);
  }

  public IType getModelType() {
    return m_modelType;
  }

  public ITypeHierarchy getLocalTypeHierarchy() {
    return m_localTypeHierarchy;
  }

  protected void collectProperties(IProgressMonitor monitor) {
    Set<? extends IPropertyBean> beanPropertyDescriptors = TypeUtility.getPropertyBeans(getModelType(), ScoutPropertyBeanFilters.getFormDataPropertyFilter(), PropertyBeanComparators.getNameComparator());
    for (IPropertyBean desc : beanPropertyDescriptors) {
      try {

        if (monitor.isCanceled()) {
          return;
        }

        if (desc.getReadMethod() != null || desc.getWriteMethod() != null) {
          if (FormDataAnnotation.isCreate(ScoutTypeUtility.findFormDataAnnotation(desc.getReadMethod()))
              && FormDataAnnotation.isCreate(ScoutTypeUtility.findFormDataAnnotation(desc.getWriteMethod()))) {
            String beanName = NamingUtility.ensureValidParameterName(desc.getBeanName());
            String lowerCaseBeanName = NamingUtility.ensureStartWithLowerCase(beanName);
            final String upperCaseBeanName = NamingUtility.ensureStartWithUpperCase(beanName);

            String propName = upperCaseBeanName + "Property";
            String resolvedSignature = SignatureUtility.getResolvedSignature(desc.getBeanSignature(), desc.getDeclaringType());
            if (!StringUtility.hasText(resolvedSignature)) {
              resolvedSignature = SignatureCache.createTypeSignature(Object.class.getName());
            }
            String unboxedSignature = SignatureUtility.unboxPrimitiveSignature(resolvedSignature);

            // property class
            TypeSourceBuilder propertyTypeBuilder = new TypeSourceBuilder(propName);
            propertyTypeBuilder.setFlags(Flags.AccPublic | Flags.AccStatic);
            String superTypeSig = SignatureCache.createTypeSignature(IRuntimeClasses.AbstractPropertyData);
            superTypeSig = DtoUtility.ENDING_SEMICOLON_PATTERN.matcher(superTypeSig).replaceAll(Signature.C_GENERIC_START + unboxedSignature + Signature.C_GENERIC_END + Signature.C_SEMICOLON);
            propertyTypeBuilder.setSuperTypeSignature(superTypeSig);
            IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
            propertyTypeBuilder.addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);
            IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(propName);
            propertyTypeBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
            addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormDataPropertyKey(propertyTypeBuilder), propertyTypeBuilder);

            // copy annotations over to the DTO
            IMethod propertyMethod = desc.getReadMethod();
            if (!TypeUtility.exists(propertyMethod)) {
              propertyMethod = desc.getWriteMethod();
            }
            if (TypeUtility.exists(propertyMethod)) {
              copyAnnotations(propertyMethod, propertyMethod.getDeclaringType(), propertyTypeBuilder);
            }

            // getter
            IMethodSourceBuilder propertyGetterBuilder = new MethodSourceBuilder("get" + propName);
            propertyGetterBuilder.setFlags(Flags.AccPublic);
            propertyGetterBuilder.setReturnTypeSignature(Signature.createTypeSignature(propName, false));
            propertyGetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return getPropertyByClass(" + propName + ".class);"));
            addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(propertyGetterBuilder), propertyGetterBuilder);

            // legacy getter
            IMethodSourceBuilder legacyPropertyGetterBuilder = new MethodSourceBuilder((SIG_FOR_IS_METHOD_NAME.equals(resolvedSignature) ? "is" : "get") + upperCaseBeanName);
            legacyPropertyGetterBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("access method for property " + upperCaseBeanName + "."));
            legacyPropertyGetterBuilder.setFlags(Flags.AccPublic);
            legacyPropertyGetterBuilder.setReturnTypeSignature(resolvedSignature);
            legacyPropertyGetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody(getLegacyGetterMethodBody(resolvedSignature, propName)));
            addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(legacyPropertyGetterBuilder), legacyPropertyGetterBuilder);

            // legacy setter
            IMethodSourceBuilder legacyPropertySetterBuilder = new MethodSourceBuilder("set" + upperCaseBeanName);
            legacyPropertySetterBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("access method for property " + upperCaseBeanName + "."));
            legacyPropertySetterBuilder.setFlags(Flags.AccPublic);
            legacyPropertySetterBuilder.setReturnTypeSignature(Signature.SIG_VOID);
            legacyPropertySetterBuilder.addParameter(new MethodParameter(lowerCaseBeanName, resolvedSignature));
            legacyPropertySetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("get" + propName + "().setValue(" + lowerCaseBeanName + ");"));
            addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(legacyPropertySetterBuilder), legacyPropertySetterBuilder);

          }
        }
      }
      catch (CoreException e) {
        ScoutSdk.logError("could append property to form data '" + getElementName() + "'.", e);
      }
    }
  }

  private String getLegacyGetterMethodBody(String propertySignature, String propertyName) {
    String nonArraySig = propertySignature;

    StringBuilder source = new StringBuilder();
    source.append("return ");
    if (Signature.SIG_BOOLEAN.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (false) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_BYTE.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_CHAR.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? ('\u0000') : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_DOUBLE.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0.0d) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_FLOAT.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0.0f) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_INT.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_LONG.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0L) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_SHORT.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0) : (get" + propertyName + "().getValue());");
    }
    else {
      source.append("get" + propertyName + "().getValue();");
    }
    return source.toString();
  }

  public ICompilationUnit getDerivedCompilationUnit() {
    return m_derivedCu;
  }
}
