/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest.model

import java.io.Serializable
import javax.ws.rs.HttpMethod
import java.net.URL
import net.loxal.client.rest.App
import java.util.Collections

data class Header(val name: String, val value: List<Any>) {
    override public fun toString(): String {
        return "$name: ${value.joinToString(separator = "###")}"
    }
}

data class RequestParameter(val paramName: String, val paramValue: Any)

data class RestCode private() {
    val method: String = HttpMethod.GET
    val headers: List<Map<String, List<Any>>> = emptyList()
    val body: String = ""
    val name: String = "Unnamed"
}

data class ClientRequestModel(builder: ClientRequestModel.Builder) : Serializable {
    val method: String = builder.method
    val url: URL = builder.url
    // TODO lookup the true type of headers => MultiValueMap<String, List<String>>)?
    val headers: List<Map<String, List<Any>>> = builder.headers
    val body: String = builder.body
    var name: String = builder.name

    public class Builder(val name: String) {
        var method: String = HttpMethod.GET
        var url: URL = App.SAMPLE_URL
        var headers: List<Map<String, List<Any>>> = emptyList()
        var body: String = ""

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

        public fun body(body: String): Builder {
            this.body = body
            return this
        }

        public fun build(): ClientRequestModel {
            return ClientRequestModel(this)
        }
    }

    class object {
        private val serialVersionUID = 5979696652154735184
        val headerKeyValueSeparator = ":"
        val lineBreak = "\n"

        // TODO unit test this
        fun headersFromText(text: String): List<Map<String, List<Any>>> {
            val headersFromText: List<Map<String, List<Any>>> = Collections.emptyList()
            if (!text.isEmpty()) {
                text.split(lineBreak).forEach { header ->
                    if (header.contains(headerKeyValueSeparator)) {
                        val (headerName, headerValue) = header.split(headerKeyValueSeparator)
                        headersFromText.plus(mapOf(Pair(headerName, headerValue)))
                    }
                }
            }

            return headersFromText
        }
    }
}
