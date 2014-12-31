package main

import feature._
import core._
import io.shapefile._
import geometry._
import geojson.FeatureJsonProtocol._
import spray.json._
import java.net.URI
import java.nio.file.{ FileSystems, Files, StandardOpenOption, Paths }
import java.io.{ FileOutputStream, PrintWriter }
import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{ Source, ForeachSink, Flow, RunnableFlow }
import scala.util.Try

object Tiger2PointsStreaming {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("system")
    implicit val mat = FlowMaterializer()
    implicit val ec = system.dispatcher

    require(args.size == 2, "Paths to input shp and output geojson required")

    if (args.size == 2) {
      val inputPathStr = args(0)
      val outputPathStr = args(1)
      val inputUri = URI.create("file://" + inputPathStr)
      val inputPath = Paths.get(inputUri)

      val shp = ShapefileReader(inputPath.toString)
      val features = shp.fc.features

      val output = new PrintWriter(new FileOutputStream(outputPathStr), true)
      output.println("""{"type": "FeatureCollection","features": [""")

      val source = Source(() => features.toIterator)

      val sink = ForeachSink[Feature] { f =>
        val json = f.toJson
        println(json)
        output.println(json + ",")
      }

      val flow = Flow[Feature]
        .map(f => AddressInterpolator.line2AddressPoint(f))
        .mapConcat(_.toList)

      val materialized = source.via(flow).to(sink).run()
      materialized.get(sink).onComplete {
        _ =>
          output.println("""]}""")
          Try(output.close())
          system.shutdown()
      }

      //Another way, connecting all at once

      //Source(() => features.toIterator)
      //  .map(f => AddressInterpolator.line2AddressPoint(f))
      //  .mapConcat(_.toList)
      //  .foreach {
      //    f =>
      //      val json = f.toJson
      //      println(json)
      //      output.println(json + ",")
      //  }
      //  .onComplete {
      //    _ =>
      //      output.println("""]}""")
      //      Try(output.close())
      //      system.shutdown()
      //  }

    }

  }

}
