package com.pet001kambala.app

import com.pet001kambala.controller.HomeController
import tornadofx.*

class NamOpsMasterFuel: App(MainWorkspace::class, Styles::class){

    init {
        reloadStylesheetsOnFocus()
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<HomeController>()
    }
}