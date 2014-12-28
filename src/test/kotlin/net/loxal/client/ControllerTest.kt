/*
 * Copyright 2014 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
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

public class ControllerTest {
    Before
    throws(javaClass<Exception>())
    public fun setUp() {
    }

    After
    throws(javaClass<Exception>())
    public fun tearDown() {
    }

    Test
    throws(javaClass<Exception>())
    public fun dummyTest() {
        assertEquals(true, true)
    }

    Test
    throws(javaClass<Exception>())
    public class AsNonApp : Application() {
        throws(javaClass<Exception>())
        override fun start(primaryStage: Stage) {
            throw AssertionError("NOP")
        }
    }

    class object {
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
