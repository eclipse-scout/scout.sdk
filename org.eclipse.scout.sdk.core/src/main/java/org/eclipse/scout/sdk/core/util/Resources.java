/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import static java.net.http.HttpResponse.BodyHandlers.buffering;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieManager;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Locale;

/**
 * Contains helper methods to access remote resources.
 */
public final class Resources {

  private static final int BUFFER_SIZE = 8192;

  private Resources() {
  }

  /**
   * Opens a connection to the given {@link URL} and returns the content as {@link InputStream}.
   *
   * @param url
   *          The {@link URL} to connect to. Must not be {@code null}.
   * @return An {@link InputStream} returning the content of the target {@link URL}.
   * @throws IOException
   *           if the connection fails.
   * @throws SdkException
   *           if the thread is interrupted while waiting for the response.
   */
  public static InputStream openStream(URL url) throws IOException {
    return openStream(toURI(url));
  }

  /**
   * Opens a connection to the given uri and returns the content as {@link InputStream}.
   *
   * @param uri
   *          The uri to connect to. Must be a valid {@link URI} and must not be {@code null}.
   * @return An {@link InputStream} returning the content of the target {@link URI}.
   * @throws IOException
   *           if the connection fails.
   * @throws SdkException
   *           if the uri is not valid or the thread is interrupted while waiting for the response.
   */
  public static InputStream openStream(String uri) throws IOException {
    try {
      return openStream(new URI(uri));
    }
    catch (URISyntaxException e) {
      throw new SdkException("Invalid URI: '{}'.", uri, e);
    }
  }

  /**
   * Opens a connection to the given {@link URI} and returns the content as {@link InputStream}.
   * 
   * @param uri
   *          The {@link URI} to connect to. Must not be {@code null}.
   * @return An {@link InputStream} returning the content of the target {@link URI}.
   * @throws IOException
   *           if the connection fails.
   * @throws SdkException
   *           if the thread is interrupted while waiting for the response.
   */
  public static InputStream openStream(URI uri) throws IOException {
    var scheme = uri.getScheme();
    if (scheme != null) {
      scheme = scheme.toLowerCase(Locale.US);
    }
    if ("http".equals(scheme) || "https".equals(scheme)) {
      return httpGet(uri);
    }
    var stream = uri.toURL().openStream();
    if (stream instanceof BufferedInputStream) {
      return stream;
    }
    return new BufferedInputStream(stream, BUFFER_SIZE);
  }

  /**
   * Performs a HTTP GET request to the given {@link URL} without any authentication.
   *
   * @param url
   *          The {@link URL} to get. Must not be {@code null}.
   * @return An {@link InputStream} returning the content of the body of the HTTP response.
   * @throws IOException
   *           if the connections fails or an error status code is returned by the server.
   * @throws SdkException
   *           if the thread is interrupted while waiting for the response.
   */
  public static InputStream httpGet(URL url) throws IOException {
    return httpGet(toURI(url));
  }

  /**
   * Performs a HTTP GET request to the given uri without any authentication.
   *
   * @param uri
   *          The uri to get. Must be a valid {@link URI} and must not be {@code null}.
   * @return An {@link InputStream} returning the content of the body of the HTTP response.
   * @throws IOException
   *           if the connections fails or an error status code is returned by the server.
   * @throws SdkException
   *           if the uri is invalid or the thread is interrupted while waiting for the response.
   */
  public static InputStream httpGet(String uri) throws IOException {
    try {
      return httpGet(new URI(uri));
    }
    catch (URISyntaxException e) {
      throw new SdkException("Invalid URI: '{}'.", uri, e);
    }
  }

  /**
   * Performs a HTTP GET request to the given {@link URI} without any authentication.
   * 
   * @param uri
   *          The {@link URI} to get. Must not be {@code null}.
   * @return An {@link InputStream} returning the content of the body of the HTTP response.
   * @throws IOException
   *           if the connections fails or an error status code is returned by the server.
   * @throws SdkException
   *           if the thread is interrupted while waiting for the response.
   */
  public static InputStream httpGet(URI uri) throws IOException {
    try {
      var timeout = Duration.ofSeconds(10);
      var request = HttpRequest.newBuilder()
          .uri(uri)
          .version(Version.HTTP_2)
          .timeout(timeout)
          .setHeader("Pragma", "no-cache") // HTTP/1.0
          .setHeader("Cache-Control", "no-cache, no-store, must-revalidate") // HTTP/1.1
          .GET()
          .build();
      var handler = buffering(ofInputStream(), BUFFER_SIZE);
      var proxySelector = ProxySelector.getDefault();
      var authenticator = Authenticator.getDefault();
      var clientBuilder = HttpClient.newBuilder()
          .followRedirects(Redirect.NORMAL)
          .cookieHandler(new CookieManager(null, null))
          .connectTimeout(timeout);
      if (proxySelector != null) {
        clientBuilder.proxy(proxySelector);
      }
      if (authenticator != null) {
        clientBuilder.authenticator(authenticator);
      }

      var response = clientBuilder.build().send(request, handler);
      var statusCode = response.statusCode();
      if (statusCode < 200 || statusCode > 299) {
        throw new IOException("Status code " + statusCode + " received getting '" + toSimple(uri) + "' (url shortened).");
      }
      return response.body();
    }
    catch (InterruptedException e) {
      throw new SdkException("Interrupted while reading from URI '{}' (url shortened).", toSimple(uri), e);
    }
  }

  static String toSimple(URI uri) {
    try {
      return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
    }
    catch (URISyntaxException e) {
      throw new SdkException("Invalid URI.", e);
    }
  }

  static URI toURI(URL url) {
    try {
      return url.toURI();
    }
    catch (URISyntaxException e) {
      throw new SdkException("URL '{}' cannot be converted to URI.", url, e);
    }
  }
}
