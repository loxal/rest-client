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
import javafx.scene.layout.HBox
import javafx.scene.text.Text
import net.loxal.client.rest.model.ClientRequest
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
import java.util.logging.Level
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
    private var findContainer: HBox = FindContainer()
    FXML
    private var findNext: Button = Button()
    FXML
    private var httpMethods: ComboBox<Text> = ComboBox()
    FXML
    private var find: TextField = TextField()
    FXML
    private var endpointUrl: TextField = TextField(App.SAMPLE_URL.toString())
    FXML
    private var requestHeaders: TextArea = TextArea()
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
    FXML
    private var menuBar: MenuBar = MenuBar()
    FXML
    private var findInResponse: TextField = TextField()

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
        Util.assignShortcut(requestPerformer, KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN), Runnable { requestPerformer.fire() })
        Util.assignShortcut(requestHeaders, KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN), Runnable { requestHeaders.requestFocus() })
        Util.assignShortcut(requestParameterData, KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN), Runnable { requestParameterData.requestFocus() })
        Util.assignShortcut(requestBody, KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN), Runnable { requestBody.requestFocus() })
        Util.assignShortcut(responseHeaders, KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN), Runnable { responseHeaders.requestFocus() })
        Util.assignShortcut(responseBody, KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN), Runnable { responseBody.requestFocus() })
        Util.assignShortcut(queryTable, KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN), Runnable { queryTable.requestFocus() })
        Util.assignShortcut(requestDeleter, KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.SHORTCUT_DOWN), Runnable { requestDeleter.fire() })
        Util.assignShortcut(requestSaver, KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), Runnable { requestSaver.fire() })
        Util.assignShortcut(requestDuplicator, KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN), Runnable { requestDuplicator.fire() })
        Util.assignShortcut(find, KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), Runnable { find.requestFocus() })

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
        menuBar.setUseSystemMenuBar(true)
    }

    FXML
    private fun findInResponse() {
        findNextOccurrence()

        fun focus() {
            findInResponse.requestFocus()
            findContainer.setVisible(true)
        }
        focus()

        findInResponse.setOnKeyReleased { keyEvent ->
            responseBody.deselect()
            findNextOccurrence()
        }
    }

    private fun findNextOccurrence() {
        val text = responseBody.getText().toLowerCase()
        val search = findInResponse.getText().toLowerCase()

        val nextOccurrence = text.indexOf(search, FindContainer.findNextFrom)
        val found = nextOccurrence != none && nextOccurrence != 0
        if (found) {
            val selectionRange = nextOccurrence + search.length()
            responseBody.selectRange(selectionRange, nextOccurrence)
            FindContainer.findNextFrom = selectionRange
        } else {
            FindContainer.findNextFrom = 0
        }
    }

    FXML
    private fun findNext() {
        responseBody.deselect()
        findNext.requestFocus()
        findNextOccurrence()
    }

    private fun reloadRequestBackup() {
        clientRequestsBackup.clear()
        clientRequestsBackup.addAll(clientRequests)
    }

    private fun enableFinder() {
        Util.assignShortcut(findContainer, KeyCodeCombination(KeyCode.ESCAPE), Runnable { rootContainer.requestFocus(); findContainer.setVisible(false) })
        Util.assignShortcut(findNext, KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN), Runnable { findNext.fire() })

        find.setOnKeyReleased { keyEvent ->
            resetFind()
            populateFindings()
        }
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
     * This is a workaround as proper rendering for combo box items is not working yet.
     */
    FXML
    private fun refillComboBoxItems() {
        httpMethods.setItems(null)
        httpMethods.setItems(httpMethodsTexts)
    }

    FXML
    private fun saveRequest() {
        updateEndpoint()

        val viewSelection: TableView.TableViewSelectionModel<ClientRequest> = queryTable.getSelectionModel()
        val selectedRequestIndex = viewSelection.getSelectedIndex()
        if (selectedRequestIndex == none) {
            saveNewRequest(viewSelection)
        } else {
            val requestName: String = viewSelection.getSelectedItem()!!.name
            val clientRequest = buildRequest(requestName)

            val fileLocation = files.get(selectedRequestIndex)
            if (Util.save(storage = fileLocation, request = clientRequest)) loadSavedRequests()

            postSaveAction(requestName, selectedRequestIndex, viewSelection)
        }
    }

    FXML
    private fun duplicateRequest() {
        updateEndpoint()
        val viewSelection: TableView.TableViewSelectionModel<ClientRequest> = queryTable.getSelectionModel()
        saveNewRequest(viewSelection)
    }

    FXML
    private fun applyCurl() {
        if (App.properties.getProperty("feature.applyCurlCommand").toBoolean()) {
            val fromCurlCommand = ClientRequest.fromCurlCliCommand(curlCommand.getText())
            if (fromCurlCommand.name.contains("Valid")) {
                applyRequest(fromCurlCommand)
                showNotification(Level.INFO, "curl command applied ${Instant.now()}")
            } else {
                showNotification(Level.WARNING, "Malformed curl command could not be applied ${Instant.now()}")
            }
        }
    }

    private fun applyRequest(request: ClientRequest) {
        this.request = request

        endpointUrl.setText(request.url.toString())
        requestBody.setText(request.body)
        requestHeaders.setText(request.headers.toString())
        setMethodInUi(request.method)
    }

    private fun saveNewRequest(viewSelection: TableView.TableViewSelectionModel<ClientRequest>) {
        val localTimestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val verboseRequestName = "$localTimestamp ${request.url.getHost()}${request.url.getPath()} ${request.method}"
        val selectedRequest: ClientRequest? = viewSelection.getSelectedItem()
        val requestName: String = if (selectedRequest identityEquals null) verboseRequestName else "${selectedRequest!!.name} ∆"
        val clientRequest = buildRequest(requestName)

        if (Util saveAsNew clientRequest) loadSavedRequests()

        val firstItem = 0
        postSaveAction(requestName, firstItem, viewSelection)
    }

    private fun buildRequest(requestName: String): ClientRequest {
        val clientRequest = ClientRequest.Builder(requestName)
                .method(request.method)
                .url(request.url)
                .body(requestBody.getText())
                .headers(ClientRequest.toHeaders(requestHeaders.getText()))
                .build()
        return clientRequest
    }

    private fun postSaveAction(requestName: String, selectedRequestIndex: Int, viewSelection: TableView.TableViewSelectionModel<ClientRequest>) {
        //        viewSelection.select(selectedRequestIndex)
        //        reloadRequestBackup()
        showNotification(Level.INFO, "“${requestName}” saved ${Instant.now()}")
    }

    FXML
    private fun doRequest() {
        updateEndpoint()

        if (validEndpoint) {
            clearPreviousResponse()
            startRequest = Instant.now()
            try {
                val response: Response
                when (request.method) {
                    HttpMethod.GET -> response = getRequest()
                    HttpMethod.POST -> response = postRequest()
                    HttpMethod.PUT -> response = putRequest()
                    HttpMethod.DELETE -> response = deleteRequest()
                    HttpMethod.HEAD -> response = headRequest()
                    HttpMethod.OPTIONS -> response = optionsRequest()
                    else -> {
                        showNotification(Level.SEVERE, "${request.method} is not assigned.")
                        response = Response.status(Response.Status.NOT_IMPLEMENTED).build()
                    }
                }

                showResponseHeaders(response)
                showStatus(response)
                showResponseBody(response)
            } catch(e: ProcessingException) {
                showNotification(Level.SEVERE, "${e.getMessage()}")
            }
        }
    }

    private fun prepareRequest(): Invocation.Builder {
        val target = Util.applyUrlRequestParameters(client.target(request.url.toString()),
                Util.extractRequestParameters(declareRequestParameters()))

        return Util.applyHeaderInfo(Util.extractHeaderData(requestHeaders.getText()), target.request())
    }

    FXML
    private fun updateEndpoint() {
        if (endpointUrl.getText().isEmpty()) {
            showNotification(Level.INFO, "Endpoint URL required")
            validEndpoint = false
            return
        }

        try {
            val targetUrl: URL = URL(endpointUrl.getText())
            val selectedHttpMethod: String? = httpMethods.getSelectionModel().getSelectedItem()?.getAccessibleText()
            request = ClientRequest.Builder("[Current Request]")
                    .method(if (selectedHttpMethod === null) HttpMethod.GET else selectedHttpMethod)
                    .body(requestBody.getText())
                    .headers(ClientRequest.toHeaders(requestHeaders.getText()))
                    .url(targetUrl)
                    .build()
            updateCurlCliCommand()
        } catch (e: MalformedURLException) {
            showNotification(Level.SEVERE, "Invalid endpoint URL: ${e.getMessage()}")
            validEndpoint = false
            return
        }

        requestParameterData.setText(request.url.getQuery())

        requestParameterData.fireEvent(ActionEvent())
        endpointUrl.fireEvent(ActionEvent())
        validEndpoint = true
    }

    private fun showNotification(level: Level, message: String) {
        when (level) {
            Level.INFO -> App.LOG.info(message)
            Level.WARNING -> App.LOG.warn(message)
            else -> App.LOG.error(message)
        }
        notification.setText(message)
    }

    private fun postRequest(): Response {
        val response: Response
        if (Util.isFormMediaType(request)) {
            response = prepareRequest().post(Entity.form(Util.toForm(request.body)))
        } else
            response = prepareRequest().post(Entity.json<String>(request.body))

        return response
    }

    private fun getRequest() = prepareRequest().get()

    private fun putRequest() = prepareRequest().put(Entity.json<String>(request.body))

    private fun deleteRequest() = prepareRequest().delete()

    private fun headRequest() = prepareRequest().head()

    private fun optionsRequest() = prepareRequest().options()

    private fun showResponseHeaders(response: Response) {
        response.getHeaders().forEach { header ->
            responseHeaders.appendText("${Headers.toString(entry = header, lineBreak = true)}")
        }
    }

    private fun showResponseBody(response: Response) {
        val formattedResponse = Util.formatJson(response.readEntity(javaClass<String>()))
        responseBody.appendText(formattedResponse)
    }

    private fun declareRequestParameters() =
            if (null identityEquals requestParameterData.getText())
                ""
            else
                requestParameterData.getText()

    FXML
    private fun clearPreviousResponse() {
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
    private fun deleteSavedRequest() {
        val selectedRequest = queryTable.getSelectionModel().getSelectedIndex()

        deleteSavedRequestFile()
        loadSavedRequests()

        queryTable.getSelectionModel().select(selectedRequest)
    }

    private fun deleteSavedRequestFile() {
        val selectedIndex = queryTable.getSelectionModel().getSelectedIndex()
        if (selectedIndex != none) {
            val fileToDelete: File = files.get(selectedIndex)
            if (fileToDelete.delete()) {
                App.LOG.info("Saved request deleted: $fileToDelete")
            } else {
                App.LOG.warn("Saved request not deleted: $fileToDelete")
            }
        }
    }

    FXML
    private fun loadSavedRequest() {
        val selectedRequest = queryTable.getSelectionModel().getSelectedItem()
        if (selectedRequest != null) {
            request = selectedRequest

            setMethodInUi(request.method)
            requestHeaders.setText(request.headers.toStringColumn())
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
            else -> showNotification(Level.SEVERE, "HTTP method has no equivalent in UI.")
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
        private val none = -1
    }
}
