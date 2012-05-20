
package org.sboles.filebrowser

import java.util.Date
import java.util.regex.Pattern

import org.apache.log4j.Logger

/**
 * Provides an interface for file browsers and implementations of common
 * methods.
 *
 * @author sboles
 */
trait FileBrowser {

  import FileBrowser.{getResource, logger}

  def baseUrl: String

  def client: BrowserHttpClient

  /**
   * Parse an index page into a list of files and folders
   * @param index Index page content
   * @return List of files and folders
   */
  def parseIndex(index: String): List[BrowserFile]

  /**
   * Get a file index
   * @param path Path to directory from base URL
   * @return List of files and folders
   */
  def index(path: String): List[BrowserFile] = getResource(client, path) match {
    case Some(index) => parseIndex(index)
    case None => Nil
  }

  /**
   * Get the content of the file
   * @param file Browser file 
   * @return Content of file or None of no file or request failure
   */
  def file(f: BrowserFile): Option[String] = getResource(client, f.path)

  /**
   * Get the content of the file at the end of the path
   * @param path Path to file from base URL
   * @return Content of file or None of no file or request failure
   */
  def file(path: String): Option[String] = getResource(client, path)

  /**
   * Get a list of folders
   * @param path Path to directory from base URL
   * @return Children of the directory that are folders
   */
  def folders(path: String): List[BrowserFile] = index(path).filter(_.folder)

  /**
   * Get a list of files
   * @param path Path to directory from base URL
   * @return Children of the directory that are files
   */
  def files(path: String): List[BrowserFile] = index(path).filter(!_.folder)

  /**
   * Get a list of files that match a pattern
   * @param path Path to directory from base URL
   * @param filter Regex used to match files
   * @return List of matching files.
   */
  def files(path: String, pattern: Pattern): List[BrowserFile] =
    files(path).filter(f => { pattern.matcher(f.name).find })

  /**
   * Get a list of files that have a last mod date after a datetime
   * @param path Path to directory from base URL
   * @param dt An end date
   * @return Files that match the search criteria
   */
  def filesAfter(path: String, dt: Date): List[BrowserFile] =
    files(path).filter(f => { f.time > dt })

  /**
   * Get a list of files that match a pattern and have a last mod date
   * after a datetime
   * @param path Path to directory from base URL
   * @param pattern Regex used to match files
   * @param dt An end date
   * @return Files that match the search criteria
   */
  def filesAfter(path: String, pattern: Pattern, dt: Date): List[BrowserFile] = {
    files(path, pattern).filter(f => { f.time > dt })
  }

  /**
   * Get a list of files that have a last mod date before a datetime
   * @param path Path to directory from base URL
   * @param dt A start date
   * @return Files that match the search criteria
   */
  def filesBefore(path: String, dt: Date): List[BrowserFile] =
    files(path).filter(f => { f.time < dt })

  /**
   * Get a list of files that match a pattern and have a last mod date
   * before a datetime
   * @param path Path to directory from base URL
   * @param patter Regex used to match files
   * @param dt A start date
   * @return Files that match the search criteria
   */
  def filesBefore(path: String, pattern: Pattern, dt: Date): List[BrowserFile] = {
    files(path, pattern).filter(f => { f.time < dt })
  }

  /**
   * Get a list of files that have a last mod date between a start and end time
   * @param path Path to directory from base URL
   * @param startDt A start date-time
   * @param endDt An end date-time
   * @return Files that match the search criteria
   */
  def filesBetween(path: String, startDt: Date, endDt: Date): List[BrowserFile] = {
    files(path).filter(f => { f.time > startDt && f.time < endDt })
  }

  /**
   * Get a list of files that match a pattern and have a last mod date
   * between a start and end time
   * @param path Path to directory from base URL
   * @param pattern Regex used to match files
   * @param startDt A start date-time
   * @param endDt An end date-time
   * @return Files that match the search criteria
   */
  def filesBetween(path: String, pattern: Pattern, startDt: Date, endDt: Date): List[BrowserFile] = {
    files(path, pattern).filter(f => { f.time > startDt && f.time < endDt })
  }
}

object FileBrowser {

  val logger = Logger.getLogger(classOf[FileBrowser])

  /**
   * GET the thing at the end of the path
   * If an error occurs during the request (non-200 response, exception)
   * the error is logged and None is returned
   * @param client Browser HTTP client
   * @param path Path to resource
   * @return Some response body or None
   */
  def getResource(client: BrowserHttpClient, path: String): Option[String] = {
    val clientResponse = client.get(path)

    clientResponse.success match {
      case true => try {
        clientResponse.httpResponse match {
          case Some(response) => Some(response.body)
          case _ => {
            logger.error("Expected a response body on 200 status")
            None
          }
        }
      }
      case false => {
        clientResponse.httpResponse match {
          case Some(response) => {
            logger.error("Request returned non-200 status: " + response.status)
            None
          }
          case None => {
            clientResponse.exceptionName match {
              case Some(name) => logger.error("Encountered exception: " + name)
              case None => ;
            }
            None
          }
        }
      }
    }
  }
}
