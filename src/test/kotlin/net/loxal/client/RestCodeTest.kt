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
import net.loxal.client.rest.model.Header
import kotlin.test.assertNotEquals

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
        //        assertEquals("curl -X \"POST\" \"https://example.com:440/endpoint/\" \\ ${ClientRequestModel.lineBreak}"
        //                + "-H \"header: [value, value1, 42.0, true]\" \\ ${ClientRequestModel.lineBreak}"
        //                + "-H \"header1: [0, 1, false, false]\" \\ ${ClientRequestModel.lineBreak}"
        //                + "-H \"\" \\ ${ClientRequestModel.lineBreak}"
        //                + "-H \"header2: \" \\ ${ClientRequestModel.lineBreak}"
        //                + "-H \"header3: value3\" \\ ${ClientRequestModel.lineBreak}"
        //                + "-d $'{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}'",
        //                clientRequest.toCurlCliCommand())
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
        assertEquals(listOf(Header.new("Header Name", listOf("Value")), Header.new("Header1", listOf("Value")), Header.new("Header2", listOf("Value :DELTA:"))).toString(),
                headersFromText.toString())
        assertNotEquals(listOf(Header.new("Header Name", listOf("Value")), Header.new("Header1", listOf("Value")), Header.new("Header2", listOf("Value"))).toString(),
                headersFromText.toString())

        assertEquals(emptyList<Header>(), ClientRequestModel.toHeaders(""))
        assertEquals(0, ClientRequestModel.toHeaders("").size())
    }

    class object {
        private val mapper: ObjectMapper = ObjectMapper()

        private val method: String = HttpMethod.POST
        private val name: String = "Test Example"
        private val endPointUrl: String = "https://example.com:440/endpoint/"

        private val bodyJson: String = "{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}"

        private val headersJson: String = "[{\"header\": [\"value\", \"value1\", 42.0, true]}, {\"header1\": [\"0\", 1, false, \"false\"]}, {}, {\"header2\": []}, {\"header3\": [\"value3\"]}]"
        private val headers: List<Header> = mapper.readValue(headersJson, javaClass<List<Header>>())

        private val restCodeUrl: String = "$endPointUrl#${RestCodeUtil.restCodeToken}{" +
                "\"headers\": ${headersJson}," +
                "\"body\": \"${bodyJson}\"," +
                "\"method\": \"${HttpMethod.POST}\"," +
                "\"name\": \"$name\"" +
                "}"

        private val url: URL = URL(restCodeUrl)
    }
}
