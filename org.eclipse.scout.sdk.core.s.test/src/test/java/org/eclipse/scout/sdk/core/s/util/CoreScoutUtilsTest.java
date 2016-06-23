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
package org.eclipse.scout.sdk.core.s.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <h3>{@link CoreScoutUtilsTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CoreScoutUtilsTest {

  private static final double DELTA = 0.00000000001;

  @Test
  public void testNewViewOrderValue() {
    IType type = CoreScoutTestingUtils.createClientJavaEnvironment().findType("formdata.client.ui.forms.IgnoredFieldsForm$MainBox$AGroupBox");
    IType first = type.innerTypes().first();
    IType second = type.innerTypes().list().get(1);
    Assert.assertEquals(-1000.0, CoreScoutUtils.getNewViewOrderValue(type, IScoutRuntimeTypes.IFormField, first.source().start() - 1), DELTA);
    Assert.assertEquals(15.0, CoreScoutUtils.getNewViewOrderValue(type, IScoutRuntimeTypes.IFormField, first.source().end() + 1), DELTA);
    Assert.assertEquals(2000.0, CoreScoutUtils.getNewViewOrderValue(type, IScoutRuntimeTypes.IFormField, second.source().end() + 1), DELTA);
  }

  @Test
  public void testValueInBetween() {
    Assert.assertEquals(1500, CoreScoutUtils.getOrderValueInBetween(1000, 2000), DELTA);
    Assert.assertEquals(1000, CoreScoutUtils.getOrderValueInBetween(0, 2000), DELTA);
    Assert.assertEquals(50, CoreScoutUtils.getOrderValueInBetween(100, 0), DELTA);
    Assert.assertEquals(1.5, CoreScoutUtils.getOrderValueInBetween(1, 2), DELTA);
    Assert.assertEquals(0.5, CoreScoutUtils.getOrderValueInBetween(0, 1), DELTA);
    Assert.assertEquals(2, CoreScoutUtils.getOrderValueInBetween(1, 3), DELTA);
    Assert.assertEquals(2.7, CoreScoutUtils.getOrderValueInBetween(2.4, 3), DELTA);
    Assert.assertEquals(3, CoreScoutUtils.getOrderValueInBetween(2, 3.1), DELTA);
    Assert.assertEquals(2.05, CoreScoutUtils.getOrderValueInBetween(2, 2.1), DELTA);
    Assert.assertEquals(2.2, CoreScoutUtils.getOrderValueInBetween(2.1, 2.3), DELTA);
    Assert.assertEquals(4, CoreScoutUtils.getOrderValueInBetween(2.1, 5.7), DELTA);
    Assert.assertEquals(4, CoreScoutUtils.getOrderValueInBetween(2.1, 6.7), DELTA);
    Assert.assertEquals(5, CoreScoutUtils.getOrderValueInBetween(2.1, 7.7), DELTA);
    Assert.assertEquals(3, CoreScoutUtils.getOrderValueInBetween(2.6, 3.7), DELTA);
    Assert.assertEquals(187, CoreScoutUtils.getOrderValueInBetween(125, 250), DELTA);
    Assert.assertEquals(2000, CoreScoutUtils.getOrderValueInBetween(1000, 100000), DELTA);
  }

  @Test
  public void testGetArtifactIdOfPom() throws SAXException, IOException, ParserConfigurationException {
    Assert.assertEquals(null, CoreScoutUtils.getArtifactIdOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>")));
    Assert.assertEquals(null, CoreScoutUtils.getArtifactIdOfPom(null));
    Assert.assertEquals("testle", CoreScoutUtils.getArtifactIdOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><artifactId>testle</artifactId></project>")));

  }

  @Test
  public void testGetGroupIdOfPom() throws SAXException, IOException, ParserConfigurationException {
    Assert.assertEquals(null, CoreScoutUtils.getGroupIdOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>")));
    Assert.assertEquals(null, CoreScoutUtils.getGroupIdOfPom(null));
    Assert.assertEquals("testle", CoreScoutUtils.getGroupIdOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><groupId>testle</groupId></project>")));
    Assert.assertEquals("testle", CoreScoutUtils.getGroupIdOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><groupId>whatever</groupId></parent><groupId>testle</groupId></project>")));
    Assert.assertEquals("testle", CoreScoutUtils.getGroupIdOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><groupId>testle</groupId></parent><groupIdA>whatever</groupIdA></project>")));
    Assert.assertEquals(null, CoreScoutUtils.getGroupIdOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><groupIdA>testle</groupIdA></parent><groupIdA>whatever</groupIdA></project>")));
  }

  @Test
  public void testGetVersionOfPom() throws SAXException, IOException, ParserConfigurationException {
    Assert.assertEquals(null, CoreScoutUtils.getVersionOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>")));
    Assert.assertEquals(null, CoreScoutUtils.getVersionOfPom(null));
    Assert.assertEquals("testle", CoreScoutUtils.getVersionOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><version>testle</version></project>")));
    Assert.assertEquals("testle", CoreScoutUtils.getVersionOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><version>whatever</version></parent><version>testle</version></project>")));
    Assert.assertEquals("testle", CoreScoutUtils.getVersionOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><version>testle</version></parent><versionA>whatever</versionA></project>")));
    Assert.assertEquals(null, CoreScoutUtils.getVersionOfPom(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><versionA>testle</versionA></parent><versionA>whatever</versionA></project>")));
  }

  @Test
  public void testGetParentArtifactId() throws SAXException, IOException, ParserConfigurationException {
    Assert.assertEquals(null, CoreScoutUtils.getParentArtifactId(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>")));
    Assert.assertEquals(null, CoreScoutUtils.getParentArtifactId(null));
    Assert.assertEquals(null, CoreScoutUtils.getParentArtifactId(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent></parent><version>testle</version></project>")));
    Assert.assertEquals("testle", CoreScoutUtils.getParentArtifactId(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><artifactId>testle</artifactId></parent></project>")));
    Assert.assertEquals(null, CoreScoutUtils.getParentArtifactId(toXmlDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><artifactIdA>testle</artifactIdA></parent><artifactId>testle</artifactId></project>")));
  }

  protected Document toXmlDocument(String xml) throws SAXException, IOException, ParserConfigurationException {
    try (Reader r = new StringReader(xml)) {
      return CoreUtils.createDocumentBuilder().parse(new InputSource(r));
    }
  }
}
