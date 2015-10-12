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

import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link ResourceBuilder}</h3>
 *
 * @author Ivan Motsch
 */
public class ResourceBuilder implements IResourceBuilder {
  private final String m_packageName;
  private final String m_fileName;
  private final List<IResourceFragmentBuilder> m_fragments = new ArrayList<>();
  private final List<IResourceFragmentBuilder> m_footerSourceBuilders = new ArrayList<>();

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

    // footer
    for (IResourceFragmentBuilder f : m_footerSourceBuilders) {
      f.createResource(source, lineDelimiter, context);
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
  public void addFooter(IResourceFragmentBuilder footerSourceBuilder) {
    m_footerSourceBuilders.add(footerSourceBuilder);
  }

  @Override
  public List<IResourceFragmentBuilder> getFooters() {
    return new ArrayList<>(m_footerSourceBuilders);
  }
}
