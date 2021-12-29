/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.namespace;

/**
 * Fixture copy of the INamespace class only existing in Scout >= 22. This class is required so that tests with Scout <
 * 22 compiles.
 */
public interface INamespace {

  /**
   * Namespace IDs should be lowercase.
   *
   * @return non-empty unique ID for the namespace.
   */
  String getId();

  /**
   * @return Order of namespace
   */
  double getOrder();
}
