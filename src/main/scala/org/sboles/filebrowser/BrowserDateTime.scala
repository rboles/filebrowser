
package org.sboles.filebrowser

import java.util.Date

/**
 * Provides an interface for a common date-time format
 *
 * @sboles
 */
trait BrowserDateTime {

  /**
   * @return Date-time as an instance of java.util.date
   */
  def dateTime: Date

  def >(that: Date): Boolean = dateTime.compareTo(that) > 0

  def >(that: BrowserDateTime): Boolean = dateTime.compareTo(that.dateTime) > 0

  def <(that: Date): Boolean = dateTime.compareTo(that) < 0

  def <(that: BrowserDateTime): Boolean = dateTime.compareTo(that.dateTime) < 0

  def ==(that: Date): Boolean = dateTime.compareTo(that) == 0

  def ==(that: BrowserDateTime): Boolean = dateTime.compareTo(that.dateTime) == 0
}
