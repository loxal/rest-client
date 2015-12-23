/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import javafx.scene.Parent
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
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.net.URL
import java.time.Instant
import java.util.*
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.toTypedArray
import kotlin.text.*

const val parameterPairEntrySeparatorRegex = "="

fun assignShortcut(control: Control, keyCodeCombination: KeyCodeCombination, action: Runnable) {
    assignShortcut(control.parent, keyCodeCombination, action)
    control.tooltip = Tooltip("${keyCodeCombination.displayText}")
}

fun assignShortcut(control: Parent, keyCodeCombination: KeyCodeCombination, action: Runnable) {
    control.scene.accelerators.put(keyCodeCombination, action)
}

fun assignShortcutToText(acceleratorContainer: AnchorPane, shortcutTarget: Text, keyCodeCombination: KeyCodeCombination, action: Runnable) {
    assignShortcut(acceleratorContainer, keyCodeCombination, action)
    shortcutTarget.accessibleText = shortcutTarget.text
    shortcutTarget.text = "${shortcutTarget.text} ${keyCodeCombination.displayText}"
}

fun createAppHome(appHomeDirectory: File) {
    if (!appHomeDirectory.exists()) {
        if (appHomeDirectory.mkdirs()) {
            App.LOG.info("$appHomeDirectory created")
        } else {
            App.LOG.warn("$appHomeDirectory creation failed")
        }
    }
}

fun loadFromFile(clientRequest: File): ClientRequest {
    val fileReader = FileReader(clientRequest)

    return ClientRequest.fromRestCode(URL(fileReader.readText()))
}

fun saveAsNew(clientRequest: ClientRequest): Boolean {
    val timestamp = Instant.now().toString()
    val windowsCompatiblePathInfix = timestamp.replace(":", "-")
    val fullFilePath = App.APP_HOME_DIRECTORY + "/" + windowsCompatiblePathInfix + "-save.${App.REST_CODE_NAME}"
    val appHomeDirectory = File(App.APP_HOME_DIRECTORY)
    createAppHome(appHomeDirectory)
    createSaveFile(fullFilePath)

    return save(storage = File(fullFilePath), request = clientRequest)
}

fun save(storage: File, request: ClientRequest): Boolean {
    try {
        val fileWriter = FileWriter(storage)
        fileWriter.write(request.toString())
        fileWriter.close()

        App.LOG.info("${App.SAVE_AS} ${request.name}: $storage")

        return true
    } catch (e: IOException) {
        App.LOG.warn("Could not serialize object: ${e.message}")
    }
    return false
}

fun createSaveFile(fullFilePath: String) {
    val saveFile = File(fullFilePath)
    if (!saveFile.exists()) {
        try {
            if (saveFile.createNewFile()) {
                App.LOG.info("$saveFile created")
            } else {
                App.LOG.warn("$saveFile creation failed")
            }
        } catch (e: IOException) {
            App.LOG.warn(e.message)
        }
    }
}

fun extractHeaderData(rawHeaderData: String): Headers {
    return ClientRequest.toHeaders(rawHeaderData)
}

fun extractRequestParameters(requestParameterContent: String): List<RequestParameter> {
    val requestParameters = ArrayList<RequestParameter>()

    if (!requestParameterContent.isEmpty()) {
        val parameterPairSeparatorRegex = "&${Constant.lineBreak}|&"
        val parameterPairs = requestParameterContent.split(parameterPairSeparatorRegex.toRegex()).toTypedArray()
        parameterPairs.forEach { parameterPair ->
            val parameterPairEntry = parameterPair.trim().split(parameterPairEntrySeparatorRegex.toRegex()).toTypedArray()
            val parameterNameIdx = 0
            val parameterValueIdx = 1
            requestParameters.add(RequestParameter(parameterPairEntry[parameterNameIdx], parameterPairEntry[parameterValueIdx]))
        }
    }

    return requestParameters
}

fun isFormMediaType(request: ClientRequest) = request.headers[HttpHeaders.CONTENT_TYPE] != null && request.headers[HttpHeaders.CONTENT_TYPE]?.get(0).toString().equals(MediaType.APPLICATION_FORM_URLENCODED)

fun toForm(payload: String): MultivaluedMap<String, String> {
    val formData: MultivaluedMap<String, String> = MultivaluedHashMap()
    payload.split("&".toRegex()).toTypedArray().forEach { pair ->
        if (!pair.isEmpty()) {
            val (key, value) = pair.split(parameterPairEntrySeparatorRegex.toRegex()).toTypedArray()
            formData.putSingle(key, value)
        }
    }

    return ImmutableMultivaluedMap(formData)
}

fun formatJson(json: String): String {
    try {
        val jsonElement = JsonParser().parse(json)
        if (jsonElement.equals(JsonNull.INSTANCE))
            return ""
        else
            return GsonBuilder().setPrettyPrinting().create().toJson(jsonElement)
    } catch (e: JsonSyntaxException) {
        App.LOG.warn("${e.cause?.message} - ${e.message}")
        return json
    }
}

// TODO test this
fun applyHeaderInfo(headers: Headers, request: Invocation.Builder): Invocation.Builder {
    fun showSingleListEntry(entry: Map.Entry<String, List<Any>>): String = if (entry.value.size.equals(1)) entry.value[0] as String else entry.value.toString()
    headers.forEach { entry -> request.header(entry.key, showSingleListEntry(entry)) }

    return request
}

fun applyUrlRequestParameters(webTarget: WebTarget, requestParameters: List<RequestParameter>): WebTarget {
    var target = webTarget

    requestParameters.forEach { requestParameter ->
        target = target.queryParam(requestParameter.paramName, requestParameter.paramValue)
    }

    return target
}