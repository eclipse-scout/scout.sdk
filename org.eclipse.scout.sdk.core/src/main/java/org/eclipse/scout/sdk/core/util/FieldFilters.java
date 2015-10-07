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
package org.eclipse.scout.sdk.core.util;

import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;

/**
 * Contains {@link IFilter}s for {@link IField}s.
 */
public final class FieldFilters {

  private FieldFilters() {
  }

  /**
   * Creates and returns a new {@link IFilter} that evaluates to <code>true</code> if a field name (
   * {@link IField#name()}) matches the given name.
   *
   * @param name
   *          The name for which the {@link IFilter} should evaluate to <code>true</code>
   * @return The new created {@link IFilter} matching the given name.
   */
  public static IFilter<IField> name(final String name) {
    return new IFilter<IField>() {
      @Override
      public boolean evaluate(IField field) {
        return Objects.equals(field.elementName(), name);
      }
    };
  }

  /**
   * Creates and returns a new {@link IFilter} that evaluates to <code>true</code> if a field name {@link IField#name()}
   * ) matches the given regular expression.
   *
   * @param regex
   *          The regex for which the {@link IFilter} should evaluate to <code>true</code>.
   * @return The new created {@link IFilter} matching the given regular expression.
   * @see Pattern
   */
  public static IFilter<IField> nameRegex(final Pattern regex) {
    return new IFilter<IField>() {
      @Override
      public boolean evaluate(IField field) {
        return regex.matcher(field.elementName()).matches();
      }
    };
  }

  /**
   * Creates and returns a new {@link IFilter} that evaluates to <code>true</code> if a field has at least all of the
   * given flags ( {@link IField#flags()}).
   *
   * @param flags
   *          The flags for which the {@link IFilter} should evaluate to <code>true</code>
   * @return The new created {@link IFilter} matching all the given flags.
   * @see Flags
   */
  public static IFilter<IField> flags(final int flags) {
    return new IFilter<IField>() {
      @Override
      public boolean evaluate(IField field) {
        return (flags & field.flags()) == flags;
      }
    };
  }
}
