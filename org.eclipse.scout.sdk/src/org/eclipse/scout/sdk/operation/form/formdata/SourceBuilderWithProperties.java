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
package org.eclipse.scout.sdk.operation.form.formdata;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.PropertyBeanComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutPropertyBeanFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.validationrule.ValidationRuleMethod;

/**
 * <h3>{@link SourceBuilderWithProperties}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 21.02.2011
 */
public class SourceBuilderWithProperties extends TypeSourceBuilder {

  private static Pattern REGEX_STRING_LITERALS = Pattern.compile("\"+[^\"]+\"", Pattern.DOTALL);

  public SourceBuilderWithProperties(final IType type) {
    super(ResourceUtility.getLineSeparator(type.getOpenable()));
    visitProperties(type);
    addValidationRules(type);
  }

  protected void visitProperties(IType type) {
    IPropertyBean[] beanPropertyDescriptors = TypeUtility.getPropertyBeans(type, ScoutPropertyBeanFilters.getFormDataPropertyFilter(), PropertyBeanComparators.getNameComparator());
    if (beanPropertyDescriptors != null) {
      for (IPropertyBean desc : beanPropertyDescriptors) {
        try {
          if (desc.getReadMethod() != null || desc.getWriteMethod() != null) {
            if (FormDataAnnotation.isCreate(ScoutTypeUtility.findFormDataAnnotation(desc.getReadMethod())) &&
                FormDataAnnotation.isCreate(ScoutTypeUtility.findFormDataAnnotation(desc.getWriteMethod()))) {
              String beanName = FormDataUtility.getValidMethodParameterName(desc.getBeanName());
              String lowerCaseBeanName = FormDataUtility.getBeanName(beanName, false);
              String upperCaseBeanName = FormDataUtility.getBeanName(beanName, true);

              String propName = upperCaseBeanName + "Property";
              String resolvedSignature = SignatureUtility.getResolvedSignature(desc.getBeanSignature(), desc.getDeclaringType());
              String unboxedSignature = FormDataUtility.unboxPrimitiveSignature(resolvedSignature);
              // property class
              TypeSourceBuilder propertyBuilder = new TypeSourceBuilder(NL);
              propertyBuilder.setElementName(propName);

              String superTypeSig = Signature.createTypeSignature(RuntimeClasses.AbstractPropertyData, true);
              superTypeSig = superTypeSig.replaceAll("\\;$", "<" + unboxedSignature + ">;");
              propertyBuilder.setSuperTypeSignature(superTypeSig);
              addBuilder(propertyBuilder, CATEGORY_TYPE_PROPERTY);
              // getter
              MethodSourceBuilder getterBuilder = new MethodSourceBuilder(NL);
              getterBuilder.setElementName("get" + propName);
              getterBuilder.setReturnSignature(Signature.createTypeSignature(propName, false));
              getterBuilder.setSimpleBody("return getPropertyByClass(" + propName + ".class);");
              addBuilder(getterBuilder, new CompositeObject(CATEGORY_METHOD_PROPERTY, lowerCaseBeanName, 1, getterBuilder));

              // legacy getter
              MethodSourceBuilder legacyGetter = new MethodSourceBuilder(NL);
              legacyGetter.setJavaDoc(" /** " + NL + "   * access method for property " + upperCaseBeanName + "." + NL + "*/");
              legacyGetter.setElementName((Signature.SIG_BOOLEAN.equals(resolvedSignature) ? "is" : "get") + upperCaseBeanName);
              legacyGetter.setReturnSignature(resolvedSignature);
              legacyGetter.setSimpleBody(getLegacyGetterMethodBody(resolvedSignature, propName));
              addBuilder(legacyGetter, new CompositeObject(CATEGORY_METHOD_PROPERTY, lowerCaseBeanName, 2, legacyGetter));

              // legacy setter
              MethodSourceBuilder legacySetter = new MethodSourceBuilder(NL);
              legacySetter.setJavaDoc(" /** " + NL + "   * access method for property " + upperCaseBeanName + "." + NL + "*/");
              legacySetter.setElementName("set" + upperCaseBeanName);
              legacySetter.addParameter(new MethodParameter(resolvedSignature, lowerCaseBeanName));
              legacySetter.setSimpleBody("get" + propName + "().setValue(" + lowerCaseBeanName + ");");
              addBuilder(legacySetter, new CompositeObject(CATEGORY_METHOD_PROPERTY, lowerCaseBeanName, 3, legacySetter));
            }
          }
        }
        catch (JavaModelException e) {
          ScoutSdk.logError("could append property to form data '" + getElementName() + "'.", e);
        }
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

  protected void addValidationRules(IType type) {
    // validation rules
    try {
      List<ValidationRuleMethod> list = ScoutTypeUtility.getValidationRuleMethods(type);
      if (list.size() > 0) {
        for (Iterator<ValidationRuleMethod> it = list.iterator(); it.hasNext();) {
          ValidationRuleMethod vm = it.next();
          String generatedSourceCode = vm.getRuleGeneratedSourceCode();
          if (generatedSourceCode != null) {
            if (generatedSourceCode.equals("null")) {
              it.remove();
              continue;
            }
            if (generatedSourceCode.equals("false")) {
              it.remove();
              continue;
            }
          }
        }
        if (list.size() > 0) {
          ValidationRuleMethodOverrideBuilder builder = new ValidationRuleMethodOverrideBuilder(list, NL);
          builder.setJavaDoc(" /** " + NL + "   * list of derived validation rules." + NL + "*/");
          builder.addAnnotation(new AnnotationSourceBuilder("Ljava.lang.Override;"));
          builder.setFlags(Flags.AccProtected);
          builder.setElementName("initValidationRules");
          builder.addParameter(new MethodParameter(Signature.createTypeSignature("java.util.Map<String,Object>", false), "ruleMap"));
          addBuilder(builder, 4);
        }
      }
    }
    catch (Throwable t) {
      ScoutSdk.logError("could not append validation rules to form field data '" + type.getFullyQualifiedName() + "'.", t);
    }
  }

  private static boolean containsBrackets(String genSource) {
    String srcWithoutStrings = REGEX_STRING_LITERALS.matcher(genSource).replaceAll("");
    return srcWithoutStrings != null && srcWithoutStrings.contains("(");
  }

  private static class ValidationRuleMethodOverrideBuilder extends MethodSourceBuilder {
    private final List<ValidationRuleMethod> m_methods;

    public ValidationRuleMethodOverrideBuilder(List<ValidationRuleMethod> methods, String nl) {
      super(nl);
      m_methods = methods;
    }

    @Override
    protected String createMethodBody(IImportValidator validator) {
      //validator.addImport("java.util.Map");
      StringBuilder buf = new StringBuilder();
      buf.append("super.initValidationRules(ruleMap);");
      for (ValidationRuleMethod vm : m_methods) {
        try {
          String generatedSourceCode = vm.getRuleGeneratedSourceCode();
          //filter
          generatedSourceCode = filterGeneratedSourceCode(vm.getImplementedMethod(), generatedSourceCode, validator);
          if (generatedSourceCode == null || containsBrackets(generatedSourceCode)) {
            //add javadoc warning
            String fqn = vm.getImplementedMethod().getDeclaringType().getFullyQualifiedName('.') + " # " + vm.getImplementedMethod().getElementName();
            buf.append("/**");
            buf.append(NL);
            buf.append(" * XXX not processed ValidationRule(" + vm.getRuleName() + ")");
            buf.append(NL);
            buf.append(" * generatedSourceCode: ");
            buf.append(generatedSourceCode);
            buf.append(NL);
            buf.append(" * at " + fqn);
            buf.append(NL);
            buf.append("*/");
            continue;
          }
          //
          String ruleDecl;
          if (vm.getRuleField() != null) {
            validator.addImport(vm.getRuleField().getDeclaringType().getFullyQualifiedName());
            ruleDecl = vm.getRuleField().getDeclaringType().getElementName() + "." + vm.getRuleField().getElementName();
          }
          else {
            ruleDecl = "\"" + vm.getRuleName() + "\"";
          }
          //
          buf.append(NL);
          buf.append("ruleMap.put(");
          buf.append(ruleDecl);
          buf.append(", ");
          buf.append(generatedSourceCode);
          buf.append(");");
        }
        catch (Exception e) {
          String fqn = vm.getImplementedMethod().getDeclaringType().getFullyQualifiedName() + "#" + vm.getImplementedMethod().getElementName();
          ScoutSdk.logError("could not append rule " + vm.getRuleName() + " from method " + fqn + ".", e);
        }
      }
      return buf.toString();
    }

    private String filterGeneratedSourceCode(IMethod sourceMethod, String sourceSnippet, IImportValidator targetValidator) throws CoreException {
      if (sourceSnippet != null) {
        IType[] refTypes = ScoutTypeUtility.getTypeOccurenceInSnippet(sourceMethod, sourceSnippet);
        for (IType refType : refTypes) {
          //if the type is a form field type it is transformed to the corresponding form data field
          ITypeHierarchy h = TypeUtility.getSuperTypeHierarchy(refType);
          if (h.contains(TypeUtility.getType(RuntimeClasses.IFormField))) {
            String formDataFieldName = FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(refType.getElementName()), true);
            return formDataFieldName + ".class";
          }
          //other client types are not supported
          String fqn = refType.getFullyQualifiedName();
          //XXX imo: aho, is there a better way to find out if targetValidator would accept that type in the import section?
          //XXX aho: yes, follows soon: if(TypeUtility.isOnClasspath(refType, targetValidator.getTargetProject())){
          if (fqn.indexOf(".client.") >= 0) {
            return null;
          }
          targetValidator.addImport(fqn);
        }
      }
      return sourceSnippet;
    }
  }
}
