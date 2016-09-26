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
package org.eclipse.scout.sdk.s2e.trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;

/**
 * <h3>{@link DefaultResourceChangeEventFilter}</h3> The default {@link IResourceChangeEvent} filter used by
 * {@link IDerivedResourceManager#setResourceChangeEventFilter(Predicate)}.
 *
 * @author Matthias Villiger
 * @since 6.1.0
 */
public class DefaultResourceChangeEventFilter implements Predicate<IResourceChangeEvent> {

  public static final String GIT_UPDATES = "org.eclipse.egit.";
  public static final String MAVEN_UPDATES = "org.eclipse.m2e.";
  public static final String SONAR_UPDATE = "org.sonarlint.";
  public static final String WEB_TOOLS_UPDATE = "org.eclipse.wst.";
  public static final String DEBUG_EVENT = "org.eclipse.debug.";
  public static final String ANNOTATION_PROCESSING_JOB = "org.eclipse.core.internal.events.NotificationManager.NotifyJob";
  public static final String ANOTATION_PROCESSING_BUILD = "org.eclipse.core.internal.events.AutoBuildJob";
  public static final String EXTERNAL_FOLDER_UPDATE = "org.eclipse.jdt.internal.core.ExternalFoldersManager.RefreshJob";
  public static final String DEBUG_INIT = "org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager.InitJob";
  public static final String REFRESH_JOB = "org.eclipse.core.internal.refresh.RefreshJob";
  public static final String WORKSPACE_INIT_JOB = "org.eclipse.jdt.internal.ui.InitializeAfterLoadJob.RealJob";
  public static final String TEAM_UPDATES = "org.eclipse.team.";

  private boolean m_isIgnoreBuildEvents;
  private boolean m_isIgnoreScoutSdkEvents;
  private Collection<String> m_excludedJobNamePrefixes;

  public DefaultResourceChangeEventFilter() {
    String[] exclusions = new String[]{GIT_UPDATES, MAVEN_UPDATES, SONAR_UPDATE, WEB_TOOLS_UPDATE, DEBUG_EVENT,
        ANNOTATION_PROCESSING_JOB, ANOTATION_PROCESSING_BUILD, EXTERNAL_FOLDER_UPDATE, DEBUG_INIT, REFRESH_JOB,
        WORKSPACE_INIT_JOB, TEAM_UPDATES};
    m_excludedJobNamePrefixes = new ArrayList<>(exclusions.length);
    Collections.addAll(m_excludedJobNamePrefixes, exclusions);

    m_isIgnoreBuildEvents = true;
    m_isIgnoreScoutSdkEvents = true;
  }

  public DefaultResourceChangeEventFilter(Collection<String> excludedJobNamePrefixes, boolean ignoreBuildEvents, boolean ignoreScoutSdkEvents) {
    m_excludedJobNamePrefixes = new ArrayList<>(Validate.notNull(excludedJobNamePrefixes));
    m_isIgnoreBuildEvents = ignoreBuildEvents;
    m_isIgnoreScoutSdkEvents = ignoreScoutSdkEvents;
  }

  @Override
  public boolean test(IResourceChangeEvent event) {
    if (event == null) {
      return false;
    }

    boolean ignoreBuildEvents = isIgnoreBuildEvents();
    if (ignoreBuildEvents && event.getBuildKind() != 0) {
      return false; // ignore build events
    }

    Job curJob = Job.getJobManager().currentJob();
    if (curJob == null) {
      return false;
    }

    if (isIgnoreScoutSdkEvents() && curJob instanceof AbstractJob) {
      // do not automatically update on Scout SDK changes. We expect the SDK to trigger manually where required.
      return false;
    }

    if (ignoreBuildEvents && (curJob.belongsTo(ResourcesPlugin.FAMILY_AUTO_BUILD) || curJob.belongsTo(ResourcesPlugin.FAMILY_MANUAL_BUILD))) {
      // ignore build changes (annotation processing)
      return false;
    }

    String jobFqn = curJob.getClass().getName().replace('$', '.');
    Predicate<String> excludedByNamePrefix = className -> getExcludedJobNamePrefixes().stream().anyMatch(prefix -> className.startsWith(prefix));
    if (excludedByNamePrefix.test(jobFqn)) {
      return false;
    }

    if (!"org.eclipse.core.internal.jobs.ThreadJob".equals(jobFqn)) {
      return true;
    }

    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    boolean isStackTraceElementExcluded = Arrays.stream(stackTrace)
        .map(element -> element.getClassName())
        .anyMatch(excludedByNamePrefix);
    return !isStackTraceElementExcluded;
  }

  /**
   * @return Gets if build events should be ignored. Default is <code>true</code>.
   */
  public boolean isIgnoreBuildEvents() {
    return m_isIgnoreBuildEvents;
  }

  /**
   * @param isIgnoreBuildEvents
   *          Specifies if build events should be ignored (<code>true</code>) or not (<code>false</code>).
   */
  public void setIgnoreBuildEvents(boolean isIgnoreBuildEvents) {
    m_isIgnoreBuildEvents = isIgnoreBuildEvents;
  }

  /**
   * @return Specifies if events coming from the Scout SDK itself should be ignored. Default is <code>true</code>.
   */
  public boolean isIgnoreScoutSdkEvents() {
    return m_isIgnoreScoutSdkEvents;
  }

  /**
   * @param isIgnoreScoutSdkEvents
   *          Specifies if events coming from the Scout SDK should be ignored (<code>true</code>) or not
   *          (<code>false</code>).
   */
  public void setIgnoreScoutSdkEvents(boolean isIgnoreScoutSdkEvents) {
    m_isIgnoreScoutSdkEvents = isIgnoreScoutSdkEvents;
  }

  /**
   * @return A live list of fully qualified class name prefixes of jobs that should be excluded. The returned list may
   *         be modified directly.
   */
  public Collection<String> getExcludedJobNamePrefixes() {
    return m_excludedJobNamePrefixes;
  }

  /**
   * @param excludedJobNamePrefixes
   *          The new excluded job fully qualified class names. May be <code>null</code>.
   */
  public void setExcludedJobNamePrefixes(Collection<String> excludedJobNamePrefixes) {
    if (excludedJobNamePrefixes == null || excludedJobNamePrefixes.isEmpty()) {
      m_excludedJobNamePrefixes = Collections.emptyList();
    }
    else {
      m_excludedJobNamePrefixes = new ArrayList<>(excludedJobNamePrefixes);
    }
  }
}
