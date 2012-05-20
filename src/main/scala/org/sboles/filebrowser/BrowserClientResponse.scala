
package org.sboles.filebrowser

/**
 * Container for data resulting from a Browser HTTP client request.
 *
 * @param success If true indicates a successful request: the client
 * received the correct response status and response body consumed
 *
 * @param httpResponse Contains a browser http response (status, body,
 * reqeuest length) unless the request failed with exception
 *
 * @param exceptionName If the request failed with exception, contains
 * the exception simple name (ex: TimeoutException)
 *
 * @author sboles
 */
case class BrowserClientResponse(success: Boolean,
                                 httpResponse: Option[BrowserHttpResponse],
                                 exceptionName: Option[String]) { }
