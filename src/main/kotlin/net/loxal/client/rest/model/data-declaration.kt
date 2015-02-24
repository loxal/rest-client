/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest.model

import java.io.Serializable
import javax.ws.rs.HttpMethod
import java.net.URL
import net.loxal.client.rest.App
import com.fasterxml.jackson.databind.ObjectMapper

data class Header(val name: String, val value: List<Any>) {
    override public fun toString(): String {
        return "$name: ${value.joinToString(separator = "###")}"
    }
}

data class RequestParameter(val paramName: String, val paramValue: Any)

data class RestCode private() {
    val method: String = HttpMethod.GET
    val headers: List<Map<String, List<Any>>> = emptyList()
    // TODO body can stay text/string?!
    val body: Map<String, Any> = emptyMap()
    val name: String = "Unnamed"
}

data class ClientRequestModel(builder: ClientRequestModel.Builder) : Serializable {
    val method: String = builder.method
    val url: URL = builder.url
    // TODO lookup the true type of headers => MultiValueMap<String, List<String>>)?
    val headers: List<Map<String, List<Any>>> = builder.headers
    val body: Map<String, Any> = builder.body
    var name: String = builder.name

    public class Builder(val name: String) {
        var method: String = HttpMethod.GET
        var url: URL = App.SAMPLE_URL
        var headers: List<Map<String, List<Any>>> = emptyList()
        var body: Map<String, Any> = emptyMap()

        fun method(method: String): Builder {
            this.method = method
            return this
        }

        public fun url(url: URL): Builder {
            this.url = url
            return this
        }

        public fun headers(headers: List<Map<String, List<Any>>>): Builder {
            this.headers = headers
            return this
        }

        public fun body(body: Map<String, Any>): Builder {
            this.body = body
            return this
        }

        public fun build(): ClientRequestModel {
            return ClientRequestModel(this)
        }
    }

    class object {
        val serialVersionUID = 5979696652154735184

        // TODO unit test this
        fun headersFromText(text: String): List<Map<String, List<Any>>> {
            val headersFromText: List<Map<String, List<Any>>> = emptyList()
            if (!text.isEmpty()) {
                text.split("\n").forEach { e ->
                    val (key, value) = e.split(":")
                    headersFromText.plus(mapOf(Pair(key, value)))
                }
            }

            return headersFromText
        }

        // TODO body can stay text/string?!
        // TODO uni test this
        fun bodyFromText(text: String): Map<String, Any> {
            if (text.isEmpty())
                return emptyMap()
            else {
                val mapper: ObjectMapper = ObjectMapper()
                val bodyFromText: Map<String, Any> = mapper.readValue(text, javaClass<Map<String, Any>>())

                return bodyFromText
            }
        }
    }
}
