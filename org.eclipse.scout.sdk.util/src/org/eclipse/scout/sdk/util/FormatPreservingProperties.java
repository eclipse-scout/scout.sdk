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
package org.eclipse.scout.sdk.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.sdk.util.log.ScoutStatus;

/**
 * <h3>{@link FormatPreservingProperties}</h3> Properties class that can load and store .properties files.<br>
 * The difference to the one provided by the JRE ({@link Properties}) is that this class preserves comments, empty lines
 * and the property order that existed in the original file when writing it again.<br>
 * <br>
 * All operations of this class are thread save and can directly be consumed from parallel threads. Multiple readers may
 * query the properties at the same time. But there is only one writer allowed (write lock is exclusive).<br>
 * <br>
 * This class holds the entire .properties file in memory when loaded from a file or input stream. Therefore it should
 * be used with care when dealing with large files.<br>
 * <br>
 * As defined in the .properties file specification the files are loaded and stored using the ISO 8859-1 encoding.
 *
 * @author Matthias Villiger
 * @since 3.10.0 05.10.2013
 * @see #ENCODING
 * @see Properties
 */
public class FormatPreservingProperties implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * According to the .properties file specification all files are encoded using ISO 8859-1 character encoding.
   */
  public static final String ENCODING = "8859_1";

  private final Properties m_properties;
  private final ReentrantReadWriteLock m_lock;
  private final ArrayList<P_PropertyLine> m_lines;
  private Map<String, String> m_origValues;

  public FormatPreservingProperties() {
    m_properties = new Properties();
    m_lines = new ArrayList<FormatPreservingProperties.P_PropertyLine>();
    m_lock = new ReentrantReadWriteLock(true);
  }

  /**
   * Loads the content of the given properties file.<br>
   * As defined in the .properties file specification the files are loaded using the ISO 8859-1 encoding.
   *
   * @param f
   *          The file to load. Must exist and be accessible and must be of the .properties file format.
   * @throws CoreException
   * @see {@link #ENCODING}
   */
  public void load(IFile f) throws CoreException {
    InputStream is = null;
    try {
      is = f.getContents();
      load(is);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
          throw new CoreException(new ScoutStatus(e));
        }
      }
    }
  }

  /**
   * Loads the content of the given input stream.<br>
   * As defined in the .properties file specification the files are loaded using the ISO 8859-1 encoding.
   *
   * @param is
   *          The input stream providing the data to load.
   * @throws CoreException
   * @see {@link #ENCODING}
   */
  public void load(InputStream is) throws CoreException {
    try {
      load(IOUtility.getContent(is, false));
    }
    catch (ProcessingException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  /**
   * Stores the properties in the given output stream.<br>
   * As defined in the .properties file specification the files are stored using the ISO 8859-1 encoding.
   *
   * @param out
   * @throws CoreException
   */
  public void store(OutputStream out) throws CoreException {
    BufferedWriter writer = null;
    try {
      m_lock.readLock().lock();
      writer = new BufferedWriter(new OutputStreamWriter(out, ENCODING));
      for (P_PropertyLine line : m_lines) {
        if (!line.ignore) {
          if (StringUtility.hasText(line.key)) {
            writer.write(getLineFormatted(line.key));
          }
          else {
            writer.write(line.comment);
          }
          writer.newLine();
        }
      }
      writer.flush();
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus(e));
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * Sets a new value for a property.<br>
   * If it is a new property, it is appended to the end of the file when storing it. The order in which the new
   * properties are appended is the same as in which they have been added to this instance.
   *
   * @param key
   *          The key of the property.
   * @param value
   *          The new value.
   */
  public void setProperty(String key, String value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException();
    }
    try {
      m_lock.writeLock().lock();
      boolean newKey = !m_properties.containsKey(key);
      m_properties.setProperty(key, value);
      if (newKey) {
        P_PropertyLine newLine = new P_PropertyLine();
        newLine.key = key;
        m_lines.add(newLine);
      }
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  /**
   * Gets the value of a property.
   *
   * @param key
   *          The key of the property to get.
   * @return The value of the property with the given key.
   */
  public String getProperty(String key) {
    if (key == null) {
      throw new IllegalArgumentException("null is not a valid property key");
    }
    try {
      m_lock.readLock().lock();
      return m_properties.getProperty(key);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * @param key
   *          The key of the property to check
   * @return true if a property with the given name exists, false otherwise.
   */
  public boolean containsProperty(String key) {
    try {
      m_lock.readLock().lock();
      return m_properties.containsKey(key);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * Removes the property with the given key.
   *
   * @param key
   *          The key of the property to remove.
   */
  public void removeProperty(String key) {
    if (key == null) {
      throw new IllegalArgumentException("null is not a valid property key");
    }
    try {
      m_lock.writeLock().lock();
      m_properties.remove(key);

      for (Iterator<P_PropertyLine> it = m_lines.iterator(); it.hasNext();) {
        P_PropertyLine line = it.next();
        if (CompareUtility.equals(key, line.key)) {
          it.remove();
          break;
        }
      }
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  /**
   * @return Gets a copy of the key-value-pairs of all properties.
   */
  public Map<String, String> getEntries() {
    try {
      m_lock.readLock().lock();
      HashMap<String, String> result = new HashMap<String, String>(m_properties.size());
      for (Entry<Object, Object> entry : m_properties.entrySet()) {
        result.put((String) entry.getKey(), (String) entry.getValue());
      }
      return result;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * @return Gets the number of properties hold.
   */
  public int size() {
    try {
      m_lock.readLock().lock();
      return m_properties.size();
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * Checks if this class has been modified since it was loaded the last time.
   *
   * @return true if the properties are different than after the last load. false otherwise.
   */
  public boolean isDirty() {
    try {
      m_lock.readLock().lock();
      if (this.size() != m_origValues.size()) {
        return true;
      }

      for (Entry<String, String> entry : m_origValues.entrySet()) {
        if (CompareUtility.notEquals(this.getProperty(entry.getKey()), entry.getValue())) {
          return true;
        }
      }
      return false;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  private void load(byte[] data) {
    BufferedReader reader = null;
    try {
      m_lock.writeLock().lock();

      reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), ENCODING));
      reader.mark(data.length + 1);

      // clear old values
      m_lines.clear();
      m_properties.clear();

      // first load the properties into the map to have the right property parsing
      m_properties.load(reader);

      // then parse out the formatting information (comments and empty lines)
      reader.reset();
      String lineContent = null;
      boolean lastLineEndsWithBackSlash = false;
      while ((lineContent = reader.readLine()) != null) {
        String lineContentTrim = lineContent.trim();
        P_PropertyLine line = new P_PropertyLine();
        line.ignore = lastLineEndsWithBackSlash;

        if (!lastLineEndsWithBackSlash) {
          if (lineContentTrim.length() < 1 || lineContentTrim.charAt(0) == '#' || lineContentTrim.charAt(0) == '!') {
            // the current line does not hold a key-value-pair
            line.comment = lineContent;
          }
          else {
            String key = findKey(lineContent);
            if (StringUtility.hasText(key)) {
              // the current line holds the value of a key
              line.key = key;
            }
            else {
              throw new IllegalArgumentException("Invalid properties file format");
            }
          }
        }

        m_lines.add(line);

        lastLineEndsWithBackSlash = lineContentTrim.length() > 0 && lineContentTrim.charAt(lineContentTrim.length() - 1) == '\\';
      }

      // remember the values loaded
      m_origValues = getEntries();
    }
    catch (IOException e) {
      // cannot happen
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          // cannot happen
        }
      }
      m_lock.writeLock().unlock();
    }
  }

  private String getLineFormatted(String key) {
    Properties parser = new Properties();
    parser.setProperty(key, m_properties.getProperty(key));
    ByteArrayOutputStream buffer = null;
    BufferedReader lineReader = null;
    try {
      buffer = new ByteArrayOutputStream();
      parser.store(buffer, null);
      lineReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer.toByteArray()), ENCODING));
      lineReader.readLine();// skip the comment
      return lineReader.readLine();
    }
    catch (IOException e) {
      // cannot happen
      return null;
    }
    finally {
      if (buffer != null) {
        try {
          buffer.close();
        }
        catch (IOException e) {
          // cannot happen
        }
      }
      if (lineReader != null) {
        try {
          lineReader.close();
        }
        catch (IOException e) {
          // cannot happen
        }
      }
    }
  }

  private String findKey(String lineContent) {
    Properties parser = new Properties();
    InputStream is = null;
    try {
      is = new ByteArrayInputStream(lineContent.getBytes(ENCODING));
      parser.load(is);
      Set<Object> keySet = parser.keySet();
      if (keySet.size() == 0) {
        // the current line could not be parsed to a key-value-pair
        return null;
      }
      else if (keySet.size() == 1) {
        // a single key-value-pair was found
        return (String) CollectionUtility.firstElement(keySet);
      }
      else {
        throw new IllegalArgumentException("Invalid properties file format");
      }
    }
    catch (IOException e) {
      // cannot happen with a byte input stream
      return null;
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
        }
      }
    }
  }

  private static final class P_PropertyLine implements Serializable {

    private static final long serialVersionUID = 1L;

    // always set
    private boolean ignore;

    // one of these is set
    private String comment;
    private String key;
  }
}
