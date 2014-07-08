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
package org.eclipse.scout.sdk.workspace.type.config;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.exception.JavaElementNotExistException;
import org.eclipse.scout.sdk.operation.jdt.method.MethodNewOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.parser.IPropertySourceParser;

/**
 * <h3>{@link ConfigPropertyUpdateOperation}</h3> ...
 *
 * @author Andreas Hoegger
 * @since 3.8.0 26.02.2013
 */
public class ConfigPropertyUpdateOperation<T> implements IOperation {

  private final IPropertySourceParser<T> m_parser;
  private ConfigurationMethod m_method;
  private T m_value;

  public ConfigPropertyUpdateOperation(ConfigurationMethod method, IPropertySourceParser<T> parser) {
    m_method = method;
    m_parser = parser;
  }

  @Override
  public String getOperationName() {
    return "Update property '" + getMethod().getMethodName() + "'...";
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      if (CompareUtility.equals(getParser().parseSourceValue(getMethod().computeDefaultValue(), getMethod().getDefaultMethod(), getMethod().getSuperTypeHierarchy()), getValue())) {
        if (getMethod().isImplemented()) {
          // delete method
          ScoutMethodDeleteOperation deleteOp = new ScoutMethodDeleteOperation(getMethod().peekMethod());
          deleteOp.validate();
          deleteOp.run(monitor, workingCopyManager);
        }
      }
      else {
        if (getMethod().isImplemented()) {
          // update
          ScoutMethodDeleteOperation deleteOp = new ScoutMethodDeleteOperation(getMethod().peekMethod());
          deleteOp.validate();
          deleteOp.run(monitor, workingCopyManager);
          workingCopyManager.reconcile(getMethod().getType().getCompilationUnit(), monitor);
          m_method = ScoutTypeUtility.getConfigurationMethod(getMethod().getType(), getMethod().getMethodName(), getMethod().getSuperTypeHierarchy());
        }
        IMethodBodySourceBuilder bodyBuilder = new IMethodBodySourceBuilder() {

          @Override
          public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
            ConfigPropertyUpdateOperation.this.createSource(methodBuilder, source, lineDelimiter, ownerProject, validator);
          }
        };
        MethodNewOperation methodOverrideOp = new MethodNewOperation(MethodSourceBuilderFactory.createMethodSourceBuilder(getMethod().peekMethod(), getMethod().getType(), bodyBuilder), getMethod().getType());
        IStructuredType structuredType = ScoutTypeUtility.createStructuredType(getMethod().getType());
        methodOverrideOp.setSibling(structuredType.getSiblingMethodConfigGetConfigured(getMethod().getMethodName()));
        methodOverrideOp.setFormatSource(true);
        appendToMethodSourceBuilder(methodOverrideOp.getSourceBuilder());
        methodOverrideOp.run(monitor, workingCopyManager);
      }
    }
    catch (JavaElementNotExistException e) {
      ScoutSdk.logWarning(e);
    }
  }

  /**
   * To modify how the source of the method should be created.
   */
  protected void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    source.append("return ").append(getParser().formatSourceValue(getValue(), lineDelimiter, validator)).append(";");
  }

  /**
   * to modify the method source builder by subclasses
   *
   * @param sourceBuilder
   */
  protected void appendToMethodSourceBuilder(IMethodSourceBuilder sourceBuilder) {
  }

  public IPropertySourceParser<T> getParser() {
    return m_parser;
  }

  public ConfigurationMethod getMethod() {
    return m_method;
  }

  public T getValue() {
    return m_value;
  }

  public void setValue(T value) {
    this.m_value = value;
  }

}
