/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import java.net.URL
import com.fasterxml.jackson.databind.ObjectMapper
import net.loxal.client.rest.model.RestCode
import net.loxal.client.rest.model.ClientRequestModel


// TODO move this methods to data DTO’s class object scope
class RestCodeUtil {
    class object {
        val restCodeToken = "RESTcode:"

        fun parseRestCode(url: URL): RestCode {
            val restCodeRaw = url.getRef()
            val restCodeData = restCodeRaw.substring(restCodeToken.length())
            val mapper = ObjectMapper()
            val restCode = mapper.readValue(restCodeData, javaClass<RestCode>())

            return restCode
        }

        fun mapToClientRequest(url: URL): ClientRequestModel {
            val restCode: RestCode = parseRestCode(url)
            val clientRequest: ClientRequestModel = convertToClientRequest(url, restCode)

            return clientRequest
        }

        private fun convertToClientRequest(url: URL, restCode: RestCode): ClientRequestModel {
            val urlRoot = "${url.getProtocol()}://${url.getHost()}${if (url.getPort() == -1) "" else ":" + url.getPort()}${url.getPath()}"

            val clientRequest = ClientRequestModel.Builder(restCode.name)
                    .method(restCode.method)
                    .headers(restCode.headers)
                    .body(restCode.body)
                    .url(URL(urlRoot))
                    .build()

            return clientRequest
        }
    }
}