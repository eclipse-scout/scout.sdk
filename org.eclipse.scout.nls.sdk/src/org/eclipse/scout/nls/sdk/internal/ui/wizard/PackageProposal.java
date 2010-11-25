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
package org.eclipse.scout.nls.sdk.internal.ui.wizard;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.fieldassist.IContentProposal;

public class PackageProposal implements IContentProposal {
  private IPackageFragment m_package;

  public PackageProposal(IPackageFragment pakkage) {
    m_package = pakkage;
  }

  public String getContent() {

    return m_package.getElementName();
  }

  public int getCursorPosition() {
    return m_package.getElementName().length();
  }

  public String getDescription() {
    return m_package.getElementName();
  }

  public String getLabel() {
    return m_package.getElementName();
  }

  public IPackageFragment getPackage() {
    return m_package;
  }

}
