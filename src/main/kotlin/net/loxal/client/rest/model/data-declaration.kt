/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest.model

import com.beust.jcommander.JCommander
import com.fasterxml.jackson.databind.ObjectMapper
import net.loxal.client.rest.App
import net.loxal.client.rest.curl.CurlCommand
import java.io.Serializable
import java.net.URL
import java.util.Arrays
import java.util.HashMap
import javax.ws.rs.HttpMethod
import kotlin.test.assertEquals

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

    companion object {
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

    companion object {
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

    // TODO test if body is omitted when it’s empty
    fun toCurlCliCommand(): String {
        val headers: StringBuilder = StringBuilder()
        this.headers.forEach { entry ->
            headers.append("-H \"${Headers.toString(entry)}\" ${Constant.consoleBreak}")
        }

        val curlBody = if (body.isEmpty()) "" else "-d $'${body}'"

        val curlCliCommand = "curl -i -X ${method} \"${url}\" \\${Constant.lineBreak}${headers}${curlBody}"

        return curlCliCommand
    }

    override fun toString() =
            "${url}#${RestCode.restCodeToken}{" +
                    "\"headers\": {${headers}}, " +
                    "\"body\": \"${body}\", " +
                    "\"method\": \"${method}\", " +
                    "\"name\": \"$name\"" +
                    "}"

    companion object {
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

        fun fromRestCode(restCodeUrl: URL): ClientRequest {
            val restCode = RestCode.parseRestCode(restCodeUrl)
            val clientRequest: ClientRequest = createClientRequest(restCodeUrl, restCode)

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

        fun fromCurlCliCommand(curlCliCommand: String): ClientRequest {
            val newCurl = curlCliCommand.replace("curl ", "").replace("\\\n", "")
            println(newCurl)
            val parsedCurl = newCurl.split("\"")
            parsedCurl.forEach { e -> println(e) }

            val jct = CurlCommand();

            val j = JCommander(jct, "-log", "2", "-groups", "unit1,unit2,unit3",
                    "-debug",
                    //                    "-Doption=value",
                    "a", "b", "c", "-X", "POST", "https://example.com:440/endpoint/", "-H", "\"header3: value3\"", "-H", "\"number: 1\"", "-H", "\": \"", "-H", "header1: [0, 1, false, false]",
                    "-d", "$'{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}'"
            );

            assertEquals("POST", jct.httpMethod)
            assertEquals("$'{'key': 'value', 'key1': 'value', 'key2': ['value', 42.24, false], 'key3': {'key3.1': true}}'", jct.data)
            assertEquals("{= , header3= value3, number= 1, header1= [0, 1, false, false]}", jct.headers.toString())


            assertEquals(2, jct.verbose?.toInt());
            assertEquals("unit1,unit2,unit3", jct.groups);
            assertEquals(true, jct.debug);
            //            assertEquals("value", jct.dynamicParams.get("option"));
            assertEquals(Arrays.asList("a", "b", "c", "https://example.com:440/endpoint/"), jct.parameters);


            //            val pattern :Pattern = Pattern.compile("curl.+-X\\ (?<httpMethod>GET|POST|DELETE|PUT|HEAD|OPTIONS)\\ [\"](?<url>http.+/)[\"]\\ .+(-H\\ .+)*.*")
            //            val matcher: Matcher = pattern.matcher(curlCliCommand.replace("\n", ""))
            //            println(curlCliCommand)
            //            val matches = matcher.matches()
            //            assertEquals("POST", matcher.group("httpMethod"))
            //            assertEquals("https://example.com:440/endpoint/", matcher.group("url"))
            //            println(matcher.group(3))
            //            assertEquals(2, matcher.groupCount())

            // TODO implement
            // TODO use a command line parser from http://stackoverflow.com/questions/367706/is-there-a-good-command-line-argument-parser-for-java
            return ClientRequest.Builder().build()
        }
    }
}

object Constant {
    val lineBreak = "\n"
    val consoleBreak = "\\$lineBreak"
    val unnamed = "Unnamed"
}
