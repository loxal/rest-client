/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import javafx.fxml.Initializable
import java.io.File
import javafx.collections.ObservableList
import net.loxal.client.rest.model.ClientRequestModel
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.Label
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.scene.control.RadioButton
import javafx.scene.control.TableView
import javafx.scene.control.TableColumn
import javafx.fxml.FXML
import javafx.scene.control.ToggleGroup
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.WebTarget
import net.loxal.client.rest.model.RequestParameter
import java.net.MalformedURLException
import javax.ws.rs.ProcessingException
import net.loxal.client.rest.model.Header
import java.net.URL
import java.util.ResourceBundle
import java.io.IOException
import javafx.collections.FXCollections
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyCodeCombination
import org.apache.commons.lang3.StringUtils
import javax.ws.rs.HttpMethod
import javax.ws.rs.client.ClientBuilder
import org.glassfish.jersey.client.ClientProperties
import javax.ws.rs.core.MediaType
import java.lang
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Response
import java.util.ArrayList
import java.util.HashSet
import com.google.gson.JsonParser
import com.google.gson.GsonBuilder
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import java.util.UUID
import java.io.ObjectOutputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.FileInputStream
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import java.util.logging.Logger
import javafx.scene.control.Tooltip
import javafx.scene.control.Control
import javafx.event.ActionEvent
import java.util.logging.Level
import kotlin.platform.platformStatic
import com.google.gson.JsonSyntaxException
import com.google.gson.JsonElement
import java.time.Instant

class Controller : Initializable {
    private val files: ObservableList<File> = FXCollections.observableArrayList<File>()
    private val clientRequestModels = FXCollections.observableArrayList<ClientRequestModel>()

    FXML
    private var requestUrlChoice: ComboBox<String> = ComboBox()
    FXML
    private var requestHeaderData: TextArea = TextArea()
    FXML
    private var responseHeaders: TextArea = TextArea()
    FXML
    private var notification: Label = Label()
    FXML
    private var responseStatus: Label = Label()
    FXML
    private var requestParameterData: TextArea = TextArea()
    FXML
    private var requestPerformer: Button = Button()
    FXML
    private var clearButton: Button = Button()
    FXML
    private var rootContainer: AnchorPane = AnchorPane()
    FXML
    private var getMethodRadio: RadioButton = RadioButton()
    FXML
    private var queryTable: TableView<ClientRequestModel> = TableView()
    FXML
    private var postMethodRadio: RadioButton = RadioButton()
    FXML
    private var deleteMethodRadio: RadioButton = RadioButton()
    FXML
    private var putMethodRadio: RadioButton = RadioButton()
    FXML
    private var headMethodRadio: RadioButton = RadioButton()
    FXML
    private var optionsMethodRadio: RadioButton = RadioButton()
    FXML
    private var requestColumn: TableColumn<ClientRequestModel, String> = TableColumn()
    FXML
    private var requestDeleter: Button = Button()
    FXML
    private var requestSaver: Button = Button()
    FXML
    private private var requestBody: TextArea = TextArea()
    FXML
    private private var responseBody: TextArea = TextArea()
    FXML
    private private var requestMethod: ToggleGroup = ToggleGroup()

    private var url: URL = URL("https://example.com")

    private var startRequest: Instant = Instant.now()

    private fun createShortcut(control: Control, keyCodeCombination: KeyCodeCombination, action: Runnable) {
        control.getScene().getAccelerators().put(keyCodeCombination, action)
        control.setTooltip(Tooltip("${keyCodeCombination.getDisplayText()}"))
    }

