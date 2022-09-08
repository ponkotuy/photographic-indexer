package com.ponkotuy.geo

object HttpUtil {
  def params(base: String, kvs: (String, String)*): String = {
    if(kvs.isEmpty) base
    else {
      s"${base}?${kvs.map{ (k, v) => s"${k}=${v}" }.mkString("&")}"
    }
  }
}
