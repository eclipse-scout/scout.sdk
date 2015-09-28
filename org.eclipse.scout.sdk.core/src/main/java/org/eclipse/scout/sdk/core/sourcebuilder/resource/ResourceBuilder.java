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
package org.eclipse.scout.sdk.core.sourcebuilder.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkConsole;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link ResourceBuilder}</h3>
 *
 * @author Ivan Motsch
 */
public class ResourceBuilder implements IResourceBuilder {
  private final String m_packageName;
  private final String m_fileName;
  private final List<IResourceFragmentBuilder> m_fragments = new ArrayList<>();
  private final StringBuilder m_errors = new StringBuilder();

  /**
   * @param packageName
   * @param fileName
   */
  public ResourceBuilder(String packageName, String fileName) {
    m_packageName = packageName;
    m_fileName = fileName;
  }

  @Override
  public void createResource(StringBuilder source, String lineDelimiter, PropertyMap context) {
    for (IResourceFragmentBuilder builder : m_fragments) {
      if (builder != null) {
        builder.createResource(source, lineDelimiter, context);
      }
    }
  }

  @Override
  public String getPackageName() {
    return m_packageName;
  }

  @Override
  public String getFileName() {
    return m_fileName;
  }

  @Override
  public void addFragment(IResourceFragmentBuilder fragment) {
    m_fragments.add(fragment);
  }

  @Override
  public List<IResourceFragmentBuilder> getFragments() {
    return Collections.unmodifiableList(m_fragments);
  }

  @Override
  public void addErrorMessage(String taskType, String msg, Throwable... exceptions) {
    SdkLog.warning(getPackageName() + " " + getFileName() + " " + msg, exceptions);
    if (msg != null) {
      m_errors.append(taskType + " [generator] " + msg);
      m_errors.append("\n");
    }
    if (exceptions != null) {
      for (Throwable t : exceptions) {
        m_errors.append(SdkConsole.getStackTrace(t));
        m_errors.append("\n");
      }
    }
  }

  protected void appendErrorMessages(StringBuilder source, String lineDelimiter, PropertyMap context, IImportCollector validator) {
    if (m_errors.length() > 0) {
      source.append("/*");
      source.append(lineDelimiter);
      source.append(m_errors.toString().replace("\n", lineDelimiter));
      source.append(lineDelimiter);
      source.append("*/");
    }
  }
}
