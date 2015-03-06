/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest.model

import java.io.Serializable
import javax.ws.rs.HttpMethod
import java.net.URL
import net.loxal.client.rest.App
import javax.ws.rs.core.MultivaluedHashMap

data class Headers() : MultivaluedHashMap<String, Any>() {
    override fun toString(): String {
        val string = StringBuilder()
        this.forEach { entry ->
            val prettyFormatHeaderValue = prettyFormatHeaderValue(entry.value)
            string.append("${entry.key}: ${prettyFormatHeaderValue}")
        }

        return string.toString()
    }

    class object {
        fun new(name: String, value: Any): Headers {
            val h: Headers = Headers()
            h.add(name, value)

            return h
        }

        private fun prettyFormatHeaderValue(value: List<Any>) =
                if (value.isEmpty()) ""
                else if (value.size() > 1)
                    value.toString()
                else value.first().toString()

        fun toString(entry: Map.Entry<String, List<Any>>) = "${entry.key}: ${prettyFormatHeaderValue(entry.value)}"
    }
}

data class RequestParameter(val paramName: String, val paramValue: Any)

data class RestCode private() {
    val method: String = HttpMethod.GET
    val headers: Headers = Headers()
    val body: String = ""
    val name: String = "Unnamed"
}

data class ClientRequestModel(builder: ClientRequestModel.Builder) : Serializable {
    val url: URL = builder.url
    val method: String = builder.method
    val headers: Headers = builder.headers
    val body: String = builder.body
    var name: String = builder.name

    class Builder(val name: String) {
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

        fun build(): ClientRequestModel = ClientRequestModel(this)
    }

    fun toCurlCliCommand(): String {
        val headers: StringBuilder = StringBuilder()
        this.headers.forEach { entry ->
            headers.append("-H \"${Headers.toString(entry)}\" \\ ${Constant.lineBreak}")
        }

        val curlCliCommand = "curl -X \"${method}\" \\ ${Constant.lineBreak}\"${url}\" \\ ${Constant.lineBreak}${headers}-d $'${body}'"

        return curlCliCommand
    }

    class object {
        private val serialVersionUID = 5979696652154735187
        val headerKeyValueSeparator = ":"

        fun toHeaders(text: String): Headers {
            val headers: Headers = Headers()
            text.split(Constant.lineBreak).forEach { header ->
                if (header.contains(headerKeyValueSeparator)) {
                    val headerName = header.substringBefore(headerKeyValueSeparator)
                    val headerValue = header.substringAfter(headerKeyValueSeparator)
                    headers.add(headerName.trim(), headerValue.trim())
                }
            }

            return headers
        }
    }
}

object Constant {
    val lineBreak = "\n"
}
