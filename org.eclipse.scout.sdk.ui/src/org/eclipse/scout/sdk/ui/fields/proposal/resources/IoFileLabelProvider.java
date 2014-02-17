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
package org.eclipse.scout.sdk.ui.fields.proposal.resources;

import java.io.File;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.SelectionStateLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link IoFileLabelProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 01.03.2012
 */
public class IoFileLabelProvider extends SelectionStateLabelProvider implements ITableLabelProvider {

  @Override
  public String getText(Object element) {
    File resource = (File) element;
    return resource.getName();
  }

  @Override
  public String getTextSelected(Object element) {
    StringBuilder text = new StringBuilder(getText(element));
    File file = (File) element;
    text.append(" (").append(file.getParent()).append(")");
    return text.toString();
  }

  @Override
  public Image getImage(Object element) {
    File file = (File) element;
    if (file.isDirectory()) {
      return ScoutSdkUi.getImage(ScoutSdkUi.FolderOpen);
    }
    else {
      return ScoutSdkUi.getImage(ScoutSdkUi.File);
    }
  }

  @Override
  public Image getImageSelected(Object element) {
    return getImage(element);
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    if (columnIndex == 0) {
      return getImage(element);
    }
    return null;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    File file = (File) element;
    switch (columnIndex) {
      case 0:
        return getText(element);
      case 1:
        return file.getParent();
      default:
        break;
    }
    return "";
  }
}
