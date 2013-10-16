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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractDtoTypeSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataUtility;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.validationrule.ValidationRuleMethod;

/**
 * <h3>{@link FormDataTypeSourceBuilder}</h3>
 * 
 * @author aho
 * @since 3.10.0 27.08.2013
 */
public class FormDataTypeSourceBuilder extends AbstractDtoTypeSourceBuilder {

  private final static Pattern REGEX_STRING_LITERALS = Pattern.compile("\"+[^\"]+\"", Pattern.DOTALL);
  private final IType iFormField = TypeUtility.getType(RuntimeClasses.IFormField);

  private FormDataAnnotation m_formDataAnnotation;

  /**
   * @param modelType
   * @param elementName
   */
  public FormDataTypeSourceBuilder(IType modelType, String elementName, FormDataAnnotation formDataAnnotation) {
    super(modelType, elementName, false);
    m_formDataAnnotation = formDataAnnotation;
    setup();
  }

  @Override
  protected void createContent() {
    super.createContent();
    collectProperties();
    try {
      collectValidationRules();
    }
    catch (Throwable t) {
      ScoutSdk.logError("could not append validation rules to form field data '" + getModelType().getFullyQualifiedName() + "'.", t);
    }
  }

  @Override
  protected String computeSuperTypeSignature() throws JavaModelException {
    String superTypeSignature = null;
    if (ScoutTypeUtility.existsReplaceAnnotation(getModelType())) {
      IType replacedType = getLocalTypeHierarchy().getSuperclass(getModelType());
      IType replacedFormFieldDataType = ScoutTypeUtility.getFormDataType(replacedType, getLocalTypeHierarchy());
      if (replacedFormFieldDataType != null) {
        superTypeSignature = SignatureCache.createTypeSignature(replacedFormFieldDataType.getFullyQualifiedName());
      }
      addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createReplaceAnnotationBuilder());
    }
    if (superTypeSignature == null) {
      superTypeSignature = FormDataUtility.computeSuperTypeSignatureForFormData(getModelType(), getFormDataAnnotation(), getLocalTypeHierarchy());

    }
    return superTypeSignature;
  }

  protected void collectValidationRules() throws CoreException {
    // validation rules
    boolean replaceAnnotationPresent = ScoutTypeUtility.existsReplaceAnnotation(getModelType());

    ITypeHierarchy hierarchy = getLocalTypeHierarchy();
    if (hierarchy == null) {
      hierarchy = TypeUtility.getSuperTypeHierarchy(getModelType());
      if (hierarchy == null) {
        ScoutSdk.logError("Cannot collect validation rules for form data. Unable to create super type hierarchy for type '" + getModelType().getFullyQualifiedName() + "'.");
        return;
      }
    }

    // If the replace annotation is available, we have to check whether the replaced field is not associated to a form field data
    boolean superTypeHasNoFormFieldData = false;
    if (replaceAnnotationPresent) {
      IType superType = hierarchy.getSuperclass(getModelType());
      superTypeHasNoFormFieldData = ScoutTypeUtility.getFormDataType(superType, hierarchy) == null;
    }

    final List<ValidationRuleMethod> list = FormDataUtility.getValidationRuleMethods(getModelType(), hierarchy.getJdtHierarchy());

    if (list.size() > 0) {
      for (Iterator<ValidationRuleMethod> it = list.iterator(); it.hasNext();) {
        ValidationRuleMethod vm = it.next();
        if (replaceAnnotationPresent) {
          if (superTypeHasNoFormFieldData && vm.isSkipRule()) {
            it.remove();
            continue;
          }
          else if (!superTypeHasNoFormFieldData && !getModelType().equals(vm.getImplementedMethod().getDeclaringType())) {
            // remove all validation rules that are not overridden by the replacement class
            it.remove();
            continue;
          }
        }
        else if (vm.isSkipRule()) {
          // class does not replace its super class. Hence remove all skipped validation rules
          it.remove();
          continue;
        }

        // skip validation rules having return value null or false
        String simpleSourceCode = vm.getRuleReturnExpression().getReturnStatement(); // pre calculate a return statement without import validation. Is sufficient for the check that is done here.
        if ("null".equals(simpleSourceCode) || "false".equals(simpleSourceCode)) {
          if (replaceAnnotationPresent && !superTypeHasNoFormFieldData) {
            // replace annotation is present and super type has already a form field data. Hence rule must be overridden in the generated method.
            vm.setSkipRule(true);
          }
          else {
            // no replace annotation is present. Hence just ignore the rule.
            it.remove();
          }
          continue;
        }
      }

      if (list.size() > 0) {
        IMethodSourceBuilder initValidationRulesBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "initValidationRules");
        initValidationRulesBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
          @Override
          public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
            source.append("super.initValidationRules(ruleMap);");
            for (ValidationRuleMethod vm : list) {
              try {
                // filter out code that contains references to types that are not accessible from our bundle.
                String generatedSourceCode = generateValidationRuleSourceCode(vm, validator);

                if (!vm.isSkipRule() && (generatedSourceCode == null || containsBrackets(generatedSourceCode))) {
                  //add javadoc warning
                  String fqn = vm.getImplementedMethod().getDeclaringType().getFullyQualifiedName('.') + " # " + vm.getImplementedMethod().getElementName();
                  source.append("/**");
                  source.append(lineDelimiter);
                  source.append(" * XXX not processed ValidationRule(" + vm.getRuleName() + ")");
                  if (vm.getRuleReturnExpression() != null) {
                    source.append(lineDelimiter);
                    source.append(" * '" + vm.getRuleReturnExpression() + "' is not accessible from here.");
                  }
                  source.append(lineDelimiter);
                  source.append(" * generatedSourceCode: ");
                  source.append(generatedSourceCode);
                  source.append(lineDelimiter);
                  source.append(" * at " + fqn);
                  source.append(lineDelimiter);
                  source.append("*/");
                }
                else {

                  String ruleDecl;
                  if (vm.getRuleField() != null) {
                    validator.getTypeName(SignatureCache.createTypeSignature(vm.getRuleField().getDeclaringType().getFullyQualifiedName())); // add to imports if necessary
                    ruleDecl = vm.getRuleField().getDeclaringType().getElementName() + "." + vm.getRuleField().getElementName();
                  }
                  else {
                    ruleDecl = "\"" + vm.getRuleName() + "\"";
                  }

                  source.append(lineDelimiter);

                  if (vm.isSkipRule()) {
                    source.append("ruleMap.remove(");
                    source.append(ruleDecl);
                    source.append(");");
                  }
                  else {
                    source.append("ruleMap.put(");
                    source.append(ruleDecl);
                    source.append(", ");
                    source.append(generatedSourceCode);
                    source.append(");");
                  }
                }
              }
              catch (Exception e) {
                String fqn = vm.getImplementedMethod().getDeclaringType().getFullyQualifiedName() + "#" + vm.getImplementedMethod().getElementName();
                ScoutSdk.logError("could not append rule " + vm.getRuleName() + " from method " + fqn + ".", e);
              }
            }
          }

          private String generateValidationRuleSourceCode(ValidationRuleMethod vrm, IImportValidator validator) throws CoreException {
            if (vrm.getRuleReturnExpression() == null) {
              return null;
            }
            String sourceSnippet = vrm.getRuleReturnExpression().getReturnStatement(validator);
            if (sourceSnippet != null) {
              Collection<IType> referencedTypes = vrm.getRuleReturnExpression().getReferencedTypes().values();
              for (IType refType : referencedTypes) {
                //if the type is a form field type it is transformed to the corresponding form data field
                ITypeHierarchy h = TypeUtility.getSuperTypeHierarchy(refType);
                if (h.contains(iFormField)) {
                  String formDataFieldName = ScoutUtility.ensureStartWithUpperCase(ScoutUtility.removeFieldSuffix(refType.getElementName()));
                  return formDataFieldName + ".class";
                }

                // check if the referenced type is accessible from the bundle that contains the form data
                IMethod implementedMethod = vrm.getImplementedMethod();
                if (TypeUtility.exists(implementedMethod) && !TypeUtility.isOnClasspath(refType, implementedMethod.getJavaProject())) {
                  // the referenced type is not accessible -> remove the rule
                  return null;
                }
              }

              // if we come here all referenced types are valid and accessible -> add them to the imports
              for (IType refType : referencedTypes) {
                validator.getTypeName(SignatureCache.createTypeSignature(refType.getFullyQualifiedName())); // add to imports if necessary
              }
            }
            return sourceSnippet;
          }

          private boolean containsBrackets(String genSource) {
            if (genSource == null) {
              return false;
            }
            String srcWithoutStrings = REGEX_STRING_LITERALS.matcher(genSource).replaceAll("");
            return srcWithoutStrings.contains("(");
          }
        });

        initValidationRulesBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("list of derived validation rules."));
        addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormDataValidationKey(initValidationRulesBuilder), initValidationRulesBuilder);
      }
    }
  }

  public FormDataAnnotation getFormDataAnnotation() {
    return m_formDataAnnotation;
  }

}
