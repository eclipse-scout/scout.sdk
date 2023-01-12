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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.manipulation.SharedASTProviderCore;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.s2e.environment.RunnableJob;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.ILinkedPositionHolder;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditProcessor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * <h3>{@link AbstractTypeProposal}</h3>
 *
 * @since 5.2.0
 */
public abstract class AbstractTypeProposal extends CUCorrectionProposal implements ILinkedPositionHolder {

  /**
   * Workaround to have a complete, valid AST even if there has been a prefix typed by the user. <br>
   * Detail: If the user writes "abc" on an empty line before an annotation, this annotation is no longer part of the
   * AST. This is because "abc@annot" is no valid pattern and gets excluded by the parser. As a workaround a semicolon
   * is added: "abc;@annot". This way the parser recognizes the annotation because the text written by the user looks
   * like another, wrong statement. The semicolon is removed again if the proposal is applied.
   */
  static final char SEARCH_STRING_END_FIX = ';';

  private final TypeProposalContext m_context;
  private final LinkedProposalModel m_linkedProposalModel;
  private final List<ICompletionProposalProvider> m_asyncProposalProviders;

  private ASTRewrite m_rewrite; // to prevent duplicate calculations
  private AstNodeFactory m_nodeFactory;

  @FunctionalInterface
  public interface IAstNodeFactoryProvider {
    AstNodeFactory createFactoryFor(AbstractTypeProposal proposal);
  }

  private static volatile IAstNodeFactoryProvider astNodeFactoryProvider = proposal -> {
    var ctx = proposal.getProposalContext();
    return new AstNodeFactory(ctx.getDeclaringType(), ctx.getIcu(), ctx.getProvider(), ctx.getScoutModelHierarchy().api(), ctx.getDeclaringTypeBinding(), proposal);
  };

  public static IAstNodeFactoryProvider getAstNodeFactoryProvider() {
    return astNodeFactoryProvider;
  }

  public static void setAstNodeFactoryProvider(IAstNodeFactoryProvider provider) {
    astNodeFactoryProvider = Ensure.notNull(provider);
  }

  protected AbstractTypeProposal(String displayName, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(displayName, cu, null, relevance, S2ESdkUiActivator.getImage(imageId));
    m_context = context;
    m_linkedProposalModel = new LinkedProposalModel();
    m_asyncProposalProviders = new ArrayList<>();
  }

  protected abstract void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException;

  @Override
  public String getAdditionalProposalInfo() {
    return null; // disable preview
  }

