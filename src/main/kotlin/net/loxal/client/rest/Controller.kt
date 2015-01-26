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
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Response
import java.util.UUID
import java.io.ObjectOutputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.FileInputStream
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.control.Tooltip
import javafx.event.ActionEvent
import java.time.Instant

private class Controller : Initializable {
    private val files: ObservableList<File> = FXCollections.observableArrayList<File>()
    private val clientRequestModels = FXCollections.observableArrayList<ClientRequestModel>()

    FXML
    private var requestUrlChoice: ComboBox<String> = ComboBox()
    FXML
    private var requestHeaderData: TextArea = TextArea("")
    FXML
    private var responseHeaders: TextArea = TextArea()
    FXML
    private var notification: Label = Label()
    FXML
    private var responseStatus: Label = Label()
    FXML
    private var requestParameterData: TextArea = TextArea("")
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

    private var request: ClientRequestModel = ClientRequestModel.Builder("[Init Request]")
            .build()

    private var startRequest: Instant = Instant.now()

    fun setAccelerators() {
        Util.createShortcut(requestUrlChoice, KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN), Runnable { requestUrlChoice.requestFocus() })
        Util.createShortcut(clearButton, KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN), Runnable { clearButton.fire() })
        Util.createShortcut(requestPerformer, KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN), Runnable { requestPerformer.fire() })
        Util.createShortcut(getMethodRadio, KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { getMethodRadio.fire() })
        Util.createShortcut(postMethodRadio, KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { postMethodRadio.fire() })
        Util.createShortcut(deleteMethodRadio, KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { deleteMethodRadio.fire() })
        Util.createShortcut(putMethodRadio, KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { putMethodRadio.fire() })
        Util.createShortcut(headMethodRadio, KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { headMethodRadio.fire() })
        Util.createShortcut(optionsMethodRadio, KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { optionsMethodRadio.fire() })
        Util.createShortcut(requestHeaderData, KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN), Runnable { requestHeaderData.requestFocus() })
        Util.createShortcut(requestParameterData, KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN), Runnable { requestParameterData.requestFocus() })
        Util.createShortcut(requestBody, KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN), Runnable { requestBody.requestFocus() })
        Util.createShortcut(responseBody, KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN), Runnable { responseBody.requestFocus() })
        Util.createShortcut(responseHeaders, KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN), Runnable { responseHeaders.requestFocus() })
        Util.createShortcut(queryTable, KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN), Runnable { queryTable.requestFocus() })
        Util.createShortcut(requestDeleter, KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.SHORTCUT_DOWN), Runnable { requestDeleter.fire() })
        Util.createShortcut(requestSaver, KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), Runnable { requestSaver.fire() })

        setShortcutForArrowKeySelection()
    }

    private fun setShortcutForArrowKeySelection() {
        queryTable.setOnKeyReleased { keyEvent ->
            if (keyEvent.getCode().equals(KeyCode.UP).or(keyEvent.getCode().equals(KeyCode.DOWN)))
                loadSavedRequest()
        }
    }

    FXML
    private fun doRequest() {
        declareUrl()

        cleanupPreviousResponse()
        startRequest = Instant.now()
        when (request.method) {
            HttpMethod.GET -> doGetRequest()
            HttpMethod.POST -> doPostRequest()
            HttpMethod.PUT -> doPutRequest()
            HttpMethod.DELETE -> doDeleteRequest()
            HttpMethod.HEAD -> doHeadRequest()
            HttpMethod.OPTIONS -> doOptionsRequest()
        }
    }

    throws(javaClass<IOException>())
    private fun loadSavedQuery(fullFilePath: String) {
        files.clear()
        try {
            FileInputStream(fullFilePath).use { fileInputStream ->
                ObjectInputStream(fileInputStream).use { objectInputStream ->
                    val clientRequestModel = objectInputStream.readObject() as ClientRequestModel
                    App.LOG.info("Load request: ${clientRequestModel.name}")
                    loadSavedRequests()
                }
            }
        } catch (e: ClassNotFoundException) {
            App.LOG.severe(e.getMessage())
        }

    }

    private fun prepareRequest(): Invocation.Builder {
        val client = ClientBuilder.newClient()

        client.property(ClientProperties.CONNECT_TIMEOUT, 2000)
        client.property(ClientProperties.READ_TIMEOUT, 2000)
        val target = applyUrlRequestParameters(client.target(request.url.toString()),
                Util.extractRequestParameters(declareRequestParameters()))
        val request = target.request(MediaType.APPLICATION_JSON_TYPE)

        return applyHeaderInfo(Util.extractHeaderData(requestHeaderData.getText()), request)
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
            request = ClientRequestModel.Builder("[Current Request]")
                    .method((requestMethod.getSelectedToggle() as RadioButton).getText())
                    .url(URL(urlValue))
                    .build()

            notification.setText("")
        } catch (e: MalformedURLException) {
            val invalidUrlMessage = "Invalid URL: ${e.getMessage()}"
            notification.setText(invalidUrlMessage)
            App.LOG.info(invalidUrlMessage)
        }

        requestParameterData.setText(request.url.getQuery())

        requestParameterData.fireEvent(ActionEvent())
        requestUrlChoice.fireEvent(ActionEvent())
    }

    private fun doPostRequest() {
        val response = prepareRequest().post(Entity.json<String>(declareRequestBody()))

        if (response.getStatus() == Response.Status.CREATED.getStatusCode() && response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {

            responseBody.appendText(Util.formatJson(response.readEntity<String>(javaClass<String>())))
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

            val responseBodyPayload = Util.formatJson(getResponse.readEntity(javaClass<String>()))
            responseBody.appendText(responseBodyPayload)

            showResponseHeaders(getResponse)
            showStatus(getResponse)
        } catch (e: ProcessingException) {
            App.LOG.severe(e.getMessage())
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
                responseBody.appendText(Util.formatJson(response.readEntity<String>(javaClass<String>())))
            } else {
                responseBody.appendText(response.getStatusInfo().getReasonPhrase())
            }
            showResponseHeaders(response)
            showStatus(response)
        } catch (e: ProcessingException) {
            App.LOG.severe(e.getMessage())
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
            App.LOG.severe(e.getMessage())
            notification.setText(e.getMessage())
        }

    }

    private fun applyHeaderInfo(headers: Set<Header>, request: Invocation.Builder): Invocation.Builder {
        headers.forEach { header -> request.header(header.name, header.value) }

        return request
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

    FXML
    private fun cleanupPreviousResponse() {
        responseHeaders.clear()
        responseBody.clear()
    }

    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {
        initializeRequestUrlChoice()
        loadSavedRequests()
    }

    private fun initializeRequestUrlChoice() {
        requestUrlChoice.getItems().add(App.SAMPLE_URL)
        requestUrlChoice.getSelectionModel().select(0)

        declareUrl()
    }

    FXML
    private fun saveRequest() {
        declareUrl()

        val requestName = "${request.method}: ${request.url.getHost()}${request.url.getPath()}"
        val clientRequestModel = ClientRequestModel.Builder(requestName)
                .method(request.method)
                .url(request.url)
                .body(requestBody.getText())
                .headers(requestHeaderData.getText())
                .build()

        val fullFilePath = App.APP_HOME_DIRECTORY + "/" + UUID.randomUUID() + "-save.serialized"
        val appHomeDirectory = File(App.APP_HOME_DIRECTORY)
        Util.createAppHome(appHomeDirectory)
        try {
            FileOutputStream(fullFilePath).use { fileOutputStream ->
                ObjectOutputStream(fileOutputStream).use { objectOutputStream ->
                    Util.createSaveFile(fullFilePath)

                    objectOutputStream.writeObject(clientRequestModel)
                    App.LOG.info("${App.SAVE_AS} ${clientRequestModel.name}")

                    loadSavedQuery(fullFilePath)
                }
            }
        } catch (e: IOException) {
            App.LOG.severe("Could not serialize object: ${e.getMessage()}")
        }


        queryTable.getSelectionModel().select(0)
    }

    private fun loadSavedRequests() {
        files.clear()
        clientRequestModels.clear()

        val appHomeDirectory = File(App.APP_HOME_DIRECTORY)
        Util.createAppHome(appHomeDirectory)

        appHomeDirectory.listFiles().forEach { file ->
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
                    App.LOG.info("${App.SAVE_AS} ${t.getTableView().getItems().get(t.getTablePosition().getRow()).name}")
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
                App.LOG.info("Saved request deleted: $fileToDelete")
            } else {
                App.LOG.severe("Saved request not deleted: $fileToDelete")
            }
        }
    }

    FXML
    private fun loadSavedRequest() {
        val selectedRequest = queryTable.getSelectionModel().getSelectedItem()
        if (selectedRequest != null) {
            request = selectedRequest

            setMethodInUi(request.method)
            requestHeaderData.setText(request.headers)
            requestParameterData.setText(request.url.getQuery())
            requestBody.setText(request.body)

            setNewTarget()
        }
    }

    private fun setMethodInUi(method: String) {
        when (method) {
            HttpMethod.GET -> requestMethod.selectToggle(getMethodRadio)
            HttpMethod.POST -> requestMethod.selectToggle(postMethodRadio)
            HttpMethod.PUT -> requestMethod.selectToggle(putMethodRadio)
            HttpMethod.DELETE -> requestMethod.selectToggle(deleteMethodRadio)
            HttpMethod.HEAD -> requestMethod.selectToggle(headMethodRadio)
            HttpMethod.OPTIONS -> requestMethod.selectToggle(optionsMethodRadio)
        }
    }

    private fun setNewTarget() {
        requestUrlChoice.getItems().clear()
        requestUrlChoice.getItems().add(request.url.toString())
        initializeRequestUrlChoice()
    }

    fun doHeadRequest() {
        try {
            val response = prepareRequest().head()

            responseBody.appendText(response.readEntity(javaClass<String>()))
            showResponseHeaders(response)
            showStatus(response)
        } catch (e: ProcessingException) {
            App.LOG.severe(e.getMessage())
            notification.setText(e.getMessage())
        }
    }

    fun doOptionsRequest() {
        try {
            val response = prepareRequest().options()

            responseBody.appendText(response.readEntity(javaClass<String>()))
            showResponseHeaders(response)
            showStatus(response)
        } catch (e: ProcessingException) {
            App.LOG.severe(e.getMessage())
            notification.setText(e.getMessage())
        }
    }

    fun showStatus(response: Response) {
        val requestDuration = Instant.now().minusMillis(startRequest.toEpochMilli()).toEpochMilli()
        responseStatus.setText("${response.getStatusInfo().getStatusCode()} ${response.getStatusInfo().getReasonPhrase()} in $requestDuration ms")
        responseStatus.setTooltip(Tooltip(response.getStatusInfo().getFamily().name()))

    }
}