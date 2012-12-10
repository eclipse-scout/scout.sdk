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
package org.eclipse.scout.sdk.operation.project;

import java.util.List;

import org.eclipse.core.resources.IFile;

/**
 * <h3>{@link SwtProductFileFillOperation}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 10.12.2012
 */
public class SwtProductFileFillOperation extends AbstractEquinoxSecurityInstallOperation {

  @Override
  public boolean isRelevant() {
    return super.isRelevant() && isNodeChecked(CreateUiSwtPluginOperation.BUNDLE_ID);
  }

  @Override
  protected void contributeProductFiles(List<IFile> l) {
    IFile dev = getProperties().getProperty(CreateUiSwtPluginOperation.PROP_PRODUCT_FILE_DEV, IFile.class);
    if (dev != null) l.add(dev);

    IFile prod = getProperties().getProperty(CreateUiSwtPluginOperation.PROP_PRODUCT_FILE_PROD, IFile.class);
    if (prod != null) l.add(prod);
  }
}
