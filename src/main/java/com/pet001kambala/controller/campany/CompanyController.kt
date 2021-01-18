package com.pet001kambala.controller.campany

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.model.Company
import com.pet001kambala.model.CompanyModel
import com.pet001kambala.repo.CompanyRepo
import com.pet001kambala.utils.EditingCell
import com.pet001kambala.utils.Results
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import kotlinx.coroutines.GlobalScope
import tornadofx.*
import kotlinx.coroutines.launch


class CompanyController : AbstractModelTableController<Company>("Companies") {

    private val companyRepo = CompanyRepo()
    private val companyModel = CompanyModel()

    init {
        companyModel.item = Company()
    }

    override val root = vbox(spacing = 10.0) {
        scrollpane {
            prefHeight = 500.0
            prefWidth = 450.0

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
                    remainingWidth()
                }

                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                vgrow = Priority.ALWAYS

                enableCellEditing()

                contextmenu {
                    item("Delete").action {
                        selectedItem?.apply {
                            GlobalScope.launch{
                                val results = companyRepo.deleteModel(this@apply)
                                if (results is Results.Success<*>)
                                    onRefresh()
                                else parseResults(results)
                            }
                        }
                    }
                    item("Departments").action{
                        val scope = Scope()
                        val model = CompanyModel()
                        model.item = selectedItem
                        setInScope(model,scope)
                        find(CompanyDepartmentController::class, scope).openModal()
                    }
                }
            }
        }

        titledpane("Add company") {
            hbox(spacing = 10.0) {
                textfield {
                    prefWidth = 300.0
                    minWidth = prefWidth
                    promptText = "Company name"
                    bind(companyModel.name)

                    required(ValidationTrigger.OnChange(),"Enter company name.")
                }
                button {
                    enableWhen { companyModel.valid }
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
                    enableWhen { companyModel.dirty }
                    graphic = FontAwesomeIconView(FontAwesomeIcon.CLOSE)
                    action {
                        companyModel.item = Company()
                    }
                }
                companyModel.validate(decorateErrors = false)
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