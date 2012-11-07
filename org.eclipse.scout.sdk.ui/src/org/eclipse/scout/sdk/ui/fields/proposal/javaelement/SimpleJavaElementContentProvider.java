/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.fields.proposal.javaelement;


/**
 * <h3>{@link SimpleJavaElementContentProvider}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 16.04.2012
 */
public class SimpleJavaElementContentProvider extends AbstractJavaElementContentProvider {

  private final Object[][] m_proposals;

  public SimpleJavaElementContentProvider(Object[] proposals) {
    m_proposals = new Object[][]{proposals};
  }

  @Override
  protected Object[][] computeProposals() {
    return m_proposals;
  }
}
