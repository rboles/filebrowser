
package org.sboles.filebrowser {

  package hfs {

    import scala.io.Source

    import java.util.regex.{Matcher, Pattern}

    /**
     * Parses the content of an HFS page
     * 
     * HFS does not return valid XHTML so we can't take advantage of
     * Scala's nice native support for XML. The nasty regular
     * expression does all the work.
     * 
     * @author sboles
     */
    object HfsPageParser {
      
      val FILE_PATTERN = Pattern.compile("^<tr><td>\\s+<a href=\"(.*?)\"><img src=\"(.*?)\"\\s+/>\\s*(<b>)*(.*?)(</b>)*</a><td align=(right|center)>(<i>)*(.*?)(</i>)*<td align=right>(.*?)<td align=right>(\\d+)$")
      
      /**
       * Parses an HFS index page into a list of BrowserFile instances
       * @param path Path to HFS index page
       * @param page HFS index page
       * @return List of browser files
       */
      def parse(path: String, page: String): List[HfsFile] = {
        Source.fromString(page).getLines.toList.flatMap(line => {
          val matcher = FILE_PATTERN.matcher(line)
          if ( matcher.find ) Some(matchToFile(matcher, path)) else None
        }).filter(_.name.length > 0)
      }

      /**
       * Transforms a Pattern match into an HfsFile instance
       * @param matcher Pattern match
       * @path Path to resource
       * @return New HfsFile instance
       */
      private def matchToFile(matcher: Matcher, path: String): HfsFile = {
        val name = matcher.group(4)
        val filePath = if ( path.endsWith("/")) path + name
                       else path + "/" + name
        val time = HfsDateTime(matcher.group(10))
        val hits = matcher.group(11).toInt
        val size = matcher.group(8)

        val folder = size match {
          case "folder" => true
          case _ => false
        }

        val fileSize: Long = size match {
          case "folder" => 0
          case _ => {
            val l = size.split(' ')
            l(1) match {
              case "KB" => (l(0).toDouble * 1024).toLong
              case "MB" => (l(0).toDouble * 1048576).toLong
              case _ => l(0).toLong
            }
          }
        }

        HfsFile(name, filePath, fileSize, time, folder, hits)
      }
    }
  }
}
