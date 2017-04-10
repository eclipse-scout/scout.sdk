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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.internal.trigger.DerivedResourceManager;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * <h3>{@link DerivedResourceManagerMock}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class DerivedResourceManagerMock {

  private final PlatformMocks m_platformMocks;

  public DerivedResourceManagerMock(PlatformMocks mocks) {
    m_platformMocks = mocks;
  }

  @SuppressWarnings("unchecked")
  public void install() {
    DerivedResourceManager manager = mock(DerivedResourceManager.class);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Set<IResource> resources = invocation.getArgument(0);
        trigger(resources);
        return null;
      }
    }).when(manager).trigger(any(Set.class));

    S2ESdkActivator activator = new S2ESdkActivator();

    try {
      Field derivedResourceManager = S2ESdkActivator.class.getDeclaredField("m_derivedResourceManager");
      derivedResourceManager.setAccessible(true);
      derivedResourceManager.set(activator, manager);

      Field plugin = S2ESdkActivator.class.getDeclaredField("plugin");
      plugin.setAccessible(true);
      plugin.set(null, activator);
    }
    catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      throw new SdkException(e);
    }
  }

  protected void trigger(Set<IResource> resources) {
    if (resources == null) {
      return;
    }
    for (IResource modelResource : resources) {
      if (modelResource != null) {
        String modelTypeFqn = modelResource.getLocation().toString();
        IType createdDto = null;
        IJavaEnvironment javaEnvironment = getJavaEnvironment();
        if (modelTypeFqn.endsWith(ISdkProperties.SUFFIX_PAGE_WITH_TABLE)) {
          createdDto = CoreScoutTestingUtils.createPageDataAssertNoCompileErrors(modelTypeFqn, javaEnvironment, javaEnvironment);
        }
        else {
          createdDto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(modelTypeFqn, javaEnvironment, javaEnvironment);
        }
        if (createdDto != null) {
          // write the new DTO content to the compilation unit
          String newDtoSource = createdDto.compilationUnit().source().toString();
          IBuffer buffer = m_platformMocks.getMockFactory().getBufferFor(createdDto.name());
          buffer.setContents(newDtoSource);
        }
      }
    }
  }

  public IJavaEnvironment getJavaEnvironment() {
    return m_platformMocks.getMockFactory().getJavaEnvironment();
  }
}
