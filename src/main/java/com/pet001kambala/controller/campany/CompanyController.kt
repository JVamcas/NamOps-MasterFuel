package com.pet001kambala.controller.campany

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.model.Company
import com.pet001kambala.model.CompanyModel
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
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import tornadofx.*


class CompanyController : AbstractModelTableController<Company>("Companies") {

    private val companyRepo = CompanyRepo()
    private val companyModel = CompanyModel()

    init {
        companyModel.item = Company()
        workspace.saveButton.show()
    }

    override val root = vbox(spacing = 10.0) {
        scrollpane {
            prefHeight = 500.0
            prefWidth = 400.0

            minHeight = prefHeight
            minWidth = prefWidth

            tableview<Company> {

                items = modelList

                smartResize()
                prefWidthProperty().bind(this@scrollpane.widthProperty())
                prefHeightProperty().bind(this@scrollpane.heightProperty())
                placeholder = Label("No companies here yet.")

                columns.add(indexColumn)
                column("Company name", Company::nameProperty).apply {
                    contentWidth(padding = 20.0, useAsMin = true)
                    setCellFactory { EditingCell(companyRepo) }
                }

                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                vgrow = Priority.ALWAYS

                enableCellEditing()
            }
        }

        titledpane("Add company") {
            hbox(spacing = 10.0) {
                textfield {
                    prefWidth = 300.0
                    minWidth = prefWidth
                    promptText = "Company name"
                    bind(companyModel.name)
                }
                button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.SAVE)
                    action {
                        companyModel.commit()
                        GlobalScope.launch {
                            val company = companyModel.item
                            val results = companyRepo.addNewModel(company)
                            if (results is Results.Success<*>) {
                                companyModel.item = Company()
                                modelList.add(company)
                                return@launch
                            }
                            parseResults(results)
                        }
                    }
                }
                button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.CLOSE)
                    action {
                        companyModel.item = Company()
                    }
                }
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