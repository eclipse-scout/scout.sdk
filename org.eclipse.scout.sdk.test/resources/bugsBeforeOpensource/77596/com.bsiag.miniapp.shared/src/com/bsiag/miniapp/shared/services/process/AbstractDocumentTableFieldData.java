package com.bsiag.miniapp.shared.services.process;

import java.util.Date;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;

public abstract class AbstractDocumentTableFieldData extends AbstractTableFieldData {
  private static final long serialVersionUID = 1L;

  public AbstractDocumentTableFieldData() {
  }

  public static final int DOCUMENT_NR_COLUMN_ID = 0;
  public static final int NAME_COLUMN_ID = 1;
  public static final int DESCRIPTION_COLUMN_ID = 2;
  public static final int DOCUMENT_TYPE_COLUMN_ID = 3;
  public static final int REGISTERED_ON_COLUMN_ID = 4;
  public static final int REGISTERED_BY_COLUMN_ID = 5;
  public static final int CHANGED_ON_COLUMN_ID = 6;
  public static final int CONTENT_COLUMN_ID = 7;

  public void setDocumentNr(int row, Long documentNr) {
    setValueInternal(row, DOCUMENT_NR_COLUMN_ID, documentNr);
  }

  public Long getDocumentNr(int row) {
    return (Long) getValueInternal(row, DOCUMENT_NR_COLUMN_ID);
  }

  public void setName(int row, String name) {
    setValueInternal(row, NAME_COLUMN_ID, name);
  }

  public String getName(int row) {
    return (String) getValueInternal(row, NAME_COLUMN_ID);
  }

  public void setDescription(int row, String description) {
    setValueInternal(row, DESCRIPTION_COLUMN_ID, description);
  }

  public String getDescription(int row) {
    return (String) getValueInternal(row, DESCRIPTION_COLUMN_ID);
  }

  public void setDocumentType(int row, Long documentType) {
    setValueInternal(row, DOCUMENT_TYPE_COLUMN_ID, documentType);
  }

  public Long getDocumentType(int row) {
    return (Long) getValueInternal(row, DOCUMENT_TYPE_COLUMN_ID);
  }

  public void setRegisteredOn(int row, Date registeredOn) {
    setValueInternal(row, REGISTERED_ON_COLUMN_ID, registeredOn);
  }

  public Date getRegisteredOn(int row) {
    return (Date) getValueInternal(row, REGISTERED_ON_COLUMN_ID);
  }

  public void setRegisteredBy(int row, Long registeredBy) {
    setValueInternal(row, REGISTERED_BY_COLUMN_ID, registeredBy);
  }

  public Long getRegisteredBy(int row) {
    return (Long) getValueInternal(row, REGISTERED_BY_COLUMN_ID);
  }

  public void setChangedOn(int row, Date changedOn) {
    setValueInternal(row, CHANGED_ON_COLUMN_ID, changedOn);
  }

  public Date getChangedOn(int row) {
    return (Date) getValueInternal(row, CHANGED_ON_COLUMN_ID);
  }

  public void setContent(int row, Object content) {
    setValueInternal(row, CONTENT_COLUMN_ID, content);
  }

  public Object getContent(int row) {
    return getValueInternal(row, CONTENT_COLUMN_ID);
  }

  @Override
  public int getColumnCount() {
    return 8;
  }

  @Override
  public Object getValueAt(int row, int column) {
    switch (column) {
      case DOCUMENT_NR_COLUMN_ID:
        return getDocumentNr(row);
      case NAME_COLUMN_ID:
        return getName(row);
      case DESCRIPTION_COLUMN_ID:
        return getDescription(row);
      case DOCUMENT_TYPE_COLUMN_ID:
        return getDocumentType(row);
      case REGISTERED_ON_COLUMN_ID:
        return getRegisteredOn(row);
      case REGISTERED_BY_COLUMN_ID:
        return getRegisteredBy(row);
      case CHANGED_ON_COLUMN_ID:
        return getChangedOn(row);
      case CONTENT_COLUMN_ID:
        return getContent(row);
      default:
        return null;
    }
  }

  @Override
  public void setValueAt(int row, int column, Object value) {
    switch (column) {
      case DOCUMENT_NR_COLUMN_ID:
        setDocumentNr(row, (Long) value);
        break;
      case NAME_COLUMN_ID:
        setName(row, (String) value);
        break;
      case DESCRIPTION_COLUMN_ID:
        setDescription(row, (String) value);
        break;
      case DOCUMENT_TYPE_COLUMN_ID:
        setDocumentType(row, (Long) value);
        break;
      case REGISTERED_ON_COLUMN_ID:
        setRegisteredOn(row, (Date) value);
        break;
      case REGISTERED_BY_COLUMN_ID:
        setRegisteredBy(row, (Long) value);
        break;
      case CHANGED_ON_COLUMN_ID:
        setChangedOn(row, (Date) value);
        break;
      case CONTENT_COLUMN_ID:
        setContent(row, value);
        break;
    }
  }
}
