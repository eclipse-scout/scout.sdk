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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h3>PageNodePageHelper</h3> ...
 */
public final class PageNodePageHelper {

  private PageNodePageHelper() {
  }

  public static List<AbstractPage> createRepresentationFor(AbstractPage parentPage, Set<IType> types, ITypeHierarchy pageTypeHierarchy) {
    ArrayList<AbstractPage> pages = new ArrayList<AbstractPage>(types.size());
    for (IType type : types) {
      if (TypeUtility.exists(type)) {
        if (pageTypeHierarchy.isSubtype(TypeUtility.getType(IRuntimeClasses.IPageWithNodes), type)) {
          // create page with node
          pages.add(new PageWithNodeNodePage(parentPage, type));
        }
        else if (pageTypeHierarchy.isSubtype(TypeUtility.getType(IRuntimeClasses.IPageWithTable), type)) {
          // create page with table
          pages.add(new PageWithTableNodePage(parentPage, type));
        }
        else {
          pages.add(new PageNodePage(parentPage, type));
        }
      }
    }
    return pages;
  }
}
