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
import kotlin.platform.platformStatic
import java.io.FileInputStream

public class App : Application() {

    override fun start(stage: Stage) {
        val viewDeclarationPath = "/net/loxal/client/rest/view/ui.fxml"
        val loader = FXMLLoader()
        val rootNode: AnchorPane
        val scene: Scene

        try {
            val fis = FileInputStream("/Users/alex/my/project/loxal/rest-client/src/main/resources/net/loxal/client/rest/view/ui.fxml")
            rootNode = loader.load<AnchorPane>(fis)
            //            rootNode = loader.load<AnchorPane>(javaClass.getResourceAsStream(viewDeclarationPath))
            scene = Scene(rootNode)
            stage.setScene(scene)

            stage.setTitle("Epvin")
            // TODO can be attached in a declarative way via *.fxml?
            stage.getIcons().add(Image("/net/loxal/client/rest/view/tool-icon-32.png"))
            rootNode.requestFocus()
            stage.requestFocus()
            stage.show()

            val controller = loader.getController<Controller>()
            controller.setAccelerators()
        } catch (e: IOException) {
            LOG.severe("${e.getCause()}\n ${e.getMessage()}")
        }
    }

    class object {
        platformStatic val LOG = Logger.getGlobal()

        public fun main(vararg args: String) {
            Application.launch(*args)
        }
    }
}
