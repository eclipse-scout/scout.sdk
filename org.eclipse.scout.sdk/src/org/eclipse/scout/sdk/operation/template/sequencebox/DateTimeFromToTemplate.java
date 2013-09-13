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
package org.eclipse.scout.sdk.operation.template.sequencebox;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;

public class DateTimeFromToTemplate extends DateFromToTemplate {

  @Override
  public String getName() {
    return "Date-Time FROM-TO";
  }

  @Override
  protected void fillFromFieldBuilder(ITypeSourceBuilder fromFieldBuilder) throws CoreException {
    super.fillFromFieldBuilder(fromFieldBuilder);
    createGetConfiguredHasTimeMethod(fromFieldBuilder);
  }

  @Override
  protected void fillToFieldBuilder(ITypeSourceBuilder toFieldBuilder) throws CoreException {
    super.fillToFieldBuilder(toFieldBuilder);
    createGetConfiguredHasTimeMethod(toFieldBuilder);
  }

  protected void createGetConfiguredHasTimeMethod(ITypeSourceBuilder builder) throws CoreException {
    IMethodSourceBuilder methodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(builder, "getConfiguredHasTime");
    methodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return true;"));
    builder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(methodBuilder), methodBuilder);
  }

}
