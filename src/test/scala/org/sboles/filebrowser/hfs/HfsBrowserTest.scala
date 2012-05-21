
package org.sboles.filebrowser {

  package hfs {

    import java.util.GregorianCalendar
    import java.util.regex.Pattern

    import junit.framework._
    import Assert._
    import org.apache.log4j.Logger

    /**
     * Tests the browser HTTP client class.
     *
     * @author sboles
     */
    object HfsBrowserTest {
      val HFS_BASE_URL = "http://localhost:80"

      val PATH = "/HFS/cache"

      val logger = Logger.getLogger(classOf[HfsBrowserTest])

      def suite: Test = {
        val suite = new TestSuite(classOf[HfsBrowserTest])
        suite
      }

      def main(args: Array[String]): Unit = {
        junit.textui.TestRunner.run(suite)
      }
    }

    class HfsBrowserTest extends TestCase("HfsBrowser") {

      import HfsBrowserTest._

      def testIndex: Unit = {
        logger.info("++ Testing HfsBrowser.index(path)")

        val browser = new HfsBrowser(HFS_BASE_URL)

        val files = browser.index(PATH)

        files.length match {
          case 0 => {
            logger.error("Expected page at: " + PATH + " to have content")
            assertTrue(false)
          }
          case _ => files.foreach(f => {logger.info(f.name)})
        }
      }

      def testFile: Unit = {
        logger.info("++ Testing HfsBrowser.file(path)")

        val browser = new HfsBrowser(HFS_BASE_URL)

        val path = PATH + "/8077.xml"

        browser.file(PATH) match {
          case Some(f) => {
            logger.info("Success, got file at " + PATH)
            logger.debug(f)
          }
          case None => {
            logger.error("Failed to request file at " + path)
            assertTrue(false)
          }
        }
      }

      def testFiles: Unit = {
        logger.info("++ Testing HfsBrowser.files(path)")

        val browser = new HfsBrowser(HFS_BASE_URL)

        val files = browser.files(PATH)

        if ( files.length == 0 ) {
          logger.error("Expected to find files at: " + PATH)
          assertTrue(false)
        } else {
          logger.info("Found " + files.length + " files at " + PATH)
        }
      }

      def testFilesStringPattern: Unit = {
        logger.info("++ Testing HfsBrowser.files(path, pattern)")

        val browser = new HfsBrowser(HFS_BASE_URL)

        val filter = ".xml"
        val pattern = Pattern.compile(filter)

        val files = browser.files(PATH, pattern)

        if ( files.length > 0 ) {
          logger.info("Found " + files.length + " matching: " + filter)
          files.foreach(f => logger.info("- " + f.name))
        } else {
          logger.error("Expected to find files matching: " +
                       filter + ", at " + PATH)
          assertTrue(false)
        }
      }

      def testFolders: Unit = {
        logger.info("++ Testing HfsBrowser.folders()")

        val browser = new HfsBrowser(HFS_BASE_URL)

        val files = browser.folders(PATH)

        if ( files.length > 0 ) {
          logger.info("Found " + files.length + " folders")
        } else {
          logger.error("Expected to find folders at: " + PATH)
          assertTrue(false)
        }
      }

      def testFilesBefore: Unit = {
        logger.info("++ Testing HfsBrowser.filesBefore(path, dateTime)")

        val browser = new HfsBrowser(HFS_BASE_URL)

        val cal = new GregorianCalendar(2012,6,1,0,0,0)

        val files = browser.filesBefore(PATH, cal.getTime)

        if ( files.length == 0 ) {
          logger.error("Execpted to find files modified before " + cal.getTime)
          assertTrue(false)
        } else {
          logger.info("Found " + files.length + " modified before " + cal.getTime)
          files.foreach(f => logger.info(f.name + ", " + f.time))
        }
      }

      def testFilesAfter: Unit = {
        logger.info("++ Testing HfsBrowser.filesAfter(path, dateTime)")

        val browser = new HfsBrowser(HFS_BASE_URL)

        val cal = new GregorianCalendar(2010,4,1,0,0,0)

        val files = browser.filesAfter(PATH, cal.getTime)

        if ( files.length == 0 ) {
          logger.error("Execpted to find files modified after " + cal.getTime)
          assertTrue(false)
        } else {
          logger.info("Found " + files.length + " modified after " + cal.getTime)
          files.foreach(f => logger.info(f.name + ", " + f.time))
        }
      }

      def testFilesBetween: Unit = {
        logger.info("++ Testing HfsBrowser.filesBetween(path, startDt, endDt)")

        val browser = new HfsBrowser(HFS_BASE_URL)

        val startDt = new GregorianCalendar(2010,4,1,0,0,0)
        val endDt = new GregorianCalendar(2012,6,1,0,0,0)

        val files = browser.filesBetween(PATH, startDt.getTime, endDt.getTime)

        if ( files.length == 0 ) {
          logger.error("Execpted to find files modified between " +
                       startDt.getTime + " and " + endDt.getTime)
          assertTrue(false)
        } else {
          logger.info("Found " + files.length + " modified between " +
                      startDt.getTime + " and " + endDt.getTime)
          files.foreach(f => logger.info(f.name + ", " + f.time))
        }
      }
    }
  }
}
