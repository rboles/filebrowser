
package org.sboles.filebrowser {

  package hfs {

    /**
     * Provides a container for data describing an HFS file.
     * HFS file data includes a ``hits'' count- the number of
     * times HFS has served the file.
     * 
     * @author sboles
     */
    case class HfsFile(name: String, path: String, size: Long,
                       time: BrowserDateTime, folder: Boolean,
                       hits: Int) extends BrowserFile {

      /**
       * @return HFS file data as debug string
       */
      override def toString: String = {
        val sb = new StringBuilder
        
        sb.append(name).append("\n")
        folder match {
          case true => sb.append(" - (folder)\n")
          case _ => sb.append(" - Size: ").append(size).append("\n")
        }  
        sb.append(" - Time: ").append(time).append("\n")
        .append(" - Hits: ").append(hits).append("\n")
        .append(" - Path: ").append(path)
        
        sb.toString
      }
      
      /**
       * @return HFS file data as JSON
       */
      def toJson: String = {
        val sb = new StringBuilder
        
        sb.append("{")
        .append(" name : \"").append(name).append("\",")
        .append(" size : \"")
        folder match {
          case true => sb.append("0")
          case _ => sb.append(size)
        }
        sb.append("\",")
        .append(" time : \"").append(time).append("\",")
        .append(" hits : \"").append(hits).append("\",")
        .append(" path : \"").append(path).append("\",")
        .append(" folder : \"").append(folder).append("\"")
        .append("}")
        
        sb.toString
      }
    }
  }
}
