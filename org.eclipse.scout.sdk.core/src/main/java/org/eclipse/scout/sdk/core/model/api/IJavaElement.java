/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.api;

import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;

/**
 * <h3>{@link IJavaElement}</h3>Represents a Java element.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public interface IJavaElement {

  /**
   * Gets the {@link IJavaEnvironment} this element belongs to.
   *
   * @return The owning {@link IJavaEnvironment}.
   */
  IJavaEnvironment javaEnvironment();

  /**
   * @return the name of this element.
   */
  String elementName();

  /**
   * @return The source of the element. Never returns <code>null</code>. Use {@link ISourceRange#isAvailable()} to check
   *         if source is actually available for this element.<br>
   *         The source is only available if the compilation unit is one of the following:
   *         <ul>
   *         <li>source in workspace</li>
   *         <li>class in jar and source attachment to jar is defined</li>
   *         </ul>
   */
  ISourceRange source();

  /**
   * Unwraps the java element into its underlying SPI class.
   *
   * @return The service provider interface that belongs to the receiver.
   */
  JavaElementSpi unwrap();
}
