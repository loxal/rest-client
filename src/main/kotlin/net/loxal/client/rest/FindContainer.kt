/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import java.net.URL
import java.util.ResourceBundle
import java.util.logging.Level

private class FindContainer : Initializable, HBox() {
    FXML
    private var findPrev: Button = Button()
    FXML
    private var findNext: Button = Button()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        App.LOG.log(Level.INFO, "initialize: $location - $resources")
    }

    init {
        App.LOG.log(Level.INFO, "init")
    }

    FXML
    private fun findNext() {
        App.LOG.log(Level.INFO, "findNext")
    }

    FXML
    private fun findPrev() {
        App.LOG.log(Level.INFO, "findPrev")
    }

    FXML
    private fun findInResponse() {
        App.LOG.log(Level.INFO, "findInResponse")
    }
}