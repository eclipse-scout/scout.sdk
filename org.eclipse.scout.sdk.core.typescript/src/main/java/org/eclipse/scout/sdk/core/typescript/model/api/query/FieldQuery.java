/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.query;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.query.AbstractQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * By default, the query returns all fields directly declared in an {@link IES6Class}.
 */
public class FieldQuery extends AbstractQuery<IField> {

  private final ES6ClassSpi m_es6ClassSpi;
  private String m_name;
  private Modifier m_requiredModifier;
  private Modifier m_notAllowedModifier;

  public FieldQuery(ES6ClassSpi es6Class) {
    m_es6ClassSpi = Ensure.notNull(es6Class);
  }

  /**
   * Limits the {@link IField} to the one with the given name.
   *
   * @param name
   *          The name to search. Default is not filtering by name.
   * @return This query.
   */
  public FieldQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String getName() {
    return m_name;
  }

  /**
   * Limit the {@link IField fields} to the ones not having the given {@link Modifier}. Default is not filtering on
   * {@link Modifier}.
   * 
   * @param modifier
   *          All {@link IField fields} having this {@link Modifier} will be excluded. If {@code null} nothing is
   *          excluded.
   * @return This query.
   */
  public FieldQuery withoutModifier(Modifier modifier) {
    m_notAllowedModifier = modifier;
    return this;
  }

  protected Modifier getNotAllowedModifier() {
    return m_notAllowedModifier;
  }

  /**
   * Limit the {@link IField fields} to the ones having the given {@link Modifier}. Default is not filtering on
   * {@link Modifier}.
   *
   * @param modifier
   *          Only {@link IField fields} having this {@link Modifier} will be included. If {@code null} nothing is
   *          filtered.
   * @return This query.
   */
  public FieldQuery withModifier(Modifier modifier) {
    m_requiredModifier = modifier;
    return this;
  }

  protected Modifier getRequiredModifier() {
    return m_requiredModifier;
  }

  protected ES6ClassSpi es6Class() {
    return m_es6ClassSpi;
  }

  @Override
  protected Stream<IField> createStream() {
    return es6Class().fields().stream()
        .filter(this::test)
        .map(FieldSpi::api);
  }

  protected boolean test(FieldSpi field) {
    var name = getName();
    if (name != null && !name.equals(field.name())) {
      return false;
    }

    var requiredModifier = getRequiredModifier();
    if (requiredModifier != null && !field.hasModifier(requiredModifier)) {
      return false;
    }

    var notAllowedModifier = getNotAllowedModifier();
    return notAllowedModifier == null || !field.hasModifier(notAllowedModifier);
  }
}
