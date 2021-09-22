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
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationManagerListener;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link NlsTableController}</h3>
 *
 * @since 7.0.0
 */
public class NlsTableController extends ViewerComparator {

  public static final int INDEX_COLUMN_REF_COUNT = 0;
  public static final int INDEX_COLUMN_KEYS = 1;
  public static final ViewerFilter EDITABLE_ONLY_FILTER = new ViewerFilter() {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      return translationOfRow(element).hasEditableStores();
    }
  };

  private static final AtomicLong ENTRY_ID = new AtomicLong();
  private static final int NUM_NON_LANGUAGE_COLS = 2;
  private static final String COLOR_INACTIVE_FOREGROUND = "scout.nlsRowInactiveForeground";

  private final TranslationManager m_manager;
  private final Color m_colorDisabledForeground;
  private final Image m_image;
  private final ITranslationManagerListener m_managerListener;
  private final DisposeListener m_disposeListener;

  private ObservedColumn[] m_observedColumns;
  private NlsTable m_view;
  private int m_sortIndex = INDEX_COLUMN_KEYS;
  private boolean m_ascSorting;
  private NlsReferenceProvider m_referenceProvider;
  private List<Language> m_langIndexCache; // performance cache
  private WritableList<TranslationTableEntry> m_translationList; // observable list wrapping m_translations
  private List<TranslationTableEntry> m_translations; // original list. is wrapped with m_translationWritableList

  protected NlsTableController(TranslationManager manager) {
    m_manager = notNull(manager);
    m_image = S2ESdkUiActivator.getImage(ISdkIcons.Text);
    m_managerListener = events -> m_view.getDisplay().asyncExec(() -> handleTranslationManagerEvents(events));
    m_disposeListener = e -> unbind();

    var colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    if (!colorRegistry.hasValueFor(COLOR_INACTIVE_FOREGROUND)) {
      colorRegistry.put(COLOR_INACTIVE_FOREGROUND, new RGB(166, 166, 166));
    }
    m_colorDisabledForeground = colorRegistry.get(COLOR_INACTIVE_FOREGROUND);
  }

  protected void handleTranslationManagerEvents(Stream<TranslationManagerEvent> events) {
    events
        .map(this::handleTranslationManagerEvent)
        .max(naturalOrder())
        .ifPresent(this::finishTranslationManagerEvents);
  }

  /**
   * Callback executed after all events have been handled. Here the table may be lay out or refreshed.
   */
  protected void finishTranslationManagerEvents(boolean requireReSort) {
    if (requireReSort) {
      preservingSelectionDo(() -> m_view.tableViewer().refresh(false, true));
    }
    else {
      // no sort of the table required. just layout the table.
      // this is necessary on some operating system to ensure new texts are visible below the cursor (when editing in Excel style).
      m_view.tableViewer().getTable().requestLayout();
    }
  }

  /**
   * Handles a {@link TranslationManagerEvent} and updates the view as required.
   *
   * @param event
   *          The event to handle.
   * @return {@code true} if the event requires the table to be re-sorted. this is the case if data in the sort column
   *         changes.
   */
  @SuppressWarnings("squid:SwitchLastCaseIsDefaultCheck")
  protected boolean handleTranslationManagerEvent(TranslationManagerEvent event) {
    switch (event.type()) {
      case TranslationManagerEvent.TYPE_NEW_TRANSLATION:
        var newTranslation = event.translation().get();
        m_translationList.add(new TranslationTableEntry(newTranslation));
        reveal(newTranslation.key());
        updateReferenceCountInTable();
        return true;
      case TranslationManagerEvent.TYPE_REMOVE_TRANSLATION:
        translationToTableEntry(event.translation().get()).ifPresent(m_translationList::remove);
        updateReferenceCountInTable();
        return false;
      case TranslationManagerEvent.TYPE_NEW_LANGUAGE:
        if (!allLanguages().contains(event.language().get())) {
          // only refresh the full table if a new column has been added.
          m_view.getDisplay().asyncExec(() -> preservingSelectionDo(this::rebind));
        }
        return false;
      case TranslationManagerEvent.TYPE_RELOAD:
        m_view.getDisplay().asyncExec(() -> preservingSelectionDo(this::rebind));
        return false;
      case TranslationManagerEvent.TYPE_KEY_CHANGED:
        m_translations.stream()
            .filter(t -> t.unwrap().key().equals(event.key().orElse(null)))
            .findAny()
            .ifPresent(m_translationList::remove);
        var translationWitNewKey = event.translation().get();
        m_translationList.add(new TranslationTableEntry(translationWitNewKey));
        reveal(translationWitNewKey.key());
        updateReferenceCountInTable();
        return true;
      case TranslationManagerEvent.TYPE_UPDATE_TRANSLATION:
        var changedTranslation = event.translation().get();
        for (var i = NUM_NON_LANGUAGE_COLS; i < m_observedColumns.length; i++) {
          m_observedColumns[i].fireChange(changedTranslation);
        }
        return true;
    }
    return false;
  }

  protected List<Language> allLanguages() {
    if (m_langIndexCache == null) {
      m_langIndexCache = m_manager.allLanguages().sorted().collect(toList());
    }
    return m_langIndexCache;
  }

  protected void bind(NlsTable view) {
    m_view = notNull(view);
    m_view.createColumns();

    m_observedColumns = new ObservedColumn[NUM_NON_LANGUAGE_COLS + allLanguages().size()];
    m_observedColumns[INDEX_COLUMN_REF_COUNT] = new ObservedColumn(INDEX_COLUMN_REF_COUNT);
    m_observedColumns[INDEX_COLUMN_KEYS] = new ObservedColumn(INDEX_COLUMN_KEYS);
    for (var i = NUM_NON_LANGUAGE_COLS; i < m_observedColumns.length; i++) {
      m_observedColumns[i] = new ObservedColumn(i);
    }

    m_translations = m_manager
        .allTranslations()
        .map(TranslationTableEntry::new)
        .collect(toList());
    m_translationList = new WritableList<>(m_translations, TranslationTableEntry.class);

    var viewer = m_view.tableViewer();
    var contentProvider = new ObservableListContentProvider<TranslationTableEntry>();
    viewer.setComparator(this);
    viewer.setLabelProvider(new NlsTableLabelProvider(contentProvider, m_observedColumns));
    viewer.setContentProvider(contentProvider);
    viewer.setInput(m_translationList);
    viewer.getControl().addDisposeListener(m_disposeListener);
    m_manager.addListener(m_managerListener);
  }

  public void preservingSelectionDo(Runnable task) {
    var table = m_view.tableViewer().getTable();
    table.setRedraw(false);
    try {
      var selection = m_view.getCursorSelection();
      task.run();
      selection.ifPresent(cell -> reveal(cell.translation().key(), cell.column()));
    }
    finally {
      table.setRedraw(true);
    }
  }

  public void reveal(String keyToSelect) {
    reveal(keyToSelect, INDEX_COLUMN_KEYS);
  }

  public void reveal(String keyToSelect, int columnToSelect) {
    for (var row : m_view.tableViewer().getTable().getItems()) {
      ITranslation e = translationOfRow(row);
      if (e.key().equals(keyToSelect)) {
        m_view.tableViewer().reveal(row.getData());
        m_view.tableCursor().ensureFocus(row, columnToSelect);
        break;
      }
    }
  }

  protected void unbind() {
    m_langIndexCache = null;
    m_manager.removeListener(m_managerListener);
    m_view.tableViewer().getControl().removeDisposeListener(m_disposeListener);
  }

  protected void rebind() {
    unbind();
    bind(m_view);
  }

  protected Optional<TranslationTableEntry> translationToTableEntry(IStackedTranslation toSearch) {
    for (var candidate : m_translations) {
      if (candidate.unwrap() == toSearch) {
        return Optional.of(candidate);
      }
    }
    return Optional.empty();
  }

  public List<IStackedTranslation> getSelectedEntries() {
    var selection = (IStructuredSelection) m_view.tableViewer().getSelection();
    List<IStackedTranslation> result = new ArrayList<>(selection.size());
    for (var o : selection) {
      result.add(translationOfRow(o));
    }
    return result;
  }

  protected String getColumnText(ITranslation element, int columnIndex) {
    switch (columnIndex) {
      case INDEX_COLUMN_REF_COUNT:
        var refProvider = getReferenceProvider();
        if (refProvider == null) {
          return "";
        }
        return Integer.toString(refProvider.getReferencesFor(element).size());
      case INDEX_COLUMN_KEYS:
        return element.key();
      default:
        var lang = languageOfColumn(columnIndex);
        var text = element.text(lang).orElse("");
        //noinspection HardcodedLineSeparator
        return Strings.replaceEach(text, new String[]{"\n", "\r"}, new String[]{" ", ""}).toString();
    }
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    var index = m_sortIndex;
    Object first;
    Object second;
    if (m_ascSorting) {
      first = e2;
      second = e1;
    }
    else {
      first = e1;
      second = e2;
    }

    var a = getColumnText(translationOfRow(first), index);
    var b = getColumnText(translationOfRow(second), index);

    if (Objects.equals(a, b)) {
      return 0;
    }
    if (a == null) {
      return -1;
    }
    if (b == null) {
      return 1;
    }

    if (index == 0 && !a.isEmpty() && !b.isEmpty()) {
      // sort by NLS entry usage (numeric)
      try {
        var numA = Integer.parseInt(a);
        var numB = Integer.parseInt(b);
        return Integer.compare(numA, numB);
      }
      catch (NumberFormatException e) {
        SdkLog.info("no valid number '{}' or '{}'.", a, b, e);
      }
    }

    return a.compareToIgnoreCase(b);
  }

  public boolean isAscSorting() {
    return m_ascSorting;
  }

  public void setAscSorting(boolean sorting) {
    m_ascSorting = sorting;
  }

  public int getSortIndex() {
    return m_sortIndex;
  }

  public void setSortIndex(int index) {
    m_sortIndex = index;
  }

  public TranslationManager translationManager() {
    return m_manager;
  }

  protected static IStackedTranslation translationOfRow(Object row) {
    return ((TranslationTableEntry) row).unwrap();
  }

  protected static IStackedTranslation translationOfRow(TableItem row) {
    return translationOfRow(row.getData());
  }

  public void setReferenceProvider(NlsReferenceProvider referenceProvider) {
    if (m_referenceProvider == referenceProvider) {
      return;
    }
    m_referenceProvider = referenceProvider;
    updateReferenceCountInTable();
  }

  public void setHideReadOnly(boolean hide) {
    preservingSelectionDo(() -> {
      if (hide) {
        m_view.tableViewer().addFilter(EDITABLE_ONLY_FILTER);
      }
      else {
        m_view.tableViewer().removeFilter(EDITABLE_ONLY_FILTER);
      }
    });
  }

  protected void updateReferenceCountInTable() {
    var referenceProvider = getReferenceProvider();
    if (referenceProvider == null) {
      return; // no need to update ref count as there is no reference provider and therefore no count
    }

    var refCountProperty = m_observedColumns[INDEX_COLUMN_REF_COUNT];
    if (refCountProperty != null) {
      m_manager.allTranslations().forEach(refCountProperty::fireChange);
    }
  }

  public NlsReferenceProvider getReferenceProvider() {
    return m_referenceProvider;
  }

  /**
   * @param index
   *          The zero based index of the viewer table. This is the table with the ref-count, key and translation
   *          columns. The first index therefore has no language
   * @return The language of the specified table column index or {@code null} if the column does not correspond to a
   *         {@link Language} (e.g. the key column).
   */
  public Language languageOfColumn(int index) {
    if (index == INDEX_COLUMN_REF_COUNT || index == INDEX_COLUMN_KEYS) {
      return null;
    }
    return allLanguages().get(index - 2);
  }

  private final class NlsTableLabelProvider extends ObservableMapLabelProvider implements ITableColorProvider {

    private NlsTableLabelProvider(ObservableListContentProvider<TranslationTableEntry> contentProvider, ObservedColumn... observedColumns) {
      super(Properties.observeEach(contentProvider.getKnownElements(), observedColumns));
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return m_image;
      }
      return null;
    }

    @Override
    public Color getForeground(Object element, int columnIndex) {
      if (INDEX_COLUMN_REF_COUNT == columnIndex) {
        return null;
      }
      var entry = translationOfRow(element);
      if (INDEX_COLUMN_KEYS == columnIndex) {
        if (!entry.hasOnlyEditableStores()) {
          return m_colorDisabledForeground;
        }
        return null;
      }
      if (columnIndex > INDEX_COLUMN_KEYS) {
        var cellEditable = entry.hasEditableStores();
        if (!cellEditable) {
          return m_colorDisabledForeground;
        }
      }
      return null;
    }

    @Override
    public Color getBackground(Object element, int columnIndex) {
      return null;
    }
  }

  private final class ObservedColumn extends SimpleValueProperty<TranslationTableEntry, CharSequence> {

    private NativeListener m_propertyChangeListener;
    private final int m_index;

    private ObservedColumn(int index) {
      m_index = index;
    }

    @Override
    public Object getValueType() {
      return null;
    }

    private void fireChange(IStackedTranslation entry) {
      if (m_propertyChangeListener != null) {
        m_propertyChangeListener.fireChange(entry);
      }
    }

    @Override
    protected CharSequence doGetValue(TranslationTableEntry source) {
      return getColumnText(source.unwrap(), m_index);
    }

    @Override
    protected void doSetValue(TranslationTableEntry source, CharSequence value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public INativePropertyListener<TranslationTableEntry> adaptListener(ISimplePropertyListener<TranslationTableEntry, ValueDiff<? extends CharSequence>> listener) {
      m_propertyChangeListener = new NativeListener(this, listener);
      //noinspection ReturnOfInnerClass
      return m_propertyChangeListener;
    }
  }

  private final class NativeListener extends NativePropertyListener<TranslationTableEntry, ValueDiff<? extends CharSequence>> {

    private NativeListener(IProperty property, ISimplePropertyListener<TranslationTableEntry, ValueDiff<? extends CharSequence>> listener) {
      super(property, listener);
    }

    private void fireChange(IStackedTranslation entry) {
      translationToTableEntry(entry).ifPresent(e -> super.fireChange(e, null));
    }

    @Override
    protected void doAddTo(TranslationTableEntry source) {
      // not necessary. is already added
    }

    @Override
    protected void doRemoveFrom(TranslationTableEntry source) {
      // not necessary
    }
  }

  /**
   * Use a translation entry with a unique id so that the hashCode does not change when the wrapped entry's hashCode
   * changes. Otherwise we cannot find it anymore (because we do hashLookup in the table).
   */
  private static final class TranslationTableEntry {
    private final IStackedTranslation m_entry;
    private final long m_id;

    private TranslationTableEntry(IStackedTranslation entry) {
      m_entry = entry;
      m_id = ENTRY_ID.getAndIncrement();
    }

    private IStackedTranslation unwrap() {
      return m_entry;
    }

    @Override
    public int hashCode() {
      return Long.hashCode(m_id);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      var other = (TranslationTableEntry) obj;
      return m_id == other.m_id;
    }
  }
}
