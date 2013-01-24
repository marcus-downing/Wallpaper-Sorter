import sbt._
import Keys._

object WallpaperSortBuild extends Build {
  lazy val root = Project("wallpaper-sorter", file(".")) settings(
    organization := "marcuatbang",
    
    name := "Wallpaper-Sorter",
    
    version := "0.1",
    
    
    //  dependencies
    //libraryDependencies += "javax.media" % "jai_core" % "1.1.3",
    
    //libraryDependencies += "com.sun.media" % "jai_codec" % "1.1.2_01"
    
    libraryDependencies += "javax.media.jai" % "com.springsource.javax.media.jai.codec" % "1.1.3",
    
    resolvers += "SpringSource" at "http://repository.springsource.com/maven/bundles/external"
  )
}
