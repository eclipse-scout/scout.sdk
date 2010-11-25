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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.translationfile.directorychooser;

import java.util.Set;

import org.eclipse.scout.nls.sdk.internal.ui.dialog.nlsDirChooser.AbstractNlsTreeItem;
import org.eclipse.swt.graphics.Image;

public class NlsTranslationPrefixFile extends AbstractNlsTreeItem {

  @Override
  public Set<AbstractNlsTreeItem> getChildren() {
    return null;
  }

  @Override
  public Image getImage() {
    return null;
  }

  @Override
  public String getText() {
    return null;
  }

  @Override
  public boolean hasChildren() {
    return false;
  }

  @Override
  public boolean isLoaded() {
    return false;
  }

}
