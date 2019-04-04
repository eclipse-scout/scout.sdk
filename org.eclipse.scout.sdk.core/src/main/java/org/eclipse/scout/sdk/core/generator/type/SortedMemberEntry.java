/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.generator.type;

import static org.eclipse.scout.sdk.core.model.api.Flags.isFinal;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPrivate;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPublic;
import static org.eclipse.scout.sdk.core.model.api.Flags.isStatic;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * <h3>{@link SortedMemberEntry}</h3>
 *
 * @since 6.1.0
 */
public class SortedMemberEntry implements Comparable<SortedMemberEntry> {

  public static final int PARSED_ORDER = 10;
  public static final int DEFAULT_ORDER = 20;
  public static final int FIELD_ORDER = 100000;
  public static final int METHOD_ORDER = 200000;
  public static final int TYPE_ORDER = 300000;
  private static final AtomicLong INPUT_ORDER = new AtomicLong();

  private final IMemberGenerator<?> m_generator;
  private final FinalValue<CompositeObject> m_sortOrder;
  private final long m_index;

  protected SortedMemberEntry(IField origin, IWorkingCopyTransformer transformer) {
    this(Ensure.notNull(origin).toWorkingCopy(transformer), origin);
  }

  public SortedMemberEntry(IType origin, IWorkingCopyTransformer transformer) {
    this(Ensure.notNull(origin).toWorkingCopy(transformer), origin);
  }

  public SortedMemberEntry(IMethod origin, IWorkingCopyTransformer transformer) {
    this(Ensure.notNull(origin).toWorkingCopy(transformer), origin);
  }

  public SortedMemberEntry(IMemberGenerator<?> generator, IJavaElement origin) {
    this(generator,
        PARSED_ORDER, origin.source()
            .map(range -> (long) range.start())
            .orElseGet(INPUT_ORDER::getAndIncrement));
  }

  public SortedMemberEntry(IMemberGenerator<?> generator) {
    this(generator, (Object[]) null);
  }

  public SortedMemberEntry(IMemberGenerator<?> generator, Object... sortOrder) {
    m_generator = Ensure.notNull(generator);
    m_sortOrder = new FinalValue<>();
    if (sortOrder != null && sortOrder.length > 0) {
      m_sortOrder.set(new CompositeObject(sortOrder));
      m_index = 0;
    }
    else {
      m_index = INPUT_ORDER.getAndIncrement();
    }
  }

  public IMemberGenerator<?> generator() {
    return m_generator;
  }

  public CompositeObject sortOrder() {
    return m_sortOrder.computeIfAbsentAndGet(this::calculateDefaultOrder);
  }

  @SuppressWarnings("unchecked")
  protected CompositeObject calculateDefaultOrder() {
    IMemberGenerator<?> generator = generator();
    if (isType()) {
      return defaultTypeOrder((ITypeGenerator<?>) generator, m_index);
    }
    if (isMethod()) {
      return defaultMethodOrder((IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>) generator, m_index);
    }
    return defaultFieldOrder((IFieldGenerator<?>) generator, m_index);
  }

  public boolean isMethod() {
    return hasType(IMethodGenerator.class);
  }

  public boolean isType() {
    return hasType(ITypeGenerator.class);
  }

  public boolean isField() {
    return hasType(IFieldGenerator.class);
  }

  public boolean hasType(Class<?> type) {
    return type.isAssignableFrom(generator().getClass());
  }

  @Override
  public int compareTo(SortedMemberEntry o) {
    return sortOrder().compareTo(o.sortOrder());
  }

  @Override
  public int hashCode() {
    return sortOrder().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SortedMemberEntry other = (SortedMemberEntry) obj;
    return sortOrder().equals(other.sortOrder());
  }

  protected static CompositeObject defaultFieldOrder(IFieldGenerator<?> generator, long insertionOrder) {
    int flags = generator.flags();
    boolean isFinal = isFinal(flags);
    boolean isConstant = isStatic(flags) && isFinal;
    boolean isSerialVersionUid = isConstant && isPrivate(flags) && "serialVersionUID".equals(generator.elementName().orElse(null));

    int pos;
    if (isSerialVersionUid) {
      pos = 1000;
    }
    else if (isConstant) {
      pos = 2000;
    }
    else if (isFinal) {
      pos = 3000;
    }
    else {
      pos = 4000;
    }

    return new CompositeObject(DEFAULT_ORDER, FIELD_ORDER, pos, insertionOrder);
  }

  protected static CompositeObject defaultTypeOrder(ITypeGenerator<?> generator, long insertionOrder) {
    int pos;
    int flags = generator.flags();
    if (isPublic(flags)) {
      pos = 1000;
    }
    else if (isStatic(flags)) {
      pos = 3000;
    }
    else {
      pos = 2000;
    }

    return new CompositeObject(DEFAULT_ORDER, TYPE_ORDER, pos, insertionOrder);
  }

  protected static CompositeObject defaultMethodOrder(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> generator, long insertionOrder) {
    boolean isConstructor = !generator.returnType().isPresent();
    int pos;
    if (isConstructor) {
      pos = 1000;
    }
    else if (isStatic(generator.flags())) {
      pos = 3000;
    }
    else {
      CharSequence methodName = generator.elementName().orElseThrow(() -> newFail("Method name is missing for generator {}", generator));
      boolean isGetterOrSetter = PropertyBean.BEAN_METHOD_NAME.matcher(methodName).matches();
      if (isGetterOrSetter) {
        pos = 2000;
      }
      else {
        pos = 4000;
      }
    }

    return new CompositeObject(DEFAULT_ORDER, METHOD_ORDER, pos, insertionOrder);
  }

  public static Object[] createDefaultMethodPos(int order) {
    return new Object[]{DEFAULT_ORDER, METHOD_ORDER, order};
  }

  public static Object[] createDefaultTypePos(int order) {
    return new Object[]{DEFAULT_ORDER, TYPE_ORDER, order};
  }

  public static Object[] createDefaultFieldPos(int order) {
    return new Object[]{DEFAULT_ORDER, FIELD_ORDER, order};
  }
}
