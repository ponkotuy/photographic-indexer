package com.ponkotuy.util

import io.circe.{Encoder, Json}
import org.apache.commons.math3.fraction.Fraction

object CustomEncoder {
  implicit val fraction: Encoder[Fraction] = new Encoder[Fraction] {
    override final def apply(f: Fraction): Json = {
      if(f.doubleValue() < 1.0) Json.fromString(s"${f.getNumerator}/${f.getDenominator}")
      else Json.fromString(s"${f.doubleValue()}")
    }
  }
}
