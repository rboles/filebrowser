
package org.sboles.filebrowser

import scala.util.control.ControlThrowable

import java.io.{BufferedReader,InputStream,InputStreamReader}

import java.net.{URI, SocketTimeoutException}
import java.security.cert.{X509Certificate,CertificateException}
import javax.net.ssl.{X509TrustManager,SSLContext}

import org.apache.log4j.Logger

import org.apache.http.{HttpHost,HttpResponse,HttpEntity}
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.ResponseHandler
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.protocol.ClientContext
import org.apache.http.client.utils.URIUtils
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.conn.scheme.{PlainSocketFactory,Scheme,SchemeRegistry}
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.protocol.BasicHttpContext

/**
 * Provides an HTTP client for file browser requests.
 *
 * Only supports GET requests
 *  
 * Uses Apache HttpComponents
 * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/index.html
 * http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/index.html
 *
 * Uses pre-emptive auth:
 * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html#d4e1028
 *
 * The file servers use certificates that do not validate, and the
 * validation exception is ignored
 * http://javaskeleton.blogspot.com/2010/07/avoiding-peer-not-authenticated-with.html
 *
 * Uses a thread-safe pooling connection manager
 * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html
 * (See: 2.9 Multithreaded request execution)
 *
 * @author sboles
 */
case class BrowserHttpClient(_baseUrl: String) {

  import BrowserHttpClient._
  
  val baseUrl: String = validateBaseUrl(_baseUrl)

  private val _target = newTarget(new URI(_baseUrl))

  private val _context = newContext(_target)

  private val _httpparams = (new DefaultHttpClient).getParams

  private val _httpclient = new DefaultHttpClient(newConnectionMgr, _httpparams)

  /**
   * @return Current value of global client connection timeout
   */
  def connectTimeout = {
    HttpConnectionParams.getConnectionTimeout(_httpparams)
  }

  /**
   * Returns the current global length of time the client waits for a
   * request response from the server. This is the timeout you are probably
   * most interested in tweaking/checking. If the value is 0, the request
   * will not timeout
   * @return Current value of client socket timeout
   */
  def socketTimeout = {
    HttpConnectionParams.getSoTimeout(_httpparams)
  }

  /**
   * Set the global length of time the client waits for a response to a
   * connect request from the server. This should probably be short:
   * 30 seconds to a minute.
   * @param ms Milliseconds
   */
  def connectTimeout_= (ms: Int): Unit = {
    HttpConnectionParams.setConnectionTimeout(_httpparams, ms)
  }

  /**
   * Set the global length of time the clients waits for a request response
   * from the server. This is the timeout you are probably most interested
   * in tweaking/checking.
   * @param ms Milliseconds
   */
  def socketTimeout_= (ms: Int): Unit = {
    HttpConnectionParams.setSoTimeout(_httpparams, ms)
  }
  
 /**
   * Direct the HTTP connection manager to shut down. Make sure
   * you are finished with the HTTP client before calling this
   * method.
   */
  def shutdown: Unit = _httpclient.getConnectionManager.shutdown

  /**
   * Makes an HTTP GET request for a resource
   * @param path Path to resource
   * @param soTimeout Socket timeout in milliseconds
   * @return HTTP response status, body and request duration
   */
  def get(path: String, soTimeout: Int): BrowserClientResponse = {

    val startTime = System.currentTimeMillis
    
    val uri = new URI(baseUrl + validatePath(path))

    logger.debug("GET: " + uri)

    try {
      val response = httpGet(_httpclient, _target, _context, uri, soTimeout)
      val status = response.getStatusLine.getStatusCode.toInt
      val success = status match {
        case 200 => true
        case _ => false
      }
      val body = consumeHttpResponse(response)
      val lengthMs = System.currentTimeMillis - startTime
      BrowserClientResponse(success,
                            Some(BrowserHttpResponse(status, body, lengthMs)),
                            None)
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
        BrowserClientResponse(false, None, Some(e.getClass.getSimpleName))
      }
    }
  }

  /**
   * Makes an HTTP GET request for a resource, protected by auth.
   * @param path Path to resource
   * @param username User name
   * @param password User password
   * @param soTimeout Socket timeout in milliseconds
   * @return HTTP response status, body and request duration
   */
  def get(path: String, username: String, password: String, soTimeout: Int): BrowserClientResponse = {

    val startTime = System.currentTimeMillis

    _httpclient.getCredentialsProvider.setCredentials(
        new AuthScope(_target.getHostName, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
        new UsernamePasswordCredentials(username, password))
    
    val uri = new URI(baseUrl + validatePath(path))

    logger.debug("GET: " + uri)

    try {
      val response = httpGet(_httpclient, _target, _context, uri, soTimeout)
      val status = response.getStatusLine.getStatusCode.toInt
      val success = status match {
        case 200 => true
        case _ => false
      }
      val body = consumeHttpResponse(response)
      val lengthMs = System.currentTimeMillis - startTime
      BrowserClientResponse(success,
                            Some(BrowserHttpResponse(status, body, lengthMs)),
                            None)
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
        BrowserClientResponse(false, None, Some(e.getClass.getSimpleName))
      }
    }
  }

  /**
   * Make a HTTP get request for a service. The request timeout
   * defaults to the global socket timeout (socketTimeout)
   * @param path URL path
   * @return HTTP response status, body and request duration
   */
  def get(path: String): BrowserClientResponse = {
    get(path, socketTimeout)
  }

  /**
   * Make a HTTP get request for a service, protected by basic auth. The
   * request timeout defaults to the global socket timeout (socketTimeout)
   * @param path URL path
   * @return HTTP response status, body and request duration
   */
  def get(path: String, username: String, password: String): BrowserClientResponse = {
    get(path, username, password, socketTimeout)
  }
  
  /**
   * Requests the resource identified by the instance of BrowserFile
   * @param browserFile Instance of BrowserFile
   * @return HTTP response status, body and request duration
   */
  def get(browserFile: BrowserFile): BrowserClientResponse = get(browserFile.path)

  /**
   * Requests the resource identified by the instance of BrowserFile,
   * protected by basic auth
   * @param browserFile Instance of BrowserFile
   * @param username User name
   * @param password User password
   * @return HTTP response status, body and request duration
   */
  def get(browserFile: BrowserFile,
          username: String, password: String): BrowserClientResponse = {
    get(browserFile.path, username, password)
  }
}

