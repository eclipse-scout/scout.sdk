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
package org.eclipse.scout.sdk.core.s.jaxws;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils.JaxWsBindingMapping;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <h3>{@link JaxWsUtilsTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class JaxWsUtilsTest {
  @Test
  public void testJaxWsBindingMapping() {
    JaxWsBindingMapping m = new JaxWsBindingMapping(true, "wsdlName", "className");
    Assert.assertFalse(m.equals(null));
    Assert.assertFalse(m.equals(new JaxWsBindingMapping(true, "wsdlName", "className2")));
    Assert.assertFalse(m.equals(new JaxWsBindingMapping(true, "wsdlName2", "className")));
    Assert.assertFalse(m.equals(new JaxWsBindingMapping(false, "wsdlName", "className")));
    Assert.assertFalse(m.equals(new JaxWsBindingMapping(true, null, "className")));
    Assert.assertFalse(m.equals(new JaxWsBindingMapping(true, "wsdlName", null)));
    Assert.assertTrue(m.equals(new JaxWsBindingMapping(true, "wsdlName", "className")));
    Assert.assertTrue(m.equals(m));

    JaxWsBindingMapping m2 = new JaxWsBindingMapping(true, null, null);
    Assert.assertFalse(m2.equals(new JaxWsBindingMapping(true, null, "className")));
    Assert.assertFalse(m2.equals(new JaxWsBindingMapping(true, "wsdlName", null)));
    Assert.assertTrue(m2.equals(new JaxWsBindingMapping(true, null, null)));
  }

  @Test
  public void testRemoveCommonSuffixes() {
    Assert.assertEquals("My", JaxWsUtils.removeCommonSuffixes("MyWebService"));
    Assert.assertEquals("My", JaxWsUtils.removeCommonSuffixes("Mywebservice"));
    Assert.assertEquals("Service", JaxWsUtils.removeCommonSuffixes("Service"));
    Assert.assertEquals("", JaxWsUtils.removeCommonSuffixes(""));
    Assert.assertEquals(null, JaxWsUtils.removeCommonSuffixes(null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetBindingPathsFromPomInvalidInput() throws XPathExpressionException {
    JaxWsUtils.getBindingPathsFromPom(null, "my'test.wsdl");
  }

  @Test
  public void testGetBindingPathsFromPom() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
    assertBindingPathsIn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>", "myTest/myTest.wsdl", Collections.<String> emptyList());

    String correct = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project xmlns=\"http://maven.apache.org/POM/4.0.0\"><build>" +
        "    <plugins>" +
        "      <plugin>" +
        "        <groupId>com.helger.maven</groupId>" +
        "        <artifactId>jaxws-maven-plugin</artifactId>" +
        "        <executions>" +
        "          <execution>" +
        "            <id>wsimport-1</id>" +
        "            <goals>" +
        "              <goal>wsimport</goal>" +
        "            </goals>" +
        "            <configuration>" +
        "              <wsdlFiles>" +
        "                <wsdlFile>myTest/myTest.wsdl</wsdlFile>" +
        "              </wsdlFiles>" +
        "              <bindingFiles>" +
        "                <bindingFile>global-binding.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxws-binding-a.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxws-binding-b.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxb-binding.xml</bindingFile>" +
        "              </bindingFiles>" +
        "            </configuration>" +
        "          </execution>" +
        "          <execution>" +
        "            <id>wsimport-2</id>" +
        "            <goals>" +
        "              <goal>wsimport</goal>" +
        "            </goals>" +
        "            <configuration>" +
        "              <wsdlFiles>" +
        "                <wsdlFile>myTest2/myTest2.wsdl</wsdlFile>" +
        "              </wsdlFiles>" +
        "              <bindingFiles>" +
        "                <bindingFile>global-binding.xml</bindingFile>" +
        "                <bindingFile>myTest2/jaxws-binding.xml</bindingFile>" +
        "                <bindingFile>myTest2/jaxb-binding.xml</bindingFile>" +
        "              </bindingFiles>" +
        "            </configuration>" +
        "          </execution>" +
        "        </executions>" +
        "      </plugin>" +
        "    </plugins>" +
        "  </build></project>";
    assertBindingPathsIn(correct, "myTest/myTest.wsdl", Arrays.<String> asList("global-binding.xml", "myTest/jaxws-binding-a.xml", "myTest/jaxws-binding-b.xml", "myTest/jaxb-binding.xml"));

    String correctWithEmptyBinding = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project xmlns=\"http://maven.apache.org/POM/4.0.0\"><build>" +
        "    <plugins>" +
        "      <plugin>" +
        "        <groupId>com.helger.maven</groupId>" +
        "        <artifactId>jaxws-maven-plugin</artifactId>" +
        "        <executions>" +
        "          <execution>" +
        "            <id>wsimport-1</id>" +
        "            <goals>" +
        "              <goal>wsimport</goal>" +
        "            </goals>" +
        "            <configuration>" +
        "              <wsdlFiles>" +
        "                <wsdlFile>myTest/myTest.wsdl</wsdlFile>" +
        "              </wsdlFiles>" +
        "              <bindingFiles>" +
        "                <bindingFile>global-binding.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxws-binding.xml</bindingFile>" +
        "                <bindingFile></bindingFile>" +
        "                <bindingFile>myTest/jaxb-binding.xml</bindingFile>" +
        "              </bindingFiles>" +
        "            </configuration>" +
        "          </execution>" +
        "        </executions>" +
        "      </plugin>" +
        "    </plugins>" +
        "  </build></project>";
    assertBindingPathsIn(correctWithEmptyBinding, "myTest/myTest.wsdl", Arrays.<String> asList("global-binding.xml", "myTest/jaxws-binding.xml", "myTest/jaxb-binding.xml"));

    String correctCaseMismatch = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project xmlns=\"http://maven.apache.org/POM/4.0.0\"><build>" +
        "    <plugins>" +
        "      <plugin>" +
        "        <groupId>com.helger.maven</groupId>" +
        "        <artifactId>jaxws-maven-plugin</artifactId>" +
        "        <executions>" +
        "          <execution>" +
        "            <id>wsimport-1</id>" +
        "            <goals>" +
        "              <goal>wsimport</goal>" +
        "            </goals>" +
        "            <configuration>" +
        "              <wsdlFiles>" +
        "                <wsdlFile>mytest/mytest.WSDL</wsdlFile>" +
        "              </wsdlFiles>" +
        "              <bindingFiles>" +
        "                <bindingFile>global-binding.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxws-binding-A.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxws-binding-B.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxb-binding.xml</bindingFile>" +
        "              </bindingFiles>" +
        "            </configuration>" +
        "          </execution>" +
        "          <execution>" +
        "            <id>wsimport-2</id>" +
        "            <goals>" +
        "              <goal>wsimport</goal>" +
        "            </goals>" +
        "            <configuration>" +
        "              <wsdlFiles>" +
        "                <wsdlFile>myTest2/myTest2.wsdl</wsdlFile>" +
        "              </wsdlFiles>" +
        "              <bindingFiles>" +
        "                <bindingFile>global-binding.xml</bindingFile>" +
        "                <bindingFile>myTest2/jaxws-binding.xml</bindingFile>" +
        "                <bindingFile>myTest2/jaxb-binding.xml</bindingFile>" +
        "              </bindingFiles>" +
        "            </configuration>" +
        "          </execution>" +
        "        </executions>" +
        "      </plugin>" +
        "    </plugins>" +
        "  </build></project>";
    assertBindingPathsIn(correctCaseMismatch, "myTest/myTest.wsdl", Arrays.<String> asList("global-binding.xml", "myTest/jaxws-binding-A.xml", "myTest/jaxws-binding-B.xml", "myTest/jaxb-binding.xml"));

    String wrong = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project xmlns=\"http://maven.apache.org/POM/4.0.0\"><build>" +
        "    <plugins>" +
        "      <plugin>" +
        "        <groupId>wrong</groupId>" +
        "        <artifactId>wrong</artifactId>" +
        "        <executions>" +
        "          <execution>" +
        "            <id>wsimport-1</id>" +
        "            <goals>" +
        "              <goal>wsimport</goal>" +
        "            </goals>" +
        "            <configuration>" +
        "              <wsdlFiles>" +
        "                <wsdlFile>myTest/myTest.wsdl</wsdlFile>" +
        "              </wsdlFiles>" +
        "              <bindingFiles>" +
        "                <bindingFile>global-binding.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxws-binding-a.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxws-binding-b.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxb-binding.xml</bindingFile>" +
        "              </bindingFiles>" +
        "            </configuration>" +
        "          </execution>" +
        "        </executions>" +
        "      </plugin>" +
        "    </plugins>" +
        "  </build></project>";
    assertBindingPathsIn(wrong, "myTest/myTest.wsdl", Collections.<String> emptyList());

    String wrongNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project xmlns=\"http://maven.apache.org/POM/5.0.0\"><build>" +
        "    <plugins>" +
        "      <plugin>" +
        "        <groupId>com.helger.maven</groupId>" +
        "        <artifactId>jaxws-maven-plugin</artifactId>" +
        "        <executions>" +
        "          <execution>" +
        "            <id>wsimport-1</id>" +
        "            <goals>" +
        "              <goal>wsimport</goal>" +
        "            </goals>" +
        "            <configuration>" +
        "              <wsdlFiles>" +
        "                <wsdlFile>myTest/myTest.wsdl</wsdlFile>" +
        "              </wsdlFiles>" +
        "              <bindingFiles>" +
        "                <bindingFile>global-binding.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxws-binding-a.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxws-binding-b.xml</bindingFile>" +
        "                <bindingFile>myTest/jaxb-binding.xml</bindingFile>" +
        "              </bindingFiles>" +
        "            </configuration>" +
        "          </execution>" +
        "        </executions>" +
        "      </plugin>" +
        "    </plugins>" +
        "  </build></project>";
    assertBindingPathsIn(wrongNamespace, "myTest/myTest.wsdl", Collections.<String> emptyList());
  }

  protected void assertBindingPathsIn(String xml, String wsdlName, List<String> expectedPaths) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
    Document d = null;
    try (Reader r = new StringReader(xml)) {
      d = CoreUtils.createDocumentBuilder().parse(new InputSource(r));
    }
    List<String> bindingPathsFromPom = JaxWsUtils.getBindingPathsFromPom(d, wsdlName);
    Assert.assertEquals(expectedPaths, bindingPathsFromPom);
  }
}
