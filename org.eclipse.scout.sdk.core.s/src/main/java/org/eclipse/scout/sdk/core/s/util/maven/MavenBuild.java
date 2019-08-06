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
package org.eclipse.scout.sdk.core.s.util.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * <h3>{@link MavenBuild}</h3> Represents a Maven Build with all arguments.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class MavenBuild {

  private File m_workingDirectory;
  private final Set<String> m_goals;
  private final Map<String, String> m_properties;
  private final Set<String> m_options;

  public MavenBuild() {
    m_properties = new LinkedHashMap<>();
    m_goals = new LinkedHashSet<>();
    m_options = new LinkedHashSet<>();

    // default properties
    withProperty("master_sanityCheck_skip", "true") // workaround for animal sniffer bug that does not close zip files! See https://github.com/mojohaus/animal-sniffer/pull/52
        .withProperty("master_coverage_skip", "true")
        .withProperty("master_test_forkCount", "1")
        .withProperty("master_test_runOrder", "filesystem")
        .withProperty("master_git-id_skip", "true")
        .withProperty("master_enforcerCheck_skip", "true");
  }

  /**
   * @return The working directory of the build.
   */
  public File getWorkingDirectory() {
    return m_workingDirectory;
  }

  /**
   * Sets the working directory of this {@link MavenBuild}.
   *
   * @param workingDirectory
   *          The new working directory. May not be <code>null</code>.
   * @return this
   */
  public MavenBuild withWorkingDirectory(File workingDirectory) {
    m_workingDirectory = Validate.notNull(workingDirectory);
    return this;
  }

  /**
   * Gets all options in a {@link Set}.
   *
   * @return A {@link Set} containing all options.
   */
  public Set<String> getOptions() {
    return Collections.unmodifiableSet(m_options);
  }

  /**
   * Gets if this {@link MavenBuild} has the given option set.
   *
   * @param option
   *          The option to check.
   * @return <code>true</code> if this build contains the given option. <code>false</code> otherwise.
   */
  public boolean hasOption(String option) {
    return m_options.contains(option);
  }

  /**
   * Gets if this {@link MavenBuild} has the given option set.
   *
   * @param option
   *          The option to check.
   * @return <code>true</code> if this build contains the given option. <code>false</code> otherwise.
   */
  public boolean hasOption(char option) {
    return hasOption(Character.toString(option));
  }

  /**
   * Adds the given option.
   *
   * @param option
   *          The option to add.
   * @return this
   */
  public MavenBuild withOption(char option) {
    return withOption(Character.toString(option));
  }

  /**
   * Adds the given option.
   *
   * @param noValueOption
   *          The option to add.
   * @return this
   */
  public MavenBuild withOption(String option) {
    Validate.isTrue(StringUtils.isNotBlank(option));
    m_options.add(option);
    return this;
  }

  /**
   * @return All goals of this {@link MavenBuild}.
   */
  public Set<String> getGoals() {
    return Collections.unmodifiableSet(m_goals);
  }

  /**
   * Adds a new goal to this {@link MavenBuild}.
   *
   * @param goals
   *          The goal to add.
   * @return this
   */
  public MavenBuild withGoal(String goals) {
    m_goals.add(goals);
    return this;
  }

  /**
   * Gets all properties and its values in a {@link List} of the form:<br>
   * <code>
   * ["keyOne=Value1", "keyTwo", "keyThree=Value3"]
   * </code>
   *
   * @return A {@link List} with all properties and its values
   */
  public List<String> getPropertiesAsList() {
    return getMapAsList(getProperties());
  }

  /**
   * Gets all properties in a {@link Map} where the key is the property key and the value is the property value.
   *
   * @return A {@link Map} containing all properties with key and value.
   */
  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(m_properties);
  }

  /**
   * Adds the given no-value-property.
   *
   * @param property
   *          The property to add.
   * @return this
   */
  public MavenBuild withProperty(char property) {
    return withProperty(Character.toString(property));
  }

  /**
   * Adds the given no-value-property.
   *
   * @param noValueProperty
   *          The property to add.
   * @return this
   */
  public MavenBuild withProperty(String noValueProperty) {
    return withProperty(noValueProperty, null);
  }

  /**
   * Adds a new property to the maven build
   *
   * @param key
   *          The key of the property (E.g. "myKey"). May not be <code>null</code>.
   * @param value
   *          The value of the property. May be <code>null</code>.
   */
  public MavenBuild withProperty(String key, String value) {
    Validate.isTrue(StringUtils.isNotBlank(key));
    m_properties.put(key.trim(), value);
    return this;
  }

  @Override
  @SuppressWarnings("pmd:NPathComplexity")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_goals == null) ? 0 : m_goals.hashCode());
    result = prime * result + ((m_options == null) ? 0 : m_options.hashCode());
    result = prime * result + ((m_properties == null) ? 0 : m_properties.hashCode());
    result = prime * result + ((m_workingDirectory == null) ? 0 : m_workingDirectory.hashCode());
    return result;
  }

  @Override
  @SuppressWarnings("pmd:NPathComplexity")
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MavenBuild other = (MavenBuild) obj;
    if (m_goals == null) {
      if (other.m_goals != null) {
        return false;
      }
    }
    else if (!m_goals.equals(other.m_goals)) {
      return false;
    }
    if (m_options == null) {
      if (other.m_options != null) {
        return false;
      }
    }
    else if (!m_options.equals(other.m_options)) {
      return false;
    }
    if (m_properties == null) {
      if (other.m_properties != null) {
        return false;
      }
    }
    else if (!m_properties.equals(other.m_properties)) {
      return false;
    }
    if (m_workingDirectory == null) {
      if (other.m_workingDirectory != null) {
        return false;
      }
    }
    else if (!m_workingDirectory.equals(other.m_workingDirectory)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Maven build in dir '").append(getWorkingDirectory()).append("': ");
    for (String goal : getGoals()) {
      builder.append(goal).append(' ');
    }
    for (String option : getOptions()) {
      builder.append('-').append(option).append(' ');
    }
    for (String prop : getPropertiesAsList()) {
      builder.append("-D").append(prop).append(' ');
    }
    return builder.toString();
  }

  /**
   * Converts the given property {@link Map} into a property {@link List}. The format is described in
   * {@link #getPropertiesAsList()}
   *
   * @param properties
   *          The property {@link Map} to convert.
   * @return The properties as {@link List}
   */
  public static List<String> getMapAsList(Map<String, String> properties) {
    List<String> props = new ArrayList<>(properties.size());
    for (Entry<String, String> prop : properties.entrySet()) {
      StringBuilder propBuilder = new StringBuilder(prop.getKey());
      if (prop.getValue() != null) {
        propBuilder.append('=');
        propBuilder.append(prop.getValue());
      }
      props.add(propBuilder.toString());
    }
    return props;
  }
}
