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

private class FindContainer : Initializable, HBox() {
    FXML
    private var findPrev: Button = Button()
    FXML
    private var findNext: Button = Button()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        App.LOG.info("initialize: $location - $resources")
    }

    init {
        App.LOG.info("init")
    }

    FXML
    private fun findNext() {
        App.LOG.info("findNext")
    }

    FXML
    private fun findPrev() {
        App.LOG.info("findPrev")
    }

    FXML
    private fun findInResponse() {
        App.LOG.info("findInResponse")
    }
}