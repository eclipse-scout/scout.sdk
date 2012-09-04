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
package org.eclipse.scout.sdk.util.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.scout.commons.StringUtility;

/**
 * <h3>{@link ResourceFilters}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2012
 */
public class ResourceFilters {

  public static IResourceFilter getMultifilterAnd(final IResourceFilter... filters) {
    return new IResourceFilter() {
      @Override
      public boolean accept(IResource candidate) {
        if (filters == null) {
          return true;
        }
        else {
          for (IResourceFilter f : filters) {
            if (!f.accept(candidate)) {
              return false;
            }
          }
          return true;
        }
      }
    };
  }

  public static IResourceFilter getMultifilterOr(final IResourceFilter... filters) {
    return new IResourceFilter() {
      @Override
      public boolean accept(IResource candidate) {
        if (filters == null) {
          return true;
        }
        else {
          for (IResourceFilter f : filters) {
            if (f.accept(candidate)) {
              return true;
            }
          }
          return false;
        }
      }
    };
  }

  public static IResourceFilter getProductFilter() {
    return new IResourceFilter() {

      @Override
      public boolean accept(IResource resource) {
        return ResourceUtility.exists(resource) && resource.getType() == IResource.FILE
            && StringUtility.equalsIgnoreCase(((IFile) resource).getFileExtension(), "product");
      }
    };
  }
}
