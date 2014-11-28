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
package org.eclipse.scout.sdk.ui.internal.extensions.codecompletion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link ScoutSdkProposalComputer}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 25.10.2013
 */
@SuppressWarnings("restriction")
public class ScoutSdkProposalComputer implements IJavaCompletionProposalComputer {

  @Override
  public void sessionStarted() {
  }

  @Override
  public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    Bundle bundle = Platform.getBundle(ScoutSdkUi.PLUGIN_ID);
    if (!(context instanceof JavaContentAssistInvocationContext) || bundle == null || bundle.getState() != Bundle.ACTIVE) {
      return CollectionUtility.emptyArrayList();
    }

    List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(4);
    try {
      JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
      CompletionContext cContext = javaContext.getCoreContext();
      if (cContext instanceof InternalCompletionContext && cContext.isExtended()) {
        InternalCompletionContext coreContext = (InternalCompletionContext) cContext;
        IJavaElement element = coreContext.getEnclosingElement();
        if (TypeUtility.exists(element)) {
          if (element.getElementType() == IJavaElement.TYPE) {
            IType declaringType = TypeUtility.getType(((IType) element).getFullyQualifiedName());

            int startOffset = -1;
            ASTNode completionNode = coreContext.getCompletionNode();
            if (completionNode != null) {
              startOffset = completionNode.sourceStart();
            }

            // don't directly use the element (AssistSourceType) because it has invalid source ranges!
            ITypeHierarchy supertypeHierarchy = TypeUtility.getSupertypeHierarchy(declaringType);
            if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ICodeType))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ICode))) {
              proposals.add(new CodeNewProposal(declaringType, startOffset));
            }
            if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITable))) {
              proposals.add(new ColumnNewProposal(declaringType, startOffset));
            }
            if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ICompositeField))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IForm))) {
              proposals.add(new FormFieldNewProposal(declaringType, startOffset));
            }
            if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IMenu))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IDesktop))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IDesktopExtension))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ICalendarItemProvider))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IPageWithNodes))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITree))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITable))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITreeNode))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IValueField))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IButton))
                || supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IImageField))) {
              proposals.add(new MenuNewProposal(declaringType, startOffset));
            }
          }
        }
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("Unable to compute Scout code completion proposals.", e);
    }
    return proposals;
  }

  @Override
  public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    return Collections.emptyList();
  }

  @Override
  public void sessionEnded() {
  }

  @Override
  public String getErrorMessage() {
    return null;
  }
}
