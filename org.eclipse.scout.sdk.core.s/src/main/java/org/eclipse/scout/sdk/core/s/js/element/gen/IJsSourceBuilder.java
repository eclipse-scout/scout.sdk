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
package org.eclipse.scout.sdk.core.s.js.element.gen;

public interface IJsSourceBuilder<TYPE extends IJsSourceBuilder<TYPE>> {

  String source();

  TYPE withLineSeparator(String lineSeparator);

  TYPE append(String s);

  TYPE append(CharSequence cs);

  TYPE append(boolean b);

  TYPE append(Number n);

  TYPE nl();

  TYPE space();

  TYPE stringLiteral(CharSequence literalValue);

  TYPE nullLiteral();

  TYPE colon();

  TYPE semicolon();

  TYPE comma();

  TYPE arrow();

  TYPE parenthesisOpen();

  TYPE parenthesisClose();

  TYPE objectStart();

  TYPE objectEnd();

  TYPE arrayStart();

  TYPE arrayEnd();

  TYPE exportDefault();
}
