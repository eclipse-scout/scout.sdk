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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link ScoutSdkProposalComputer}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 25.10.2013
 */
public class ScoutSdkProposalComputer implements IJavaCompletionProposalComputer {

  @Override
  public void sessionStarted() {
  }

  @Override
  public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(1);
    if (!(context instanceof JavaContentAssistInvocationContext) || Platform.getBundle(ScoutSdkUi.PLUGIN_ID).getState() != Bundle.ACTIVE) {
      return proposals;
    }

    try {
      JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
      ICompilationUnit icu = javaContext.getCompilationUnit();
      IJavaElement element = JdtUtility.findJavaElement(icu, context.getInvocationOffset(), 0);
      if (TypeUtility.exists(element) && element.getElementType() == IJavaElement.TYPE) {
        IType declaringType = (IType) element;
        if (TypeUtility.isSubtype(TypeUtility.getType(IRuntimeClasses.ICodeType), declaringType, declaringType.newSupertypeHierarchy(monitor)) ||
            TypeUtility.isSubtype(TypeUtility.getType(IRuntimeClasses.ICode), declaringType, declaringType.newSupertypeHierarchy(monitor))) {
          proposals.add(new CodeNewProposal(declaringType));
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
    return null;
  }

  @Override
  public void sessionEnded() {
  }

  @Override
  public String getErrorMessage() {
    return null;
  }

}
