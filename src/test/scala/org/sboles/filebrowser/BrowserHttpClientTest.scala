
package org.sboles.filebrowser

import junit.framework._
import Assert._
import org.apache.log4j.Logger

/**
 * Tests the browser HTTP client class.
 *
 * Tests are run against HFS and IIS web servers.
 * 
 * @author sboles
 */
object BrowserHttpClientTest {
  val HFS_BASE_URL = "http://localhost:80"

  val logger = Logger.getLogger(classOf[BrowserHttpClientTest])

  def suite: Test = {
    val suite = new TestSuite(classOf[BrowserHttpClientTest])
    suite
  }

  def checkResponse(clientResp: BrowserClientResponse): Unit = {
    clientResp.success match {
      case true => {
        logger.info("Success")
        clientResp.httpResponse match {
          case Some(response) => {
            logger.info("Response statu: " + response.status)
            logger.debug(response.body)
          }
          case None => {
            logger.error("Client returned success but no response")
            assertTrue(false)
          }
        }
      }
      case false => {
        clientResp.httpResponse match {
          case Some(response) => {
            logger.error("Client returned status: " + response.status)
            assertTrue(false)
          }
          case None => {
            logger.error("Client request failed with exception")
            clientResp.exceptionName match {
              case Some(name) => logger.error(name)
              case _ => ;
            }
            assertTrue(false)
          }
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    junit.textui.TestRunner.run(suite)
  }
}

class BrowserHttpClientTest extends TestCase("BrowserHttpClient") {

  import BrowserHttpClientTest._

  def testHfsGet: Unit = {
    logger.info("Testing HTTP client with HFS GET request")

    val path = "/HFS/cache/"

    val client: BrowserHttpClient = new BrowserHttpClient(HFS_BASE_URL)

    val clientResp = client.get(path)

    checkResponse(client.get(path))
  }
}
