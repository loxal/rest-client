/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import com.sun.javafx.collections.ImmutableObservableList
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import net.loxal.client.rest.model.ClientRequest
import net.loxal.client.rest.model.Constant
import net.loxal.client.rest.model.Headers
import org.glassfish.jersey.client.ClientProperties
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.net.MalformedURLException
import java.net.URL
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.ResourceBundle
import javax.ws.rs.HttpMethod
import javax.ws.rs.ProcessingException
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.Response

private class Controller : Initializable {
    private var validEndpoint: Boolean = false
    private val files: ObservableList<File> = FXCollections.observableArrayList<File>()
    private val clientRequests = FXCollections.observableArrayList<ClientRequest>()
    private val clientRequestsBackup = FXCollections.observableArrayList<ClientRequest>()

    FXML
    private var httpMethods: ComboBox<Text> = ComboBox()
    FXML
    private var find: TextField = TextField()
    FXML
    private var endpointUrl: TextField = TextField(App.SAMPLE_URL.toString())
    FXML
    private var requestHeaderData: TextArea = TextArea()
    FXML
    private var responseHeaders: TextArea = TextArea()
    FXML
    private var curlCommand: TextArea = TextArea()
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
    private var rootContainer = AnchorPane()
    FXML
    private var queryTable: TableView<ClientRequest> = TableView()
    FXML
    private var requestColumn: TableColumn<ClientRequest, String> = TableColumn()
    FXML
    private var requestDeleter: Button = Button()
    FXML
    private var requestSaver: Button = Button()
    FXML
    private var requestDuplicator: Button = Button()
    FXML
    private var requestBody: TextArea = TextArea()
    FXML
    private var responseBody: TextArea = TextArea()

    private var request: ClientRequest = ClientRequest.Builder("[Init Request]").build()
    private var startRequest: Instant = Instant.now()

    private val getMethod = Text(HttpMethod.GET)
    private val postMethod = Text(HttpMethod.POST)
    private val deleteMethod = Text(HttpMethod.DELETE)
    private val putMethod = Text(HttpMethod.PUT)
    private val optionsMethod = Text(HttpMethod.OPTIONS)
    private val headMethod = Text(HttpMethod.HEAD)
    private val httpMethodsTexts = ImmutableObservableList(getMethod, postMethod, deleteMethod, putMethod, headMethod, optionsMethod)

    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {
        updateEndpoint()
        loadSavedRequests()
    }

