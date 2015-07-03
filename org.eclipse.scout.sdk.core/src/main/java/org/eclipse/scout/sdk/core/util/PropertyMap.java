/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>{@link PropertyMap}</h3>
 * Property Map with typed getter.
 *
 * @since 5.1.0
 */
public class PropertyMap {

  private final Map<String, Object> m_props;

  public PropertyMap() {
    m_props = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  public <T extends Object> T getProperty(String key, Class<T> type) {
    return (T) m_props.get(key);
  }

  public void setProperty(String key, Object value) {
    m_props.put(key, value);
  }

  public Map<String, Object> getPropertiesMap() {
    return new HashMap<>(m_props);
  }

  public boolean containsKey(String key) {
    return m_props.containsKey(key);
  }
}
