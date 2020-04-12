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
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link FactoryPathGenerator}</h3>
 *
 * @since 7.0.0
 */
public class FactoryPathGenerator implements ISourceGenerator<ISourceBuilder<?>> {

  private String m_rtVersion;

  @Override
  @SuppressWarnings("HardcodedFileSeparator")
  public void generate(ISourceBuilder<?> builder) {
    String scoutVersion = rtVersion().orElse(ScoutProjectNewHelper.SCOUT_ARCHETYPES_VERSION);
    builder.append("<factorypath>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/eclipse/scout/rt/org.eclipse.scout.jaxws.apt/").append(scoutVersion)
        .append("/org.eclipse.scout.jaxws.apt-").append(scoutVersion).append(".jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/com/unquietcode/tools/jcodemodel/codemodel/1.0.3/codemodel-1.0.3.jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/eclipse/scout/rt/org.eclipse.scout.rt.platform/").append(scoutVersion)
        .append("/org.eclipse.scout.rt.platform-").append(scoutVersion).append(".jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/eclipse/scout/rt/org.eclipse.scout.rt.server.jaxws/").append(scoutVersion)
        .append("/org.eclipse.scout.rt.server.jaxws-").append(scoutVersion).append(".jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/jakarta/servlet/jakarta.servlet-api/4.0.3/jakarta.servlet-api-4.0.3.jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/jakarta/jws/jakarta.jws-api/1.1.1/jakarta.jws-api-1.1.1.jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/jakarta/annotation/jakarta.annotation-api/1.3.5/jakarta.annotation-api-1.3.5.jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/jakarta/xml/ws/jakarta.xml.ws-api/2.3.3/jakarta.xml.ws-api-2.3.3.jar\" enabled=\"true\" runInBatchMode=\"false\"/>").nl();
    builder.append("</factorypath>").nl();
  }

  public Optional<String> rtVersion() {
    return Strings.notBlank(m_rtVersion);
  }

  public FactoryPathGenerator withRtVersion(String rtVersion) {
    m_rtVersion = rtVersion;
    return this;
  }
}
