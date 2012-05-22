
package org.sboles.filebrowser {

  package hfs {

    import org.apache.log4j.Logger

    /**
     * Provides an HFS file browser and HFS index parsing.
     * 
     * @param _baseUrl Base URL of HFS server
     * 
     * @author sboles
     */
    case class HfsBrowser(_baseUrl: String) extends FileBrowser {
  
      import HfsBrowser.logger
  
      private val _client = new BrowserHttpClient(_baseUrl)

      val baseUrl = _client.baseUrl

      override def client = _client

      override def parseIndex(path: String, index: String) =
        HfsPageParser.parse(path, index)
    }

    object HfsBrowser {
      val logger = Logger.getLogger(classOf[HfsBrowser])
    }
  }
}
