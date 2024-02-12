/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scout.sdk.core.java.JavaTypes.simpleName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;

public final class TranslationTestsHelper {
  private TranslationTestsHelper() {
  }

  public static ITranslationStore createStore(String fqn, double order, Map<String, Map<Language, String>> entries) {
    return createStore(fqn, order, true, entries);
  }

  public static ITranslationStore createStore(String fqn, double order, boolean isEditable, Map<String, Map<Language, String>> entries) {
    var serviceType = mock(IType.class);
    when(serviceType.name()).thenReturn(fqn);
    when(serviceType.elementName()).thenReturn(simpleName(fqn));

    var svc = mock(TextProviderService.class);
    when(svc.order()).thenReturn(order);
    when(svc.type()).thenReturn(serviceType);

    var mock = isEditable ? mock(IEditableTranslationStore.class) : mock(ITranslationStore.class);
    Collection<ITranslationEntry> allEntries = new ArrayList<>();
    for (var entry : entries.entrySet()) {
      var entryMock = mock(ITranslationEntry.class);
      when(entryMock.key()).thenReturn(entry.getKey());
      when(entryMock.texts()).thenReturn(entry.getValue());
      when(entryMock.store()).thenReturn(mock);
      allEntries.add(entryMock);
    }

    when(mock.size()).then(invocation -> (long) allEntries.size());
    when(mock.isEditable()).thenReturn(isEditable);
    when(mock.get(anyString())).then(invocation -> {
      String keyToFind = invocation.getArgument(0);
      return allEntries.stream()
          .filter(e -> e.key().equals(keyToFind))
          .findAny();
    });
    when(mock.get(anyString(), any())).then(invocation -> {
      String key = invocation.getArgument(0);
      Language lang = invocation.getArgument(1);
      return allEntries.stream().filter(e -> e.key().equals(key)).findFirst().flatMap(e -> e.text(lang));
    });
    when(mock.containsKey(anyString())).then(invocation -> mock.get(invocation.<String> getArgument(0)).isPresent());
    when(mock.keys()).thenAnswer(invocation -> allEntries.stream().map(ITranslation::key));
    when(mock.languages()).thenAnswer(invocation -> allEntries.stream().flatMap(ITranslation::languages).distinct());
    when(mock.service()).thenReturn(svc);
    when(mock.entries()).then(invocation -> allEntries.stream());
    when(mock.containsLanguage(any(Language.class))).then(invocation -> allEntries.stream().anyMatch(t -> t.texts().containsKey(invocation.getArgument(0, Language.class))));

    if (isEditable) {
      var editableStore = (IEditableTranslationStore) mock;
      when(editableStore.changeKey(anyString(), anyString())).then(invocation -> {
        String oldKey = invocation.getArgument(0);
        String newKey = invocation.getArgument(1);
        for (var e : allEntries) {
          if (e.key().equals(oldKey)) {
            when(e.key()).thenReturn(newKey);
            return e;
          }
        }
        return null;
      });
      when(editableStore.setTranslation(any())).then(invocation -> {
        ITranslation newTranslation = invocation.getArgument(0);
        var existingTranslation = allEntries.stream().filter(e -> e.key().equals(newTranslation.key())).findAny().orElse(null);

        if (existingTranslation == null) {
          // add
          ITranslationEntry newEntry = new TranslationEntry(newTranslation, editableStore);
          allEntries.add(newEntry);
          return newEntry;
        }

        // update
        ITranslationEntry newEntry = new TranslationEntry(newTranslation, editableStore);
        allEntries.removeIf(entry -> entry.key().equals(newTranslation.key()));
        allEntries.add(newEntry);
        return newEntry;

      });
    }
    return mock;
  }

  public static ITranslationStore createStore(String fqn, double order, String... keys) {
    var entries = Stream.of(keys)
        .collect(toMap(identity(), k -> Map.of(Language.LANGUAGE_DEFAULT, "default"), Ensure::failOnDuplicates));
    return createStore(fqn, order, entries);
  }
}
