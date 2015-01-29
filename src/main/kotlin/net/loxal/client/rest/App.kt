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

            stage.setTitle("Epvin")
            // TODO can be attached in a declarative way via *.fxml?
            stage.getIcons().add(Image("/net/loxal/client/rest/view/tool-icon-256.png"))
            rootNode.requestFocus()
            stage.show()

            val controller = loader.getController<Controller>()
            controller.setAccelerators()
        } catch (e: IOException) {
            LOG.severe("${e.getCause()}\n ${e.getMessage()}")
        }
    }

    class object {
        val LOG = Logger.getGlobal()
        val SAMPLE_URL = "https://example.com"
        val SAVE_AS = "Save request as:"

        val APP_HOME_DIRECTORY = if (System.getenv("HOME") == null) {
            System.getenv("USERPROFILE")
        } else {
            System.getenv("HOME")
        } + "/.loxal/rest-client/request"

        fun main(vararg args: String) {
            Application.launch(*args)
        }
    }
}
