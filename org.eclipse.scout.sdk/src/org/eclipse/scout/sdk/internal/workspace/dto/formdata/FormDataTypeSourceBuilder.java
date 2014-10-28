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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractDtoTypeSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.DtoUtility;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.method.IMethodReturnExpressionRewrite;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.validationrule.ValidationRuleMethod;

/**
 * <h3>{@link FormDataTypeSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public class FormDataTypeSourceBuilder extends AbstractDtoTypeSourceBuilder {

  private static final Pattern REGEX_STRING_LITERALS = Pattern.compile("\"+[^\"]+\"", Pattern.DOTALL);
  private static final Pattern REGEX_CONSTRUCTOR_CALL = Pattern.compile("new\\s+[A-Za-z][a-zA-Z0-9_\\.]{0,200}\\s*\\([^\\(\\)]*\\)");

  private FormDataAnnotation m_formDataAnnotation;

  /**
   * @param modelType
   * @param elementName
   */
  public FormDataTypeSourceBuilder(IType modelType, String elementName, FormDataAnnotation formDataAnnotation, ICompilationUnit derivedCu, IProgressMonitor monitor) {
    super(modelType, elementName, false, derivedCu, monitor);
    m_formDataAnnotation = formDataAnnotation;
    setup(monitor);
  }

  @Override
  protected void createContent(IProgressMonitor monitor) {
    super.createContent(monitor);
    if (monitor.isCanceled()) {
      return;
    }

    collectProperties(monitor);
    if (monitor.isCanceled()) {
      return;
    }

    try {
      collectValidationRules(monitor);
      if (monitor.isCanceled()) {
        return;
      }
    }
    catch (Exception t) {
      ScoutSdk.logError("could not append validation rules to form field data '" + getModelType().getFullyQualifiedName() + "'.", t);
    }
  }

  @Override
  protected String computeSuperTypeSignature() throws JavaModelException {
    String superTypeSignature = null;
    if (ScoutTypeUtility.existsReplaceAnnotation(getModelType())) {
      IType replacedType = getLocalTypeHierarchy().getSuperclass(getModelType());
      IType replacedFormFieldDataType = DtoUtility.getFormDataType(replacedType, getLocalTypeHierarchy());
      if (replacedFormFieldDataType != null) {
        superTypeSignature = SignatureCache.createTypeSignature(replacedFormFieldDataType.getFullyQualifiedName());
      }
      addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createReplaceAnnotationBuilder());
    }
    if (superTypeSignature == null) {
      superTypeSignature = DtoUtility.computeSuperTypeSignatureForFormData(getModelType(), getFormDataAnnotation(), getLocalTypeHierarchy());
    }
    return superTypeSignature;
  }

  protected void collectValidationRules(IProgressMonitor monitor) throws CoreException {
    // validation rules
    boolean replaceAnnotationPresent = ScoutTypeUtility.existsReplaceAnnotation(getModelType());

    ITypeHierarchy hierarchy = getLocalTypeHierarchy();
    if (hierarchy == null) {
      hierarchy = TypeUtility.getSupertypeHierarchy(getModelType());
      if (hierarchy == null) {
        ScoutSdk.logError("Cannot collect validation rules for form data. Unable to create super type hierarchy for type '" + getModelType().getFullyQualifiedName() + "'.");
        return;
      }
    }

    // If the replace annotation is available, we have to check whether the replaced field is not associated to a form field data
    boolean superTypeHasNoFormFieldData = false;
    if (replaceAnnotationPresent) {
      IType superType = hierarchy.getSuperclass(getModelType());
      superTypeHasNoFormFieldData = DtoUtility.getFormDataType(superType, hierarchy) == null;
    }

    final List<ValidationRuleMethod> list = DtoUtility.getValidationRuleMethods(getModelType(), hierarchy, monitor);
    if (monitor.isCanceled()) {
      return;
    }

    if (list.size() > 0) {
      for (Iterator<ValidationRuleMethod> it = list.iterator(); it.hasNext();) {
        if (monitor.isCanceled()) {
          return;
        }

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
                String generatedSourceCode = generateValidationRuleSourceCode(vm, ownerProject, validator);

                if (!vm.isSkipRule() && generatedSourceCode == null) {
                  //add javadoc warning
                  String fqn = vm.getImplementedMethod().getDeclaringType().getFullyQualifiedName('.') + "#" + vm.getImplementedMethod().getElementName();
                  source.append("/**");
                  source.append(lineDelimiter);
                  source.append(" * XXX not processed ValidationRule(" + vm.getRuleName() + ")");
                  if (vm.getRuleReturnExpression() != null) {
                    source.append(lineDelimiter);
                    source.append(" * '" + vm.getRuleReturnExpression() + "' is not accessible from here.");
                  }
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

          private String generateValidationRuleSourceCode(ValidationRuleMethod vrm, IJavaProject formDataProject, IImportValidator validator) throws CoreException {
            if (vrm.getRuleReturnExpression() == null) {
              return null;
            }

            final BooleanHolder rewritePossible = new BooleanHolder(true);
            final Map<SimpleName, IJavaElement> referencedElements = vrm.getRuleReturnExpression().getReferencedElements();
            String sourceSnippet = vrm.getRuleReturnExpression().getReturnStatement(validator, formDataProject, new IMethodReturnExpressionRewrite() {
              @Override
              public boolean rewriteElement(SimpleName node, IJavaElement element, IImportValidator v, IJavaProject classPath, StringBuffer buffer) {
                if (element.getElementType() == IJavaElement.TYPE) {
                  if (node.getParent() instanceof QualifiedName) {
                    QualifiedName parent = (QualifiedName) node.getParent();
                    if (node == parent.getQualifier()) {
                      if (referencedElements.containsKey(parent.getName())) {
                        // type can be ignored because we will handle the second part of the qualified name
                        return true;
                      }
                    }
                    if (parent.getParent() instanceof QualifiedName) {
                      // we are not the leaf -> ignore
                      return true;
                    }
                  }

                  IType type = (IType) element;
                  ITypeHierarchy h = TypeUtility.getSupertypeHierarchy(type);
                  if (h.contains(TypeUtility.getType(IRuntimeClasses.IValueField))) {
                    String formDataFieldName = NamingUtility.ensureStartWithUpperCase(ScoutUtility.removeFieldSuffix(type.getElementName()));
                    buffer.append(formDataFieldName);
                    return true; // rewrite done
                  }
                }

                rewritePossible.setValue(false);
                return false;
              }
            });

            if (!rewritePossible.getValue()) {
              // we could not rewrite the statement so that all references could be resolved in the context of the form data
              return null;
            }

            if (sourceSnippet != null) {
              // it is no reference to another form field and all types are accessible: check for foreign method calls
              if (!REGEX_CONSTRUCTOR_CALL.matcher(sourceSnippet).matches()) { // constructors are allowed because the type is accessible (checked before)
                String srcWithoutStrings = REGEX_STRING_LITERALS.matcher(sourceSnippet).replaceAll(""); // remove string literals
                if (srcWithoutStrings.contains("(")) {
                  // if it still contains a bracket: skip the rule
                  return null;
                }
              }
            }
            return sourceSnippet;
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
