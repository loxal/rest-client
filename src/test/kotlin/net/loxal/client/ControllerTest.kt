/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client

import javafx.application.Application
import javafx.stage.Stage
import net.loxal.client.rest.App
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals

class ControllerTest {
    Before
    public fun setUp() {
    }

    After
    public fun tearDown() {
    }

    Test
    public fun dummyTest() {
        assertEquals(true, true)
    }

    Test
    public class AsNonApp : Application() {
        override fun start(primaryStage: Stage) {
            throw AssertionError("NOP")
        }
    }

    companion object {
        BeforeClass
        public fun initJavaFxEnvironment() {
            val thread = object : Thread("JavaFx Init Thread") {
                override fun run() {
                    Application.launch(javaClass<App>())
                }
            }
            thread.setDaemon(true)
            thread.start()
        }
    }
}
