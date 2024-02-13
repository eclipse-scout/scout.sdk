/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.zip.GZIPInputStream;

import org.eclipse.scout.sdk.core.log.SdkLog;

/**
 * Contains helper methods to access remote resources.
 */
public final class Resources {

  private static final int BUFFER_SIZE = 8192;

  public static final String PROTOCOL_HTTP = "http";
  public static final String PROTOCOL_HTTPS = "https";

  public static final String HEADER_PRAGMA = "Pragma";
  public static final String HEADER_CACHE_CONTROL = "Cache-Control";
  public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";

  public static final String ENCODING_GZIP = "gzip";

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
   *           if the url is not valid or the thread is interrupted while waiting for the response.
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
    if (PROTOCOL_HTTPS.equalsIgnoreCase(scheme) || PROTOCOL_HTTP.equalsIgnoreCase(scheme)) {
      return httpGet(uri);
    }
    var stream = uri.toURL().openStream();
    if (stream instanceof BufferedInputStream) {
      return stream;
    }
    return new BufferedInputStream(stream, BUFFER_SIZE);
  }

  /**
   * Performs an HTTP GET request to the given {@link URL}.
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
   * Performs an HTTP GET request to the given uri {@link String}.
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
   * Performs an HTTP GET request to the given {@link URI}.
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
      return doHttpGetWithRetry(uri);
    }
    catch (InterruptedException e) {
      throw new SdkException("Interrupted while reading from URI {}", toSimple(uri), e);
    }
  }

  static InputStream doHttpGetWithRetry(URI uri) throws IOException, InterruptedException {
    IOException exception = null;
    var numRetries = 5; // retry 5 times as some resources might be temporary unavailable
    for (var attempt = 1; attempt <= numRetries; attempt++) {
      try {
        return doHttpGet(uri);
      }
      catch (HttpTimeoutException e) {
        exception = new IOException("HTTP GET timed out for URI " + toSimple(uri), e);
        SdkLog.debug(exception);
      }
      catch (IOException e) {
        SdkLog.debug(e);
        exception = e;
      }
      if (attempt < numRetries) {
        Thread.sleep(getSleepTimeAfterAttempt(attempt)); // quick wait between retries
      }
    }
    throw exception;
  }

  @SuppressWarnings({"UnsecureRandomNumberGeneration", "NumericCastThatLosesPrecision"})
  static long getSleepTimeAfterAttempt(int attempt) {
    var baseTimeout = attempt * 300L;
    var randomOffset = (long) (Math.random() * 200);
    return baseTimeout + randomOffset;
  }

  static InputStream doHttpGet(URI uri) throws IOException, InterruptedException {
    var timeout = Duration.ofSeconds(10);
    var request = HttpRequest.newBuilder()
        .uri(uri)
        .version(Version.HTTP_2)
        .timeout(timeout)
        .setHeader(HEADER_PRAGMA, "no-cache")
        .setHeader(HEADER_CACHE_CONTROL, "no-cache, max-age=0, must-revalidate")
        .setHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP) // support gzip compression
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
      throw new IOException("HTTP status code " + statusCode + " received from " + toSimple(uri));
    }

    var encoding = response.headers()
        .firstValue(HEADER_CONTENT_ENCODING)
        .orElse("")
        .trim();
    if (ENCODING_GZIP.equalsIgnoreCase(encoding)) {
      return new GZIPInputStream(response.body());
    }
    return response.body();
  }

  static String toSimple(URI uri) {
    try {
      var shortened = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null);
      if (shortened.equals(uri)) {
        return uri.toString();
      }
      return shortened + " (url shortened)";
    }
    catch (URISyntaxException e) {
      SdkLog.debug("Cannot simplify uri '{}'.", uri, e);
      return uri.toString();
    }
  }

  static URI toURI(URL url) throws IOException {
    try {
      return url.toURI();
    }
    catch (URISyntaxException e) {
      throw new IOException("URL '" + url + "' cannot be converted to URI.", e);
    }
  }
}
