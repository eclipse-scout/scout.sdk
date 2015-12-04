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
package org.eclipse.scout.maven.plugins.updatesite;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FileUtility {

  private final static int BUFFER_SIZE = 1024;

  public static void copy(File inputFile, File outputFile) throws IOException {
    if (inputFile.isDirectory()) {
      if (!outputFile.exists()) {
        outputFile.mkdirs();
      }
      for (File f : inputFile.listFiles()) {
        copyToDir(f, outputFile);
      }
    }
    else {
      InputStream in = null;
      OutputStream out = null;
      try {
        in = new FileInputStream(inputFile);
        out = new FileOutputStream(outputFile);
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer)) != -1) {
          out.write(buffer, 0, read);
        }
      }
      finally {
        if (in != null) {
          try {
            in.close();
          }
          catch (IOException e) {
            // void
          }
        }
        if (out != null) {
          try {
            out.close();
          }
          catch (IOException e) {
            // void
          }
        }
      }
    }
  }

  public static void copyToDir(File input, File toDir) throws IOException {
    copyToDir(input, toDir, input.getParentFile().getAbsoluteFile().toURI());
  }

  public static void copyToDir(File input, File toDir, URI relPath) throws IOException {
    if (input.isDirectory()) {
      for (File f : input.listFiles()) {
        copyToDir(f, toDir, relPath);
      }
    }
    else {
      InputStream in = null;
      OutputStream out = null;
      try {
        in = new FileInputStream(input);
        File outFile = new File(toDir.getAbsolutePath()
            + File.separator
            + relPath.relativize(input.toURI()).toString());
        if (!outFile.exists()) {
          outFile.getParentFile().mkdirs();
        }
        out = new FileOutputStream(outFile);
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer)) != -1) {
          out.write(buffer, 0, read);
        }
      }
      finally {
        if (in != null) {
          try {
            in.close();
          }
          catch (IOException e) {
            // void
          }
        }
        if (out != null) {
          try {
            out.close();
          }
          catch (IOException e) {
            // void
          }
        }
      }
    }
  }

  public static boolean deleteFile(File file) {
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        boolean success = deleteFile(f);
        if (!success) {
          return false;
        }
      }
    }
    return file.delete();
  }

  /**
   * retrieve content as raw bytes
   */
  public static byte[] getContent(InputStream stream) throws IOException {
    return getContent(stream, true);
  }

  public static byte[] getContent(InputStream stream, boolean autoClose) throws IOException {
    BufferedInputStream in = null;
    try {
      in = new BufferedInputStream(stream);
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] b = new byte[10240];
      int len;
      while ((len = in.read(b)) > 0) {
        buffer.write(b, 0, len);
      }
      buffer.close();
      byte[] data = buffer.toByteArray();
      return data;
    }
    finally {
      if (autoClose) {
        if (in != null) {
          in.close();
        }
      }
    }
  }

  public static byte[] getContent(String filename) throws IOException {
    try (FileInputStream stream = new FileInputStream(filename)) {
      return getContent(stream, true);
    }
    catch (FileNotFoundException e) {
      IOException io = new IOException("filename: " + filename);
      io.initCause(e);
      throw io;
    }
  }

  /**
   * retrieve content as string (correct charcter conversion)
   */
  public static String getContent(Reader stream) throws IOException {
    return getContent(stream, true);
  }

  public static String getContent(Reader stream, boolean autoClose) throws IOException {
    BufferedReader in = null;
    try {
      in = new BufferedReader(stream);
      StringWriter buffer = new StringWriter();
      char[] b = new char[10240];
      int len;
      while ((len = in.read(b)) > 0) {
        buffer.write(b, 0, len);
      }
      buffer.close();
      return buffer.toString();
    }
    finally {
      if (autoClose) {
        if (in != null) {
          in.close();
        }
      }
    }
  }

  public static void compressArchive(File srcDir, File archiveFile) throws IOException {
    JarOutputStream zOut = null;
    try {
      archiveFile.delete();
      zOut = new JarOutputStream(new FileOutputStream(archiveFile));
      addFolderToJar(srcDir, srcDir, zOut);
    }
    finally {
      if (zOut != null) {
        try {
          zOut.close();
        }
        catch (Throwable t) {
        }
      }
    }
  }

  private static void addFolderToJar(File baseDir, File srcdir, JarOutputStream zOut) throws IOException {
    if ((!srcdir.exists()) || (!srcdir.isDirectory())) {
      throw new IOException("source directory " + srcdir + " does not exist or is not a folder");
    }
    for (File f : srcdir.listFiles()) {
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
    String name = src.getAbsolutePath();
    String prefix = baseDir.getAbsolutePath();
    if (prefix.endsWith("/") || prefix.endsWith("\\")) {
      prefix = prefix.substring(0, prefix.length() - 1);
    }
    name = name.substring(prefix.length() + 1);
    name = name.replace('\\', '/');
    long timestamp = src.lastModified();
    byte[] data = readFile(src);
    addFileToJar(name, data, timestamp, zOut);
  }

  private static void addFileToJar(String name, byte[] data, long timestamp, JarOutputStream zOut) throws IOException {
    ZipEntry entry = new ZipEntry(name);
    entry.setTime(timestamp);
    zOut.putNextEntry(entry);
    zOut.write(data);
    zOut.closeEntry();
  }

  public static byte[] readFile(File source) throws IOException {
    if (!source.exists()) {
      throw new FileNotFoundException(source.getAbsolutePath());
    }
    if (!source.canRead()) {
      throw new IOException("cannot read " + source);
    }
    if (source.isDirectory()) {
      // source can not be a directory
      throw new IOException("source is a directory: " + source);
    }
    FileInputStream input = null;
    try {
      input = new FileInputStream(source);
      byte[] data = new byte[(int) source.length()];
      int n = 0;
      while (n < data.length) {
        n += input.read(data, n, data.length - n);
      }
      return data;
    }
    finally {
      if (input != null) {
        try {
          input.close();
        }
        catch (Throwable e) {
        }
      }
    }
  }

  public static void writeDOM(Document doc, File file) throws MojoExecutionException {
    try {
      Transformer transFormer = TransformerFactory.newInstance().newTransformer();
      transFormer.transform(new DOMSource(doc), new StreamResult(file));
    }
    catch (TransformerConfigurationException e) {
      throw new MojoExecutionException("Could not write XML file ", e);
    }
    catch (TransformerException e) {
      throw new MojoExecutionException("Could not write XML file ", e);
    }
  }

  public static void extractArchive(File archiveFile, File destinationDir) throws IOException {
    destinationDir.mkdirs();
    destinationDir.setLastModified(archiveFile.lastModified());
    String localFile = destinationDir.getName();
    JarFile jar = new JarFile(archiveFile);
    try {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry file = entries.nextElement();
        String name = file.getName();
        if (name.startsWith(localFile)) {
          name = name.substring(localFile.length());
        }
        while (name.startsWith("/") || name.startsWith("\\")) {
          name = name.substring(1);
        }
        File f = new File(destinationDir, name);
        if (file.isDirectory()) { // if its a directory, create it
          f.mkdirs();
          if (file.getTime() >= 0) {
            f.setLastModified(file.getTime());
          }
          continue;
        }
        f.getParentFile().mkdirs();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
          is = jar.getInputStream(file);
          fos = new FileOutputStream(f);
          // Copy the bits from instream to outstream
          byte[] buf = new byte[102400];
          int len;
          while ((len = is.read(buf)) > 0) {
            fos.write(buf, 0, len);
          }
        }
        finally {
          if (fos != null) {
            fos.close();
          }
          if (is != null) {
            is.close();
          }
        }
        if (file.getTime() >= 0) {
          f.setLastModified(file.getTime());
        }
      }
    }
    finally {
      if (jar != null) {
        try {
          jar.close();
        }
        catch (Throwable t) {
        }
      }
    }
  }

  public static Document readDOM(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    Document doc = builder.parse(xmlFile);
    return doc;
  }
}
