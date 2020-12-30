package com.pet001kambala.app

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.CssRule.Companion.c
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val cmenu by cssclass()
    }

    init {
        label and heading {
            padding = box(10.px)
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }
        cmenu{
            menu{
                textFill = Color.WHITE
            }
        }
    }
}