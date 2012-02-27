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

import org.eclipse.core.resources.IResource;
import org.eclipse.scout.sdk.ui.fields.proposal.SelectionStateLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link ResourceLabelProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 15.02.2012
 */
public class ResourceLabelProvider extends SelectionStateLabelProvider {

  @Override
  public String getText(Object element) {
    IResource resource = (IResource) element;
    return resource.getName();
  }

  @Override
  public String getTextSelected(Object element) {
    StringBuilder text = new StringBuilder(getText(element));
    IResource resource = (IResource) element;
    text.append(" (").append(resource.getFullPath().toString()).append(")");
    return text.toString();
  }

  @Override
  public Image getImage(Object element) {
    IResource resource = (IResource) element;
    switch (resource.getType()) {
      case IResource.FILE:
        return ScoutSdkUi.getImage(ScoutSdkUi.File);
      case IResource.FOLDER:
        return ScoutSdkUi.getImage(ScoutSdkUi.FolderOpen);
      default:
        return ScoutSdkUi.getImage(ScoutSdkUi.Default);
    }
  }

  @Override
  public Image getImageSelected(Object element) {
    return getImage(element);
  }
}
