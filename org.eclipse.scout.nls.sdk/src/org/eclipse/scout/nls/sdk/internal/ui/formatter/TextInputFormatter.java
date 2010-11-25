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
package org.eclipse.scout.nls.sdk.internal.ui.formatter;

public class TextInputFormatter<T> implements IInputFormatter<T> {

  public String format(Object source, T value) {
    return (String) value;
  }

  @SuppressWarnings("unchecked")
  public T parse(Object source, String input) {
    return (T) input;

  }

}
