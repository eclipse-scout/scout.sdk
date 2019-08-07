/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import static java.util.Comparator.naturalOrder;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.notNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

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
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStoreStackListener;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStackEvent;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Table;
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
      return entryOfRow(element).store().isEditable();
    }
  };

  private static final AtomicLong ENTRY_ID = new AtomicLong();
  private static final int NUM_NON_LANGUAGE_COLS = 2;
  private static final String COLOR_INACTIVE_FOREGROUND = "scout.nlsRowInactiveForeground";

  private final TranslationStoreStack m_stack;
  private final Color m_colorDisabledForeground;
  private final Image m_image;
  private final ITranslationStoreStackListener m_stackListener;
  private final DisposeListener m_disposeListener;

  private WritableList<TranslationTableEntry> m_translations;
  private ObservedColumn[] m_observedColumns;
  private NlsTable m_view;
  private int m_sortIndex = INDEX_COLUMN_KEYS;
  private boolean m_ascSorting;
  private NlsReferenceProvider m_referenceProvider;
  private List<Language> m_langIndexCache; // performance cache

  protected NlsTableController(TranslationStoreStack stack) {
    m_stack = notNull(stack);
    m_image = S2ESdkUiActivator.getImage(ISdkIcons.Text);
    m_stackListener = events -> events
        .map(this::handleTranslationStoreStackEvent)
        .max(naturalOrder())
        .filter(Boolean::booleanValue)
        .ifPresent(b -> preservingSelectionDo(() -> m_view.tableViewer().refresh(false, true)));
    m_disposeListener = e -> unbind();

    ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    if (!colorRegistry.hasValueFor(COLOR_INACTIVE_FOREGROUND)) {
      colorRegistry.put(COLOR_INACTIVE_FOREGROUND, new RGB(166, 166, 166));
    }
    m_colorDisabledForeground = colorRegistry.get(COLOR_INACTIVE_FOREGROUND);
  }

  /**
   * Handles a {@link TranslationStoreStackEvent} and updates the view as required.
   *
   * @param event
   *          The event to handle.
   * @return {@code true} if the event requires the table to be re-sorted. this is the case if data in the sort column
   *         changes.
   */
  @SuppressWarnings("squid:SwitchLastCaseIsDefaultCheck")
  protected boolean handleTranslationStoreStackEvent(TranslationStoreStackEvent event) {
    switch (event.type()) {
      case TranslationStoreStackEvent.TYPE_NEW_TRANSLATION:
        m_translations.add(new TranslationTableEntry(event.entry().get()));
        updateReferenceCountInTable();
        return true;
      case TranslationStoreStackEvent.TYPE_REMOVE_TRANSLATION:
        translationToTableEntry(event.entry().get()).ifPresent(m_translations::remove);
        updateReferenceCountInTable();
        return false;
      case TranslationStoreStackEvent.TYPE_NEW_LANGUAGE:
        if (!allLanguages().contains(event.language().get())) {
          // only refresh the full table if a new column has been added.
          m_view.getDisplay().asyncExec(() -> preservingSelectionDo(this::rebind));
        }
        return false;
      case TranslationStoreStackEvent.TYPE_RELOAD:
        m_view.getDisplay().asyncExec(() -> preservingSelectionDo(this::rebind));
        return false;
      case TranslationStoreStackEvent.TYPE_KEY_CHANGED:
        m_observedColumns[INDEX_COLUMN_KEYS].fireChange(event.entry().get());
        return m_sortIndex == INDEX_COLUMN_KEYS;
      case TranslationStoreStackEvent.TYPE_UPDATE_TRANSLATION:
        ITranslationEntry changedTranslation = event.entry().get();
        for (int i = NUM_NON_LANGUAGE_COLS; i < m_observedColumns.length; i++) {
          m_observedColumns[i].fireChange(changedTranslation);
        }
        return m_sortIndex > INDEX_COLUMN_KEYS;
    }
    return false;
  }

  protected List<Language> allLanguages() {
    if (m_langIndexCache == null) {
      m_langIndexCache = m_stack.allEditableLanguages().sorted().collect(toList());
    }
    return m_langIndexCache;
  }

  protected void bind(NlsTable view) {
    m_view = notNull(view);
    m_view.createColumns();

    m_observedColumns = new ObservedColumn[NUM_NON_LANGUAGE_COLS + allLanguages().size()];
    m_observedColumns[INDEX_COLUMN_REF_COUNT] = new ObservedColumn(INDEX_COLUMN_REF_COUNT);
    m_observedColumns[INDEX_COLUMN_KEYS] = new ObservedColumn(INDEX_COLUMN_KEYS);
    for (int i = NUM_NON_LANGUAGE_COLS; i < m_observedColumns.length; i++) {
      m_observedColumns[i] = new ObservedColumn(i);
    }

    m_translations = new WritableList<>(m_stack
        .allEntries()
        .map(TranslationTableEntry::new)
        .collect(toList()), TranslationTableEntry.class);

    TableViewer viewer = m_view.tableViewer();
    ObservableListContentProvider<TranslationTableEntry> contentProvider = new ObservableListContentProvider<>();
    viewer.setComparator(this);
    viewer.setLabelProvider(new NlsTableLabelProvider(contentProvider, m_observedColumns));
    viewer.setContentProvider(contentProvider);
    viewer.setInput(m_translations);
    viewer.getControl().addDisposeListener(m_disposeListener);
    m_stack.addListener(m_stackListener);
  }

  public void preservingSelectionDo(Runnable task) {
    Table table = m_view.tableViewer().getTable();
    table.setRedraw(false);
    try {
      Optional<NlsTableCell> selection = m_view.getCursorSelection();
      task.run();
      selection.ifPresent(cell -> reveal(cell.entry().key(), cell.column()));
    }
    finally {
      table.setRedraw(true);
    }
  }

  public void reveal(String keyToSelect) {
    reveal(keyToSelect, INDEX_COLUMN_KEYS);
  }

  public void reveal(String keyToSelect, int columnToSelect) {
    for (TableItem row : m_view.tableViewer().getTable().getItems()) {
      ITranslation e = entryOfRow(row);
      if (e.key().equals(keyToSelect)) {
        m_view.tableViewer().reveal(row.getData());
        m_view.tableCursor().ensureFocus(row, columnToSelect);
        break;
      }
    }
  }

  protected void unbind() {
    m_langIndexCache = null;
    m_stack.removeListener(m_stackListener);
    m_view.tableViewer().getControl().removeDisposeListener(m_disposeListener);
  }

  protected void rebind() {
    unbind();
    bind(m_view);
  }

  protected Optional<TranslationTableEntry> translationToTableEntry(ITranslationEntry toSearch) {
    for (TranslationTableEntry candidate : m_translations) {
      if (candidate.unwrap() == toSearch) {
        return Optional.of(candidate);
      }
    }
    return Optional.empty();
  }

  public List<ITranslationEntry> getSelectedEntries() {
    IStructuredSelection selection = (IStructuredSelection) m_view.tableViewer().getSelection();
    List<ITranslationEntry> result = new ArrayList<>(selection.size());
    for (Iterator<?> it = selection.iterator(); it.hasNext();) {
      result.add(entryOfRow(it.next()));
    }
    return result;
  }

  protected String getColumnText(ITranslation element, int columnIndex) {
    switch (columnIndex) {
      case INDEX_COLUMN_REF_COUNT:
        NlsReferenceProvider refProvider = getReferenceProvider();
        if (refProvider == null) {
          return "";
        }
        List<Match> references = refProvider.getReferencesFor(element);
        if (references == null) {
          return "0";
        }
        return Integer.toString(references.size());
      case INDEX_COLUMN_KEYS:
        return element.key();
      default:
        Language lang = languageOfColumn(columnIndex);
        String text = element.translation(lang).orElse("");
        return Strings.replaceEach(text, new String[]{"\n", "\r"}, new String[]{" ", ""});
    }
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    int index = m_sortIndex;
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

    String a = getColumnText(entryOfRow(first), index);
    String b = getColumnText(entryOfRow(second), index);

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
        int numA = Integer.parseInt(a);
        int numB = Integer.parseInt(b);
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

  public TranslationStoreStack stack() {
    return m_stack;
  }

  protected static ITranslationEntry entryOfRow(Object row) {
    return ((TranslationTableEntry) row).unwrap();
  }

  protected static ITranslationEntry entryOfRow(TableItem row) {
    return entryOfRow(row.getData());
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
    ObservedColumn refCountProperty = m_observedColumns[INDEX_COLUMN_REF_COUNT];
    if (refCountProperty != null) {
      m_stack.allEntries().forEach(refCountProperty::fireChange);
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
      ITranslationEntry entry = entryOfRow(element);
      if (!entry.store().isEditable()) {
        return m_colorDisabledForeground;
      }
      if (columnIndex > INDEX_COLUMN_KEYS) {
        Language lang = languageOfColumn(columnIndex);
        boolean langExists = entry.store().languages().anyMatch(isEqual(lang));
        if (!langExists) {
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

  private final class ObservedColumn extends SimpleValueProperty<TranslationTableEntry, String> {

    private NativeListener m_propertyChangeListener;
    private final int m_index;

    private ObservedColumn(int index) {
      m_index = index;
    }

    @Override
    public Object getValueType() {
      return null;
    }

    private void fireChange(ITranslationEntry entry) {
      if (m_propertyChangeListener != null) {
        m_propertyChangeListener.fireChange(entry);
      }
    }

    @Override
    protected String doGetValue(TranslationTableEntry source) {
      return getColumnText(source.unwrap(), m_index);
    }

    @Override
    protected void doSetValue(TranslationTableEntry source, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public INativePropertyListener<TranslationTableEntry> adaptListener(ISimplePropertyListener<TranslationTableEntry, ValueDiff<? extends String>> listener) {
      m_propertyChangeListener = new NativeListener(this, listener);
      return m_propertyChangeListener;
    }
  }

  private final class NativeListener extends NativePropertyListener<TranslationTableEntry, ValueDiff<? extends String>> {

    private NativeListener(IProperty property, ISimplePropertyListener<TranslationTableEntry, ValueDiff<? extends String>> listener) {
      super(property, listener);
    }

    private void fireChange(ITranslationEntry entry) {
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
    private final ITranslationEntry m_entry;
    private final long m_id;

    private TranslationTableEntry(ITranslationEntry entry) {
      m_entry = entry;
      m_id = ENTRY_ID.getAndIncrement();
    }

    private ITranslationEntry unwrap() {
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

      TranslationTableEntry other = (TranslationTableEntry) obj;
      return m_id == other.m_id;
    }
  }
}
