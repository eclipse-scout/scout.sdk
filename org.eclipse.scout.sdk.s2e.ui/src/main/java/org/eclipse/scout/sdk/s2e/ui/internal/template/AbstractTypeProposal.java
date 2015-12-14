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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.PositionInformation;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.util.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.ui.internal.util.ast.ILinkedPositionHolder;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * <h3>{@link AbstractTypeProposal}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public abstract class AbstractTypeProposal extends ASTRewriteCorrectionProposal implements ILinkedPositionHolder {

  private final TypeProposalContext m_context;
  private final LinkedProposalModel m_linkedProposalModel;
  private final List<ICompletionProposalProvider> m_asyncProposalProviders;

  private ASTRewrite m_rewrite;
  private AstNodeFactory m_nodeFactory;

  public interface IAstNodeFactoryProvider {
    AstNodeFactory createFactoryFor(AbstractTypeProposal proposal);
  }

  private static volatile IAstNodeFactoryProvider astNodeFactoryProvider = new IAstNodeFactoryProvider() {
    @Override
    public AstNodeFactory createFactoryFor(AbstractTypeProposal proposal) {
      return new AstNodeFactory(proposal.getProposalContext().getDeclaringType(), proposal.getProposalContext().getIcu(), proposal.getProposalContext().getProvider(),
          proposal.getProposalContext().getDeclaringTypeBinding(), proposal.getImportRewrite(), proposal);
    }
  };

  public static IAstNodeFactoryProvider getAstNodeFactoryProvider() {
    return astNodeFactoryProvider;
  }

  public static void setAstNodeFactoryProvider(IAstNodeFactoryProvider provider) {
    astNodeFactoryProvider = Validate.notNull(provider);
  }

  public AbstractTypeProposal(String displayName, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(displayName, cu, null, relevance, S2ESdkUiActivator.getImage(imageId));
    m_context = context;
    m_linkedProposalModel = new LinkedProposalModel();
    m_asyncProposalProviders = new LinkedList<>();
  }

  protected abstract void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException;

  @Override
  public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
    return null; // disable preview
  }

  @Override
  protected ASTRewrite getRewrite() throws CoreException {
    if (m_rewrite == null) {
      AstNodeFactory factory = getFactory();
      if (factory.getImportRewrite() != getImportRewrite()) {
        setImportRewrite(factory.getImportRewrite());
      }

      Type superType = getBestMatchingSuperType(m_context.getDefaultSuperClasses());
      m_rewrite = factory.getRewrite();
      fillRewrite(factory, superType);
    }
    return m_rewrite;
  }

  protected Type getBestMatchingSuperType(Iterable<String> candidates) {
    IJavaEnvironment env = getFactory().getJavaEnvironment();
    for (String superTypeCandidate : candidates) {
      if (env.findType(superTypeCandidate) != null) {
        // found! return as best candidate
        return getFactory().newTypeReference(superTypeCandidate);
      }
    }
    throw new IllegalArgumentException("No default super type available in context.");
  }

  protected synchronized AstNodeFactory getFactory() {
    if (m_nodeFactory == null) {
      m_nodeFactory = astNodeFactoryProvider.createFactoryFor(this);
    }
    return m_nodeFactory;
  }

  public TypeProposalContext getProposalContext() {
    return m_context;
  }

  @Override
  protected void performChange(IEditorPart part, IDocument document) throws CoreException {
    getRewrite(); // trigger rewrite creation

    // start proposal calculation
    for (ICompletionProposalProvider a : m_asyncProposalProviders) {
      a.load();
    }

    try {
      super.performChange(part, document);

      if (m_linkedProposalModel.hasLinkedPositions() && part instanceof JavaEditor) {
        // enter linked mode
        ITextViewer viewer = ((JavaEditor) part).getViewer();
        new LinkedAsyncProposalModelPresenter().enterLinkedMode(viewer, part, didOpenEditor(), m_linkedProposalModel);
      }
      else if (part instanceof ITextEditor) {
        LinkedProposalPositionGroup.PositionInformation endPosition = m_linkedProposalModel.getEndPosition();
        if (endPosition != null) {
          // select a result
          int pos = endPosition.getOffset() + endPosition.getLength();
          ((ITextEditor) part).selectAndReveal(pos, 0);
        }
      }
    }
    catch (BadLocationException e) {
      throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
    }
  }

  @Override
  public void addLinkedPositionProposalsHierarchy(String groupId, String hierarchyBaseTypeFqn) {
    addLinkedPositionProposalProvider(groupId, new P_HierarchyCallable(hierarchyBaseTypeFqn));
  }

  private void addLinkedPositionProposalProvider(String groupId, Callable<Proposal[]> callable) {
    FutureTask<Proposal[]> future = new FutureTask<>(callable);
    LinkedProposalPositionGroup group = m_linkedProposalModel.getPositionGroup(groupId, false);
    if (group == null || !(group instanceof ICompletionProposalProvider)) {
      LinkedAsyncProposalPositionGroup newGroup = new LinkedAsyncProposalPositionGroup(groupId, future);
      m_asyncProposalProviders.add(newGroup);
      if (group != null) {
        // already added positions. copy over
        for (PositionInformation info : group.getPositions()) {
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

  private final class P_HierarchyCallable implements Callable<Proposal[]> {

    private final String m_hierarchyBaseTypeFqn;

    private P_HierarchyCallable(String hierarchyBaseTypeFqn) {
      m_hierarchyBaseTypeFqn = Validate.notNull(hierarchyBaseTypeFqn);
    }

    @Override
    public Proposal[] call() throws Exception {
      Set<String> abstractClassesInHierarchy = S2eUtils.findAbstractClassesInHierarchy(getFactory().getJavaProject(), m_hierarchyBaseTypeFqn, null);
      List<Proposal> result = new ArrayList<>(abstractClassesInHierarchy.size());
      for (String fqn : abstractClassesInHierarchy) {
        ITypeBinding binding = getFactory().resolveTypeBinding(fqn);
        if (binding != null) {
          result.add(new P_JavaLinkedModeProposal(getFactory().getIcu(), binding, 10));
        }
      }
      return result.toArray(new Proposal[result.size()]);
    }
  }

  private static final class P_JavaLinkedModeProposal extends Proposal {
    private final ITypeBinding m_typeProposal;
    private final ICompilationUnit m_compilationUnit;

    private P_JavaLinkedModeProposal(ICompilationUnit unit, ITypeBinding typeProposal, int relevance) {
      super(BindingLabelProvider.getBindingLabel(typeProposal, JavaElementLabels.ALL_DEFAULT | JavaElementLabels.ALL_POST_QUALIFIED), null, relevance);
      m_typeProposal = typeProposal;
      m_compilationUnit = unit;
      ImageDescriptor desc = BindingLabelProvider.getBindingImageDescriptor(m_typeProposal, BindingLabelProvider.DEFAULT_IMAGEFLAGS);
      if (desc != null) {
        setImage(JavaPlugin.getImageDescriptorRegistry().get(desc));
      }
    }

    @Override
    public TextEdit computeEdits(int offset, LinkedPosition position, char trigger, int stateMask, LinkedModeModel model) throws CoreException {
      ImportRewrite impRewrite = StubUtility.createImportRewrite(m_compilationUnit, true);
      String replaceString = impRewrite.addImport(m_typeProposal);

      MultiTextEdit composedEdit = new MultiTextEdit();
      composedEdit.addChild(new ReplaceEdit(position.getOffset(), position.getLength(), replaceString));
      composedEdit.addChild(impRewrite.rewriteImports(null));
      return composedEdit;
    }
  }
}
