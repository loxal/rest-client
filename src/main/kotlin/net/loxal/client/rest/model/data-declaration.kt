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

data class Headers private() : MultivaluedHashMap<String, Any>() {
    override fun toString(): String {
        val string = StringBuilder()
        this.forEach { entry ->
            val prettyFormatHeaderValue =
                    if (entry.value.isEmpty()) entry.value.toString()
                    else if (entry.value.size() > 1)
                        entry.value.toString()
                    else entry.value.first().toString()
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
    }
}

data class RequestParameter(val paramName: String, val paramValue: Any)

data class RestCode private() {
    val method: String = HttpMethod.GET
    val headers: List<Headers> = Collections.emptyList()
    val body: String = ""
    val name: String = "Unnamed"
}

data class ClientRequestModel(builder: ClientRequestModel.Builder) : Serializable {
    val url: URL = builder.url
    val method: String = builder.method
    val headers: List<Headers> = builder.headers
    val body: String = builder.body
    var name: String = builder.name

    class Builder(val name: String) {
        var method: String = HttpMethod.GET
        var url: URL = App.SAMPLE_URL
        var headers: List<Headers> = Collections.emptyList()
        var body: String = ""

        fun method(method: String): Builder {
            this.method = method
            return this
        }

        fun url(url: URL): Builder {
            this.url = url
            return this
        }

        fun headers(headers: List<Headers>): Builder {
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
        if (!this.headers.isEmpty()) {
            this.headers.forEach { header ->
                headers.append("-H \"${header}\" \\ ${Constant.lineBreak}")
            }
        }

        val curlCliCommand = "curl -X \"${method}\" \\ ${Constant.lineBreak}\"${url}\" \\ ${Constant.lineBreak}${headers}-d $'${body}'"

        return curlCliCommand
    }

    class object {
        private val serialVersionUID = 5979696652154735187
        val headerKeyValueSeparator = ":"

        fun toHeaders(text: String): List<Headers> {
            val headers: MutableList<Headers> = arrayListOf()
            text.split(Constant.lineBreak).forEach { header ->
                if (header.contains(headerKeyValueSeparator)) {
                    val headerName = header.substringBefore(headerKeyValueSeparator)
                    val headerValue = header.substringAfter(headerKeyValueSeparator)
                    headers.add(Headers.new(headerName.trim(), headerValue.trim()))
                }
            }

            return headers
        }
    }
}

object Constant {
    val lineBreak = "\n"
}
