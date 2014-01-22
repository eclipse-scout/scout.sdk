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
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;

/**
 * <h3>{@link ResourceFilters}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2012
 */
public class ResourceFilters {

  private static final IResourceFilter PRODUCT_FILE_FILTER = getFileExtensionFilter("product");
  private static final IResourceFilter TARGET_FILE_FILTER = getFileExtensionFilter("target");

  /**
   * @param fileName
   *          The filename to search.
   * @param ignoreCase
   *          true if the search should ignore the case of the filename, false if a case-sensitive search should be
   *          performed.
   * @return A new filter that only accepts files with the given name.
   */
  public static IResourceFilter getFileNameFilter(final String fileName, boolean ignoreCase) {
    if (ignoreCase) {
      return new IResourceFilter() {
        @Override
        public boolean accept(IResourceProxy resource) {
          return resource.getType() == IResource.FILE && resource.getName().equalsIgnoreCase(fileName);
        }
      };
    }
    else {
      return new IResourceFilter() {
        @Override
        public boolean accept(IResourceProxy resource) {
          return resource.getType() == IResource.FILE && resource.getName().equals(fileName);
        }
      };
    }
  }

  /**
   * @param fileExtension
   *          The file extension that must be available (without dot).
   * @return creates and returns a filter that only accepts files with the given extension. Consider using one of the
   *         predefined file extension filters: {@link ResourceFilters#getProductFileFilter()},
   *         {@link ResourceFilters#getTargetFileFilter()}.
   */
  public static IResourceFilter getFileExtensionFilter(String fileExtension) {
    final String ext = "." + fileExtension.toLowerCase().trim();
    return new IResourceFilter() {
      @Override
      public boolean accept(IResourceProxy resource) {
        return resource.getType() == IResource.FILE && resource.getName().toLowerCase().endsWith(ext);
      }
    };
  }

  /**
   * @param filters
   *          all of these filters must be fulfilled
   * @return Creates and returns a filter that is fulfilled if all of the given filters are accepted.
   */
  public static IResourceFilter getMultifilterAnd(final IResourceFilter... filters) {
    return getMultifilter(false, filters);
  }

  /**
   * @param filters
   *          at least one of this filter must be fulfilled
   * @return Creates and returns a filter that is fulfilled if a least one of the given filters is accepted.
   */
  public static IResourceFilter getMultifilterOr(final IResourceFilter... filters) {
    return getMultifilter(true, filters);
  }

  private static IResourceFilter getMultifilter(final boolean or, final IResourceFilter... filters) {
    if (filters == null || filters.length < 1) {
      return null; /* no filter. not allowed. */
    }

    return new IResourceFilter() {
      @Override
      public boolean accept(IResourceProxy candidate) {
        for (IResourceFilter f : filters) {
          boolean accepted = f.accept(candidate);
          if (or == accepted) {
            return accepted;
          }
        }
        return !or;
      }
    };
  }

  /**
   * @return Gets a filter that accepts all files that have "product" as file extension.<br>
   *         Calling this method is faster than using {@link ResourceFilters#getFileExtensionFilter(String)}.
   */
  public static IResourceFilter getProductFileFilter() {
    return PRODUCT_FILE_FILTER;
  }

  /**
   * @return Gets a filter that accepts all files that have "target" as file extension.<br>
   *         Calling this method is faster than using {@link ResourceFilters#getFileExtensionFilter(String)}.
   */
  public static IResourceFilter getTargetFileFilter() {
    return TARGET_FILE_FILTER;
  }

  /**
   * Gets all product files that contain the given id as dependency.
   * 
   * @param or
   *          if true, the list of ids is OR connected. Otherwise all of the given ids must be found in the product to
   *          be accepted by this filter (AND).
   * @param pluginOrFeatureIds
   *          The feature ids or plug-in symbolic names
   * @return a filter that returns all product files that fulfill the given criteria.
   */
  public static IResourceFilter getProductFileByContentFilter(final boolean or, final String... pluginOrFeatureIds) {
    final IResourceFilter productFileContentFilter = new IResourceFilter() {
      @Override
      public boolean accept(IResourceProxy resource) {
        // resource must be a product file here
        IFile productFile = (IFile) resource.requestResource();
        try {
          ProductFileModelHelper pfmh = new ProductFileModelHelper(productFile);
          for (String id : pluginOrFeatureIds) {
            boolean exists = pfmh.ProductFile.existsDependency(id);
            if (or == exists) {
              return exists;
            }
          }
        }
        catch (CoreException e) {
          SdkUtilActivator.logError("Unable to parse content of product file '" + productFile.getFullPath().toOSString() + "'.", e);
        }
        return !or;
      }
    };
    return getMultifilterAnd(getProductFileFilter(), productFileContentFilter);
  }
}
