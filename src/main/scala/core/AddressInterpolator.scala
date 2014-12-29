package core

import geometry._
import feature._
import geojson.FeatureJsonProtocol._

object AddressInterpolator {

  case class AddressRange(start: Int, end: Int) {
    def count: Int = end - start
  }

  case class AddressList(right: List[Int], left: List[Int])

  def calculateAddressRange(f: Feature, a: Int): AddressRange = {
    val addressIsEven = (a % 2 == 0)
    val rightRangeIsEven = (f.values.getOrElse("RFROMHN", "0").toString.toInt % 2 == 0)
    val leftRangeIsEven = (f.values.getOrElse("LFROMHN", "0").toString.toInt % 2 == 0)

    val prefix =
      if (addressIsEven && rightRangeIsEven)
        "R"
      else if (addressIsEven && leftRangeIsEven)
        "L"
      else if (!addressIsEven && !rightRangeIsEven)
        "R"
      else if (!addressIsEven && !leftRangeIsEven)
        "L"

    val end = f.values.getOrElse(s"${prefix}TOHN", "0").toString.toInt
    val start = f.values.getOrElse(s"${prefix}FROMHN", "0").toString.toInt

    AddressRange(start, end)
  }

  def str2Int(str: String): Int = {
    str match {
      case "" => 0
      case _ => str.toInt
    }
  }

  def filterNumbers(numbers: List[Int]): List[Int] = {
    numbers.head % 2 match {
      case 0 => numbers.filter(n => n % 2 == 0)
      case _ => numbers.filter(n => n % 2 != 0)
    }
  }

  def lines2AddressPoints(features: List[Feature]): List[Feature] = {
    features.map(f => line2AddressPoint(f)).flatten
  }

  def line2AddressPoint(feature: Feature): List[Feature] = {
    val values = feature.values
    val rfromhn = str2Int(values.get("RFROMHN").get.toString)
    val rtohn = str2Int(values.get("RTOHN").get.toString)
    val lfromhn = str2Int(values.get("LFROMHN").get.toString)
    val ltohn = str2Int(values.get("LTOHN").get.toString)

    val rNumbers = List.range(rfromhn, rtohn + 1)
    val lNumbers = List.range(lfromhn, ltohn + 1)
    val rightNumbers = if (rNumbers.size > 0) filterNumbers(rNumbers) else Nil
    val leftNumbers = if (lNumbers.size > 0) filterNumbers(lNumbers) else Nil

    val pointsRight = rightNumbers.map { number =>
      interpolate(feature, AddressRange(rfromhn, rtohn), number)
    }

    val pointsLeft = leftNumbers.map { number =>
      interpolate(feature, AddressRange(lfromhn, ltohn), number)
    }

    pointsRight ++ pointsLeft
  }

  def interpolate(feature: Feature, range: AddressRange, a: Int): Feature = {
    val sign = a % 2 match {
      case 0 => -1
      case _ => 1
    }
    val line = feature.geometry.asInstanceOf[Line]
    val l = line.length
    val d = range.end - range.start
    val x = a - range.start
    val dist = x * l / d
    //TODO: make offset distance a bit more intelligent
    val geometry = line.pointAtDistWithOffset(dist, sign * 0.0001)
    val addressField = Field("address", StringType())
    val geomField = Field("geometry", GeometryType())
    val schema = Schema(geomField, addressField)
    val fullname = feature.values.get("FULLNAME").getOrElse("")
    val address = a.toString + " " + fullname
    val values = Map("geometry" -> geometry, "address" -> address)
    Feature(schema, values)
  }

}

