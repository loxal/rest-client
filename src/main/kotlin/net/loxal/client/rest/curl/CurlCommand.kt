/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.client.rest.curl


import com.beust.jcommander.DynamicParameter
import com.beust.jcommander.Parameter
import com.beust.jcommander.internal.Lists
import java.util.HashMap

/**
 * https://github.com/cbeust/jcommander
 * http://jcommander.org
 */
class CurlCommand {

    Parameter(names = array("-X"), description = "HTTP method")
    var httpMethod: String = "GET"

    Parameter(names = array("-d"), description = "Data")
    var data: String = ""

    DynamicParameter(names = array("-H"), description = "Headers", assignment = ":")
    var headers: Map<String, String> = HashMap()

    Parameter
    public var parameters: List<String> = Lists.newArrayList<String>()

    Parameter(names = array("-log", "-verbose"), description = "Level of verbosity")
    public var verbose: Int? = 1

    Parameter(names = array("-groups"), description = "Comma-separated list of group names to be run")
    public var groups: String? = null

    Parameter(names = array("-debug"), description = "Debug mode")
    public var debug: Boolean = false

    //    DynamicParameter(names = array("-D"), description = "Dynamic parameters go here")
    //    public var dynamicParams: Map<String, String> = HashMap()
}