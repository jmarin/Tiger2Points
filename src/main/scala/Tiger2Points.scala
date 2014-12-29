package main

import core._
import io.shapefile._
import feature._
import geometry._
import geojson.FeatureJsonProtocol._
import java.nio.file.{ FileSystems, Files, StandardOpenOption }
import spray.json._

object Tiger2Points extends App {

  def str2Int(str: String): Int = {
    str match {
      case "" => 0
      case _ => str.toInt
    }
  }

  println("Converting Tiger line to GeoJSON points")

  val path = FileSystems.getDefault.getPath("src/test/resources", "NewHampshireAve.shp").toFile.getAbsolutePath
  val jsonPath = FileSystems.getDefault.getPath("src/test/resources", "NewHampshireAve.geojson")
  val segment = ShapefileReader(path)
  //println(segment.fc.features.head.values.get("LFROMHN"))
  val feature = segment.fc.features.head
  val points = AddressInterpolator.lines2AddressPoints(segment.fc.features.toList)
  val fc = FeatureCollection(points)
  val json = fc.toJson
  Files.delete(jsonPath)
  val geojson = Files.createFile(jsonPath)
  Files.write(jsonPath, json.toString.getBytes, StandardOpenOption.WRITE)
}