object BrowserHttpClient {

  val DEFAULT_CONNECT_TIMEOUT = 30 * 1000

  val DEFAULT_SOCKET_TIMEOUT = 60 * 1000
  
  private val logger = Logger.getLogger(classOf[HttpClient])
    
  /**
   * Timeout specific exception
   */
  class TimeoutException(msg:String) extends RuntimeException(msg) with ControlThrowable

   /**
   * X509 trust manager quietly ignores some certificate exceptions
   */
  private val trustMgr = new X509TrustManager {
    @throws(classOf[CertificateException])
    def checkClientTrusted(xcs: Array[X509Certificate], client: String): Unit = { }

    @throws(classOf[CertificateException])
    def checkServerTrusted(xcs: Array[X509Certificate], server: String): Unit = { }

    def getAcceptedIssuers: Array[X509Certificate] = null
  }

  /**
   * Creates a new HTTP target
   * @param uri URI
   * @return new HTTP target
   */
  private def newTarget(uri: URI): HttpHost = {
    new HttpHost(uri.getHost, uri.getPort, uri.getScheme)
  }

  /**
   * Creates a new basic HTTP context with an auth cache
   * @param target HTTP target
   * @return Basic HTTP context
   */
  private def newContext(target: HttpHost): BasicHttpContext = {
    val authCache = new BasicAuthCache
    authCache.put(target, new BasicScheme)
    val context = new BasicHttpContext
    context.setAttribute(ClientContext.AUTH_CACHE, authCache)
    context
  }

