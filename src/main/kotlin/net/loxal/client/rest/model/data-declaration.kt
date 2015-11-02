/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest.model

import com.fasterxml.jackson.databind.ObjectMapper
import net.loxal.client.rest.App
import java.io.Serializable
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.ws.rs.HttpMethod

class Headers() : HashMap<String, List<Any>>() {
    override fun toString() = toString(", ")

    fun toStringColumn() = toString("\n")

    private fun toString(separator: String): String {
        val string = StringBuilder()
        var idx = 0
        this.forEach { entry ->
            val prettyFormatHeaderValue = prettyFormatHeaderValue(entry.value)
            string.append("${entry.key}: $prettyFormatHeaderValue")

            val notLastElement = size > ++idx
            if (notLastElement) {
                string.append(separator)
            }
        }

        return string.toString()
    }

    fun put(key: String, value: Any): List<Any>? {
        return super.put(key, listOf(value))
    }

    companion object {
        private const val serialVersionUID = 3979696252154731188

        fun new(name: String, value: Any): Headers {
            val h: Headers = Headers()
            h.put(name, listOf(value))

            return h
        }

        private fun prettyFormatHeaderValue(value: List<Any>) =
                if (value.isEmpty())
                    ""
                else if (value.size > 1)
                    value.toString()
                else
                    value.first().toString()

        fun toString(entry: Map.Entry<String, List<Any>>, lineBreak: Boolean = false) =
                "${entry.key}: ${prettyFormatHeaderValue(entry.value)}${if (lineBreak) Constant.lineBreak else ""}"
    }
}

data class RequestParameter(val paramName: String, val paramValue: Any)

data class RestCode(val method: String = HttpMethod.GET) {
    val headers: Headers = Headers()
    val body: String = ""
    val name: String = Constant.unnamed

    companion object {
        val restCodeToken = "${App.REST_CODE_NAME}:"

        fun parseRestCode(url: URL): RestCode {
            val restCodeRaw = url.ref
            val restCodeData = restCodeRaw.substring(restCodeToken.length)
            val mapper = ObjectMapper()
            val restCode = mapper.readValue(restCodeData, RestCode::class.java)

            return restCode
        }
    }
}

data class ClientRequest(var builder: ClientRequest.Builder) : Serializable {
    val url: URL = builder.url
    val method: String = builder.method
    val headers: Headers = builder.headers
    val body: String = builder.body
    val name: String = builder.name

    class Builder(var name: String = Constant.unnamed) {
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

    // TODO test if body is omitted when it’s empty
    fun toCurlCliCommand(): String {
        val headers: StringBuilder = StringBuilder()
        this.headers.forEach { entry ->
            headers.append("-H \"${Headers.toString(entry)}\" ${Constant.consoleBreak}")
        }

        val curlBody = if (body.isEmpty()) "" else "-d $'$body'"

        val curlCliCommand = "curl -i -X $method $url \\${Constant.lineBreak}$headers$curlBody"

        return curlCliCommand
    }

    override fun toString() =
            "$url#${RestCode.restCodeToken}{" +
                    "\"headers\": {$headers}, " +
                    "\"body\": \"$body\", " +
                    "\"method\": \"$method\", " +
                    "\"name\": \"$name\"" +
                    "}"

    companion object {
        private const val serialVersionUID = 5979496652154735188
        val headerKeyValueSeparator = ":"

        fun toHeaders(text: String): Headers {
            val headers: Headers = Headers()
            text.split(Constant.lineBreak.toRegex()).toTypedArray().forEach { header ->
                if (header.contains(headerKeyValueSeparator)) {
                    val headerName = header.substringBefore(headerKeyValueSeparator)
                    val headerValue = header.substringAfter(headerKeyValueSeparator)
                    headers.put(headerName.trim(), listOf(headerValue.trim()))
                }
            }

            return headers
        }

        fun fromRestCode(restCodeUrl: URL): ClientRequest {
            val restCode = RestCode.parseRestCode(restCodeUrl)
            val clientRequest: ClientRequest = createClientRequest(restCodeUrl, restCode)

            return clientRequest
        }

        private fun createClientRequest(url: URL, restCode: RestCode): ClientRequest {
            val urlRoot = "${url.protocol}://${url.host}${if (url.port == -1) "" else ":" + url.port}${url.path}"

            val clientRequest = ClientRequest.Builder(restCode.name)
                    .method(restCode.method)
                    .headers(restCode.headers)
                    .body(restCode.body)
                    .url(URL(urlRoot))
                    .build()

            return clientRequest
        }

        fun fromCurlCliCommand(curlCliCommand: String): ClientRequest {
            val method: Pattern = Pattern.compile("curl.+-X\\ (?<httpMethod>GET|POST|DELETE|PUT|HEAD|OPTIONS)\\ .*")
            val url: Pattern = Pattern.compile("curl.+[\"]?(?<url>http.+/)[\"]?\\ ")
            val body: Pattern = Pattern.compile("curl.+-d\\ \\$'(?<data>.*)'.*")
            val header: Pattern = Pattern.compile("-H\\ \"(?<header>.*\\:.*)\"")

            val cleanCurlCommand = curlCliCommand.replace("\n", "").replace("\\", "")
            val methodMatcher: Matcher = method.matcher(cleanCurlCommand)
            val urlMatcher: Matcher = url.matcher(cleanCurlCommand)
            val bodyMatcher: Matcher = body.matcher(cleanCurlCommand)
            val headerMatcher: Matcher = header.matcher(cleanCurlCommand)
            val request: ClientRequest.Builder = ClientRequest.Builder("[Malformed curl CLI command]")

            if (methodMatcher.matches())
                request.method(methodMatcher.group("httpMethod"))
            if (bodyMatcher.find())
                request.body(bodyMatcher.group("data"))
            if (urlMatcher.find()) {
                request.url(URL(urlMatcher.group("url")))
                request.name = "[Valid curl CLI command]"
            }

            if (headerMatcher.find()) {
                val headersText = StringBuilder()
                headerMatcher.group().split("-H ".toRegex()).toTypedArray().forEach { header ->
                    headersText.append("${header.replace("\"", "")}${Constant.lineBreak}")
                }
                request.headers(ClientRequest.toHeaders(headersText.toString()))
            }
            return request.build()
        }
    }
}

object Constant {
    val lineBreak = "\n"
    val consoleBreak = "\\$lineBreak"
    val unnamed = "Unnamed"
}
