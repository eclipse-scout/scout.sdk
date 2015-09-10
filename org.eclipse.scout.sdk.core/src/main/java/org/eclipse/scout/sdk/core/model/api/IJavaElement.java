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

  IJavaEnvironment getJavaEnvironment();

  /**
   * @return the element name
   *         <p>
   *         this is the relative or simple name of the element inside its containing scope
   */
  String getElementName();

  /**
   * @return the source of the element or null. The source is only available if the compilation unit is one of the
   *         following
   *         <ul>
   *         <li>source in workspace</li>
   *         <li>class in jar and source attachement to jar is defined</li>
   *         </ul>
   */
  ISourceRange getSource();

  JavaElementSpi unwrap();

  void internalSetSpi(JavaElementSpi spi);
}
