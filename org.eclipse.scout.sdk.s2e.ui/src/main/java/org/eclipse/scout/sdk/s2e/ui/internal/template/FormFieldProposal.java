/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.s2e.ui.internal.util.ast.AstInnerTypeGetterBuilder;
import org.eclipse.scout.sdk.s2e.ui.internal.util.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.ui.internal.util.ast.WrappedTrackedNodePosition;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;
import org.eclipse.scout.sdk.s2e.util.ast.ITypeBindingVisitor;

/**
 * <h3>{@link FormFieldProposal}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class FormFieldProposal extends AbstractTypeProposal {

  public FormFieldProposal(String name, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(name, relevance, imageId, cu, context);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException {
    AST ast = factory.getAst();
    Deque<TypeDeclaration> parentTypes = AstUtils.getDeclaringTypes(factory.getType());
    TypeDeclaration formType = parentTypes.getLast();

    // form field
    TypeDeclaration formFieldType = createFormFieldType(superType);

    // import to not yet created form field type
    addFormFieldImport(parentTypes);

    SimpleName formFieldSimpleName = ast.newSimpleName(getProposalContext().getDefaultName() + getProposalContext().getSuffix());
    Type formFieldGetterReturnType = AstUtils.getInnerTypeReturnType(formFieldSimpleName, getProposalContext().getDeclaringType());

    AstInnerTypeGetterBuilder formFieldGetter = factory.newInnerTypeGetter()
        .withMethodNameToFindInnerType("getFieldByClass")
        .withName(getProposalContext().getDefaultName())
        .withReadOnlySuffix(getProposalContext().getSuffix())
        .withReturnType(formFieldGetterReturnType);

    ITypeBinding iExtensionSuperType = getIExtensionSuperType();
    if (iExtensionSuperType != null) {
      MethodInvocation getOwner = ast.newMethodInvocation();
      getOwner.setName(ast.newSimpleName("getOwner"));
      formFieldGetter.withMethodToFindInnerTypeExpression(getOwner);

      if (AstUtils.isInstanceOf(factory.getDeclaringTypeBinding(), IScoutRuntimeTypes.IFormExtension)) {
        ITypeBinding[] typeArguments = iExtensionSuperType.getTypeArguments();
        if (typeArguments.length > 0) {
          ITypeBinding extendedForm = typeArguments[0];
          Set<ITypeBinding> composites = new LinkedHashSet<>();
          collectCompositeTypes(extendedForm, composites);
          if (!composites.isEmpty()) {
            // add @Extends to FormFields in FormExtensions
            SingleMemberAnnotation extendsAnnot = ast.newSingleMemberAnnotation();
            String extendsTypeName = factory.getImportRewrite().addImport(IScoutRuntimeTypes.Extends, factory.getContext());
            extendsAnnot.setTypeName(ast.newSimpleName(extendsTypeName));

            ITypeBinding first = composites.iterator().next();
            TypeLiteral typeLiteral = ast.newTypeLiteral();
            Type type = factory.newTypeReference(Bindings.getFullyQualifiedName(first));
            typeLiteral.setType(type);
            extendsAnnot.setValue(typeLiteral);
            AstUtils.addAnnotationTo(extendsAnnot, formFieldType);

            addLinkedPosition(factory.getRewrite().track(type), false, AstNodeFactory.EXTENDS_TYPE_GROUP);
            for (ITypeBinding composite : composites) {
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
    for (ITypeBinding innerType : owner.getDeclaredTypes()) {
      if (AstUtils.isInstanceOf(innerType, IScoutRuntimeTypes.ICompositeField)) {
        collector.add(innerType);
        collectCompositeTypes(innerType, collector);
      }
    }
  }

  protected ITypeBinding getIExtensionSuperType() {
    final ITypeBinding[] result = new ITypeBinding[1];
    AstUtils.visitHierarchy(getFactory().getDeclaringTypeBinding(), new ITypeBindingVisitor() {
      @Override
      public boolean visit(ITypeBinding type) {
        if (IScoutRuntimeTypes.IExtension.equals(type.getErasure().getQualifiedName())) {
          result[0] = type;
        }
        return result[0] == null;
      }
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
        .withOrderDefinitionType(IScoutRuntimeTypes.IFormField)
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .withSuperType(superType)
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert()
        .get();
  }

  protected String getNlsMethodName() {
    return "getConfiguredLabel";
  }

  private void addFormFieldImport(Deque<TypeDeclaration> parentTypes) throws CoreException {
    AstNodeFactory factory = getFactory();
    String fullyQualifiedName = AstUtils.getFullyQualifiedName(parentTypes, factory.getRoot(), '.');
    ImportDeclaration formFieldTypeImport = factory.getAst().newImportDeclaration();
    formFieldTypeImport.setStatic(false);
    formFieldTypeImport.setOnDemand(false);
    SimpleName simpleImportName = factory.getAst().newSimpleName(getProposalContext().getDefaultName() + getProposalContext().getSuffix());
    QualifiedName importName = factory.getAst().newQualifiedName(factory.getAst().newName(fullyQualifiedName), simpleImportName);
    formFieldTypeImport.setName(importName);

    // linked positions
    ITrackedNodePosition importPos = new WrappedTrackedNodePosition(getRewrite().track(simpleImportName), 0, -getProposalContext().getSuffix().length());
    addLinkedPosition(importPos, false, AstNodeFactory.TYPE_NAME_GROUP);

    factory.getImportsRewriteList().insertLast(formFieldTypeImport, null);
  }
}
