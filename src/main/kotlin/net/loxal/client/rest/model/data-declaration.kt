/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest.model

import java.io.Serializable
import javax.ws.rs.HttpMethod
import java.net.URL
import net.loxal.client.rest.App
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.HashMap

data class Headers() : HashMap<String, List<Any>>() {
    override fun toString() = toString(", ")

    fun toStringColumn() = toString("\n")

    private fun toString(separator: String): String {
        val string = StringBuilder()
        var idx = 0
        this.forEach { entry ->
            val prettyFormatHeaderValue = prettyFormatHeaderValue(entry.value)
            string.append("${entry.key}: ${prettyFormatHeaderValue}")

            val notLastElement = size() > ++idx
            if (notLastElement) {
                string.append(separator)
            }
        }

        return string.toString()
    }

    fun put(key: String, value: Any): List<Any>? {
        return super.put(key, listOf(value))
    }

    class object {
        private val serialVersionUID = 3979696252154731188

        fun new(name: String, value: Any): Headers {
            val h: Headers = Headers()
            h.put(name, listOf(value))

            return h
        }

        private fun prettyFormatHeaderValue(value: List<Any>) =
                if (value.isEmpty())
                    ""
                else if (value.size() > 1)
                    value.toString()
                else
                    value.first().toString()

        fun toString(entry: Map.Entry<String, List<Any>>, lineBreak: Boolean = false) =
                "${entry.key}: ${prettyFormatHeaderValue(entry.value)}${if (lineBreak) Constant.lineBreak else ""}"
    }
}

data class RequestParameter(val paramName: String, val paramValue: Any)

data class RestCode private() {
    val method: String = HttpMethod.GET
    val headers: Headers = Headers()
    val body: String = ""
    val name: String = Constant.unnamed

    class object {
        val restCodeToken = "RESTcode:"

        fun parseRestCode(url: URL): RestCode {
            val restCodeRaw = url.getRef()
            val restCodeData = restCodeRaw.substring(restCodeToken.length())
            val mapper = ObjectMapper()
            val restCode = mapper.readValue(restCodeData, javaClass<RestCode>())

            return restCode
        }
    }
}

data class ClientRequest(builder: ClientRequest.Builder) : Serializable {
    val url: URL = builder.url
    val method: String = builder.method
    val headers: Headers = builder.headers
    val body: String = builder.body
    val name: String = builder.name

    class Builder(val name: String = Constant.unnamed) {
        var method: String = HttpMethod.GET
        var url: URL = App.SAMPLE_URL
        var headers: Headers = Headers()
        var body: String = ""

        fun method(method: String): Builder {
            this.method = method
            return this
        }

        fun url(url: URL): Builder {
            this.url = url
            return this
        }

        fun headers(headers: Headers): Builder {
            this.headers = headers
            return this
        }

        fun body(body: String): Builder {
            this.body = body
            return this
        }

        fun build(): ClientRequest = ClientRequest(this)
    }

    fun toCurlCliCommand(): String {
        val headers: StringBuilder = StringBuilder()
        this.headers.forEach { entry ->
            headers.append("-H \"${Headers.toString(entry)}\" ${Constant.consoleBreak}")
        }

        val curlCliCommand = "curl -X \"${method}\" \"${url}\" \\${Constant.lineBreak}${headers}-d $'${body}'"

        return curlCliCommand
    }

    override fun toString() =
            "${url}#${RestCode.restCodeToken}{" +
                    "\"headers\": {${headers}}, " +
                    "\"body\": \"${body}\", " +
                    "\"method\": \"${method}\", " +
                    "\"name\": \"$name\"" +
                    "}"

    class object {
        private val serialVersionUID = 5979496652154735188
        val headerKeyValueSeparator = ":"

        fun toHeaders(text: String): Headers {
            val headers: Headers = Headers()
            text.split(Constant.lineBreak).forEach { header ->
                if (header.contains(headerKeyValueSeparator)) {
                    val headerName = header.substringBefore(headerKeyValueSeparator)
                    val headerValue = header.substringAfter(headerKeyValueSeparator)
                    headers.put(headerName.trim(), listOf(headerValue.trim()))
                }
            }

            return headers
        }

        fun toClientRequest(url: URL): ClientRequest {
            val restCode = RestCode.parseRestCode(url)
            val clientRequest: ClientRequest = createClientRequest(url, restCode)

            return clientRequest
        }

        private fun createClientRequest(url: URL, restCode: RestCode): ClientRequest {
            val urlRoot = "${url.getProtocol()}://${url.getHost()}${if (url.getPort() == -1) "" else ":" + url.getPort()}${url.getPath()}"

            val clientRequest = ClientRequest.Builder(restCode.name)
                    .method(restCode.method)
                    .headers(restCode.headers)
                    .body(restCode.body)
                    .url(URL(urlRoot))
                    .build()

            return clientRequest
        }
    }
}

object Constant {
    val lineBreak = "\n"
    val consoleBreak = "\\$lineBreak"
    val unnamed = "Unnamed"
}
