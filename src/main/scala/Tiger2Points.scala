package main

import core._
import io.shapefile._
import feature._
import geometry._
import geojson.FeatureJsonProtocol._
import java.net.URI
import java.nio.file.{ FileSystems, Files, StandardOpenOption, Paths }
import spray.json._

object Tiger2Points {

  def main(args: Array[String]) {

    require(args.size == 2, "paths to input shp and output geojson required")

    if (args.size == 2) {
      val inputPathStr = args(0)
      val inputUri = URI.create("file://" + inputPathStr)
      val outputPathStr = args(1)
      val outputUri = URI.create("file://" + outputPathStr)
      val inputPath = Paths.get(inputUri)
      val outputPath = Paths.get(outputUri)

      println(s"Converting Tiger line ${inputPath} to GeoJSON points ${outputPath}")

      val shp = ShapefileReader(inputPath.toString)
      val features = shp.fc.features
      val points = AddressInterpolator.lines2AddressPoints(features.toList)
      val fc = FeatureCollection(points)
      val json = fc.toJson
      if (Files.exists(outputPath)) {
        Files.delete(outputPath)
      }
      val geojson = Files.createFile(outputPath)
      Files.write(outputPath, json.toString.getBytes, StandardOpenOption.WRITE)

    }

  }
}
