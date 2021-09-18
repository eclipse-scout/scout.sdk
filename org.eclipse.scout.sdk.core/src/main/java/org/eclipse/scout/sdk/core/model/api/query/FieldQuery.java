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
package org.eclipse.scout.sdk.core.model.api.query;

import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.spliterator.HierarchicalStreamBuilder;
import org.eclipse.scout.sdk.core.model.api.spliterator.WrappingSpliterator;

/**
 * <h3>{@link FieldQuery}</h3> Field query that by default returns all {@link IField}s directly declared on the owner.
 *
 * @since 5.1.0
 */
public class FieldQuery extends AbstractQuery<IField> implements Predicate<IField> {

  private final IType m_type;

  private boolean m_includeSuperClasses;
  private boolean m_includeSuperInterfaces;

  private String m_name;
  private int m_flags = -1;

  public FieldQuery(IType type) {
    m_type = type;
  }

  protected IType getType() {
    return m_type;
  }

  /**
   * Include or exclude super types visiting when searching for {@link IField}s.
   *
   * @param b
   *          {@code true} if all super classes and super interfaces should be checked for {@link IField}s. Default is
   *          {@code false}.
   * @return this
   */
  public FieldQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Include or exclude super class visiting when searching for {@link IField}s.
   *
   * @param b
   *          {@code true} if all super classes should be checked for {@link IField}s. Default is {@code false}.
   * @return this
   */
  public FieldQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  /**
   * Include or exclude super interface visiting when searching for {@link IField}s.
   *
   * @param b
   *          {@code true} if all super interfaces should be checked for {@link IField}s. Default is {@code false}.
   * @return this
   */
  public FieldQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  protected boolean isIncludeSuperInterfaces() {
    return m_includeSuperInterfaces;
  }

  /**
   * Limit the {@link IField}s to the ones having at least all of the given flags.
   *
   * @param flags
   *          The flags that must exist on the {@link IField}.
   * @return this
   * @see Flags
   */
  public FieldQuery withFlags(int flags) {
    m_flags = flags;
    return this;
  }

  protected int getFlags() {
    return m_flags;
  }

  /**
   * Limit the {@link IField}s to the given name (see {@link IField#elementName()}).
   *
   * @param name
   *          The {@link IField} name. Default is no filtering.
   * @return this
   */
  public FieldQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String getName() {
    return m_name;
  }

  /**
   * Tests if the given {@link IField} fulfills the filter criteria of this query.
   */
  @Override
  public boolean test(IField f) {
    var name = getName();
    if (name != null && !name.equals(f.elementName())) {
      return false;
    }

    var flags = getFlags();
    return flags < 0 || (f.flags() & m_flags) == m_flags;
  }

  protected static Spliterator<IField> getFieldsSpliterator(@SuppressWarnings("TypeMayBeWeakened") IType level) {
    return new WrappingSpliterator<>(level.unwrap().getFields());
  }

  @Override
  protected Stream<IField> createStream() {

    return new HierarchicalStreamBuilder<IField>()
        .withSuperClasses(isIncludeSuperClasses())
        .withSuperInterfaces(isIncludeSuperInterfaces())
        .withStartType(true)
        .build(getType(), FieldQuery::getFieldsSpliterator)
        .filter(this);
  }
}
