package edu.arizona.sista.bionlp

import edu.arizona.sista.odin._
import edu.arizona.sista.odin.domains.bigmechanism.dryrun2015.Ruler.readRules
import edu.arizona.sista.odin.domains.bigmechanism.dryrun2015.DarpaActions
import edu.arizona.sista.odin.domains.bigmechanism.summer2015.{ LocalGrounder, Coref }
import edu.arizona.sista.processors.Document
import edu.arizona.sista.processors.bionlp.BioNLPProcessor

class ReachSystem {
  // read rule files
  val rules = readRules()
  // initialize actions object
  val actions = new DarpaActions
  // initialize grounder
  val grounder = new LocalGrounder
  // initialize coref
  val coref = new Coref
  // define flow
  val flow = grounder andThen coref
  // start engine
  val engine = ExtractorEngine(rules, actions, flow.apply)
  // initialize processor
  val processor = new BioNLPProcessor
  processor.annotate("something")

  def extractFrom(entry: Nxml2FriesEntry): Seq[Mention] =
    extractFrom(entry.text, entry.name, entry.chunkId)

  def extractFrom(text: String, docId: String, chunkId: String): Seq[Mention] = {
    val name = s"${docId}_${chunkId}"
    val doc = processor.annotate(text, keepText = true)
    doc.id = Some(name)
    extractFrom(doc)
  }

  def extractFrom(doc: Document): Seq[Mention] = {
    require(doc.id.isDefined, "document must have an id")
    require(doc.text.isDefined, "document should keep original text")
    engine.extractFrom(doc)
  }
}
