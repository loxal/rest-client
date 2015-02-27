/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.GsonBuilder
import net.loxal.client.rest.model.RequestParameter
import java.util.ArrayList
import net.loxal.client.rest.model.Header
import java.io.File
import java.lang
import java.io.IOException
import javafx.scene.control.Control
import javafx.scene.input.KeyCodeCombination
import javafx.scene.control.Tooltip
import javax.ws.rs.client.WebTarget
import javax.ws.rs.client.Invocation
import net.loxal.client.rest.model.ClientRequestModel

final class Util {
    class object {
        final fun assignShortcut(control: Control, keyCodeCombination: KeyCodeCombination, action: Runnable) {
            control.getScene().getAccelerators().put(keyCodeCombination, action)
            control.setTooltip(Tooltip("${keyCodeCombination.getDisplayText()}"))
        }

        final fun createAppHome(appHomeDirectory: File) {
            if (!appHomeDirectory.exists()) {
                if (appHomeDirectory.mkdirs()) {
                    App.LOG.info(lang.String.format("%s created", appHomeDirectory))
                } else {
                    App.LOG.severe(lang.String.format("%s creation failed", appHomeDirectory))
                }
            }
        }

        final fun createSaveFile(fullFilePath: String) {
            val saveFile = File(fullFilePath)
            if (!saveFile.exists()) {
                try {
                    if (saveFile.createNewFile()) {
                        App.LOG.info("$saveFile created")
                    } else {
                        App.LOG.severe("$saveFile creation failed")
                    }
                } catch (e: IOException) {
                    App.LOG.severe(e.getMessage())
                }

            }
        }

        final fun extractHeaderData(rawHeaderData: String): List<Header> {
            fun containsHeaderDeclaration(headerDataPair: List<String>): Boolean = headerDataPair.size() > 1
            val headers = ArrayList<Header>()
            if (!rawHeaderData.isEmpty()) {
                rawHeaderData.split(ClientRequestModel.lineBreak).forEach { header ->
                    if (header.contains(ClientRequestModel.headerKeyValueSeparator)) {
                        val (headerName, headerValue) = header.split(ClientRequestModel.headerKeyValueSeparator)
                        headers.plus(mapOf(Pair(headerName, headerValue)))
                    }
                }
            }

            return headers
        }

        final fun extractRequestParameters(requestParameterContent: String): List<RequestParameter> {
            val requestParameters = ArrayList<RequestParameter>()

            if (!requestParameterContent.isEmpty()) {
                val parameterPairSeparatorRegex = "&${ClientRequestModel.lineBreak}|&"
                val parameterPairs = requestParameterContent.split(parameterPairSeparatorRegex)
                val parameterPairEntrySeparatorRegex = "="
                parameterPairs.forEach { parameterPair ->
                    val parameterPairEntry = parameterPair.trim().split(parameterPairEntrySeparatorRegex)
                    val parameterNameIdx = 0
                    val parameterValueIdx = 1
                    requestParameters.add(RequestParameter(parameterPairEntry[parameterNameIdx], parameterPairEntry[parameterValueIdx]))
                }
            }

            return requestParameters
        }

        final fun formatJson(json: String): String {
            val jsonElement: JsonElement
            try {
                jsonElement = JsonParser().parse(json)
            } catch (e: JsonSyntaxException) {
                App.LOG.warning(e.getMessage())
                App.LOG.warning(e.getCause().toString())
                return json
            }
            return GsonBuilder().setPrettyPrinting().create().toJson(jsonElement)
        }

        final fun applyHeaderInfo(headers: List<Header>, request: Invocation.Builder): Invocation.Builder {
            headers.forEach { header -> request.header(header.name, header.value) }

            return request
        }

        final fun applyUrlRequestParameters(webTarget: WebTarget, requestParameters: List<RequestParameter>): WebTarget {
            var target = webTarget

            requestParameters.forEach { requestParameter ->
                target = target.queryParam(requestParameter.paramName, requestParameter.paramValue)
            }

            return target
        }
    }
}