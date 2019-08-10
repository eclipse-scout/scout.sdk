/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;

/**
 * <h3>{@link JaxbBindingGenerator}</h3>
 *
 * @since 7.0.0
 */
public class JaxbBindingGenerator implements ISourceGenerator<ISourceBuilder<?>> {
  @Override
  public void generate(ISourceBuilder<?> builder) {
    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").nl();
    builder.append("<!-- binding to customize xsd schema artifacts (jaxb-namespace: http://java.sun.com/xml/ns/jaxb) -->").nl();
    builder.append("<bindings xmlns=\"http://java.sun.com/xml/ns/jaxb\" version=\"2.1\">").nl();
    builder.append("</bindings>").nl();
  }
}
