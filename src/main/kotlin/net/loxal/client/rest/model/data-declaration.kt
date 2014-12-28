/*
 * Copyright 2014 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest.model

import java.io.Serializable
import kotlin.platform.platformStatic

data class Header(val name: String, val value: List<Any>) {
    override public fun toString(): String {
        return "$name: ${value.joinToString(separator = "###")}"
    }
}

data class RequestParameter(val paramName: String, val paramValue: Any)

data class ClientRequestModel(builder: ClientRequestModel.Builder) : Serializable {
    val url: String = builder.url
    val headers: String = builder.headers
    val parameters: String = builder.parameters
    val body: String = builder.body
    var name: String = builder.name

    public class Builder(val name: String) {
        var url: String = ""
        var headers: String = ""
        var parameters: String = ""
        var body: String = ""

        public fun url(url: String): Builder {
            this.url = url
            return this
        }

        public fun headers(headers: String): Builder {
            this.headers = headers
            return this
        }

        public fun parameters(parameters: String): Builder {
            this.parameters = parameters
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
        platformStatic val serialVersionUID = 5979696652154735181
    }
}
