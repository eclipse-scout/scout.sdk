/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test.types;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.internal.typecache.HierarchyCache;
import org.eclipse.scout.sdk.util.internal.typecache.ProjectContextTypeHierarchyResult;
import org.eclipse.scout.sdk.util.typecache.TypeHierarchyConstraints;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h3>{@link ProjectContextTypeHierarchyResultTest}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.06.2014
 */
public class ProjectContextTypeHierarchyResultTest extends AbstractScoutSdkTest {

  private static final String BUNDLE_NAME_CLIENT = "test.client";
  private static final String BUNDLE_NAME_SHARED = "test.shared";
  private static final String BUNDLE_NAME_SERVER = "test.server";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/util/typeCache", BUNDLE_NAME_CLIENT, BUNDLE_NAME_SHARED, BUNDLE_NAME_SERVER);
  }

  @Test
  public void testAbstractRefresh() throws Exception {
    IType companyForm = SdkAssert.assertTypeExists("test.client.ui.forms.CompanyForm");
    IType iForm = SdkAssert.assertTypeExists(IRuntimeClasses.IForm);

    TypeHierarchyConstraints constraints = new TypeHierarchyConstraints(iForm, companyForm.getJavaProject());
    constraints.modifiersSet(Flags.AccAbstract);
    ProjectContextTypeHierarchyResult projectContextTypeHierarchy = (ProjectContextTypeHierarchyResult) HierarchyCache.getInstance().getProjectContextTypeHierarchy(constraints);

    // check that the company form is not part of the hierarchy initially (not abstract)
    SdkAssert.assertFalse(projectContextTypeHierarchy.isCreated());
    SdkAssert.assertFalse(projectContextTypeHierarchy.contains(companyForm));
    SdkAssert.assertTrue(projectContextTypeHierarchy.isCreated());

    // make the company form abstract
    ICompilationUnit icu = companyForm.getCompilationUnit();
    try {
      icu.becomeWorkingCopy(null);
      String newSrc = icu.getSource().replace("public class CompanyForm extends", "public abstract class CompanyForm extends");
      icu.getBuffer().setContents(newSrc);
      icu.getBuffer().save(new NullProgressMonitor(), true);
      icu.commitWorkingCopy(true, null);
    }
    finally {
      icu.discardWorkingCopy();
    }

    // check that the company form is now part of the hierarchy
    SdkAssert.assertFalse(projectContextTypeHierarchy.isCreated());
    SdkAssert.assertTrue(projectContextTypeHierarchy.contains(companyForm));
    SdkAssert.assertTrue(projectContextTypeHierarchy.isCreated());
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
