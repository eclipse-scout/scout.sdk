/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.classid;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.callInEclipseEnvironment;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
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
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link ClassIdQuickAssistProcessor}</h3>
 *
 * @since 5.1.0
 */
public class ClassIdQuickAssistProcessor implements IQuickAssistProcessor {

  @Override
  public boolean hasAssists(IInvocationContext context) {
    IJavaCompletionProposal[] assists = getAssists(context, null);
    return assists != null && assists.length > 0;
  }

  @Override
  @SuppressWarnings("squid:S1168")
  public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) {
    ClassIdTarget selectedType = getTarget(context.getCoveringNode());
    if (selectedType != null && !JdtUtils.exists(selectedType.m_annotation)) {
      CompilationUnitRewrite rewrite = createRewrite(selectedType.m_type, selectedType.m_td);
      return new IJavaCompletionProposal[]{new ClassIdAddProposal(rewrite)};
    }
    return null;
  }

  private static CompilationUnitRewrite createRewrite(IType type, TypeDeclaration td) {
    CompilationUnitRewrite cuRewrite = new CompilationUnitRewrite(DefaultWorkingCopyOwner.PRIMARY, type.getCompilationUnit(), (CompilationUnit) td.getRoot());

    ListRewrite listRewrite = cuRewrite.getASTRewrite().getListRewrite(td, td.getModifiersProperty());

    // annotation
    SingleMemberAnnotation classIdAnnotation = callInEclipseEnvironment(
        (e, p) -> new AstNodeFactory(td, type.getCompilationUnit(), e)
            .newClassIdAnnotation(type.getFullyQualifiedName()))
                .result();

    // imports
    cuRewrite.getImportRewrite().addImport(IScoutRuntimeTypes.ClassId);

    // add the annotation
    ASTNode sibling = AstUtils.getAnnotationSibling(td, classIdAnnotation);
    if (sibling == null) {
      listRewrite.insertLast(classIdAnnotation, null);
    }
    else {
      listRewrite.insertBefore(classIdAnnotation, sibling, null);
    }

    return cuRewrite;
  }

  private static ClassIdTarget getTarget(ASTNode selectedNode) {
    if (selectedNode != null && selectedNode.getParent() != null) {
      boolean isValidNodeType = selectedNode.getNodeType() == ASTNode.SIMPLE_NAME
          || selectedNode.getNodeType() == ASTNode.QUALIFIED_NAME
          || selectedNode.getNodeType() == ASTNode.MODIFIER
          || selectedNode.getNodeType() == ASTNode.TYPE_DECLARATION;
      if (isValidNodeType) {
        TypeDeclaration typeDecl;
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
                if (JdtUtils.hierarchyContains(superTypeHierarchy, IScoutRuntimeTypes.ITypeWithClassId)) {
                  IAnnotation annotation = JdtUtils.getAnnotation(t, IScoutRuntimeTypes.ClassId);
                  return new ClassIdTarget(typeDecl, t, annotation);
                }
              }
            }
            catch (JavaModelException e) {
              SdkLog.error("Unable to check if type '{}' is anonymous.", t.getFullyQualifiedName(), e);
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
      super("Add @ClassId annotation", cur.getCu(), 1000, JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
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

    private final TypeDeclaration m_td;
    private final IType m_type;
    private final IAnnotation m_annotation;

    private ClassIdTarget(TypeDeclaration td, IType type, IAnnotation annotation) {
      m_td = td;
      m_type = type;
      m_annotation = annotation;
    }
  }
}
