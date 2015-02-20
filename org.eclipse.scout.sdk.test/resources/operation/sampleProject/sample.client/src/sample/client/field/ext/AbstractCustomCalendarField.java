/**
 *
 */
package sample.client.field.ext;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;

/**
 * @author Andreas Hoegger
 */
public abstract class AbstractCustomCalendarField extends AbstractCalendarField<AbstractCustomCalendarField.Calendar> {

  @Order(10)
  public class Calendar extends AbstractCalendar {

  }
}
