/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client

import org.junit.Test
import kotlin.test.assertEquals
import java.net.URL
import com.fasterxml.jackson.databind.ObjectMapper
import net.loxal.client.rest.model.RestCode
import net.loxal.client.rest.model.ClientRequestModel
import javax.ws.rs.HttpMethod
import net.loxal.client.rest.RestCodeUtil

class RestCodeTest {
    Test
    fun mapRestCode() {
        val restCode: RestCode = RestCodeUtil.parseRestCode(url)
        validateRestCode(restCode)
    }

    private fun validateRestCode(restCode: RestCode) {
        assertEquals(method, restCode.method)
        assertEquals(name, restCode.name)
        assertEquals(headers, restCode.headers)
        assertEquals(body, restCode.body)
    }

    Test
    fun fromRestCodeModelToClientRequestModel() {
        val clientRequest: ClientRequestModel = RestCodeUtil.mapToClientRequest(url)
        validateClientRequest(clientRequest)
    }

    private fun validateClientRequest(clientRequest: ClientRequestModel) {
        assertEquals(headers, clientRequest.headers)
        assertEquals(body, clientRequest.body)
        assertEquals(method, clientRequest.method)
        assertEquals(name, clientRequest.name)
        assertEquals(endPointUrl, clientRequest.url.toString())
    }

    class object {
        private val mapper: ObjectMapper = ObjectMapper()

        private val method: String = HttpMethod.POST
        private val name: String = "Test Example"

        private val endPointUrl: String = "https://example.com:440/endpoint/"

        private val bodyJson: String = "{\"key\": \"value\", \"key1\": \"value\", \"key2\": [\"value\", 42.24, false], \"key3\": {\"key3.1\": true}}"
        private val body: Map<String, Any> = mapper.readValue(bodyJson, javaClass<Map<String, Any>>())

        private val headersJson: String = "[{\"header\": [\"value\", \"value1\", 42.0, true]}, {\"header1\": [\"0\", 1, false, \"false\"]}, {}, {\"header2\": []}, {\"header3\": [\"value3\"]}]"
        private val headers: List<Map<String, List<Any>>> = mapper.readValue(headersJson, javaClass<List<Map<String, List<Any>>>>())

        private val restCodeUrl: String = "$endPointUrl#${RestCodeUtil.restCodeToken}{" +
                "\"headers\": ${headersJson}," +
                "\"body\": ${bodyJson}," +
                "\"method\": \"${HttpMethod.POST}\"," +
                "\"name\": \"$name\"" +
                "}"

        private val url: URL = URL(restCodeUrl)
    }
}
