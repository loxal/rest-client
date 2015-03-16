/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.GsonBuilder
import net.loxal.client.rest.model.RequestParameter
import java.util.ArrayList
import net.loxal.client.rest.model.Headers
import java.io.File
import java.lang
import java.io.IOException
import javafx.scene.control.Control
import javafx.scene.input.KeyCodeCombination
import javafx.scene.control.Tooltip
import javax.ws.rs.client.WebTarget
import javax.ws.rs.client.Invocation
import net.loxal.client.rest.model.ClientRequest
import net.loxal.client.rest.model.Constant
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.io.InvalidClassException
import java.io.WriteAbortedException
import java.time.Instant
import com.google.gson.JsonNull

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

        final fun loadFromFile(clientRequest: File): ClientRequest {
            FileInputStream(clientRequest).use { fileInputStream ->
                ObjectInputStream(fileInputStream).use { objectInputStream ->
                    try {
                        return objectInputStream.readObject() as ClientRequest
                    } catch(e: ClassCastException) {
                        App.LOG.severe("$e")
                    } catch(e: InvalidClassException) {
                        App.LOG.severe("$e")
                    } catch(e: WriteAbortedException) {
                        App.LOG.severe("$e")
                    }
                }
            }

            throw RuntimeException("Could not load ${clientRequest}")
        }

        final fun saveToFile(clientRequest: ClientRequest): Boolean {
            val fullFilePath = App.APP_HOME_DIRECTORY + "/" + Instant.now() + "-save.serialized"
            val appHomeDirectory = File(App.APP_HOME_DIRECTORY)
            Util.createAppHome(appHomeDirectory)
            try {
                FileOutputStream(fullFilePath).use { fileOutputStream ->
                    ObjectOutputStream(fileOutputStream).use { objectOutputStream ->
                        Util.createSaveFile(fullFilePath)
                        objectOutputStream.writeObject(clientRequest)
                        App.LOG.info("${App.SAVE_AS} ${clientRequest.name}: $fullFilePath")

                        return true
                    }
                }
            } catch (e: IOException) {
                App.LOG.severe("Could not serialize object: ${e.getMessage()}")
            }
            return false
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

        final fun extractHeaderData(rawHeaderData: String): Headers {
            return ClientRequest.toHeaders(rawHeaderData)
        }

        final fun extractRequestParameters(requestParameterContent: String): List<RequestParameter> {
            val requestParameters = ArrayList<RequestParameter>()

            if (!requestParameterContent.isEmpty()) {
                val parameterPairSeparatorRegex = "&${Constant.lineBreak}|&"
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
            try {
                val jsonElement = JsonParser().parse(json)
                if (jsonElement.equals(JsonNull.INSTANCE))
                    return ""
                else
                    return GsonBuilder().setPrettyPrinting().create().toJson(jsonElement)
            } catch (e: JsonSyntaxException) {
                App.LOG.warning(e.getMessage())
                App.LOG.warning(e.getCause().toString())
                return json
            }
        }

        // TODO test this
        final fun applyHeaderInfo(headers: Headers, request: Invocation.Builder): Invocation.Builder {
            fun showSingleListEntry(entry: Map.Entry<String, List<Any>>): String = if (entry.value.size().equals(1)) entry.value.get(0) as String else entry.value.toString()
            headers.forEach { entry -> request.header(entry.key, showSingleListEntry(entry)) }

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