/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.classid;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.callInEclipseEnvironmentSync;

import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.util.ApiHelper;
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
    var assists = getAssists(context, null);
    return assists != null && assists.length > 0;
  }

  @Override
  @SuppressWarnings("squid:S1168")
  public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) {
    var selectedType = getTarget(context.getCoveringNode());
    if (selectedType != null && !JdtUtils.exists(selectedType.m_annotation)) {
      var rewrite = createRewrite(selectedType);
      return new IJavaCompletionProposal[]{new ClassIdAddProposal(rewrite)};
    }
    return null;
  }

  private static CompilationUnitRewrite createRewrite(ClassIdTarget target) {
    var type = target.m_type;
    var td = target.m_td;
    var cuRewrite = new CompilationUnitRewrite(DefaultWorkingCopyOwner.PRIMARY, type.getCompilationUnit(), (CompilationUnit) td.getRoot());

    var listRewrite = cuRewrite.getASTRewrite().getListRewrite(td, td.getModifiersProperty());

    // create annotation element
    var classIdAnnotation = callInEclipseEnvironmentSync(
        (e, p) -> new AstNodeFactory(td, type.getCompilationUnit(), e, target.m_scoutApi).newClassIdAnnotation(type.getFullyQualifiedName()),
        new NullProgressMonitor());

    // imports
    cuRewrite.getImportRewrite().addImport(classIdAnnotation.getTypeName().getFullyQualifiedName());

    // add the annotation
    var sibling = AstUtils.getAnnotationSibling(td, classIdAnnotation);
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
      var isValidNodeType = IntStream.of(ASTNode.SIMPLE_NAME, ASTNode.QUALIFIED_NAME, ASTNode.MODIFIER, ASTNode.TYPE_DECLARATION)
          .anyMatch(nodeType -> nodeType == selectedNode.getNodeType());
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

        var resolveTypeBinding = typeDecl.resolveBinding();
        if (resolveTypeBinding != null) {
          var javaElement = resolveTypeBinding.getJavaElement();
          if (JdtUtils.exists(javaElement) && javaElement.getElementType() == IJavaElement.TYPE) {
            var t = (IType) javaElement;
            try {
              if (!t.isBinary() && !t.isAnonymous()) {
                var superTypeHierarchy = t.newSupertypeHierarchy(null);
                var scoutApi = ApiHelper.requireScoutApiFor(t);
                var classIdFqn = scoutApi.ClassId().fqn();
                if (JdtUtils.hierarchyContains(superTypeHierarchy, scoutApi.ITypeWithClassId().fqn())) {
                  var annotation = JdtUtils.getAnnotation(t, classIdFqn);
                  return new ClassIdTarget(typeDecl, t, annotation, scoutApi);
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
    private final IScoutApi m_scoutApi;

    private ClassIdTarget(TypeDeclaration td, IType type, IAnnotation annotation, IScoutApi api) {
      m_td = td;
      m_type = type;
      m_annotation = annotation;
      m_scoutApi = api;
    }
  }
}
