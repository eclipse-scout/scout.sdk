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
package org.eclipse.scout.sdk.core.model;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.collections4.Predicate;

/**
 * Contains predefined field filters
 */
public final class FieldFilters {

  private FieldFilters() {
  }

  public static Predicate<IField> getNameFilter(final String name) {
    return new Predicate<IField>() {
      @Override
      public boolean evaluate(IField field) {
        return Objects.equals(field.getName(), name);
      }
    };
  }

  public static Predicate<IField> getFlagsFilter(final int flags) {
    return new Predicate<IField>() {
      @Override
      public boolean evaluate(IField field) {
        return (flags & field.getFlags()) == flags;
      }
    };
  }

  public static Predicate<IField> getNameRegexFilter(final Pattern regex) {
    return new Predicate<IField>() {
      @Override
      public boolean evaluate(IField field) {
        return regex.matcher(field.getName()).matches();
      }
    };
  }

  public static Predicate<IField> getPrivateNotStaticNotFinalNotAbstract() {
    return new Predicate<IField>() {
      @Override
      public boolean evaluate(IField field) {
        int flags = field.getFlags();
        return Flags.isPrivate(flags) && !Flags.isStatic(flags) && !Flags.isFinal(flags) && !Flags.isAbstract(flags);
      }
    };
  }
}
