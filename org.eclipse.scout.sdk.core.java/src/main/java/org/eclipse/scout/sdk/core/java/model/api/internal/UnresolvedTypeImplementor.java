/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.internal;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.SimpleGenerators;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.java.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IPackage;
import org.eclipse.scout.sdk.core.java.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.java.model.api.internal.UnresolvedTypeImplementor.UnresolvedTypeSpi;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractSpiElement;
import org.eclipse.scout.sdk.core.java.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.java.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

public class UnresolvedTypeImplementor extends AbstractJavaElementImplementor<UnresolvedTypeSpi> implements IUnresolvedType {

  UnresolvedTypeImplementor(UnresolvedTypeSpi spi) {
    super(spi);
  }

  @Override
  public String name() {
    return m_spi.getName();
  }

  @Override
  public IPackage containingPackage() {
    return m_spi.getPackage();
  }

  @Override
  public String reference() {
    return name();
  }

  /**
   * @return true if {@link #type()} returns a non-empty {@link Optional}.
   */
  @Override
  public boolean exists() {
    return type().isPresent();
  }

  @Override
  public Optional<IType> type() {
    return Optional.ofNullable(m_spi.getType());
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    return type()
        .map(IType::children)
        .orElseGet(Stream::empty);
  }

  @Override
  public ITypeGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return SimpleGenerators.createUnresolvedTypeGenerator(this, transformer);
  }

  @Override
  public ITypeGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }

  public static final class UnresolvedTypeSpi extends AbstractSpiElement<IUnresolvedType> implements JavaElementSpi {
    private final IType m_type;
    private final String m_name;
    private final IPackage m_package;
    private final String m_simpleName;

    public UnresolvedTypeSpi(JavaEnvironmentSpi env, IType type) {
      super(env);

      m_type = type;
      m_name = type.name();
      m_package = type.compilationUnit()
          .map(ICompilationUnit::containingPackage)
          .orElseGet(() -> env.getPackage(null).wrap());
      m_simpleName = type.elementName();
    }

    public UnresolvedTypeSpi(JavaEnvironmentSpi env, String name) {
      super(env);

      m_type = null;
      m_name = name;
      var dot = name.lastIndexOf(JavaTypes.C_DOT);
      if (dot > 0) {
        m_package = env.getPackage(name.substring(0, dot)).wrap();
        m_simpleName = name.substring(dot + 1);
      }
      else {
        m_package = env.getPackage(null).wrap();
        m_simpleName = name;
      }
    }

    @Override
    public String getElementName() {
      return m_simpleName;
    }

    @Override
    public ISourceRange getSource() {
      if (m_type == null) {
        return null;
      }
      return m_type.unwrap().getSource();
    }

    public IPackage getPackage() {
      return m_package;
    }

    public String getName() {
      return m_name;
    }

    public IType getType() {
      return m_type;
    }

    @Override
    public TreeVisitResult acceptPreOrder(IDepthFirstJavaElementVisitor visitor, int level, int index) {
      return visitor.preVisit(wrap(), level, index);
    }

    @Override
    public boolean acceptPostOrder(IDepthFirstJavaElementVisitor visitor, int level, int index) {
      return visitor.postVisit(wrap(), level, index);
    }

    @Override
    public TreeVisitResult acceptLevelOrder(IBreadthFirstJavaElementVisitor visitor, int level, int index) {
      return visitor.visit(wrap(), level, index);
    }

    @Override
    protected IUnresolvedType internalCreateApi() {
      return new UnresolvedTypeImplementor(this);
    }

    @Override
    public JavaElementSpi internalFindNewElement() {
      return null;
    }
  }
}
