
package org.sboles.filebrowser

import java.util.Date

/**
 * Provides an interface for a file or folder returned by the file browser
 *
 * @sboles
 */
trait BrowserFile {

  /**
   * @return File basename
   */
  def name: String

  /**
   * @return Path to the file
   */
  def path: String

  /**
   * @return Size of the file in bytes
   */
  def size: Long

  /**
   * @return The last mod date of the file
   */
  def time: BrowserDateTime

  /**
   * @return True if the file is a directory
   */
  def folder: Boolean

  /**
   * @return True if the file is a directory
   */
  def directory: Boolean = folder
}
