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
package org.eclipse.scout.sdk.s2e.ui.internal.classid;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerationContext;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.util.DefaultAstVisitor;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * <h3>{@link ClassIdQuickAssistProcessor}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class ClassIdQuickAssistProcessor implements IQuickAssistProcessor {

  @Override
  public boolean hasAssists(IInvocationContext context) throws CoreException {
    IJavaCompletionProposal[] assists = getAssists(context, null);
    return assists != null && assists.length > 0;
  }

  @Override
  public IJavaCompletionProposal[] getAssists(final IInvocationContext context, IProblemLocation[] locations) throws CoreException {
    final ClassIdTarget selectedType = getTarget(context.getCoveringNode());
    if (selectedType != null) {
      if (!JdtUtils.exists(selectedType.annotation)) {
        CompilationUnitRewrite rewrite = createRewrite(selectedType.type, selectedType.td);
        if (rewrite != null) {
          return new IJavaCompletionProposal[]{new ClassIdAddProposal(rewrite)};
        }
      }
    }
    return null;
  }

  private static CompilationUnitRewrite createRewrite(IType type, TypeDeclaration td) throws JavaModelException {
    CompilationUnitRewrite cuRewrite = new CompilationUnitRewrite(DefaultWorkingCopyOwner.PRIMARY, type.getCompilationUnit(), (CompilationUnit) td.getRoot());

    ListRewrite listRewrite = cuRewrite.getASTRewrite().getListRewrite(td, td.getModifiersProperty());

    // annotation
    SingleMemberAnnotation newAnnotation = td.getAST().newSingleMemberAnnotation();
    newAnnotation.setTypeName(td.getAST().newSimpleName(Signature.getSimpleName(IScoutRuntimeTypes.ClassId)));

    // value
    StringLiteral id = td.getAST().newStringLiteral();
    String newId = ClassIdGenerators.generateNewId(new ClassIdGenerationContext(type));
    if (StringUtils.isBlank(newId)) {
      return null;
    }
    id.setLiteralValue(newId);
    newAnnotation.setValue(id);

    // imports
    if (!isClassIdImportPresent(type.getCompilationUnit())) {
      cuRewrite.getImportRewrite().addImport(IScoutRuntimeTypes.ClassId);
    }

    // add the annotation
    TextEditGroup group = cuRewrite.createGroupDescription("");
    ASTNode sibling = getSibling(td, id.getEscapedValue(), newAnnotation);
    if (sibling == null) {
      listRewrite.insertLast(newAnnotation, group);
    }
    else {
      listRewrite.insertBefore(newAnnotation, sibling, group);
    }

    return cuRewrite;
  }

  private static ASTNode getSibling(final TypeDeclaration td, String newAnnotValue, SingleMemberAnnotation newAnnotation) {
    final ArrayList<Annotation> annotations = new ArrayList<>();
    td.accept(new DefaultAstVisitor() {
      @Override
      public boolean visitNode(ASTNode node) {
        return false;
      }

      @Override
      public boolean visit(TypeDeclaration node) {
        return node == td;
      }

      @Override
      public boolean visit(MarkerAnnotation node) {
        annotations.add(node);
        return super.visit(node);
      }

      @Override
      public boolean visit(NormalAnnotation node) {
        annotations.add(node);
        return super.visit(node);
      }

      @Override
      public boolean visit(SingleMemberAnnotation node) {
        annotations.add(node);
        return super.visit(node);
      }
    });

    if (annotations.size() > 0) {
      // there are already annotations. find the best sibling
      int newAnnotLen = newAnnotation.getTypeName().getFullyQualifiedName().length() + newAnnotValue.length() + 3;
      Annotation[] orderedAnnotations = annotations.toArray(new Annotation[annotations.size()]);
      for (int i = orderedAnnotations.length - 1; i >= 0; i--) {
        int len = orderedAnnotations[i].getLength();
        if (len > 0 && len >= newAnnotLen) {
          return orderedAnnotations[i];
        }
      }
    }
    for (Object o : td.modifiers()) {
      if (o instanceof Modifier) {
        return (Modifier) o;
      }
    }
    return null;
  }

  private static boolean isClassIdImportPresent(ICompilationUnit icu) throws JavaModelException {
    for (IImportDeclaration importDecl : icu.getImports()) {
      if (IScoutRuntimeTypes.ClassId.equals(importDecl.getElementName())) {
        return true;
      }
    }
    return false;
  }

  private static ClassIdTarget getTarget(ASTNode selectedNode) {
    if (selectedNode != null && selectedNode.getParent() != null) {
      if (selectedNode.getNodeType() == ASTNode.SIMPLE_NAME || selectedNode.getNodeType() == ASTNode.QUALIFIED_NAME || selectedNode.getNodeType() == ASTNode.MODIFIER || selectedNode.getNodeType() == ASTNode.TYPE_DECLARATION) {
        TypeDeclaration typeDecl = null;
        if (selectedNode.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
          typeDecl = (TypeDeclaration) selectedNode.getParent();
        }
        else if (selectedNode.getNodeType() == ASTNode.TYPE_DECLARATION) {
          typeDecl = (TypeDeclaration) selectedNode;
        }
        else {
          return null;
        }

        ITypeBinding resolveTypeBinding = typeDecl.resolveBinding();
        if (resolveTypeBinding != null) {
          IJavaElement javaElement = resolveTypeBinding.getJavaElement();
          if (JdtUtils.exists(javaElement) && javaElement.getElementType() == IJavaElement.TYPE) {
            IType t = (IType) javaElement;
            try {
              if (!t.isBinary() && !t.isAnonymous()) {
                ITypeHierarchy superTypeHierarchy = t.newSupertypeHierarchy(null);

                Set<IType> jdtTypes = JdtUtils.resolveJdtTypes(IScoutRuntimeTypes.ITypeWithClassId);
                for (IType iTypeWithClassId : jdtTypes) {
                  if (JdtUtils.exists(iTypeWithClassId)) {
                    if (superTypeHierarchy.contains(iTypeWithClassId)) {
                      IAnnotation annotation = JdtUtils.getAnnotation(t, IScoutRuntimeTypes.ClassId);
                      return new ClassIdTarget(typeDecl, t, annotation);
                    }
                  }
                }
              }
            }
            catch (CoreException e) {
              SdkLog.error("Unable to check if type '" + t.getFullyQualifiedName() + "' is anonymous.", e);
            }
          }
        }
      }
    }
    return null;
  }

  private static final class ClassIdAddProposal extends CUCorrectionProposal {

    private final CompilationUnitRewrite m_rewrite;

    private ClassIdAddProposal(CompilationUnitRewrite cur) {
      super("Add @ClassId annotation", cur.getCu(), 100, JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
      m_rewrite = cur;
    }

    @Override
    protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
      Map<?, ?> options = m_rewrite.getCu().getJavaProject().getOptions(true);
      editRoot.addChild(m_rewrite.getASTRewrite().rewriteAST(document, options));
      if (m_rewrite.getImportRewrite().hasRecordedChanges()) {
        editRoot.addChild(m_rewrite.getImportRewrite().rewriteImports(null));
      }
    }
  }

  private static final class ClassIdTarget {

    private final TypeDeclaration td;
    private final IType type;
    private final IAnnotation annotation;

    private ClassIdTarget(TypeDeclaration td, IType type, IAnnotation annotation) {
      this.td = td;
      this.type = type;
      this.annotation = annotation;
    }
  }
}
