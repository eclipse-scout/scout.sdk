/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.classid;

/**
 * <h3>{@link IClassIdGenerator}</h3> Class Id generator to be registered using the
 * 'org.eclipse.scout.sdk.classIdGenerator' extension point.<br>
 * Provides the default class id values when creating new class id annotations.<br>
 * The generators are evaluated in the prioritized order in which they have been registered in the plugin.xml file (high
 * priorities first). The first generator that provides an id (non-null value) for a type will be used. All subsequent
 * generators are not evaluated.<br>
 * Classes that implement this interface must provide a default constructor if they are used as classIdGenerator
 * extension.
 *
 * @author Matthias Villiger
 * @since 3.10.0 02.01.2014
 * @see ClassId
 * @see ITypeWithClassId
 * @see ClassIdGenerators
 * @see ClassIdGenerationContext
 */
public interface IClassIdGenerator {
  /**
   * Generates a new class id as string for the given input.
   *
   * @param context
   *          The {@link ClassIdGenerationContext} in which the annotation will be created.
   * @return The string containing the new Id
   */
  String generate(ClassIdGenerationContext context);
}
