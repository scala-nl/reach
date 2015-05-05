package edu.arizona.sista.bionlp

import java.io.File
import scala.collection.JavaConverters._
import com.typesafe.config._
import org.apache.commons.io.{ FileUtils, FilenameUtils }
import edu.arizona.sista.processors.bionlp.BioNLPProcessor
import edu.arizona.sista.odin._
import edu.arizona.sista.odin.domains.bigmechanism.dryrun2015.Ruler.readRules
import edu.arizona.sista.odin.domains.bigmechanism.dryrun2015.DarpaActions
import edu.arizona.sista.odin.domains.bigmechanism.dryrun2015.mentionToStrings
import edu.arizona.sista.odin.domains.bigmechanism.summer2015.{ LocalGrounder, Coref }
import edu.arizona.sista.odin.extern.export.fries._

object RunSystem extends App {
  // use specified config file or the default one if one is not provided
  val config =
    if (args.isEmpty) ConfigFactory.load()
    else ConfigFactory.parseFile(new File(args(0))).resolve()

  val nxmlDir = new File(config.getString("nxmlDir"))
  val friesDir = new File(config.getString("friesDir"))
  val encoding = config.getString("encoding")

  var outputType:String = "text"            // output type can be 'fries' or 'text' is assumed
  scala.util.control.Exception.ignoring(classOf[ConfigException.Missing]) {
    outputType = config.getString("outputType")  // ignore error: default is already set
  }

  // if nxmlDir does not exist there is nothing to do
  if (!nxmlDir.exists) {
    sys.error(s"${nxmlDir.getCanonicalPath} does not exist")
  }

  // if friesDir does not exist create it
  if (!friesDir.exists) {
    println(s"creating ${friesDir.getCanonicalPath}")
    FileUtils.forceMkdir(friesDir)
  } else if (!friesDir.isDirectory) {
    sys.error(s"${friesDir.getCanonicalPath} is not a directory")
  }

  println("initializing processors ...")
  val proc = new BioNLPProcessor
  proc.annotate("something")

  println("initializing odin ...")
  val rules = readRules()
  val actions = new DarpaActions
  val grounder = new LocalGrounder
  val coref = new Coref
  val flow = grounder andThen coref
  val engine = ExtractorEngine(rules, actions, flow.apply)

  println("initializing nxml2fries ...")
  val nxml2fries = new Nxml2Fries(
    config.getString("nxml2fries.executable"),
    config.getBoolean("nxml2fries.removeCitations"),
    config.getStringList("nxml2fries.ignoreSections").asScala.toSet,
    encoding)

  // process papers in parallel
  for (file <- nxmlDir.listFiles.par if file.getName.endsWith(".nxml")) {
    val paperId = FilenameUtils.removeExtension(file.getName)

    // process individual sections and collect all mentions
    val paperMentions = nxml2fries.extractEntries(file) flatMap { entry =>
      val name = s"${entry.name}_${entry.chunkId}"
      println(s"working on $name ...")
      val doc = proc.annotate(entry.text, keepText = true)
      doc.id = Some(name)
      engine.extractFrom(doc)
    }

    if (outputType == "fries") {
      val outFile = new File(friesDir, s"$paperId.json")
      val frier = new FriesOutput()
      frier.toJSON(paperMentions, outFile)
    }
    else {                                  // assume text output
      val lines = paperMentions.flatMap(mentionToStrings)
      val outFile = new File(friesDir, s"$paperId.txt")
      FileUtils.writeLines(outFile, lines.asJavaCollection)
    }
  }
}