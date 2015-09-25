/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import java.net.URL
import java.util.*

class FindContainer : Initializable, HBox() {
    @FXML
    private var findNext: Button = Button()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        App.LOG.info("initialize: $location - $resources")
    }

    init {
        App.LOG.trace("init")
    }

    @FXML
    private fun findNext() {
        App.LOG.info("findNext")
    }

    @FXML
    private fun findInResponse() {
        App.LOG.info("findInResponse")
    }

    companion object {
        var findNextFrom: Int = 0
    }
}