/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * <h3>{@link TabBoxProposal}</h3>
 *
 * @since 5.2.0
 */
public class TabBoxProposal extends FormFieldProposal {

  public TabBoxProposal(String name, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(name, relevance, imageId, cu, context);
  }

  @Override
  protected String getNlsMethodName() {
    return null;
  }
}
