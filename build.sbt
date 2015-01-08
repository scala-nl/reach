name := "reach"

version := "1.0"

scalaVersion := "2.10.4"

resolvers ++= Seq(
  "BioPAX Releases" at "http://biopax.sourceforge.net/m2repo/releases",
  "BioPAX Snapshots" at "http://biopax.sourceforge.net/m2repo/snapshots"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "junit" % "junit" % "4.10" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "edu.arizona.sista" %% "processors" % "4.0-SNAPSHOT",
  "edu.arizona.sista" %% "processors" % "4.0-SNAPSHOT" classifier "models",
  "org.apache.lucene" % "lucene-core" % "4.2.1",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.2.1",
  "org.apache.lucene" % "lucene-queryparser" % "4.2.1",
  "org.apache.lucene" % "lucene-highlighter" % "4.2.1",
  "org.biopax.paxtools" % "paxtools-core" % "4.2.1",
  "jline" % "jline" % "2.11"
)
