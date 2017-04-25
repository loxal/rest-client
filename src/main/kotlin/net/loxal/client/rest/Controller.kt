/*
 * Copyright 2017 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import com.sun.javafx.collections.ImmutableObservableList
import javafx.collections.FXCollections
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
import java.io.FileWriter
import java.net.MalformedURLException
import java.net.URL
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Level
import javax.ws.rs.HttpMethod
import javax.ws.rs.ProcessingException
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.Response

internal class Controller : Initializable {
    private var validEndpoint: Boolean = false
    private val requestFiles = FXCollections.observableMap<ClientRequest, File>(LinkedHashMap<ClientRequest, File>())
    private val requests = FXCollections.observableList<ClientRequest>(LinkedList<ClientRequest>())
    private val requestFilesBackup = FXCollections.observableMap<ClientRequest, File>(LinkedHashMap<ClientRequest, File>())

    @FXML
    private var findContainer: HBox = FindContainer()
    @FXML
    private var findNext: Button = Button()
    @FXML
    private var httpMethods: ComboBox<Text> = ComboBox()
    @FXML
    private var findRequest: TextField = TextField()
    @FXML
    private var endpointUrl: TextField = TextField(App.SAMPLE_URL.toString())
    @FXML
    private var requestHeaders: TextArea = TextArea()
    @FXML
    private var responseHeaders: TextArea = TextArea()
    @FXML
    private var curlCommand: TextArea = TextArea()
    @FXML
    private var notification: Label = Label()
    @FXML
    private var responseStatus: Label = Label()
    @FXML
    private var requestParameterData: TextArea = TextArea()
    @FXML
    private var requestPerformer: Button = Button()
    @FXML
    private var rootContainer = AnchorPane()
    @FXML
    private var queryTable: TableView<ClientRequest> = TableView()
    @FXML
    private var requestColumn: TableColumn<ClientRequest, String> = TableColumn()
    @FXML
    private var requestDeleter: Button = Button()
    @FXML
    private var requestSaver: Button = Button()
    @FXML
    private var requestDuplicator: Button = Button()
    @FXML
    private var requestBody: TextArea = TextArea()
    @FXML
    private var responseBody: TextArea = TextArea()
    @FXML
    private var menuBar: MenuBar = MenuBar()
    @FXML
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
        assignShortcut(endpointUrl, KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN), Runnable { endpointUrl.requestFocus() })
        assignShortcut(requestPerformer, KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN), Runnable { requestPerformer.fire() })
        assignShortcut(requestHeaders, KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN), Runnable { requestHeaders.requestFocus() })
        assignShortcut(requestParameterData, KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN), Runnable { requestParameterData.requestFocus() })
        assignShortcut(requestBody, KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN), Runnable { requestBody.requestFocus() })
        assignShortcut(responseHeaders, KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN), Runnable { responseHeaders.requestFocus() })
        assignShortcut(responseBody, KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN), Runnable { responseBody.requestFocus() })
        assignShortcut(queryTable, KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN), Runnable { queryTable.requestFocus() })
        assignShortcut(requestDeleter, KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.SHORTCUT_DOWN), Runnable { requestDeleter.fire() })
        assignShortcut(requestSaver, KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), Runnable { requestSaver.fire() })
        assignShortcut(requestDuplicator, KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN), Runnable { requestDuplicator.fire() })
        assignShortcut(findRequest, KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), Runnable { findRequest.requestFocus() })

        fun initHttpMethods() {
            httpMethods.items = httpMethodsTexts
            httpMethods.selectionModel.select(0)
            assignShortcutToText(rootContainer, getMethod, KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.GET) })
            assignShortcutToText(rootContainer, postMethod, KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.POST) })
            assignShortcutToText(rootContainer, deleteMethod, KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.DELETE) })
            assignShortcutToText(rootContainer, putMethod, KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.PUT) })
            assignShortcutToText(rootContainer, headMethod, KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.HEAD) })
            assignShortcutToText(rootContainer, optionsMethod, KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), Runnable { setMethodInUi(HttpMethod.OPTIONS) })
        }
        initHttpMethods()

        setShortcutForArrowKeySelection()

        reloadRequestBackup()
        enableFinder()
        menuBar.isUseSystemMenuBar = true
    }

    @FXML
    private fun findInResponse() {
        findNextOccurrence()

        fun focus() {
            findInResponse.requestFocus()
            findContainer.isVisible = true
        }
        focus()

        findInResponse.setOnKeyReleased { _ ->
            responseBody.deselect()
            findNextOccurrence()
        }
    }

    private fun findNextOccurrence() {
        val text = responseBody.text.toLowerCase()
        val search = findInResponse.text.toLowerCase()

        val nextOccurrence = text.indexOf(search, FindContainer.findNextFrom)
        val found = nextOccurrence != none && nextOccurrence != 0
        if (found) {
            val selectionRange = nextOccurrence + search.length
            responseBody.selectRange(selectionRange, nextOccurrence)
            FindContainer.findNextFrom = selectionRange
        } else {
            FindContainer.findNextFrom = 0
        }
    }

    @FXML
    private fun findNext() {
        responseBody.deselect()
        findNext.requestFocus()
        findNextOccurrence()
    }

    private fun enableFinder() {
        assignShortcut(findContainer, KeyCodeCombination(KeyCode.ESCAPE), Runnable { rootContainer.requestFocus(); findContainer.isVisible = false })
        assignShortcut(findNext, KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN), Runnable { findNext.fire() })

        findRequest.setOnKeyReleased { _ ->
            resetFind()
            populateFindings()
        }
    }

    private fun reloadRequestBackup() {
        requestFilesBackup.clear()
        requestFilesBackup.putAll(requestFiles)
    }

    private fun resetFind() {
        requestFiles.clear()
        requestFiles.putAll(requestFilesBackup)
    }

    private fun populateFindings() {
        val requestModelsForSearch = FXCollections.observableMap<ClientRequest, File>(LinkedHashMap<ClientRequest, File>())
        requestModelsForSearch.putAll(requestFiles)

        requestFiles.clear()
        requests.clear()

        requestModelsForSearch.forEach { savedRequest ->
            if (found(savedRequest.key)) {
                requestFiles.put(savedRequest.key, savedRequest.value)
                requests.add(savedRequest.key)
            }
        }
    }

    private fun found(savedRequest: ClientRequest) =
            savedRequest.name.toLowerCase().contains(findRequest.text.toLowerCase())


    private fun setShortcutForArrowKeySelection() =
            queryTable.setOnKeyReleased { keyEvent ->
                if ((keyEvent.code == KeyCode.UP).or(keyEvent.code == KeyCode.DOWN))
                    loadSavedRequest()
            }


    /**
     * This is a workaround as proper rendering for combo box items is not working yet.
     */
    @FXML
    private fun refillComboBoxItems() {
        httpMethods.items = null
        httpMethods.items = httpMethodsTexts
    }

    @FXML
    private fun saveRequest() {
        updateEndpoint()

        val viewSelection: TableView.TableViewSelectionModel<ClientRequest> = queryTable.selectionModel
        val selectedRequestIndex = viewSelection.selectedIndex
        if (selectedRequestIndex == none) {
            saveNewRequest(viewSelection)
        } else {
            val requestName: String = viewSelection.selectedItem.name
            val clientRequest = buildRequest(requestName)

            val fileLocation = requestFiles.values.toList()[selectedRequestIndex]
            if (save(storage = fileLocation, request = clientRequest)) loadSavedRequests()

            postSaveAction(requestName, selectedRequestIndex, viewSelection)
        }
    }

    @FXML
    private fun duplicateRequest() {
        updateEndpoint()
        val viewSelection: TableView.TableViewSelectionModel<ClientRequest> = queryTable.selectionModel
        saveNewRequest(viewSelection)
    }

    @FXML
    private fun applyCurl() {
        if (App.properties.getProperty("feature.applyCurlCommand").toBoolean()) {
            val fromCurlCommand = ClientRequest.fromCurlCliCommand(curlCommand.text)
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

        endpointUrl.text = request.url.toString()
        requestBody.text = request.body
        requestHeaders.text = request.headers.toString()
        setMethodInUi(request.method)
    }

    private fun saveNewRequest(viewSelection: TableView.TableViewSelectionModel<ClientRequest>) {
        val localTimestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val verboseRequestName = "$localTimestamp ${request.url.host}${request.url.path} ${request.method}"
        val selectedRequest: ClientRequest? = viewSelection.selectedItem
        val selectedIndex = viewSelection.selectedIndex
        val requestName: String = if (selectedRequest === null) verboseRequestName else "${selectedRequest.name} ∆"
        val clientRequest = buildRequest(requestName)

        if (saveAsNew(clientRequest)) loadSavedRequests()

        postSaveAction(requestName, selectedIndex, viewSelection)
    }

    private fun buildRequest(requestName: String): ClientRequest {
        val clientRequest = ClientRequest.Builder(requestName)
                .method(request.method)
                .url(request.url)
                .body(requestBody.text)
                .headers(ClientRequest.toHeaders(requestHeaders.text))
                .build()
        return clientRequest
    }

    private fun postSaveAction(requestName: String, selectedRequestIndex: Int, viewSelection: TableView.TableViewSelectionModel<ClientRequest>) {
        viewSelection.select(selectedRequestIndex)
        showNotification(Level.INFO, "“$requestName” saved ${Instant.now()}")
    }

    @FXML
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
                showNotification(Level.SEVERE, "${e.message}")
            }
        }
    }

    private fun prepareRequest(): Invocation.Builder {
        val target = applyUrlRequestParameters(client.target(request.url.toString()),
                extractRequestParameters(declareRequestParameters()))

        return applyHeaderInfo(extractHeaderData(requestHeaders.text), target.request())
    }

    @FXML
    private fun updateEndpoint() {
        if (endpointUrl.text.isEmpty()) {
            showNotification(Level.INFO, "Endpoint URL required")
            validEndpoint = false
            return
        }

        try {
            val targetUrl: URL = URL(endpointUrl.text)
            val selectedHttpMethod: String? = httpMethods.selectionModel.selectedItem?.accessibleText
            request = ClientRequest.Builder("[Current Request]")
                    .method(if (selectedHttpMethod === null) HttpMethod.GET else selectedHttpMethod)
                    .body(requestBody.text)
                    .headers(ClientRequest.toHeaders(requestHeaders.text))
                    .url(targetUrl)
                    .build()
            updateCurlCliCommand()
        } catch (e: MalformedURLException) {
            showNotification(Level.SEVERE, "Invalid endpoint URL: ${e.message}")
            validEndpoint = false
            return
        }

        requestParameterData.text = request.url.query

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
        notification.text = message
    }

    private fun postRequest(): Response {
        val response: Response
        if (isFormMediaType(request))
            response = prepareRequest().post(Entity.form(toForm(request.body)))
         else
            response = prepareRequest().post(Entity.json<String>(request.body))

        return response
    }

    private fun getRequest() = prepareRequest().get()

    private fun putRequest() = prepareRequest().put(Entity.json<String>(request.body))

    private fun deleteRequest() = prepareRequest().delete()

    private fun headRequest() = prepareRequest().head()

    private fun optionsRequest() = prepareRequest().options()

    private fun showResponseHeaders(response: Response) {
        response.headers.forEach { header ->
            responseHeaders.appendText(Headers.toString(entry = header, lineBreak = true))
        }
    }

    private fun showResponseBody(response: Response) {
        val formattedResponse = formatJson(response.readEntity(String::class.java))
        responseBody.appendText(formattedResponse)
    }

    private fun declareRequestParameters() =
            if (null === requestParameterData.text)
                ""
            else
                requestParameterData.text

    @FXML
    private fun clearPreviousResponse() {
        responseHeaders.clear()
        responseBody.clear()
        responseStatus.text = ""
        notification.text = ""
    }

    private fun loadSavedRequests() {
        requestFiles.clear()
        requests.clear()

        val appHomeDirectory = File(App.APP_HOME_DIRECTORY)
        createAppHome(appHomeDirectory)

        appHomeDirectory.listFiles().toList().sortedDescending().forEach { file ->
            requestFiles.put(loadFromFile(file), file)
            requests.add(loadFromFile(file))
        }

        reloadRequestBackup()

        requestColumn.cellValueFactory = PropertyValueFactory<ClientRequest, String>("name")
        requestColumn.cellFactory = TextFieldTableCell.forTableColumn<ClientRequest>()

        onEditRequestListener()

        queryTable.items = requests

        if (!findRequest.text.isEmpty()) populateFindings()
    }

    @FXML
    private fun deleteSavedRequest() {
        val selectedRequest = queryTable.selectionModel.selectedIndex
        val selectedRequestItem = queryTable.selectionModel.selectedItem

        deleteSavedRequestFile()
        loadSavedRequests()

        queryTable.selectionModel.select(selectedRequest)
        showNotification(Level.INFO, "“${selectedRequestItem.name}” deleted ${Instant.now()}")
    }

    private fun deleteSavedRequestFile() {
        val selectedIndex = queryTable.selectionModel.selectedIndex
        if (selectedIndex != none) {
            val fileToDelete = requestFiles.values.toList()[selectedIndex]
            if (fileToDelete.delete()) {
                App.LOG.info("Saved request deleted: $fileToDelete")
            } else {
                App.LOG.warn("Saved request not deleted: $fileToDelete")
            }
        }
    }

    @FXML
    private fun loadSavedRequest() {
        val selectedRequest = queryTable.selectionModel.selectedItem
        if (selectedRequest != null) {
            request = selectedRequest

            setMethodInUi(request.method)
            requestHeaders.text = request.headers.toStringColumn()
            requestParameterData.text = request.url.query
            requestBody.text = request.body
            endpointUrl.text = request.url.toString()
        }
    }

    private fun setMethodInUi(method: String) {
        when (method) {
            HttpMethod.GET -> httpMethods.selectionModel.select(0)
            HttpMethod.POST -> httpMethods.selectionModel.select(1)
            HttpMethod.DELETE -> httpMethods.selectionModel.select(2)
            HttpMethod.PUT -> httpMethods.selectionModel.select(3)
            HttpMethod.HEAD -> httpMethods.selectionModel.select(4)
            HttpMethod.OPTIONS -> httpMethods.selectionModel.select(5)
            else -> showNotification(Level.SEVERE, "HTTP method has no equivalent in UI.")
        }
    }

    private fun showStatus(response: Response) {
        val requestDuration = Instant.now().minusMillis(startRequest.toEpochMilli()).toEpochMilli()
        val responseInfo = "Time: ${Instant.now()}\nStatus: ${response.statusInfo.statusCode} ${response.statusInfo.reasonPhrase} in $requestDuration ms"
        responseStatus.text = responseInfo
        responseStatus.tooltip = Tooltip(response.statusInfo.family.name)
    }

    init {
        val connectTimeout = App.properties["connect.timeout"] ?: 500
        client.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout)
        val readTimeout = App.properties["read.timeout"] ?: 5000
        client.property(ClientProperties.READ_TIMEOUT, readTimeout)
    }

    private val onEditRequestListener = {
        requestColumn.setOnEditCommit({ request ->
            val newRequestName = request.newValue
            val clientRequestCopy = request.tableView.items[request.tablePosition.row]
            val selectedRequest = request.tableView.selectionModel.selectedIndex
            val clientRequestRenamed = ClientRequest.Builder(newRequestName)
                    .url(clientRequestCopy.url)
                    .headers(clientRequestCopy.headers)
                    .body(clientRequestCopy.body)
                    .method(clientRequestCopy.method)
                    .build()
            request.tableView.items[request.tablePosition.row] = clientRequestRenamed

            val file = requestFiles.values.toList()[request.tablePosition.row]

            val fileWriter = FileWriter(file)
            fileWriter.write(request.tableView.items[request.tablePosition.row].toString())
            fileWriter.close()

            loadSavedRequests()
            App.LOG.info("${App.SAVE_AS} ${request.tableView.items[request.tablePosition.row].name}")

            request.tableView.selectionModel.select(selectedRequest)
        })
    }

    private val updateCurlCliCommand = { curlCommand.text = request.toCurlCliCommand() }

    private companion object {
        private val client = ClientBuilder.newClient()
        private val none = -1
    }
}
