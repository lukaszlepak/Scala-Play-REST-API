name := "onlinePM"
 
version := "1.0" 
      
lazy val `onlinepm` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , evolutions, ehcache , ws , specs2 % Test , guice )

libraryDependencies ++= Seq( "org.postgresql" % "postgresql" % "42.2.14" )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      