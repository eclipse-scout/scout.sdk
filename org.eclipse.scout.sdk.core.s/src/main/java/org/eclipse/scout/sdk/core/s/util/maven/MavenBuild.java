/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.util.maven;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link MavenBuild}</h3> Represents a Maven Build with all arguments.
 *
 * @since 5.2.0
 */
public class MavenBuild {

  public static final char OPTION_BATCH_MODE = 'B';
  public static final char OPTION_DEBUG = 'X';
  public static final char OPTION_UPDATE_SNAPSHOTS = 'U';
  public static final char OPTION_OFFLINE = 'o';
  public static final char OPTION_NON_RECURSIVE = 'N';
  public static final String PROPERTY_SKIP_TESTS = "skipTests";
  public static final String PROPERTY_SKIP_TEST_CREATION = "maven.test.skip";
  private final Set<String> m_goals;
  private final Map<String, String> m_properties;
  private final Set<String> m_options;
  private Path m_workingDirectory;

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

  /**
   * @return The working directory of the build.
   */
  public Path getWorkingDirectory() {
    return m_workingDirectory;
  }

  /**
   * Sets the working directory of this {@link MavenBuild}.
   *
   * @param workingDirectory
   *          The new working directory. May not be {@code null}.
   * @return this
   */
  public MavenBuild withWorkingDirectory(Path workingDirectory) {
    m_workingDirectory = Ensure.notNull(workingDirectory);
    return this;
  }

  /**
   * Gets all options in a {@link Set}.
   *
   * @return A {@link Set} containing all options.
   */
  public Set<String> getOptions() {
    return unmodifiableSet(m_options);
  }

  /**
   * Gets if this {@link MavenBuild} has the given option set.
   *
   * @param option
   *          The option to check.
   * @return {@code true} if this build contains the given option. {@code false} otherwise.
   */
  public boolean hasOption(String option) {
    return m_options.contains(option);
  }

  /**
   * Gets if this {@link MavenBuild} has the given option set.
   *
   * @param option
   *          The option to check.
   * @return {@code true} if this build contains the given option. {@code false} otherwise.
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
   * @param option
   *          The option to add.
   * @return this
   */
  public MavenBuild withOption(String option) {
    m_options.add(Ensure.notBlank(option));
    return this;
  }

  /**
   * Removes all options.
   *
   * @return this
   */
  public MavenBuild clearOptions() {
    m_options.clear();
    return this;
  }

  /**
   * @return All goals of this {@link MavenBuild}.
   */
  public Set<String> getGoals() {
    return unmodifiableSet(m_goals);
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
   * Clears all goals.
   *
   * @return this
   */
  public MavenBuild clearGoals() {
    m_goals.clear();
    return this;
  }

  /**
   * Gets all properties and its values in a {@link List} of the form:<br>
   * {@code
   * ["keyOne=Value1", "keyTwo", "keyThree=Value3"]
   * }
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
    return unmodifiableMap(m_properties);
  }

  /**
   * Removes all properties.
   *
   * @return this
   */
  public MavenBuild clearProperties() {
    m_properties.clear();
    return this;
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
   *          The property to add without "-D" prefix. Must not be {@code null}.
   * @return this
   */
  public MavenBuild withProperty(String noValueProperty) {
    return withProperty(noValueProperty, null);
  }

  /**
   * Adds a new property to the maven build.
   *
   * @param key
   *          The key of the property (E.g. "myKey") without "-D" prefix. Must not be {@code null}.
   * @param value
   *          The value of the property. May be {@code null}.
   */
  public MavenBuild withProperty(String key, String value) {
    m_properties.put(Ensure.notBlank(key).trim(), value);
    return this;
  }

  @Override
  @SuppressWarnings({"pmd:NPathComplexity", "NonFinalFieldReferencedInHashCode"})
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + m_goals.hashCode();
    result = prime * result + m_options.hashCode();
    result = prime * result + m_properties.hashCode();
    result = prime * result + (m_workingDirectory == null ? 0 : m_workingDirectory.hashCode());
    return result;
  }

  @Override
  @SuppressWarnings("NonFinalFieldReferenceInEquals")
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
    if (!m_goals.equals(other.m_goals)) {
      return false;
    }
    if (!m_options.equals(other.m_options)) {
      return false;
    }
    if (!m_properties.equals(other.m_properties)) {
      return false;
    }
    return Objects.equals(m_workingDirectory, other.m_workingDirectory);
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
}
