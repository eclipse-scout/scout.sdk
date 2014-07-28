/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test.nls;

import org.eclipse.scout.nls.sdk.internal.search.NlsFindKeysJob;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.search.ui.text.Match;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link NlsSearchKeyReferencesTest}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.11.2013
 */
public class NlsSearchKeyReferencesTest extends AbstractSdkTestWithSampleProject {

  public static final String nlsCounter01Key = "NlsCounter01Key";
  public static final String nlsCounter02Key = "NlsCounter02Key";
  public static final String nlsCounter03Key = "NlsCounter03Key";

  @Test
  public void testCountNlsKeyUsage() throws Exception {
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    NlsFindKeysJob job = new NlsFindKeysJob(nlsProject, "aJobTitle");
    job.schedule();
    job.join();
    Match[] key01Matches = job.getMatches(nlsCounter01Key);
    Assert.assertEquals(2, key01Matches.length);

    Match[] key02Matches = job.getMatches(nlsCounter02Key);
    Assert.assertEquals(1, key02Matches.length);

    Match[] key03Matches = job.getMatches(nlsCounter03Key);
    Assert.assertEquals(0, key03Matches.length);

  }

}
