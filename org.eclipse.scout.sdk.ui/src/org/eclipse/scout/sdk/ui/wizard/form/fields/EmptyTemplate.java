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
package org.eclipse.scout.sdk.ui.wizard.form.fields;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.template.IContentTemplate;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class EmptyTemplate implements IContentTemplate {

  @Override
  public void apply(IType type, IWorkingCopyManager manager, IProgressMonitor monitor) throws CoreException {
    // void
  }

  @Override
  public String getName() {
    return Texts.get("None");
  }
}
