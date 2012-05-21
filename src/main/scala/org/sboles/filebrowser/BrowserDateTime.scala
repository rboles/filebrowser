
package org.sboles.filebrowser

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Provides an interface for a common date-time format
 *
 * @sboles
 */
trait BrowserDateTime {

  /**
   * @return Date-time as an instance of java.util.Date
   */
  def dateTime: Date

  def >(that: Date): Boolean = dateTime.compareTo(that) > 0

  def >(that: BrowserDateTime): Boolean = dateTime.compareTo(that.dateTime) > 0

  def <(that: Date): Boolean = dateTime.compareTo(that) < 0

  def <(that: BrowserDateTime): Boolean = dateTime.compareTo(that.dateTime) < 0

  def ==(that: Date): Boolean = dateTime.compareTo(that) == 0

  def ==(that: BrowserDateTime): Boolean = dateTime.compareTo(that.dateTime) == 0
}

object BrowserDateTime {

  val dtFormat = new SimpleDateFormat("yyyyMMddHHmmss")

  def zeroPad(v: String): String = v.length match {
    case 0 => "00"
    case 1 => "0" + v
    case _ => v
  }
}
