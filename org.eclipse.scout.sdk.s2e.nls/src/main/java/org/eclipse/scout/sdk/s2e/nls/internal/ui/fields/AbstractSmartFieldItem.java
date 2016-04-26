/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.fields;

import java.util.Objects;

import org.eclipse.swt.graphics.Image;

public abstract class AbstractSmartFieldItem implements Comparable<AbstractSmartFieldItem> {

  public abstract String getText();

  public abstract Image getImage();

  @Override
  public int compareTo(AbstractSmartFieldItem o) {
    return getText().compareTo(o.getText());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AbstractSmartFieldItem other = (AbstractSmartFieldItem) obj;
    return Objects.equals(getText(), other.getText());
  }

  @Override
  public int hashCode() {
    return getText().hashCode();
  }
}