  /**
   * Create a new thread safe connection manager, initialized for HTTP
   * and HTTPS
   * @return Connection manager
   */
  private def newConnectionMgr: ThreadSafeClientConnManager = {
    val sslCtx = SSLContext.getInstance("TLS")
    sslCtx.init(null, Array(trustMgr), null)
    val psf = new PlainSocketFactory
    val ssf = new SSLSocketFactory(sslCtx)
    val sr = new SchemeRegistry
    val ccm = new ThreadSafeClientConnManager(sr)
    ccm.getSchemeRegistry.register(new Scheme("https", 443, ssf))
    ccm.getSchemeRegistry.register(new Scheme("http", 80, psf))
    ccm
  }

  /**
   * Ensures that path is absolute; if not, appends ``/''
   * @param path
   * @return Validated path
   */
  def validatePath(path: String): String = {
    if ( path.startsWith("/") ) path
    else "/" + path
  }

  /**
   * Validates a base URL; makes sure the URL has a protocol and does not
   * and with ``/''
   * @param R25WS base URL
   * @return Massaged base URL
   * @throws IllegalArgumentException If base URL is null or length is 0
   */
  def validateBaseUrl(baseUrl: String): String = {
    var url = {
      if ( !baseUrl.toLowerCase.startsWith("http") ) "http://" + baseUrl
      else baseUrl
    }
    
    if ( url.endsWith("/") ) url.dropRight(1)
    else url 
  }

  /**
   * Makes HTTP GET request for a resource.
   * @param target HTTP host
   * @param context Auth context
   * @param uri Request URI
   * @param soTimeout Socket timeout in milliseconds
   * @return Instance of HttpResponse
   * @throws IllegalStateException If there is an error making the
   * HTTP request.
   * @throws TimeoutException If the request time exceeds socket timeout
   */
  @throws(classOf[IllegalStateException])
  @throws(classOf[TimeoutException])
  def httpGet(client: HttpClient, target: HttpHost, context: BasicHttpContext, uri: URI, soTimeout: Int): HttpResponse = {
    val method: HttpGet = new HttpGet(uri)

    HttpConnectionParams.setSoTimeout(method.getParams, soTimeout)

    method.setHeader("Connection", "close")
    
    val httpResp: HttpResponse = {
      try {
        client.execute(target, method, context)
      } catch {
        case cte:ConnectTimeoutException => {
          val msg = "Attempt to connect to host timed out"
          throw new TimeoutException(msg)
        }
        case ste:SocketTimeoutException => {
          val msg = "Timed out waiting for host response"
          throw new TimeoutException(msg)
        }
        case e:Exception => {
          val msg = "Failed to make HTTP GET request"+
          " for resource: "+uri.toString+
          "; with exception: "+e+"; "+e.getMessage
          throw new IllegalStateException(msg)
        }
      }
    }
    
    httpResp
  }

  /**
   * Consumes the content of the HTTP response entity and returns
   * the content as a string
   * @param stream Input stream from HTTP response entity
   * @return Response content as a string
   * @throws IllegalStateException If there is an error getting the
   * HTTP response entity
   */
  @throws(classOf[IllegalStateException])
  def consumeHttpResponse(httpResp: HttpResponse): String = {
    val ent: HttpEntity = {
      try {
        httpResp.getEntity
      } catch {
        case e => {
          val msg = "Failed to get response entity"+
          " from HTTP request: "+e+": "+e.getMessage
          throw new IllegalStateException(msg)
        }
      }
    }
    
    val stream:InputStream = {
      try {
        ent.getContent
      } catch {
        case e:Exception => {
          val msg = "Failed to get input stream"+
          " from response entity; "+e.getMessage
          logger.error(msg)
          throw new IllegalStateException(msg)
        }
      }
    }
    
    try {
      val br = new BufferedReader(new InputStreamReader(stream))
      val sb = new StringBuilder
      var line = ""
      while ( line != null ) {
    	line = br.readLine
    	if ( line != null ) sb.append(line).append("\n")
      }
      sb.toString
    } catch {
      case e => {
        val msg = "IO exception reading from"+
          " response input stream; "+e.getMessage
        throw new IllegalStateException(msg)
      }
    } finally {
      stream.close
    }
  }
}
