/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.js.element;

import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;

public class JsElementModelMethodNode implements IJsElementModelMethodNode {

  private final IMethod m_method;

  protected JsElementModelMethodNode(IMethod method) {
    m_method = Ensure.notNull(method);
  }

  public static JsElementModelMethodNode create(IMethod method) {
    return new JsElementModelMethodNode(method);
  }

  @Override
  public IMethod method() {
    return m_method;
  }

  @Override
  public String name() {
    return method().elementName();
  }

  @Override
  public Optional<IType> returnType() {
    return method().returnType();
  }

  @Override
  public IType requireReturnType() {
    return method().requireReturnType();
  }

  @Override
  public String sourceOfBody() {
    return method()
        .sourceOfBody()
        .map(ISourceRange::asCharSequence)
        .map(CharSequence::toString)
        .map(String::trim)
        .orElse("");
  }
}
