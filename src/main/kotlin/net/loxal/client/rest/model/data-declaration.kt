/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest.model

import java.io.Serializable
import javax.ws.rs.HttpMethod
import java.net.URL
import net.loxal.client.rest.App
import java.util.Collections
import javax.ws.rs.core.MultivaluedHashMap

data class Header private() : MultivaluedHashMap<String, Any>() {
    var name: String = ""
    var value: List<Any> = emptyList()

    override fun toString(): String = "$name: ${if (value.size() > 1) value.toString() else value.first().toString()}"

    class object {
        fun new(name: String, value: List<Any>): Header {
            val h: Header = Header()
            h.name = name
            h.value = value

            return h
        }
    }
}

data class RequestParameter(val paramName: String, val paramValue: Any)

data class RestCode private() {
    val method: String = HttpMethod.GET
    val headers: List<Header> = emptyList()
    val body: String = ""
    val name: String = "Unnamed"
}

data class ClientRequestModel(builder: ClientRequestModel.Builder) : Serializable {
    val url: URL = builder.url
    val method: String = builder.method
    val headers: List<Header> = builder.headers
    val body: String = builder.body
    var name: String = builder.name

    class Builder(val name: String) {
        var method: String = HttpMethod.GET
        var url: URL = App.SAMPLE_URL
        var headers: List<Header> = emptyList()
        var body: String = ""

        fun method(method: String): Builder {
            this.method = method
            return this
        }

        fun url(url: URL): Builder {
            this.url = url
            return this
        }

        fun headers(headers: List<Header>): Builder {
            this.headers = headers
            return this
        }

        fun body(body: String): Builder {
            this.body = body
            return this
        }

        fun build(): ClientRequestModel = ClientRequestModel(this)
    }

    fun toCurlCliCommand(): String {
        val headers: StringBuilder = StringBuilder()
        this.headers.forEach { header ->
            headers.append("-H \"${header}\"")
        }

        val curlCliCommand = "curl -X \"${this.method}\" \"${this.url}\" -d $'${this.body}'"

        return curlCliCommand
    }

    class object {
        private val serialVersionUID = 5979696652154735186
        val headerKeyValueSeparator = ":"
        val lineBreak = "\n"

        fun headersFromText(text: String): List<Header> {
            val headersFromText: List<Header> = Collections.emptyList()
            if (!text.isEmpty()) {
                text.split(lineBreak).forEach { header ->
                    if (header.contains(headerKeyValueSeparator)) {
                        val (headerName, headerValue) = header.split(headerKeyValueSeparator)
                        headersFromText.plus(Pair(headerName, headerValue))
                    }
                }
            }

            return headersFromText
        }
    }
}
