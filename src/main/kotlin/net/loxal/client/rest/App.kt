/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.io.IOException
import java.net.URL
import java.util.Properties
import java.util.logging.Logger

class App : Application() {

    override fun start(stage: Stage) {
        val loader = FXMLLoader()
        val rootNode: AnchorPane
        val scene: Scene

        try {
            rootNode = loader.load<AnchorPane>(javaClass.getResourceAsStream("ui.fxml"))
            scene = Scene(rootNode)
            stage.setScene(scene)

            stage.setTitle("Epvin v${properties.getProperty("project.version")} | www.loxal.net/epvin-rest-client")
            stage.getIcons().add(Image("/net/loxal/client/rest/view/tool-icon-256.png"))
            rootNode.requestFocus()
            stage.show()

            val controller = loader.getController<Controller>()
            controller.initAccelerators()
        } catch (e: IOException) {
            LOG.severe("${e.getCause()}\n ${e.getMessage()}")
        }
    }

    init {
        properties.load(javaClass.getResourceAsStream("/app.properties"))
    }

    companion object {
        val LOG = Logger.getGlobal()
        val SAMPLE_URL = URL("https://example.com")
        val SAVE_AS = "Save request as:"
        private val properties = Properties()

        val APP_HOME_DIRECTORY = if (System.getenv("HOME").identityEquals(null)) {
            System.getenv("USERPROFILE")
        } else {
            System.getenv("HOME")
        } + "/.loxal/rest-client/request"

        fun main(vararg args: String) = Application.launch(*args)
    }
}
