package com.pet001kambala.app

import com.pet001kambala.controller.home.HomeController
import tornadofx.*

class NamOpsMasterFuel: App(MainWorkspace::class, Styles::class){


    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<HomeController>()
    }
}