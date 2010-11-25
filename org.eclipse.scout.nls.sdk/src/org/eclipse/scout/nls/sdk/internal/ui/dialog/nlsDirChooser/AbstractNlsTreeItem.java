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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.nlsDirChooser;

import java.util.Set;

import org.eclipse.swt.graphics.Image;

public abstract class AbstractNlsTreeItem implements Comparable<AbstractNlsTreeItem> {
  public abstract boolean isLoaded();

  public abstract boolean hasChildren();

  public abstract Set<AbstractNlsTreeItem> getChildren();

  public abstract String getText();

  public abstract Image getImage();

  public int compareTo(AbstractNlsTreeItem o) {
    return getText().compareTo(o.getText());
  }
}
