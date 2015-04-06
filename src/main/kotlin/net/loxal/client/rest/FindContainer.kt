/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest

import javafx.fxml.Initializable
import javafx.scene.layout.HBox
import java.net.URL
import java.util.ResourceBundle

private class FindContainer : Initializable, HBox() {
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        println("initialize")
        throw UnsupportedOperationException()
    }

    init {
        println("init")
    }
}