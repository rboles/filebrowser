
package org.sboles.filebrowser {

  package hfs {

    import java.util.regex.Pattern
    import scala.collection.mutable.Queue
    import scala.io.Source

    /**
     * Parses the content of an HFS page
     * 
     * Does the grunt work of parsing file information out of an HFS
     * response. HFS does not return valid XHTML so we can't take
     * advantage of Scala's nice native support for XML. So, the nasty
     * regular expressions.
     * 
     * @author sboles
     */
    object HfsPageParser {
      
      val PATH_PATTERN = Pattern.compile("<div id=folder>(.*?)</div>")
      val NAME_PATTERN = Pattern.compile("\\s*<b>(.*?)</b>")
      val SIZE_PATTERN = Pattern.compile("\\s*<i>(.*?)</i>")
      val FILE_PATTERN = Pattern.compile("^<tr><td>\\s+<a href=\".*?\"><img src=\".*?\" />\\s*(.*?)</a><td align=(right|center)>(.*?)<td align=right>(.*?)<td align=right>(.*?)$")
      
      /**
       * Parses an HFS response page into a list of BrowserFile instances
       * @param page HFS response page
       * @return List of browser files
       */
      def parse(page: String): List[HfsFile] = {
        val files = new Queue[HfsFile]
        
        // 0: nothing; 1: found path
        var readState = 0
        var path = ""
        
        Source.fromString(page).getLines.foreach(line => {
          readState match {
            case 0 => {
              val m = PATH_PATTERN.matcher(line)
              if ( m.find ) {
                path = m.group(1)
                readState = 1
              }
            }
            case 1 => {
              val m = FILE_PATTERN.matcher(line)
              if ( m.find ) {
                val name = massageName(m.group(1))
                val filePath = path + name
                val time = HfsDateTime(m.group(4))
                val hits = m.group(5).toInt
                val size = massageSize(m.group(3))

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

                if ( name.length > 0 )
                  files += HfsFile(name, filePath, fileSize, time, folder, hits)
              }
            }
          }
        })
        
        files.toList
      }
      
     /**
      * Massages the folder/file name. If the name is null an empty
      * string is returned
      * @param v Folder or file name
      * @return Massaged value or empty string
      */
      def massageName(v: String): String = {
        if ( v == null ) {
          ""
        } else {
          val m = NAME_PATTERN.matcher(v)
          if ( m.find ) m.group(1) else v
        }
      }
      
     /**
      * Massages the folder/file size. If the node is a folder, the
      * size value is the string "folder". If the value is null, an
      * empty string is returned.
      * @param v Size string
      * @return Massaged value or empty string
      */
      def massageSize(v: String): String = {
        if ( v == null ) {
          ""
        } else {
          val m = SIZE_PATTERN.matcher(v)
          if ( m.find ) m.group(1) else v
        }
      }
    }
  }
}
