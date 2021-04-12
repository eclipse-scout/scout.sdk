/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.maven.plugins.updatesite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public final class FileUtility {

  private FileUtility() {
  }

  public static void copy(File inputFile, File outputFile) throws IOException {
    if (inputFile.isDirectory()) {
      ensureDirExists(outputFile);
      //noinspection ConstantConditions
      for (var f : inputFile.listFiles()) {
        copyToDir(f, outputFile);
      }
      return;
    }

    try (InputStream in = new FileInputStream(inputFile); OutputStream out = new FileOutputStream(outputFile)) {
      copy(in, out);
    }
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    var buffer = new byte[8192];
    int read;
    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
    }
  }

  public static void copyToDir(File input, File toDir) throws IOException {
    copyToDir(input, toDir, input.getParentFile().getAbsoluteFile().toURI());
  }

  public static void copyToDir(File input, File toDir, URI relPath) throws IOException {
    // folder
    if (input.isDirectory()) {
      //noinspection ConstantConditions
      for (var f : input.listFiles()) {
        copyToDir(f, toDir, relPath);
      }
      return;
    }

    // file
    var outFile = new File(toDir.getAbsolutePath() + File.separator + relPath.relativize(input.toURI()));
    ensureDirExists(outFile);

    try (InputStream in = new FileInputStream(input); OutputStream out = new FileOutputStream(outFile)) {
      copy(in, out);
    }
  }

  public static boolean deleteFile(File file) {
    if (file.isDirectory()) {
      //noinspection ConstantConditions
      for (var f : file.listFiles()) {
        var success = deleteFile(f);
        if (!success) {
          return false;
        }
      }
    }
    return file.delete();
  }

  public static void compressArchive(File srcDir, File archiveFile) throws IOException {
    //noinspection ResultOfMethodCallIgnored
    archiveFile.delete();
    try (var zOut = new JarOutputStream(new FileOutputStream(archiveFile))) {
      addFolderToJar(srcDir, srcDir, zOut);
    }
  }

  private static void addFolderToJar(File baseDir, File srcdir, JarOutputStream zOut) throws IOException {
    if (!srcdir.exists() || !srcdir.isDirectory()) {
      throw new IOException("source directory " + srcdir + " does not exist or is not a folder");
    }
    //noinspection ConstantConditions
    for (var f : srcdir.listFiles()) {
      if (f.exists() && (!f.isHidden())) {
        if (f.isDirectory()) {
          addFolderToJar(baseDir, f, zOut);
        }
        else {
          addFileToJar(baseDir, f, zOut);
        }
      }
    }
  }

  private static void addFileToJar(File baseDir, File src, JarOutputStream zOut) throws IOException {
    var name = src.getAbsolutePath();
    var prefix = baseDir.getAbsolutePath();
    if (prefix.endsWith("/") || prefix.endsWith("\\")) {
      prefix = prefix.substring(0, prefix.length() - 1);
    }
    name = name.substring(prefix.length() + 1);
    name = name.replace('\\', '/');
    var timestamp = src.lastModified();
    var data = readFile(src);
    addFileToJar(name, data, timestamp, zOut);
  }

  private static void addFileToJar(String name, byte[] data, long timestamp, JarOutputStream zOut) throws IOException {
    var entry = new ZipEntry(name);
    entry.setTime(timestamp);
    zOut.putNextEntry(entry);
    zOut.write(data);
    zOut.closeEntry();
  }

  public static byte[] readFile(File source) throws IOException {
    return Files.readAllBytes(source.toPath());
  }

  @SuppressWarnings({"squid:S1166", "squid:S1141", "NestedTryStatement"})
  public static void writeDOM(Document doc, File file) throws MojoExecutionException {
    try {
      var tf = TransformerFactory.newInstance();
      try {
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      }
      catch (TransformerConfigurationException e) {
        // nop
      }
      try {
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      }
      catch (IllegalArgumentException e) {
        // nop
      }
      try {
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      }
      catch (IllegalArgumentException e) {
        // nop
      }
      var transformer = tf.newTransformer();

      transformer.transform(new DOMSource(doc), new StreamResult(file));
    }
    catch (TransformerException e) {
      throw new MojoExecutionException("Could not write XML file ", e);
    }
  }

  public static void ensureDirExists(File dir) throws IOException {
    if (dir == null) {
      return;
    }
    if (!dir.isDirectory()) {
      dir = dir.getParentFile();
    }
    if (!dir.exists() && !dir.mkdirs()) {
      throw new IOException("Unable to create directory '" + dir.getAbsolutePath() + "'.");
    }
  }

  @SuppressWarnings({"NestedTryStatement", "ResultOfMethodCallIgnored"})
  public static void extractArchive(File archiveFile, File targetDir) throws IOException {
    var destinationDir = targetDir.getCanonicalFile();
    var destinationPath = destinationDir.toPath();
    destinationDir.mkdirs();
    destinationDir.setLastModified(archiveFile.lastModified());
    var localFile = destinationDir.getName();
    try (var jar = new JarFile(archiveFile)) {
      var entries = jar.entries();
      while (entries.hasMoreElements()) {
        var file = entries.nextElement();
        var name = file.getName();
        if (name.startsWith(localFile)) {
          name = name.substring(localFile.length());
        }
        while (name.startsWith("/") || name.startsWith("\\")) {
          name = name.substring(1);
        }
        var f = new File(destinationDir, name).getCanonicalFile();
        if (!f.toPath().startsWith(destinationPath)) {
          // security check (see https://github.com/snyk/zip-slip-vulnerability)
          throw new IllegalArgumentException("Entry is outside of the target dir: " + name);
        }

        if (file.isDirectory()) { // if its a directory, create it
          ensureDirExists(f);
        }
        else {
          ensureDirExists(f.getParentFile());
          try (var is = jar.getInputStream(file); var fos = new FileOutputStream(f)) {
            copy(is, fos);
          }
        }
        if (file.getTime() >= 0) {
          f.setLastModified(file.getTime());
        }
      }
    }
  }

  public static Document readDOM(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
    return createDocumentBuilder().parse(xmlFile);
  }

  /**
   * Creates a new {@link DocumentBuilder} to create a DOM of an XML file.<br>
   * Use {@link DocumentBuilder#parse(File)} to create a new {@link Document}.
   *
   * @return The created builder. All external entities are disabled to prevent XXE.
   * @throws ParserConfigurationException
   *           if a {@link DocumentBuilder} cannot be created which satisfies the configuration requested.
   */
  @SuppressWarnings("squid:S1166")
  public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
    var dbf = DocumentBuilderFactory.newInstance();
    Map<String, Boolean> features = new HashMap<>(5);
    features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
    features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
    features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
    features.put("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
    features.put(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
    dbf.setXIncludeAware(false);
    dbf.setExpandEntityReferences(false);

    for (var a : features.entrySet()) {
      var feature = a.getKey();
      var enabled = a.getValue();
      try {
        dbf.setFeature(feature, enabled);
      }
      catch (ParserConfigurationException e) {
        // nop
      }
    }
    return dbf.newDocumentBuilder();
  }
}
