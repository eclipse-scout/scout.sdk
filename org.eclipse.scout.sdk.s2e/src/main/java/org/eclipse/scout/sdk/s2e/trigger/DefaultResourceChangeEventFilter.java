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
  public static final String JDT_DEBUG_EVENT = "org.eclipse.jdt.internal.debug.";
  public static final String ANNOTATION_PROCESSING_JOB = "org.eclipse.core.internal.events.NotificationManager.NotifyJob";
  public static final String ANOTATION_PROCESSING_BUILD = "org.eclipse.core.internal.events.AutoBuildJob";
  public static final String EXTERNAL_FOLDER_UPDATE = "org.eclipse.jdt.internal.core.ExternalFoldersManager.RefreshJob";
  public static final String DEBUG_INIT = "org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager.InitJob";
  public static final String REFRESH_JOB = "org.eclipse.core.internal.refresh.RefreshJob";
  public static final String WORKSPACE_INIT_JOB = "org.eclipse.jdt.internal.ui.InitializeAfterLoadJob.RealJob";
  public static final String TEAM_UPDATES = "org.eclipse.team.";
  public static final String SEARCH = "org.eclipse.search2.";
  public static final String MARKER_UPDATE = "org.eclipse.ui.internal.views.markers.";
  public static final String JAVA_INDEX_UPDATE_JOB_NAME = "Updating Java index";

  private final Collection<String> m_excludedJobClassNamePrefixes;
  private final Collection<String> m_excludedJobNames;
  private boolean m_isIgnoreBuildEvents;
  private boolean m_isIgnoreScoutSdkEvents;

  public DefaultResourceChangeEventFilter() {
    String[] defaultJobExclusionsFqn = new String[]{GIT_UPDATES, MAVEN_UPDATES, SONAR_UPDATE, WEB_TOOLS_UPDATE, DEBUG_EVENT,
        JDT_DEBUG_EVENT, ANNOTATION_PROCESSING_JOB, ANOTATION_PROCESSING_BUILD, EXTERNAL_FOLDER_UPDATE, DEBUG_INIT,
        REFRESH_JOB, WORKSPACE_INIT_JOB, TEAM_UPDATES, SEARCH, MARKER_UPDATE};
    m_excludedJobClassNamePrefixes = new ArrayList<>(defaultJobExclusionsFqn.length);
    Collections.addAll(m_excludedJobClassNamePrefixes, defaultJobExclusionsFqn);

    m_excludedJobNames = new ArrayList<>(1);
    m_excludedJobNames.add(JAVA_INDEX_UPDATE_JOB_NAME);

    m_isIgnoreBuildEvents = true;
    m_isIgnoreScoutSdkEvents = true;
  }

  @Override
  @SuppressWarnings("pmd:NPathComplexity")
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
    String jobName = curJob.getName();

    Predicate<String> excludedClassNamePrefixes = className -> getExcludedJobClassNamePrefixes().stream().anyMatch(prefix -> className.startsWith(prefix));
    Predicate<String> excludedJobNames = name -> getExcludedJobNames().stream().anyMatch(n -> name.equals(n));

    if ("org.eclipse.core.internal.jobs.ThreadJob".equals(jobFqn)) {
      // for thread jobs: check current stack trace
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      return !Arrays.stream(stackTrace)
          .map(element -> element.getClassName())
          .anyMatch(excludedClassNamePrefixes);
    }
    if (jobFqn.startsWith("org.eclipse.core.runtime.jobs.Job.")) {
      // for Job factory methods: check job name
      return !excludedJobNames.test(jobName);
    }
    return !excludedClassNamePrefixes.test(jobFqn) && !excludedJobNames.test(jobName);
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
   * @return A live list of fully qualified job class name prefixes that should be excluded. The returned list may be
   *         modified directly.
   */
  public Collection<String> getExcludedJobClassNamePrefixes() {
    return m_excludedJobClassNamePrefixes;
  }

  /**
   * @param excludedJobClassNamePrefixes
   *          The new list of fully qualified class names of jobs to be excluded. Inner types are separated using '.'.
   *          May be {@code null}.
   */
  public void setExcludedJobClassNamePrefixes(final Collection<String> excludedJobClassNamePrefixes) {
    m_excludedJobClassNamePrefixes.clear();
    if (excludedJobClassNamePrefixes != null) {
      m_excludedJobClassNamePrefixes.addAll(excludedJobClassNamePrefixes);
    }
  }

  /**
   * @return A live list of job names ({@link Job#getName()}) that should be excluded. Please note: Job names may be
   *         language dependent! The returned list may be modified directly.
   */
  public Collection<String> getExcludedJobNames() {
    return m_excludedJobNames;
  }

  /**
   * @param excludedJobNames
   *          The new list of job names ({@link Job#getName()}) that should be excluded. May be {@code null}.
   */
  public void setExcludedJobNames(final Collection<String> excludedJobNames) {
    m_excludedJobNames.clear();
    if (excludedJobNames != null) {
      m_excludedJobNames.addAll(excludedJobNames);
    }
  }
}