  @Override
  public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
    return null; // disable async preview
  }

  protected ASTRewrite getRewrite() throws CoreException {
    if (m_rewrite == null) {
      var factory = getFactory();
      var superType = getBestMatchingSuperType(m_context.getDefaultSuperClasses());
      m_rewrite = factory.getRewrite();

      fillRewrite(factory, superType);
    }
    return m_rewrite;
  }

  protected Type getBestMatchingSuperType(Iterable<String> candidates) {
    var env = getFactory().getScoutElementProvider().toScoutJavaEnvironment(getFactory().getJavaProject());
    for (var superTypeCandidate : candidates) {
      if (env.exists(superTypeCandidate)) {
        // found! return as best candidate
        return getFactory().newTypeReference(superTypeCandidate);
      }
    }
    throw new IllegalArgumentException("No default super type available in context.");
  }

  protected synchronized AstNodeFactory getFactory() {
    if (m_nodeFactory == null) {
      //noinspection AccessToStaticFieldLockedOnInstance
      m_nodeFactory = astNodeFactoryProvider.createFactoryFor(this);
    }
    return m_nodeFactory;
  }

  public TypeProposalContext getProposalContext() {
    return m_context;
  }

  @Override
  protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
    try {
      var rewrite = getRewrite();
      var edit = rewrite.rewriteAST();
      var searchString = m_context.getSearchString();
      if (searchString != null) {
        // remove the search prefix
        var len = searchString.length();
        edit.addChild(new DeleteEdit(m_context.getInsertPosition() - len, len + String.valueOf(SEARCH_STRING_END_FIX).length()));
      }
      editRoot.addChild(edit);
      editRoot.addChild(getFactory().getImportRewrite().rewriteImports(null));
    }
    catch (IllegalArgumentException e) {
      throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
    }
  }

  @Override
  protected void performChange(IEditorPart part, IDocument document) {
    try {
      applySearchStringFix(document);

      scheduleAstRetrieval();

      getRewrite(); // trigger rewrite creation

      // start proposal calculation
      for (var a : m_asyncProposalProviders) {
        a.load();
      }

      super.performChange(part, document);

      tryEnterLinkedMode(part);
    }
    catch (CoreException | BadLocationException e) {
      SdkLog.error("Unable to insert new element.", e);
    }
  }

  private void applySearchStringFix(IDocument document) throws CoreException {
    if (m_context.getSearchString() == null) {
      return; // not necessary
    }

    try {
      var insertFixEdit = new InsertEdit(m_context.getInsertPosition(), String.valueOf(SEARCH_STRING_END_FIX));
      var proc = new TextEditProcessor(document, insertFixEdit, TextEdit.UPDATE_REGIONS);
      proc.performEdits();
    }
    catch (MalformedTreeException | BadLocationException e) {
      throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
    }
  }

  private void scheduleAstRetrieval() {
    RunnableFuture<CompilationUnit> astInitializer = new FutureTask<>(new P_AstInitCallable(m_context.getIcu()));
    Job astInitializerJob = new RunnableJob("Get AST", astInitializer);
    astInitializerJob.setUser(false);
    astInitializerJob.setSystem(true);
    astInitializerJob.setPriority(Job.INTERACTIVE);
    astInitializerJob.schedule();
    m_context.setCompilationUnit(astInitializer);
  }

  private void tryEnterLinkedMode(IEditorPart part) throws BadLocationException {
    if (m_linkedProposalModel.hasLinkedPositions() && part instanceof JavaEditor) {
      // enter linked mode
      ITextViewer viewer = ((JavaEditor) part).getViewer();
      LinkedAsyncProposalModelPresenter.enterLinkedMode(viewer, part, didOpenEditor(), m_linkedProposalModel);
    }
    else if (part instanceof ITextEditor) {
      var endPosition = m_linkedProposalModel.getEndPosition();
      if (endPosition != null) {
        // select a result
        var pos = endPosition.getOffset() + endPosition.getLength();
        ((ITextEditor) part).selectAndReveal(pos, 0);
      }
    }
  }

  @Override
  public void addLinkedPositionProposalsHierarchy(String groupId, String hierarchyBaseTypeFqn) {
    addLinkedPositionProposalProvider(groupId, new P_HierarchyCallable(hierarchyBaseTypeFqn));
  }

  @Override
  public void addLinkedPositionProposalsBoolean(String groupId) {
    addLinkedPositionProposal(groupId, Boolean.FALSE.toString());
    addLinkedPositionProposal(groupId, Boolean.TRUE.toString());
  }

  private void addLinkedPositionProposalProvider(String groupId, Callable<Proposal[]> callable) {
    var future = new FutureTask<>(callable);
    var group = m_linkedProposalModel.getPositionGroup(groupId, false);
    if (!(group instanceof ICompletionProposalProvider)) {
      var newGroup = new LinkedAsyncProposalPositionGroup(groupId, future);
      m_asyncProposalProviders.add(newGroup);
      if (group != null) {
        // already added positions. copy over
        for (var info : group.getPositions()) {
          newGroup.addPosition(info);
        }
      }
      group = newGroup;
      m_linkedProposalModel.addPositionGroup(group);
    }
  }

  @Override
  public void addLinkedPosition(ITrackedNodePosition position, boolean isFirst, String groupID) {
    m_linkedProposalModel.getPositionGroup(groupID, true).addPosition(position, isFirst);
  }

  @Override
  public void addLinkedPositionProposal(String groupId, String proposal) {
    m_linkedProposalModel.getPositionGroup(groupId, true).addProposal(proposal, null, 10);
  }

  @Override
  public void addLinkedPositionProposal(String groupID, ITypeBinding type) {
    m_linkedProposalModel.getPositionGroup(groupID, true).addProposal(type, getCompilationUnit(), 10);
  }

  /**
   * Sets the end position of the linked mode to the end of the passed range.
   *
   * @param position
   *          The position that describes the end position of the linked mode.
   */
  public void setEndPosition(ITrackedNodePosition position) {
    m_linkedProposalModel.setEndPosition(position);
  }

  private static final class P_AstInitCallable implements Callable<CompilationUnit> {

    private final ICompilationUnit m_icu;

    private P_AstInitCallable(ICompilationUnit icu) {
      m_icu = icu;
    }

    @Override
    public CompilationUnit call() {
      var shared = SharedASTProviderCore.getAST(m_icu, SharedASTProviderCore.WAIT_ACTIVE_ONLY, null);
      if (shared.getAST().hasResolvedBindings()) { // the template completion requires type bindings!
        return shared;
      }

      var parser = ASTParser.newParser(AST.getJLSLatest());
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setSource(m_icu);
      parser.setResolveBindings(true);
      return (CompilationUnit) parser.createAST(null);
    }
  }

  private final class P_HierarchyCallable implements Callable<Proposal[]> {

    private final String m_hierarchyBaseTypeFqn;

    private P_HierarchyCallable(String hierarchyBaseTypeFqn) {
      m_hierarchyBaseTypeFqn = Ensure.notNull(hierarchyBaseTypeFqn);
    }

    @Override
    public Proposal[] call() {
      var abstractClassesInHierarchy = JdtUtils.findAbstractClassesInHierarchy(getFactory().getJavaProject(), m_hierarchyBaseTypeFqn, null);
      return abstractClassesInHierarchy.stream()
          .map(type -> getFactory().resolveTypeBinding(type.getFullyQualifiedName()))
          .filter(Objects::nonNull)
          .map(binding -> new P_JavaLinkedModeProposal(getFactory().getIcu(), binding, 10))
          .toArray(Proposal[]::new);
    }
  }

  private static final class P_JavaLinkedModeProposal extends Proposal {
    private final ITypeBinding m_typeProposal;
    private final ICompilationUnit m_compilationUnit;

    private P_JavaLinkedModeProposal(ICompilationUnit unit, ITypeBinding typeProposal, int relevance) {
      super(BindingLabelProvider.getBindingLabel(typeProposal, JavaElementLabels.ALL_DEFAULT | JavaElementLabels.ALL_POST_QUALIFIED), null, relevance);
      m_typeProposal = typeProposal;
      m_compilationUnit = unit;
      var desc = BindingLabelProvider.getBindingImageDescriptor(m_typeProposal, BindingLabelProvider.DEFAULT_IMAGEFLAGS);
      if (desc != null) {
        setImage(JavaPlugin.getImageDescriptorRegistry().get(desc));
      }
    }

    @Override
    public TextEdit computeEdits(int offset, LinkedPosition position, char trigger, int stateMask, LinkedModeModel model) throws CoreException {
      var impRewrite = CodeStyleConfiguration.createImportRewrite(m_compilationUnit, true);
      var replaceString = impRewrite.addImport(m_typeProposal);

      TextEdit composedEdit = new MultiTextEdit();
      composedEdit.addChild(new ReplaceEdit(position.getOffset(), position.getLength(), replaceString));
      composedEdit.addChild(impRewrite.rewriteImports(null));
      return composedEdit;
    }
  }
}
