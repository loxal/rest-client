/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client

import com.fasterxml.jackson.databind.ObjectMapper
import net.loxal.client.rest.model.ClientRequest
import net.loxal.client.rest.model.Headers
import net.loxal.client.rest.model.RestCode
import org.junit.Test
import java.net.URL
import javax.ws.rs.HttpMethod
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RestCodeTest {
    /**
     * TODO This test might be superfluous as #parseRestCode is called in #fromRestCode.
     */
    Test
    fun mapRestCode() {
        val restCode: RestCode = RestCode.parseRestCode(restCodeUrl)
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
        val clientRequest: ClientRequest = ClientRequest.fromRestCode(restCodeUrl)
        validateClientRequest(clientRequest)
    }

    private fun validateClientRequest(clientRequest: ClientRequest) {
        assertEquals(headers, clientRequest.headers)
        assertEquals(bodyJson, clientRequest.body)
        assertEquals(method, clientRequest.method)
        assertEquals(name, clientRequest.name)
        assertEquals(endpointUrl, clientRequest.url.toString())
    }

    Test
    fun `ClientRequest model to curl CLI command conversion`() {
        val clientRequest = ClientRequest.fromRestCode(restCodeUrl)
        assertEquals(curlCliCommand, clientRequest.toCurlCliCommand())
    }

    Test
    fun `curl CLI command to ClientRequest model`() {
        val clientRequest = ClientRequest.fromCurlCliCommand(curlCliCommand)
        assertEquals("POST", clientRequest.method)
        assertEquals("{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}", clientRequest.body)
        assertEquals(URL("https://example.com:440/endpoint/"), clientRequest.url)
        assertEquals("[From curl CLI command]", clientRequest.name)

        val headers: Headers = Headers()
        headers.put("header", listOf("value", "value1", 42.0, true))
        headers.put("header1", listOf(0, 1, false, false))
        headers.put("header2", "")
        headers.put("number", 1)
        headers.put("header3", "value3")
        headers.put("", "")
        assertEquals(headers.toString(), clientRequest.headers.toString())
    }

    Test
    fun `Headers’ toString with lineBreak`() {
        val showHeaders = "Server: RESTkit v1\n"
        val header = Headers.new("Server", "RESTkit v1").entrySet().first()

        assertEquals(showHeaders, Headers.toString(entry = header, lineBreak = true))
        assertNotEquals(showHeaders, Headers.toString(entry = header, lineBreak = false))
        assertNotEquals(showHeaders, Headers.toString(entry = header))
    }

    Test
    fun `ClientRequest’s toString`() {
        val clientRequestWithName = ClientRequest.Builder(name).url(URL(endpointUrl)).headers(headers).body(bodyJson).method(method).build()
        assertEquals("""https://example.com:440/endpoint/#RESTcode:{"headers": {: , number: 1, header3: value3, header2: , header1: [0, 1, false, false], header: [value, value1, 42.0, true]}, "body": "{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}", "method": "POST", "name": "Test Example"}""",
                clientRequestWithName.toString())

        val clientRequestWithDefaultName = ClientRequest.Builder().url(URL(endpointUrl)).headers(headers).body(bodyJson).method(method).build()
        assertEquals("""https://example.com:440/endpoint/#RESTcode:{"headers": {: , number: 1, header3: value3, header2: , header1: [0, 1, false, false], header: [value, value1, 42.0, true]}, "body": "{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}", "method": "POST", "name": "Unnamed"}""",
                clientRequestWithDefaultName.toString())
    }

    Test
    fun toHeaders() {
        val headerValueReference = "Header Name: Value"
        val headerFromText = ClientRequest.toHeaders(headerValueReference)
        assertEquals(1, headerFromText.size())
        assertEquals(headerValueReference, headerFromText.toString())
        assertEquals(headerValueReference, ClientRequest.toHeaders("  Header Name   :  Value ").toString())

        val headersFromText = ClientRequest.toHeaders("  Header Name   :  Value  \nHeader1:Value "
                + " \n  Header2  :Value :DELTA:\nMultivalue-Header :[First Value, Second Value, Another Value]")
        assertEquals(4, headersFromText.size())
        val headers = Headers()
        headers.put("Header Name", "Value")
        headers.put("Header1", "Value")
        headers.put("Multivalue-Header", listOf("First Value", "Second Value", "Another Value"))
        headers.put("Header2", "Value :DELTA:")
        assertEquals(headers.toString(), headersFromText.toString())

        val headersOdd = headers
        headersOdd.put("Additional", "Entry")
        assertNotEquals(headersOdd.toString(), headersFromText.toString())

        assertEquals(Headers(), ClientRequest.toHeaders(""))
        assertEquals(0, ClientRequest.toHeaders("").size())
    }

    companion object {
        private val mapper: ObjectMapper = ObjectMapper()

        private val method: String = HttpMethod.POST
        private val name: String = "Test Example"
        private val endpointUrl: String = "https://example.com:440/endpoint/"

        private val bodyJson: String = "{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}"

        private val headersJson: String = "{\"header\": [\"value\", \"value1\", 42.0, true], " +
                "\"header1\": [\"0\", 1, false, \"false\"], \"header2\": [], " +
                "\"number\": [1], \"\": [], \"header3\": [\"value3\"]}"
        private val headers: Headers = mapper.readValue(headersJson, javaClass<Headers>())

        private val restCodeUrlRaw: String = "$endpointUrl#${RestCode.restCodeToken}{" +
                "\"headers\": ${headersJson}," +
                "\"body\": \"${bodyJson}\"," +
                "\"method\": \"${HttpMethod.POST}\"," +
                "\"name\": \"$name\"" +
                "}"

        private val restCodeUrl: URL = URL(restCodeUrlRaw)

        private val curlCliCommand: String = """curl -i -X POST https://example.com:440/endpoint/ \
-H ": " \
-H "number: 1" \
-H "header3: value3" \
-H "header2: " \
-H "header1: [0, 1, false, false]" \
-H "header: [value, value1, 42.0, true]" \
-d $'{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}'"""
    }
}
