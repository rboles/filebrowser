
package org.sboles.filebrowser {

  package hfs {

    import java.util.Date

    /**
     * Normalizes and stores an HFS date-time string as an instance of
     * java.util.Date
     *
     * @author sboles
     */
    case class HfsDateTime(hfsDateTime: String) extends BrowserDateTime {

      val _dateTime = HfsDateTime.normalize(hfsDateTime)

      def dateTime: Date = _dateTime

      override def toString = _dateTime.toString
    }

    object HfsDateTime {

      import BrowserDateTime.{dtFormat, zeroPad}

      /**
       * @param v HFS date-time value
       * @return New instance of Date
       */
      def normalize(hfsDateTime: String): Date = {

        val atoms = hfsDateTime.split(" ")

        val date = {
          val dt = atoms(0).split("/")
          val mo = zeroPad(dt(0))
          val dy = zeroPad(dt(1))
          val yr = dt(2)

          yr + mo + dy
        }

        val time = {
          if ( atoms.length > 1 ) {
            val tm = atoms(1).split(":")
            val mi = zeroPad(tm(1))
            val ss = zeroPad(tm(2))
            val am = atoms(2).toUpperCase
            
            val hr = {
              if ( am == "PM" ) {
                zeroPad((tm(0).toInt + 12).toString)
              } else {
                zeroPad(tm(0))
              }
            }

            hr + mi + ss
          } else {
            "000000"
          }
        }

        dtFormat.parse(date + time)
      }
    }
  }
}
