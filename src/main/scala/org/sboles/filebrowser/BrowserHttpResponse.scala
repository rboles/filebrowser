
package org.sboles.filebrowser

/**
 * Provides a container for a FileBrowser HTTP response
 *
 * @param status Response status
 * @param body Response body
 * @param lengthMillis Request lenght in milliseconds
 *
 * @author sboles
 */
case class BrowserHttpResponse(
  val status: Int, val body: String, val lengthMillis: Long) { }

