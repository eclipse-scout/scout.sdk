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
package org.eclipse.scout.sdk.internal.test.operation.jdt;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithJdtTestProject;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.testing.compare.CompareUtility;
import org.eclipse.scout.sdk.testing.compare.ICompareResult;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link SourceFormatOperationTest}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 08.04.2013
 */
public class SourceFormatOperationTest extends AbstractSdkTestWithJdtTestProject {

  @Test
  public void testFormatCompilationUnit() throws Exception {
    //  create unformatted compilationunit
    CompilationUnitNewOperation icuNewOp = new CompilationUnitNewOperation("FormatTestIcu01.java", "jdt.test.client.format.output", getClientJavaProject()) {
      @Override
      protected void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("package ").append(getPackageFragmentName()).append(";");
        source.append("public class FormatTestIcu01{public FormatTestIcu01(){}}");
      }
    };
    executeBuildAssertNoCompileErrors(icuNewOp);
    ICompilationUnit icu = icuNewOp.getCreatedCompilationUnit();
    SdkAssert.assertExist(icu);
    JavaElementFormatOperation formatOp = new JavaElementFormatOperation(icu, true);
    executeBuildAssertNoCompileErrors(formatOp);

    ICompareResult<String> result = CompareUtility.compareSource(icu, ((IFile) getClientProject().findMember("resources/sourceReferences/format/FormatTestIcu01.txt")).getContents(), true);
    if (!result.isEqual()) {
      Assert.fail(result.toString());
    }
  }

  @Test
  public void testFormatMethodOperationTest() throws Exception {
    CompilationUnitNewOperation icuNewOp = new CompilationUnitNewOperation("FormatMethodTest01.java", "jdt.test.client.format.output", getClientJavaProject()) {
      @Override
      protected void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        TestWorkspaceUtility.copyTemplateToBuffer(getClientProject(), "resources/sourceReferences/format/FormatMethodTest01Input.txt", source, lineDelimiter);
      }
    };
    executeBuildAssertNoCompileErrors(icuNewOp);

    ICompilationUnit icu = icuNewOp.getCreatedCompilationUnit();
    SdkAssert.assertExist(icu);
    IType type = SdkAssert.assertTypeExists(icu, "FormatMethodTest01");
    IMethod method = SdkAssert.assertMethodExist(type, "toBeFormatted");
    JavaElementFormatOperation formatOp = new JavaElementFormatOperation(method, true);
    executeBuildAssertNoCompileErrors(formatOp);

    ICompareResult<String> result = CompareUtility.compareSource(icu, ((IFile) getClientProject().findMember("resources/sourceReferences/format/FormatMethodTest01Output.txt")).getContents(), true);
    if (!result.isEqual()) {
      Assert.fail(result.toString());
    }
  }
}
