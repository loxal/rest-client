/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client

import net.loxal.client.rest.model.ClientRequest
import net.loxal.client.rest.model.Headers
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals

class CurlTest {
    @Test
    fun `curl CLI command to ClientRequest model`() {
        val clientRequest = ClientRequest.fromCurlCliCommand(RestCodeTest.curlCliCommand)
        assertEquals("POST", clientRequest.method)
        assertEquals("{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}", clientRequest.body)
        assertEquals(URL("https://example.com:440/endpoint/"), clientRequest.url)
        assertEquals("[Valid curl CLI command]", clientRequest.name)

        val headers: Headers = Headers()
        headers.put("header", listOf("value", "value1", 42.0, true))
        headers.put("header1", listOf(0, 1, false, false))
        headers.put("header2", "")
        headers.put("number", 1)
        headers.put("header3", "value3")
        headers.put("", "")
        assertEquals(headers.toString(), clientRequest.headers.toString())
    }

    @Test
    fun `from simple Curl`() {
        val clientRequest = ClientRequest.fromCurlCliCommand("curl -i -X PUT https://example.com/endpoint/ \\")
        assertEquals("PUT", clientRequest.method)
        assertEquals("", clientRequest.body)
        assertEquals(URL("https://example.com/endpoint/"), clientRequest.url)
        assertEquals("[Valid curl CLI command]", clientRequest.name)
        assertEquals(Headers().toString(), clientRequest.headers.toString())
    }
}
