/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import javafx.scene.control.Control
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import net.loxal.client.rest.model.ClientRequest
import net.loxal.client.rest.model.Constant
import net.loxal.client.rest.model.Headers
import net.loxal.client.rest.model.RequestParameter
import org.glassfish.jersey.internal.util.collection.ImmutableMultivaluedMap
import java.io.*
import java.lang
import java.time.Instant
import java.util.ArrayList
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap

final class Util {
    companion object {
        val parameterPairEntrySeparatorRegex = "="

        final fun assignShortcut(control: Control, keyCodeCombination: KeyCodeCombination, action: Runnable) {
            control.getScene().getAccelerators().put(keyCodeCombination, action)
            control.setTooltip(Tooltip("${keyCodeCombination.getDisplayText()}"))
        }

        final fun assignShortcutToText(acceleratorContainer: AnchorPane, shortcutTarget: Text, keyCodeCombination: KeyCodeCombination, action: Runnable) {
            shortcutTarget.setAccessibleText(shortcutTarget.getText())
            acceleratorContainer.getScene().getAccelerators().put(keyCodeCombination, action)
            shortcutTarget.setText("${shortcutTarget.getText()} ${keyCodeCombination.getDisplayText()}")
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

        final fun saveAsNew(clientRequest: ClientRequest): Boolean {
            val timestamp = Instant.now().toString()
            val windowsCompatiblePathInfix = timestamp.replace(":", "-")
            val fullFilePath = App.APP_HOME_DIRECTORY + "/" + windowsCompatiblePathInfix + "-save.serialized"
            val appHomeDirectory = File(App.APP_HOME_DIRECTORY)
            Util.createAppHome(appHomeDirectory)
            Util.createSaveFile(fullFilePath)

            return save(storage = File(fullFilePath), request = clientRequest)
        }

        final fun save(storage: File, request: ClientRequest): Boolean {
            try {
                FileOutputStream(storage).use { fileOutputStream ->
                    ObjectOutputStream(fileOutputStream).use { objectOutputStream ->
                        objectOutputStream.writeObject(request)
                        App.LOG.info("${App.SAVE_AS} ${request.name}: $storage")

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
                parameterPairs.forEach { parameterPair ->
                    val parameterPairEntry = parameterPair.trim().split(parameterPairEntrySeparatorRegex)
                    val parameterNameIdx = 0
                    val parameterValueIdx = 1
                    requestParameters.add(RequestParameter(parameterPairEntry[parameterNameIdx], parameterPairEntry[parameterValueIdx]))
                }
            }

            return requestParameters
        }

        final fun isFormMediaType(request: ClientRequest) = request.headers.get(HttpHeaders.CONTENT_TYPE) != null && request.headers.get(HttpHeaders.CONTENT_TYPE).get(0).toString().equals(MediaType.APPLICATION_FORM_URLENCODED)

        final fun toForm(payload: String): MultivaluedMap<String, String> {
            val formData: MultivaluedMap<String, String> = MultivaluedHashMap()
            payload.split("&").forEach { pair ->
                val (key, value) = pair.split(parameterPairEntrySeparatorRegex)
                formData.putSingle(key, value)
            }

            return ImmutableMultivaluedMap(formData)
        }

        final fun formatJson(json: String): String {
            try {
                val jsonElement = JsonParser().parse(json)
                if (jsonElement.equals(JsonNull.INSTANCE))
                    return ""
                else
                    return GsonBuilder().setPrettyPrinting().create().toJson(jsonElement)
            } catch (e: JsonSyntaxException) {
                App.LOG.warning("${e.getCause()?.getMessage()} - ${e.getMessage()}")
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