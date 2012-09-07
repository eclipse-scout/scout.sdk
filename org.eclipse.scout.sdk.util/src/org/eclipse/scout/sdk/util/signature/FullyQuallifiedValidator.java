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
package org.eclipse.scout.sdk.util.signature;

import org.eclipse.jdt.core.Signature;

/**
 *
 */
public class FullyQuallifiedValidator implements IImportValidator {

  @Override
  public void addImport(String fqn) {
  }

  @Override
  public String[] getImportsToCreate() {
    return null;
  }

  @Override
  public String getTypeName(String singleTypeSignature) {
    String pckName = Signature.getSignatureQualifier(singleTypeSignature);
    String simpleName = Signature.getSignatureSimpleName(singleTypeSignature);
    return pckName + "." + simpleName;
  }

}
