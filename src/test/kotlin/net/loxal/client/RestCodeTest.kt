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

class RestCodeTest {
    Test
    public fun fromRestCode() {
        val restCodeUrl = "https://www.example.com/endpoint/#RESTcode:{" +
                "\"headers\": [{\"header\": [\"value0\", \"value1\", 42.0, true]}, {\"header1\": [\"0\", 1, false, \"false\"]}, {}, {\"header2\": []}]," +
                "\"body\": {\"key\": \"value\", \"key1\": \"value\", \"key2\": [\"value\", 42.24, false], \"key3\": {\"key3.1\": true}}," +
                "\"method\": \"POST\"," +
                "\"name\": \"Test Example\"" +
                "}"

        val url = URL(restCodeUrl)
        println(url.getUserInfo())
        //        println(url.getContent())
        println(url.getPath())
        println(url.getQuery())
        println(url.getFile())
        val restCode = url.getRef()
        val restCodeRaw = restCode.substring("RESTcode:".length())
        val mapper = ObjectMapper()
        val restCodeModel = mapper.readValue(restCodeRaw, javaClass<RestCode>())
        println(restCodeModel.headers)

        val clientRequestModel = ClientRequestModel.Builder(restCodeModel.name)
                .headers(restCodeModel.headers.toString())
                .body(restCodeModel.body.toString())
                .method(restCodeModel.method)
                .build()

        assertEquals(clientRequestModel.method, "POST")
        assertEquals(clientRequestModel.headers, restCodeModel.headers.toString())
        assertEquals(clientRequestModel.body, restCodeModel.body.toString())
    }

    Test
    public fun toRestCode() {
        assertEquals(true, true)
    }
}
