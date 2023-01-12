/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.WrappedTrackedNodePosition;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link FormFieldProposal}</h3>
 *
 * @since 5.2.0
 */
public class FormFieldProposal extends AbstractTypeProposal {

  public FormFieldProposal(String name, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(name, relevance, imageId, cu, context);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException {
    var ast = factory.getAst();
    var parentTypes = AstUtils.getDeclaringTypes(factory.getType());
    var formType = parentTypes.getLast();

    // form field
    var formFieldType = createFormFieldType(superType);

    // import to not yet created form field type
    addFormFieldImport(parentTypes);

    var formFieldSimpleName = ast.newSimpleName(getProposalContext().getDefaultName() + getProposalContext().getSuffix());
    var formFieldGetterReturnType = AstUtils.getInnerTypeReturnType(formFieldSimpleName, getProposalContext().getDeclaringType());
    var scoutApi = factory.getScoutApi();

    var formFieldGetter = factory.newInnerTypeGetter()
        .withMethodNameToFindInnerType(scoutApi.IForm().getFieldByClassMethodName())
        .withName(getProposalContext().getDefaultName())
        .withReadOnlySuffix(getProposalContext().getSuffix())
        .withReturnType(formFieldGetterReturnType);

    var iExtensionSuperType = getIExtensionSuperType();
    if (iExtensionSuperType != null) {
      var getOwner = ast.newMethodInvocation();
      getOwner.setName(ast.newSimpleName(scoutApi.IExtension().getOwnerMethodName()));
      formFieldGetter.withMethodToFindInnerTypeExpression(getOwner);

      if (AstUtils.isInstanceOf(factory.getDeclaringTypeBinding(), scoutApi.IFormExtension().fqn())) {
        var typeArguments = iExtensionSuperType.getTypeArguments();
        if (typeArguments.length > 0) {
          var extendedForm = typeArguments[0];
          Collection<ITypeBinding> composites = new LinkedHashSet<>();
          collectCompositeTypes(extendedForm, composites);
          if (!composites.isEmpty()) {
            // add @Extends to FormFields in FormExtensions
            var extendsAnnotation = ast.newSingleMemberAnnotation();
            var extendsTypeName = factory.getImportRewrite().addImport(scoutApi.Extends().fqn(), factory.getContext());
            extendsAnnotation.setTypeName(ast.newSimpleName(extendsTypeName));

            var first = composites.iterator().next();
            var typeLiteral = ast.newTypeLiteral();
            var type = factory.newTypeReference(Bindings.getFullyQualifiedName(first));
            typeLiteral.setType(type);
            extendsAnnotation.setValue(typeLiteral);
            AstUtils.addAnnotationTo(extendsAnnotation, formFieldType);

            addLinkedPosition(factory.getRewrite().track(type), false, AstNodeFactory.EXTENDS_TYPE_GROUP);
            for (var composite : composites) {
              addLinkedPositionProposal(AstNodeFactory.EXTENDS_TYPE_GROUP, composite);
            }
          }
        }
      }
    }

    formFieldGetter.in(formType).insert();

    // specify the cursor position after form field creation
    List<BodyDeclaration> bodyDeclarations = formFieldType.bodyDeclarations();
    if (!bodyDeclarations.isEmpty()) {
      setEndPosition(getRewrite().track(bodyDeclarations.get(bodyDeclarations.size() - 1)));
    }
    else {
      setEndPosition(new WrappedTrackedNodePosition(getRewrite().track(formFieldType.getSuperclassType()), 2, 0));
    }
  }

  protected void collectCompositeTypes(ITypeBinding owner, Collection<ITypeBinding> collector) {
    if (owner == null) {
      return;
    }
    for (var innerType : owner.getDeclaredTypes()) {
      if (AstUtils.isInstanceOf(innerType, getFactory().getScoutApi().ICompositeField().fqn())) {
        collector.add(innerType);
        collectCompositeTypes(innerType, collector);
      }
    }
  }

  protected ITypeBinding getIExtensionSuperType() {
    var result = new ITypeBinding[1];
    AstUtils.visitHierarchy(getFactory().getDeclaringTypeBinding(), type -> {
      if (getFactory().getScoutApi().IExtension().fqn().equals(type.getErasure().getQualifiedName())) {
        result[0] = type;
      }
      return result[0] == null;
    });
    return result[0];
  }

  protected TypeDeclaration createFormFieldType(Type superType) {
    return getFactory().newType(getProposalContext().getDefaultName())
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withNlsMethod(getNlsMethodName())
        .withOrder(true)
        .withClassId(true)
        .withProposalBaseFqn(getProposalContext().getProposalInterfaceFqn())
        .withOrderDefinitionType(getFactory().getScoutApi().IFormField().fqn())
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .withSuperType(superType)
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert()
        .get();
  }

  protected String getNlsMethodName() {
    return getFactory().getScoutApi().AbstractFormField().getConfiguredLabelMethodName();
  }

  private void addFormFieldImport(Deque<TypeDeclaration> parentTypes) throws CoreException {
    var factory = getFactory();
    var fullyQualifiedName = AstUtils.getFullyQualifiedName(parentTypes, factory.getRoot(), JavaTypes.C_DOT);
    var formFieldTypeImport = factory.getAst().newImportDeclaration();
    formFieldTypeImport.setStatic(false);
    formFieldTypeImport.setOnDemand(false);
    var simpleImportName = factory.getAst().newSimpleName(getProposalContext().getDefaultName() + getProposalContext().getSuffix());
    var importName = factory.getAst().newQualifiedName(factory.getAst().newName(fullyQualifiedName), simpleImportName);
    formFieldTypeImport.setName(importName);

    // linked positions
    ITrackedNodePosition importPos = new WrappedTrackedNodePosition(getRewrite().track(simpleImportName), 0, -getProposalContext().getSuffix().length());
    addLinkedPosition(importPos, false, AstNodeFactory.TYPE_NAME_GROUP);

    factory.getImportsRewriteList().insertLast(formFieldTypeImport, null);
  }
}
