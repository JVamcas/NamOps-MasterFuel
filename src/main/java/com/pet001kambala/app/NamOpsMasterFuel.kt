package com.pet001kambala.app

import tornadofx.*
import com.pet001kambala.controller.home.LoginController
import com.pet001kambala.utils.SessionManager

class NamOpsMasterFuel : App(MainWorkspace::class, Styles::class) {


    override fun onBeforeShow(view: UIComponent) {

        workspace.dock<LoginController>()
    }

    override fun stop() {
        super.stop()
        try{
            SessionManager.newInstance!!.close()
            println("Session factory closed")
        }
        catch (e: Exception){

        }
    }
}