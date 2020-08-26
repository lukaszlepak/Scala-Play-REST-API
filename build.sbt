name := "onlinePM"
 
version := "1.0" 
      
lazy val `onlinepm` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.2"

libraryDependencies ++= Seq( ehcache , ws , specs2 % Test , guice )

libraryDependencies += "com.h2database" % "h2" % "1.4.200"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
)

libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-play-json" % "4.2.0"
)

libraryDependencies += jdbc % "test"

libraryDependencies ++= Seq( "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test" )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      