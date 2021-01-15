package com.pet001kambala.controller.campany

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.model.Company
import com.pet001kambala.model.User
import com.pet001kambala.repo.CompanyRepo
import com.pet001kambala.utils.EditingCell
import com.pet001kambala.utils.Results
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*


class CompanyController : AbstractModelTableController<Company>("Companies") {

    private val companyRepo = CompanyRepo()

    override val root = vbox(spacing = 10.0) {
        scrollpane {
            prefHeight = 300.0
            prefWidth = 300.0
            tableview<Company> {

                smartResize()
                prefWidthProperty().bind(this@scrollpane.widthProperty())
                prefHeightProperty().bind(this@scrollpane.heightProperty())
                placeholder = Label("No companies here yet.")

                columns.add(indexColumn)
                column("Company name", Company::nameProperty ).apply {
                    contentWidth(padding = 20.0, useAsMin = true)
                    setCellFactory { EditingCell<Company>() }
                }

                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                vgrow = Priority.ALWAYS
            }
        }
        hbox(spacing = 10.0) {
            id = "hbox-one"
            alignment = Pos.CENTER_RIGHT
            region { hgrow = Priority.ALWAYS }
            button {
                graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS)
            }
            button {
                graphic = FontAwesomeIconView(FontAwesomeIcon.CLOSE)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        currentStage?.isResizable = false
        title = "Companies"
    }

    override suspend fun loadModels(): ObservableList<Company> {
        val loadResults = companyRepo.loadAllCompanies()
        if (loadResults is Results.Success<*>)
            return loadResults.data as ObservableList<Company>
        return observableListOf()
    }
}