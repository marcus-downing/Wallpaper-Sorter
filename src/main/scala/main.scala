// import java.io.{FileInputStream, File}
// import com.sun.image.codec.jpeg.{JPEGCodec, JPEGImageDecoder}
// import com.sun.media.jai.codecimpl.{JPEGCodec, JPEGImageDecoder}
// import com.sun.media.jai.codec.FileSeekableStream
// import javax.media.jai.{JAI, RenderedOp}
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import scala.Console

case class Resolution (md5: String, width: Int, height: Int) {
  val aspectRatio = width.toDouble / height.toDouble
}

case class AspectRatio (val x: Int, val y: Int, annotation: String = "") {
  val ratio = x.toDouble / y.toDouble
  val name = {
    val a = if(annotation!="")(" "+annotation) else ""
    "Wallpapers "+x+" x "+y+a
  }
  def distance (other: Double): Double = Math.abs((other - ratio) / (other + ratio))
  def distance (other: Resolution): Double = distance(other.aspectRatio)
}

object WallpaperSorter {
  val source = "/mnt/d/Wallpapers/to sort"
  val destination = "/mnt/d/Wallpapers/sorted"

  val aspectRatios =
        AspectRatio(9,21) ::
        AspectRatio(9,16) ::
        AspectRatio(10,16) ::
        AspectRatio(9,14) ::
        AspectRatio(10,14) ::
        AspectRatio(2,3,"iPhone") ::
        AspectRatio(3,4) ::
        AspectRatio(4,5) ::
        AspectRatio(1,1) ::
        AspectRatio(5,4) ::
        AspectRatio(4,3) ::
        AspectRatio(3,2) ::
        AspectRatio(14,10) ::
        AspectRatio(14,9) ::
        AspectRatio(16,10) ::
        AspectRatio(16,9) ::
        AspectRatio(21,9) ::
        AspectRatio(28,9, "(14 x 9)") ::
        AspectRatio(28,10,"(14 x 10)") ::
        AspectRatio(32,9, "(16 x 9)") ::
        AspectRatio(32,10,"(16 x 10)") ::
        AspectRatio(42,9, "(14 x 9)") ::
        AspectRatio(42,10,"(14 x 10)") ::
        AspectRatio(48,9, "(16 x 9)") ::
        AspectRatio(48,10,"(16 x 10)") ::
        Nil

    def selectAspectRatio (resolution: Resolution): AspectRatio = {
      var closest: AspectRatio = aspectRatios(0)
      for (aspectRatio <- aspectRatios) {
        val distance = aspectRatio.distance(resolution)
        val cdistance = closest.distance(resolution)
        if (aspectRatio.distance(resolution) < closest.distance(resolution))
          closest = aspectRatio
      }
      closest
    }
    
    def processFiles (current: File, path: List[String], destFolder: File) {
      // println("Scanning " + current.getAbsolutePath)

      val md = java.security.MessageDigest.getInstance("MD5")
      val files = current.listFiles
      if (files == null)
        return
      files.foreach { file: File =>
        if (file.isDirectory) {
          val name = file.getName
          processFiles(file, name :: path, destFolder)
        } else {
          val name = file.getName
          val extension = name.replaceFirst(".*\\.([^.]+)", "$1")

          //  find the resolution of that image
          val res = try {
            // val stream = new FileSeekableStream(file)
            //           val image = JAI.create("stream", stream);
            val image = ImageIO.read(file)
            val height = image.getHeight
            val width = image.getWidth
            val md5 = {
              val fis = new java.io.FileInputStream(file)
              try {
                val dis = new java.security.DigestInputStream(fis, md)
                while(dis.available > 0) dis.read()
              } finally { fis.close() }
              val digest = md.digest()

              val sb = new StringBuffer()
              for (byte <- digest)
                sb.append(Integer.toHexString(0xff & byte))
              sb.toString();
            }

            Some(Resolution(md5, width, height))
          } catch {
            case e => e.printStackTrace
          }

          //  if we got one, move the file
          res match {
            case Some(r: Resolution) =>
              val aspectRatio = selectAspectRatio(r)
              val destFolder2Loc = destFolder.getAbsolutePath + "/" + aspectRatio.name + "/" + path.reverse.mkString("/")
              val destFolder2 = new File(destFolder2Loc)
              if (!destFolder2.exists) {
                println(Console.GREEN+"Creating folder: " + destFolder2Loc) 
                destFolder2.mkdirs
              }

              val newname = r.md5.substring(0,10)+" ("+r.width+" x "+r.height+")."+extension
              val pathLoc = (newname :: path).reverse.mkString("/")
              //val pathLoc = (name :: path).reverse.mkString("/")
              val destLoc = destFolder.getAbsolutePath + "/" + aspectRatio.name + "/" + pathLoc
              println(Console.RESET+"Moving file: "+file.getAbsolutePath+" to "+Console.BOLD+destLoc)
              
              val destFile = new File(destLoc)
              file.renameTo(destFile)
            case _ =>
          }

        }
      }
      if (current.list.length == 0 && path.length > 0) {
        println(Console.RED+"Removing folder: " + current.getAbsolutePath)
        current.delete
      }
    }

  def main(args: Array[String]): Unit = {
    val folder = new File(source)
    val destFolder = new File(destination)
    processFiles(folder, Nil, destFolder)
  }
}
