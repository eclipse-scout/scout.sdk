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
package org.eclipse.scout.sdk.s2e.nls.internal.model;

import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.scout.sdk.s2e.nls.model.NlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;

public class InheritedNlsEntry extends NlsEntry {

  public InheritedNlsEntry(INlsEntry row, INlsProject project) {
    super(row);
  }

  @Override
  public int getType() {
    return TYPE_INHERITED;
  }

  @Override
  public void addTranslation(Language language, String text) {
    NlsCore.logError("an inhertited NLS entry can not be modified!", new Exception());
  }

  @Override
  public void setKey(String key) {
    NlsCore.logError("an inhertited NLS entry can not be modified!", new Exception());
  }

}
