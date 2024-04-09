package com.github.dafutils.authentication

import scala.language.postfixOps
import scala.util.control.NonFatal

class OauthSignatureParser {

  def parse(oauthHeaderValue: String): OauthParameters = 
    try {
      val oauthParametersAsMap = parseAsMap(oauthHeaderValue)
  
      OauthParameters(
        signatureMethod = oauthParametersAsMap("oauth_signature_method"),
        oauthSignature = oauthParametersAsMap("oauth_signature"),
        timestamp = oauthParametersAsMap("oauth_timestamp"),
        consumerKey = oauthParametersAsMap("oauth_consumer_key"),
        nonce = oauthParametersAsMap("oauth_nonce"),
        version = oauthParametersAsMap("oauth_version")
      )
    } catch {
      case NonFatal(_) => 
        throw new IllegalArgumentException(s"Failed parsing oauth header: [$oauthHeaderValue]")
    }
  
  def parseAsMap(oauthHeaderValue: String): Map[String, String] = try {
    val fieldsAndValues = oauthHeaderValue
      .stripPrefix("OAuth ")
      .split(",")
      .map(_.trim)
      .flatMap(_.split("="))

    fieldsAndValues
      .grouped(2)
      .map { fieldAndValue =>
        fieldAndValue(0) -> fieldAndValue(1).drop(1).dropRight(1)
      } toMap

  } catch {
    case NonFatal(_) =>
      throw new IllegalArgumentException(s"Failed parsing oauth header: [$oauthHeaderValue]")
  }
  
}
