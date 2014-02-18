package org.eclipse.scout.sdk.ui.extensions.quickassist;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.jface.text.IDocument;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerationContext;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerators;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.util.proposal.CUCorrectionProposal;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ast.visitor.DefaultAstVisitor;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

@SuppressWarnings("restriction")
public class ClassIdQuickAssistProcessor implements IQuickAssistProcessor {

  @Override
  public boolean hasAssists(IInvocationContext context) throws CoreException {
    IJavaCompletionProposal[] assists = getAssists(context, null);
    return assists != null && assists.length > 0;
  }

  @Override
  public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
    ClassIdTarget selectedType = getSelectedType(context);
    if (selectedType != null) {
      CompilationUnitRewrite rewrite = createRewrite(selectedType.type, selectedType.td);
      ClassIdAddProposal prop = new ClassIdAddProposal(rewrite);
      return new IJavaCompletionProposal[]{prop};
    }
    return null;
  }

  private CompilationUnitRewrite createRewrite(IType type, TypeDeclaration td) throws JavaModelException {
    CompilationUnitRewrite cuRewrite = new CompilationUnitRewrite(DefaultWorkingCopyOwner.PRIMARY, type.getCompilationUnit(), (CompilationUnit) td.getRoot());

    ListRewrite listRewrite = cuRewrite.getASTRewrite().getListRewrite(td, td.getModifiersProperty());

    // annotation
    SingleMemberAnnotation newAnnotation = td.getAST().newSingleMemberAnnotation();
    newAnnotation.setTypeName(td.getAST().newSimpleName(NamingUtility.getSimpleName(IRuntimeClasses.ClassId)));

    // value
    StringLiteral id = td.getAST().newStringLiteral();
    id.setLiteralValue(ClassIdGenerators.generateNewId(new ClassIdGenerationContext(type)));
    newAnnotation.setValue(id);

    // imports
    if (!isClassIdImportPresent(type.getCompilationUnit())) {
      cuRewrite.getImportRewrite().addImport(IRuntimeClasses.ClassId);
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

  private ASTNode getSibling(final TypeDeclaration td, String newAnnotValue, SingleMemberAnnotation newAnnotation) {
    final ArrayList<Annotation> annotations = new ArrayList<Annotation>();
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

  private boolean isClassIdImportPresent(ICompilationUnit icu) throws JavaModelException {
    for (IImportDeclaration importDecl : icu.getImports()) {
      if (IRuntimeClasses.ClassId.equals(importDecl.getElementName())) {
        return true;
      }
    }
    return false;
  }

  private ClassIdTarget getSelectedType(IInvocationContext context) throws JavaModelException {
    ClassIdTarget t = getTarget(context.getCoveringNode());
    return applyFilter(t);
  }

  private ClassIdTarget applyFilter(ClassIdTarget input) throws JavaModelException {
    if (input != null) {
      if (TypeUtility.exists(input.type) && !input.type.isBinary() && !input.type.isAnonymous()) {
        IAnnotation annotation = JdtUtility.getAnnotation(input.type, IRuntimeClasses.ClassId);
        if (!TypeUtility.exists(annotation)) {
          IType filterType = TypeUtility.getType(IRuntimeClasses.ITypeWithClassId);
          if (TypeUtility.exists(filterType)) {
            ITypeHierarchy superTypeHierarchy = TypeUtility.getSuperTypeHierarchy(input.type);
            if (superTypeHierarchy.contains(filterType)) {
              return input;
            }
          }
        }
      }
    }
    return null;
  }

  private ClassIdTarget getTarget(ASTNode selectedNode) {
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
          if (TypeUtility.exists(javaElement) && javaElement.getElementType() == IJavaElement.TYPE) {
            return new ClassIdTarget(typeDecl, (IType) javaElement);
          }
        }
      }
    }
    return null;
  }

  private final static class ClassIdAddProposal extends CUCorrectionProposal {

    private final CompilationUnitRewrite m_rewrite;

    private ClassIdAddProposal(CompilationUnitRewrite cur) {
      super("Add @ClassId annotation", cur.getCu(), 100, JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
      m_rewrite = cur;
    }

    @Override
    protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
      Map options = m_rewrite.getCu().getJavaProject().getOptions(true);
      editRoot.addChild(m_rewrite.getASTRewrite().rewriteAST(document, options));
      if (m_rewrite.getImportRewrite().hasRecordedChanges()) {
        editRoot.addChild(m_rewrite.getImportRewrite().rewriteImports(null));
      }
    }
  }

  private final static class ClassIdTarget {

    private final TypeDeclaration td;
    private final IType type;

    private ClassIdTarget(TypeDeclaration td, IType type) {
      this.td = td;
      this.type = type;
    }
  }
}
