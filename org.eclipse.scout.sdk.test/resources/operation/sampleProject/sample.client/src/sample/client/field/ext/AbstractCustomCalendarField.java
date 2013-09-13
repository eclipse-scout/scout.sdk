/**
 *
 */
package sample.client.field.ext;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;
import org.eclipse.scout.rt.extension.client.ui.basic.calendar.AbstractExtensibleCalendar;

/**
 * @author aho
 */
public class AbstractCustomCalendarField extends AbstractCalendarField<AbstractCustomCalendarField.Calendar> {

  @Order(10)
  public class Calendar extends AbstractExtensibleCalendar {

  }
}