    fun setAccelerators() {
        createShortcut(requestUrlChoice, KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN), Runnable { requestUrlChoice.requestFocus() })
        createShortcut(clearButton, KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN), Runnable { clearButton.fire() })
        createShortcut(requestPerformer, KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN), Runnable { requestPerformer.fire() })
        createShortcut(getMethodRadio, KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { getMethodRadio.fire() })
        createShortcut(postMethodRadio, KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { postMethodRadio.fire() })
        createShortcut(deleteMethodRadio, KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { deleteMethodRadio.fire() })
        createShortcut(putMethodRadio, KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { putMethodRadio.fire() })
        createShortcut(headMethodRadio, KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { headMethodRadio.fire() })
        createShortcut(optionsMethodRadio, KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { optionsMethodRadio.fire() })
        createShortcut(requestHeaderData, KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN), Runnable { requestHeaderData.requestFocus() })
        createShortcut(requestParameterData, KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN), Runnable { requestParameterData.requestFocus() })
        createShortcut(requestBody, KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN), Runnable { requestBody.requestFocus() })
        createShortcut(responseBody, KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN), Runnable { responseBody.requestFocus() })
        createShortcut(responseHeaders, KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN), Runnable { responseHeaders.requestFocus() })
        createShortcut(queryTable, KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN), Runnable { queryTable.requestFocus() })
        createShortcut(requestDeleter, KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.SHORTCUT_DOWN), Runnable { requestDeleter.fire() })
        createShortcut(requestSaver, KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), Runnable { requestSaver.fire() })
    }

    FXML
    private fun doRequest() {
        val selectedRequestMethod = (this.requestMethod.getSelectedToggle() as RadioButton).getText()

        declareUrl()
        cleanupPreviousResponse()
        startRequest = Instant.now()
        when (selectedRequestMethod) {
            HttpMethod.GET -> doGetRequest()
            HttpMethod.POST -> doPostRequest()
            HttpMethod.PUT -> doPutRequest()
            HttpMethod.DELETE -> doDeleteRequest()
            HttpMethod.HEAD -> doHeadRequest()
            HttpMethod.OPTIONS -> doOptionsRequest()
        }
    }

    private fun prepareRequest(): Invocation.Builder {
        val client = ClientBuilder.newClient()

        client.property(ClientProperties.CONNECT_TIMEOUT, 2000)
        client.property(ClientProperties.READ_TIMEOUT, 2000)
        val target = applyUrlRequestParameters(client.target(url.toURI()), extractRequestParameters())
        val request = target.request(MediaType.APPLICATION_JSON_TYPE)

        return applyHeaderInfo(extractHeaderData(), request)
    }

    private fun applyUrlRequestParameters(webTarget: WebTarget, requestParameters: List<RequestParameter>): WebTarget {
        var target = webTarget

        requestParameters.forEach { requestParameter ->
            target = target.queryParam(requestParameter.paramName, requestParameter.paramValue)
        }

        return target
    }

    FXML
    private fun declareUrl() {
        val urlValue: String = if (requestUrlChoice.getValue() == null)
            requestUrlChoice.getPromptText()
        else
            requestUrlChoice.getValue()

        try {
            this.url = URL(urlValue)
            notification.setText("")
        } catch (e: MalformedURLException) {
            val invalidUrlMessage = "Invalid URL: ${e.getMessage()}"
            notification.setText(invalidUrlMessage)
            LOG.info(invalidUrlMessage)
        }

        requestParameterData.setText(this.url.getQuery())

        requestParameterData.fireEvent(ActionEvent())
        requestUrlChoice.fireEvent(ActionEvent())
    }

    private fun doPostRequest() {
        val response = prepareRequest().post(Entity.json<String>(declareRequestBody()))

        if (response.getStatus() == Response.Status.CREATED.getStatusCode() && response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {

            responseBody.appendText(formatJson(response.readEntity<String>(javaClass<String>())))
            val stringHeaders = response.getHeaders()

            for (header in stringHeaders.entrySet()) {
                responseHeaders.appendText(header.getKey() + ": " + header.getValue() + "\n")
            }
        } else {
            responseBody.appendText(response.getStatusInfo().getReasonPhrase())
        }

        showResponseHeaders(response)
        showStatus(response)
    }

    private fun declareRequestBody(): String {
        val reqBody = requestBody.getText()
        if (StringUtils.EMPTY == reqBody) {
            return requestBody.getPromptText()
        } else {
            return requestBody.getText()
        }
    }

    private fun doGetRequest() {
        try {
            val getResponse = prepareRequest().get()
            // TODO support XML
            LOG.setLevel(Level.FINE)
            LOG.fine("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!TEST FINE")
            LOG.log(Level.FINE, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!TEST FINE")

            val responseBodyPayload = formatJson(getResponse.readEntity(javaClass<String>()))
            LOG.info("responseBody.getStyleClass(): ${responseBody.getStyleClass()}")
            responseBody.getStyleClass().add(getResponse.getStatusInfo().getFamily().name())
            responseHeaders.getStyleClass().add(getResponse.getStatusInfo().getFamily().name())

            responseBody.appendText(responseBodyPayload)

            showResponseHeaders(getResponse)
            showStatus(getResponse)
        } catch (e: ProcessingException) {
            LOG.severe(e.getMessage())
            notification.setText(e.getMessage())
        }
    }

    private fun showResponseHeaders(getResponse: Response) {
        getResponse.getHeaders().forEach { header ->
            responseHeaders.appendText("${Header(header.getKey(), header.getValue())} \n")
        }
    }

    private fun doPutRequest() {
        try {

            val response = prepareRequest().put(Entity.json<String>(" {  \"key\" : \"value\" }"))

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                responseBody.appendText(formatJson(response.readEntity<String>(javaClass<String>())) + "OK")
            } else {
                responseBody.appendText(response.getStatusInfo().getReasonPhrase() + "FAILED")
            }
            showResponseHeaders(response)
            showStatus(response)
        } catch (e: ProcessingException) {
            LOG.severe(e.getMessage())
            notification.setText(e.getMessage())
        }

    }

    private fun doDeleteRequest() {
        try {
            val response = prepareRequest().delete()

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                responseBody.appendText(response.getStatusInfo().getReasonPhrase())
            }
            showResponseHeaders(response)
            showStatus(response)
        } catch (e: ProcessingException) {
            LOG.severe(e.getMessage())
            notification.setText(e.getMessage())
        }

    }

    private fun applyHeaderInfo(headers: Set<Header>, request: Invocation.Builder): Invocation.Builder {
        headers.forEach { header -> request.header(header.name, header.value) }

        return request
    }

    private fun extractRequestParameters(): List<RequestParameter> {
        val requestParameters = ArrayList<RequestParameter>()
        val requestParameterContent: String

        requestParameterContent = declareRequestParameters()

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

    private fun declareRequestParameters(): String {
        val requestParameterContent: String

        if (StringUtils.EMPTY == requestParameterData.getText() || null == requestParameterData.getText()) {
            requestParameterContent = StringUtils.EMPTY
        } else {
            requestParameterContent = requestParameterData.getText()
        }
        return requestParameterContent
    }

    private fun extractHeaderData(): Set<Header> {
        val headerNameIdx = 0
        val headerValueIdx = 1
        val headers = HashSet<Header>()

        val rawHeaderData = requestHeaderData.getText()
        if (!rawHeaderData.isEmpty()) {
            for (rawHeaderLine in rawHeaderData.split("\\n")) {
                val headerDataPair = rawHeaderLine.split(":\\s")
                val header = Header(headerDataPair[headerNameIdx], listOf(headerDataPair[headerValueIdx]))
                headers.add(header)
            }
        }

        return headers
    }

    FXML
    private fun cleanupPreviousResponse() {
        responseHeaders.clear()
        responseBody.clear()
    }

    private fun formatJson(json: String): String {
        val jsonElement: JsonElement
        try {
            jsonElement = JsonParser().parse(json)
        } catch (e: JsonSyntaxException) {
            LOG.warning(e.getMessage())
            LOG.warning(e.getCause().toString())
            return json
        }
        return GsonBuilder().setPrettyPrinting().create().toJson(jsonElement)
    }

    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {
        initializeRequestUrlChoice()
        loadSavedRequests()
    }

    private fun initializeRequestUrlChoice() {
        requestUrlChoice.getItems().add("https://api.twitter.com/1.1/trends/place.json?id=676757")
        requestUrlChoice.getSelectionModel().select(0)

        declareUrl()
    }

    FXML
    private fun saveRequest() {
        val requestUrl = requestUrlChoice.getSelectionModel().getSelectedItem()
        val clientRequestModel = ClientRequestModel.Builder(LocalTime.now().format(DateTimeFormatter.ISO_TIME))
                .url(requestUrl).body(requestBody.getText()).headers(requestHeaderData.getText())
                .parameters(requestParameterData.getText()).build()


        val fullFilePath = APP_HOME_DIRECTORY + "/" + UUID.randomUUID() + "-save.serialized"
        val appHomeDirectory = File(APP_HOME_DIRECTORY)
        createAppHome(appHomeDirectory)
        try {
            FileOutputStream(fullFilePath).use { fileOutputStream ->
                ObjectOutputStream(fileOutputStream).use { objectOutputStream ->
                    createSaveFile(fullFilePath)

                    objectOutputStream.writeObject(clientRequestModel)
                    LOG.info("${SAVE_AS} ${clientRequestModel.name}")

                    loadSavedQuery(fullFilePath)
                }
            }
        } catch (e: IOException) {
            LOG.severe("Could not serialize object: ${e.getMessage()}")
        }


        queryTable.getSelectionModel().select(0)
    }

    private fun createAppHome(appHomeDirectory: File) {
        if (!appHomeDirectory.exists()) {
            if (appHomeDirectory.mkdirs()) {
                LOG.info(lang.String.format("%s created", appHomeDirectory))
            } else {
                LOG.severe(lang.String.format("%s creation failed", appHomeDirectory))
            }
        }
    }

    private fun createSaveFile(fullFilePath: String) {
        val saveFile = File(fullFilePath)
        if (!saveFile.exists()) {
            try {
                if (saveFile.createNewFile()) {
                    LOG.info("$saveFile created")
                } else {
                    LOG.severe("$saveFile creation failed")
                }
            } catch (e: IOException) {
                LOG.severe(e.getMessage())
            }

        }
    }

    throws(javaClass<IOException>())
    private fun loadSavedQuery(fullFilePath: String) {
        files.clear()
        try {
            FileInputStream(fullFilePath).use { fileInputStream ->
                ObjectInputStream(fileInputStream).use { objectInputStream ->
                    val clientRequestModel = objectInputStream.readObject() as ClientRequestModel
                    LOG.info("Load request: ${clientRequestModel.name}")
                    loadSavedRequests()
                }
            }
        } catch (e: ClassNotFoundException) {
            LOG.severe(e.getMessage())
        }

    }

    private fun loadSavedRequests() {
        files.clear()
        clientRequestModels.clear()

        val appHomeDirectory = File(APP_HOME_DIRECTORY)
        createAppHome(appHomeDirectory)

        appHomeDirectory.listFiles().forEach {
            file ->
            files.add(file)
            FileInputStream(file).use { fileInputStream ->
                ObjectInputStream(fileInputStream).use { objectInputStream ->
                    val clientRequestModel: ClientRequestModel = objectInputStream.readObject() as ClientRequestModel
                    clientRequestModels.add(clientRequestModel)
                }
            }
        }


        requestColumn.setCellValueFactory(PropertyValueFactory<ClientRequestModel, String>("name"))
        requestColumn.setCellFactory(TextFieldTableCell.forTableColumn<ClientRequestModel>())

        requestColumn.setOnEditCommit({ t ->
            t.getTableView().getItems().get(t.getTablePosition().getRow()).name = t.getNewValue();

            val file = files.get(t.getTablePosition().getRow());

            FileOutputStream(file).use {
                fileOutputStream ->
                ObjectOutputStream(fileOutputStream).use {
                    objectOutputStream ->
                    objectOutputStream.writeObject(t.getTableView().getItems().get(t.getTablePosition().getRow()))
                    loadSavedRequests()
                    LOG.info("${SAVE_AS} ${t.getTableView().getItems().get(t.getTablePosition().getRow()).name}")
                }
            }
        })

        queryTable.setItems(clientRequestModels)
    }

    FXML
    private fun deleteRequest() {
        deleteSavedRequestFile()
        loadSavedRequests()
        queryTable.getSelectionModel().selectFirst()
    }

    private fun deleteSavedRequestFile() {
        val selectedIndex = queryTable.getSelectionModel().getSelectedIndex()
        if (selectedIndex != -1) {
            val fileToDelete: File = files.get(selectedIndex)
            if (fileToDelete.delete()) {
                LOG.info("Saved request deleted: $fileToDelete")
            } else {
                LOG.severe("Saved request not deleted: $fileToDelete")
            }
        }
    }

    FXML
    private fun loadSavedRequest() {
        val selectedRequest = queryTable.getSelectionModel().getSelectedItem()
        if (selectedRequest != null) {
            requestHeaderData.setText(selectedRequest.headers)
            requestParameterData.setText(selectedRequest.parameters)
            requestBody.setText(selectedRequest.body)

            setNewTarget(selectedRequest)
        }
    }

    private fun setNewTarget(selectedRequest: ClientRequestModel) {
        requestUrlChoice.getItems().clear()
        requestUrlChoice.getItems().add(selectedRequest.url)
        initializeRequestUrlChoice()
    }

    class object {
        platformStatic val LOG = Logger.getGlobal()
        platformStatic val SAVE_AS = "Save request as:"
        private val APP_HOME_DIRECTORY = System.getenv("HOME") + "/.loxal/restClient/request"
    }

    fun doHeadRequest() {
        try {
            val response = prepareRequest().head()
            LOG.setLevel(Level.FINE)
            LOG.fine("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!TEST FINE")
            LOG.log(Level.FINE, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!TEST FINE")

            responseBody.getStyleClass().add(response.getStatusInfo().getFamily().name())
            responseHeaders.getStyleClass().add(response.getStatusInfo().getFamily().name())
            responseBody.appendText(response.readEntity(javaClass<String>()))
            showResponseHeaders(response)
            showStatus(response)
        } catch (e: ProcessingException) {
            LOG.severe(e.getMessage())
            notification.setText(e.getMessage())
        }
    }

    fun doOptionsRequest() {
        try {
            val response = prepareRequest().options()

            responseBody.getStyleClass().add(response.getStatusInfo().getFamily().name())
            responseHeaders.getStyleClass().add(response.getStatusInfo().getFamily().name())
            responseBody.appendText(response.readEntity(javaClass<String>()))
            showResponseHeaders(response)
            showStatus(response)
        } catch (e: ProcessingException) {
            LOG.severe(e.getMessage())
            notification.setText(e.getMessage())
        }
    }

    fun showStatus(response: Response) {
        val requestDuration = Instant.now().minusMillis(startRequest.toEpochMilli()).toEpochMilli()
        responseStatus.setText("${response.getStatusInfo().getStatusCode()} ${response.getStatusInfo().getReasonPhrase()} in $requestDuration ms")
        responseStatus.setTooltip(Tooltip(response.getStatusInfo().getFamily().name()))

    }
}