    fun initAccelerators() {
        Util.assignShortcut(endpointUrl, KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN), Runnable { endpointUrl.requestFocus() })
        Util.assignShortcut(clearButton, KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN), Runnable { clearButton.fire() })
        Util.assignShortcut(requestPerformer, KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN), Runnable { requestPerformer.fire() })
        Util.assignShortcut(requestHeaderData, KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN), Runnable { requestHeaderData.requestFocus() })
        Util.assignShortcut(requestParameterData, KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN), Runnable { requestParameterData.requestFocus() })
        Util.assignShortcut(requestBody, KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN), Runnable { requestBody.requestFocus() })
        Util.assignShortcut(responseHeaders, KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN), Runnable { responseHeaders.requestFocus() })
        Util.assignShortcut(responseBody, KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN), Runnable { responseBody.requestFocus() })
        Util.assignShortcut(queryTable, KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN), Runnable { queryTable.requestFocus() })
        Util.assignShortcut(requestDeleter, KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.SHORTCUT_DOWN), Runnable { requestDeleter.fire() })
        Util.assignShortcut(requestSaver, KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), Runnable { requestSaver.fire() })
        Util.assignShortcut(requestDuplicator, KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN), Runnable { requestDuplicator.fire() })
        Util.assignShortcut(find, KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN), Runnable { find.requestFocus() })

        fun initHttpMethods() {
            httpMethods.setItems(httpMethodsTexts)
            httpMethods.getSelectionModel().select(0)
            Util.assignShortcutToText(rootContainer, getMethod, KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.GET) })
            Util.assignShortcutToText(rootContainer, postMethod, KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.POST) })
            Util.assignShortcutToText(rootContainer, deleteMethod, KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.DELETE) })
            Util.assignShortcutToText(rootContainer, putMethod, KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.PUT) })
            Util.assignShortcutToText(rootContainer, headMethod, KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.HEAD) })
            Util.assignShortcutToText(rootContainer, optionsMethod, KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.OPTIONS) })
        }
        initHttpMethods()

        setShortcutForArrowKeySelection()

        reloadRequestBackup()

        enableFinder()
    }

    private fun reloadRequestBackup() {
        clientRequestsBackup.clear()
        clientRequestsBackup.addAll(clientRequests)
    }

    private fun enableFinder() =
            find.setOnKeyReleased { keyEvent ->
                resetFind()
                populateFindings()
            }

    private fun populateFindings() {
        val clientRequestModelsForSearch = clientRequests.copyToArray()
        clientRequests.clear()
        clientRequestModelsForSearch.forEach { savedRequest ->
            if (found(savedRequest)) {
                clientRequests.add(savedRequest)
            }
        }
    }

    private fun found(savedRequest: ClientRequest) =
            savedRequest.name.toLowerCase().contains(find.getText().toLowerCase())


    private fun resetFind() {
        clientRequests.clear()
        clientRequests.addAll(clientRequestsBackup)
    }

    private fun setShortcutForArrowKeySelection() =
            queryTable.setOnKeyReleased { keyEvent ->
                if (keyEvent.getCode().equals(KeyCode.UP).or(keyEvent.getCode().equals(KeyCode.DOWN)))
                    loadSavedRequest()
            }


    /**
     * This is a workaround as proper rendering for combobox items is not working yet.
     */
    FXML
    private fun refillComboboxItems() {
        httpMethods.setItems(null)
        httpMethods.setItems(httpMethodsTexts)
    }

    FXML
    private fun saveRequest() {
        updateEndpoint()

        val selectedRequest: ClientRequest? = queryTable.getSelectionModel().getSelectedItem()
        val selectedRequestIndex = queryTable.getSelectionModel().getSelectedIndex()
        val fileLocation = files.get(selectedRequestIndex)

        val requestName: String = selectedRequest!!.name
        val clientRequest = ClientRequest.Builder(requestName)
                .method(request.method)
                .url(request.url)
                .body(requestBody.getText())
                .headers(ClientRequest.toHeaders(requestHeaderData.getText()))
                .build()

        if (Util.save(storage = fileLocation, request = clientRequest)) loadSavedRequests()

        val selectSavedRequest = { queryTable.getSelectionModel().select(selectedRequestIndex) }
        selectSavedRequest()

        reloadRequestBackup()
    }

    FXML
    private fun duplicateRequest() {
        updateEndpoint()

        val localTimestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val verboseRequestName = "$localTimestamp ${request.url.getHost()}${request.url.getPath()} ${request.method}"
        val selectedRequest: ClientRequest? = queryTable.getSelectionModel().getSelectedItem()

        val requestName: String = if (selectedRequest identityEquals null) verboseRequestName else "${selectedRequest!!.name} ∆"
        val clientRequest = ClientRequest.Builder(requestName)
                .method(request.method)
                .url(request.url)
                .body(requestBody.getText())
                .headers(ClientRequest.toHeaders(requestHeaderData.getText()))
                .build()

        if (Util saveAsNew clientRequest) loadSavedRequests()

        val selectFirstSavedRequest = { queryTable.getSelectionModel().select(0) }
        selectFirstSavedRequest()

        reloadRequestBackup()

    }

    FXML
    private fun doRequest() {
        updateEndpoint()

        if (validEndpoint) {
            cleanupPreviousResponse()
            startRequest = Instant.now()
            when (request.method) {
                HttpMethod.GET -> doGetRequest()
                HttpMethod.POST -> doPostRequest()
                HttpMethod.PUT -> doPutRequest()
                HttpMethod.DELETE -> doDeleteRequest()
                HttpMethod.HEAD -> doHeadRequest()
                HttpMethod.OPTIONS -> doOptionsRequest()
                else -> App.LOG.severe("${request.method} is not assigned.")
            }
        }
    }

    private fun prepareRequest(): Invocation.Builder {
        val target = Util.applyUrlRequestParameters(client.target(request.url.toString()),
                Util.extractRequestParameters(declareRequestParameters()))

        return Util.applyHeaderInfo(Util.extractHeaderData(requestHeaderData.getText()), target.request())
    }

    FXML
    private fun updateEndpoint() {
        if (endpointUrl.getText().isEmpty()) {
            showNotification("Endpoint URL required")
            validEndpoint = false
            return
        }

        try {
            val targetUrl: URL = URL(endpointUrl.getText())
            val selectedHttpMethod: String? = httpMethods.getSelectionModel().getSelectedItem()?.getAccessibleText()
            request = ClientRequest.Builder("[Current Request]")
                    .method(if (selectedHttpMethod === null) HttpMethod.GET else selectedHttpMethod)
                    .body(requestBody.getText())
                    .headers(ClientRequest.toHeaders(requestHeaderData.getText()))
                    .url(targetUrl)
                    .build()
            updateCurlCliCommand()
        } catch (e: MalformedURLException) {
            showNotification("Invalid endpoint URL: ${e.getMessage()}")
            validEndpoint = false
            return
        }

        requestParameterData.setText(request.url.getQuery())

        requestParameterData.fireEvent(ActionEvent())
        endpointUrl.fireEvent(ActionEvent())
        validEndpoint = true
    }

    private fun showNotification(message: String) {
        App.LOG.info(message)
        notification.setText(message)
    }

    private fun doPostRequest() {
        val response: Response
        if (Util.isFormMediaType(request)) {
            response = prepareRequest().post(Entity.form(Util.toForm(request.body)))
        } else
            response = prepareRequest().post(Entity.json<String>(request.body))

        val responsePayload = Util.formatJson(response.readEntity<String>(javaClass<String>()))
        if (response.getStatus() == Response.Status.CREATED.getStatusCode() && response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            val stringHeaders = response.getHeaders()
            stringHeaders.entrySet().forEach { header ->
                responseHeaders.appendText("${header.getKey()}: ${header.getValue()}${Constant.lineBreak}")
            }
        }
        responseBody.appendText(responsePayload)

        showResponseHeaders(response)
        showStatus(response)
    }

    private fun doGetRequest() {
        try {
            val getResponse = prepareRequest().get()

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
            responseHeaders.appendText("${Headers.toString(entry = header, lineBreak = true)}")
        }
    }

    private fun doPutRequest() {
        try {
            val response = prepareRequest().put(Entity.json<String>(request.body))

            val responsePayload = Util.formatJson(response.readEntity<String>(javaClass<String>()))
            responseBody.appendText(responsePayload)

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

    private fun declareRequestParameters() =
            if (null identityEquals requestParameterData.getText())
                ""
            else
                requestParameterData.getText()

    FXML
    private fun cleanupPreviousResponse() {
        responseHeaders.clear()
        responseBody.clear()
        responseStatus.setText("")
        notification.setText("")
    }

    private fun loadSavedRequests() {
        files.clear()
        clientRequests.clear()

        val appHomeDirectory = File(App.APP_HOME_DIRECTORY)
        Util.createAppHome(appHomeDirectory)

        appHomeDirectory.listFiles().toLinkedList().sortDescending().forEach { file ->
            files.add(file)
            clientRequests.add(Util.loadFromFile(file))
        }

        requestColumn.setCellValueFactory(PropertyValueFactory<ClientRequest, String>("name"))
        requestColumn.setCellFactory(TextFieldTableCell.forTableColumn<ClientRequest>())

        onEditClientRequestListener()
        queryTable.setItems(clientRequests)
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
            requestHeaderData.setText(request.headers.toStringColumn())
            requestParameterData.setText(request.url.getQuery())
            requestBody.setText(request.body)
            endpointUrl.setText(request.url.toString())
        }
    }

    private fun setMethodInUi(method: String) {
        when (method) {
            HttpMethod.GET -> httpMethods.getSelectionModel().select(0)
            HttpMethod.POST -> httpMethods.getSelectionModel().select(1)
            HttpMethod.DELETE -> httpMethods.getSelectionModel().select(2)
            HttpMethod.PUT -> httpMethods.getSelectionModel().select(3)
            HttpMethod.HEAD -> httpMethods.getSelectionModel().select(4)
            HttpMethod.OPTIONS -> httpMethods.getSelectionModel().select(5)
        }
    }

    private fun doHeadRequest() {
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

    private fun doOptionsRequest() {
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

    private fun showStatus(response: Response) {
        val requestDuration = Instant.now().minusMillis(startRequest.toEpochMilli()).toEpochMilli()
        val responseInfo = "Time: ${Instant.now()}\nStatus: ${response.getStatusInfo().getStatusCode()} ${response.getStatusInfo().getReasonPhrase()} in $requestDuration ms"
        responseStatus.setText(responseInfo)
        responseStatus.setTooltip(Tooltip(response.getStatusInfo().getFamily().name()))
    }

    init {
        client.property(ClientProperties.CONNECT_TIMEOUT, 500)
        client.property(ClientProperties.READ_TIMEOUT, 4000)
    }

    private val onEditClientRequestListener = {
        requestColumn.setOnEditCommit({ clientRequest ->
            val newClientRequestName = clientRequest.getNewValue()
            val clientRequestCopy = clientRequest.getTableView().getItems().get(clientRequest.getTablePosition().getRow())
            val clientRequestRenamed = ClientRequest.Builder(newClientRequestName)
                    .url(clientRequestCopy.url)
                    .headers(clientRequestCopy.headers)
                    .body(clientRequestCopy.body)
                    .method(clientRequestCopy.method)
                    .build()
            clientRequest.getTableView().getItems().set(clientRequest.getTablePosition().getRow(), clientRequestRenamed)

            val file = files.get(clientRequest.getTablePosition().getRow());
            FileOutputStream(file).use { fileOutputStream ->
                ObjectOutputStream(fileOutputStream).use { objectOutputStream ->
                    objectOutputStream.writeObject(clientRequest.getTableView().getItems().get(clientRequest.getTablePosition().getRow()))
                    loadSavedRequests()
                    App.LOG.info("${App.SAVE_AS} ${clientRequest.getTableView().getItems().get(clientRequest.getTablePosition().getRow()).name}")
                }
            }
        })
    }

    private val updateCurlCliCommand = { curlCommand.setText(request.toCurlCliCommand()) }

    private companion object {
        private val client = ClientBuilder.newClient()
    }
}
