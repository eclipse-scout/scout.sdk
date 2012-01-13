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
package org.eclipse.scout.sdk.rap.operations.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;

public class UpdateProductRapV1_5 extends AbstractUpdateProductOperation {
  public static final String BUNDLE_RWT_OSGI = "org.eclipse.rap.rwt.osgi";

  /**
   * @param productFile
   */
  public UpdateProductRapV1_5(IFile productFile) {
    super(productFile);
  }

  @Override
  protected void updateProductModel(ProductFileModelHelper productModel) throws CoreException {
    productModel.ProductFile.addDependency(BUNDLE_RWT_OSGI, false);
  }
}
