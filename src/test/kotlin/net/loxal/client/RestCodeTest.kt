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
import net.loxal.client.rest.model.Headers
import kotlin.test.assertNotEquals
import net.loxal.client.rest.model.Constant

class RestCodeTest {
    /**
     * TODO This test might be superfluous as #parseRestCode is called in #mapToClientRequest.
     */
    Test
    fun mapRestCode() {
        val restCode: RestCode = RestCodeUtil.parseRestCode(url)
        validateRestCode(restCode)
    }

    private fun validateRestCode(restCode: RestCode) {
        assertEquals(method, restCode.method)
        assertEquals(name, restCode.name)
        assertEquals(headers, restCode.headers)
        assertEquals(bodyJson, restCode.body)
    }

    Test
    fun fromRestCodeModelToClientRequestModel() {
        val clientRequest: ClientRequestModel = RestCodeUtil.mapToClientRequest(url)
        validateClientRequest(clientRequest)
    }

    private fun validateClientRequest(clientRequest: ClientRequestModel) {
        assertEquals(headers, clientRequest.headers)
        assertEquals(bodyJson, clientRequest.body)
        assertEquals(method, clientRequest.method)
        assertEquals(name, clientRequest.name)
        assertEquals(endPointUrl, clientRequest.url.toString())
    }

    Test
    fun clientRequestModelToCurl() {
        val clientRequest = RestCodeUtil.mapToClientRequest(url)
        val consoleBreak = "\\ ${Constant.lineBreak}"
        assertEquals("curl -X \"POST\" $consoleBreak"
                + "\"https://example.com:440/endpoint/\" $consoleBreak"
                + "-H \"header: [value, value1, 42.0, true]\" $consoleBreak"
                + "-H \"header1: [0, 1, false, false]\" $consoleBreak"
                + "-H \"\" $consoleBreak"
                + "-H \"header2: []\" $consoleBreak"
                + "-H \"header3: value3\" $consoleBreak"
                + "-d $'{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}'",
                clientRequest.toCurlCliCommand())
    }

    Test
    fun toHeaders() {
        val headerValueReference = "Header Name: Value"
        val headerFromText = ClientRequestModel.toHeaders(headerValueReference)
        assertEquals(1, headerFromText.size())
        assertEquals(headerValueReference, headerFromText.first().toString())
        assertEquals(headerValueReference, ClientRequestModel.toHeaders("  Header Name   :  Value ").first().toString())

        val headersFromText = ClientRequestModel.toHeaders("  Header Name   :  Value  \nHeader1:Value \n  Header2  :Value :DELTA:")
        assertEquals(3, headersFromText.size())
        assertEquals(listOf(Headers.new("Header Name", "Value"), Headers.new("Header1", "Value"), Headers.new("Header2", "Value :DELTA:")).toString(),
                headersFromText.toString())
        assertNotEquals(listOf(Headers.new("Header Name", listOf("Value")), Headers.new("Header1", listOf("Value")), Headers.new("Header2", listOf("Value"))).toString(),
                headersFromText.toString())

        assertEquals(emptyList<Headers>(), ClientRequestModel.toHeaders(""))
        assertEquals(0, ClientRequestModel.toHeaders("").size())
    }

    class object {
        private val mapper: ObjectMapper = ObjectMapper()

        private val method: String = HttpMethod.POST
        private val name: String = "Test Example"
        private val endPointUrl: String = "https://example.com:440/endpoint/"

        private val bodyJson: String = "{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}"

        private val headersJson: String = "[{\"header\": [\"value\", \"value1\", 42.0, true]}, {\"header1\": [\"0\", 1, false, \"false\"]}, {}, {\"header2\": []}, {\"header3\": [\"value3\"]}]"
        private val headers: List<Headers> = mapper.readValue(headersJson, javaClass<List<Headers>>())

        private val restCodeUrl: String = "$endPointUrl#${RestCodeUtil.restCodeToken}{" +
                "\"headers\": ${headersJson}," +
                "\"body\": \"${bodyJson}\"," +
                "\"method\": \"${HttpMethod.POST}\"," +
                "\"name\": \"$name\"" +
                "}"

        private val url: URL = URL(restCodeUrl)
    }
}
