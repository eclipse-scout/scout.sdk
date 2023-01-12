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
