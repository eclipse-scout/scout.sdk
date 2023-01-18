/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.junit.jupiter.api.Test;

public class WebServiceFormPageInputTest {

  @Test
  public void testCalcDisplayName() {
    assertEquals("Test", createInput("test.wsdl").getDisplayName());
    assertEquals("Test Name", createInput("Test-name - PortType").getDisplayName());
  }

  protected static WebServiceFormPageInput createInput(String fileName) {
    var jp = mock(IJavaProject.class);
    when(jp.exists()).thenReturn(true);
    return new WebServiceFormPageInput(Paths.get(fileName), jp, mock(IScoutApi.class));
  }
}
