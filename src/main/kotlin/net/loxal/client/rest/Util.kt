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
import org.apache.commons.lang3.StringUtils
import net.loxal.client.rest.model.Header
import java.util.HashSet
import java.io.File
import java.lang
import java.io.IOException
import javafx.scene.control.Control
import javafx.scene.input.KeyCodeCombination
import javafx.scene.control.Tooltip

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

        final fun extractHeaderData(rawHeaderData: String): Set<Header> {
            val headerNameIdx = 0
            val headerValueIdx = 1
            val headers = HashSet<Header>()

            if (!rawHeaderData.isEmpty()) {
                for (rawHeaderLine in rawHeaderData.split("\\n")) {
                    val headerDataPair = rawHeaderLine.split(":\\s")
                    val header = Header(headerDataPair[headerNameIdx], listOf(headerDataPair[headerValueIdx]))
                    headers.add(header)
                }
            }

            return headers
        }

        final fun extractRequestParameters(requestParameterContent: String): List<RequestParameter> {
            val requestParameters = ArrayList<RequestParameter>()

            if (StringUtils.EMPTY != requestParameterContent) {
                val parameterPairSeparatorRegex = "&\n|&"
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
    }
}