/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.testing.mock;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;

import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerationContext;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.classid.IClassIdGenerator;

/**
 * <h3>{@link PlatformMocks}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PlatformMocks {

  private final PlatformMockFactory m_factory;

  public PlatformMocks() {
    m_factory = new PlatformMockFactory();
  }

  public void initFieldMocks(Object testObj) {
    Field[] fields = testObj.getClass().getDeclaredFields();
    for (Field f : fields) {
      if (isJdtMock(f)) {
        Object mock = createMock(f);
        if (mock != null) {
          trySetMock(f, mock, testObj);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void initAutomaticClassIdGeneration() {
    if (ClassIdGenerators.isAutomaticallyCreateClassIdAnnotation()) {
      return;
    }

    ClassIdGenerators.generateNewId(new ClassIdGenerationContext("nothing")); // ensure the generators are loaded

    try {
      Field allGeneratorsOrderedField = ClassIdGenerators.class.getDeclaredField("allGeneratorsOrdered");
      allGeneratorsOrderedField.setAccessible(true);
      Collection<IClassIdGenerator> allGeneratorsOrdered = (Collection<IClassIdGenerator>) allGeneratorsOrderedField.get(null);
      if (allGeneratorsOrdered.isEmpty()) {
        IClassIdGenerator testingGenerator = new IClassIdGenerator() {
          @Override
          public String generate(ClassIdGenerationContext context) {
            return UUID.randomUUID().toString();
          }
        };
        allGeneratorsOrdered.add(testingGenerator);
      }
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      throw new SdkException(e);
    }

    ClassIdGenerators.setAutomaticallyCreateClassIdAnnotation(true);
  }

  public void initDerivedResourceManagerMock() {
    new DerivedResourceManagerMock(this).install();
  }

  public PlatformMockFactory getMockFactory() {
    return m_factory;
  }

  protected Object createMock(Field f) {
    Class<?> mockType = f.getType();
    return getMockFactory().createMock(mockType);
  }

  protected void trySetMock(Field field, Object mock, Object instance) {
    try {
      field.setAccessible(true);
      field.set(instance, mock);
    }
    catch (IllegalArgumentException | IllegalAccessException e) {
      throw new SdkException("Failed to set mock to field '" + field.getName() + "' in class '" + field.getDeclaringClass().getName() + "'.", e);
    }
  }

  protected boolean isJdtMock(Field f) {
    return f.isAnnotationPresent(PlatformMock.class);
  }
}
