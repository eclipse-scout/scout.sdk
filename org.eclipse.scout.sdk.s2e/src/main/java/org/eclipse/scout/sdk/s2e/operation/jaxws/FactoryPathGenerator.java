/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutVariousApi.JaxWsConstants;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link FactoryPathGenerator}</h3>
 *
 * @since 7.0.0
 */
public class FactoryPathGenerator implements ISourceGenerator<ISourceBuilder<?>> {

  private String m_rtVersion;
  private JaxWsConstants m_jaxWsConstants;

  @Override
  public void generate(ISourceBuilder<?> builder) {
    var scoutVersion = rtVersion().orElseThrow(() -> newFail("No Scout version provided"));
    var jaxWsConstants = jaxWsConstants().orElseThrow(() -> newFail("No JaxWs constants provided"));

    builder.append("<factorypath>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/eclipse/scout/rt/org.eclipse.scout.jaxws.apt/").append(scoutVersion)
        .append("/org.eclipse.scout.jaxws.apt-").append(scoutVersion).append(".jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/").append(jaxWsConstants.codeModelFactoryPath()).append("\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/eclipse/scout/rt/org.eclipse.scout.rt.platform/").append(scoutVersion)
        .append("/org.eclipse.scout.rt.platform-").append(scoutVersion).append(".jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/eclipse/scout/rt/org.eclipse.scout.rt.server.jaxws/").append(scoutVersion)
        .append("/org.eclipse.scout.rt.server.jaxws-").append(scoutVersion).append(".jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/").append(jaxWsConstants.servletFactoryPath()).append("\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/").append(jaxWsConstants.slf4jFactoryPath()).append("\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    var jwsApi = jaxWsConstants.jwsFactoryPath();
    if (Strings.hasText(jwsApi)) {
      builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/").append(jwsApi).append("\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    }
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/").append(jaxWsConstants.annotationApiPath()).append("\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/").append(jaxWsConstants.wsApiPath()).append("\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("</factorypath>").nl();
  }

  public Optional<String> rtVersion() {
    return Strings.notBlank(m_rtVersion);
  }

  public FactoryPathGenerator withRtVersion(String rtVersion) {
    m_rtVersion = rtVersion;
    return this;
  }

  public Optional<JaxWsConstants> jaxWsConstants() {
    return Optional.ofNullable(m_jaxWsConstants);
  }

  public FactoryPathGenerator withJaxWsConstants(JaxWsConstants jaxWsConstants) {
    m_jaxWsConstants = jaxWsConstants;
    return this;
  }
}
