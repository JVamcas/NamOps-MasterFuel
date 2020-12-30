package com.pet001kambala.app

import tornadofx.*
import com.pet001kambala.controller.home.LoginController

class NamOpsMasterFuel : App(MainWorkspace::class, Styles::class) {


    override fun onBeforeShow(view: UIComponent) {

        workspace.dock<LoginController>()
    }
}