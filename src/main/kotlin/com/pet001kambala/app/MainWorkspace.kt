package com.pet001kambala.app

import com.pet001kambala.controller.MainMenu
import tornadofx.*

class MainWorkspace: Workspace() {

    init {
        add(MainMenu::class)
        add(RestProgressBar::class)
    }
}