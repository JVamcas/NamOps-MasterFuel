package com.pet001kambala.controller.campany

import com.pet001kambala.controller.AbstractModelTableController
import com.pet001kambala.model.CompanyModel
import com.pet001kambala.model.DepartmentC
import com.pet001kambala.model.DepartmentModel
import com.pet001kambala.repo.DepartmentRepo
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

class CompanyDepartmentController : AbstractModelTableController<DepartmentC>("") {

    private val deptModel = DepartmentModel()
    private val deptRepo = DepartmentRepo()
    private val companyModel: CompanyModel by inject()

    init {
        deptModel.item = DepartmentC()
    }

    override val root = vbox(spacing = 10.0) {
        scrollpane {
            prefHeight = 300.0
            prefWidth = 150.0

            minHeight = prefHeight
            minWidth = prefWidth

            tableview<DepartmentC> {

                items = modelList

                smartResize()
                prefWidthProperty().bind(this@scrollpane.widthProperty())
                prefHeightProperty().bind(this@scrollpane.heightProperty())
                placeholder = Label("No departments here yet.")

                columns.add(indexColumn)
                column("Department name", DepartmentC::nameProperty).apply {
                    contentWidth(padding = 20.0, useAsMin = true)
                    setCellFactory { EditingCell<DepartmentC>(deptRepo) }
                    remainingWidth()
                }

                contextmenu {
                    item("Delete").action {
                        selectedItem?.apply {
                            GlobalScope.launch {
                                val results = deptRepo.deleteModel(this@apply)
                                if (results is Results.Success<*>)
                                    onRefresh()
                                else parseResults(results)
                            }
                        }
                    }
                }

                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                vgrow = Priority.ALWAYS

                enableCellEditing()
            }
        }

        titledpane("Add department") {
            hbox(spacing = 10.0) {
                textfield {
                    prefWidth = 200.0
                    minWidth = prefWidth
                    promptText = "Deparment name"
                    bind(deptModel.name)

                    required(ValidationTrigger.OnChange(), "Enter department name.")
                }
                button {
                    enableWhen { deptModel.valid }
                    graphic = FontAwesomeIconView(FontAwesomeIcon.SAVE)
                    action {
                        deptModel.commit()
                        GlobalScope.launch {
                            val dept = deptModel.item
                            dept.company = companyModel.item

                            val results = deptRepo.addNewModel(dept)
                            if (results is Results.Success<*>) {
                                deptModel.item = DepartmentC()
                                modelList.add(dept)
                                return@launch
                            }
                            parseResults(results)
                        }
                    }
                }
                button {
                    enableWhen { deptModel.dirty }
                    graphic = FontAwesomeIconView(FontAwesomeIcon.CLOSE)
                    action {
                        deptModel.item = DepartmentC()
                    }
                }
                deptModel.validate(decorateErrors = false)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        currentStage?.isResizable = false
        title = "${companyModel.item} - Departments"
    }

    override suspend fun loadModels(): ObservableList<DepartmentC> {
        val loadResults = deptRepo.loadAllDepartments(companyModel.item)
        if (loadResults is Results.Success<*>)
            return loadResults.data as ObservableList<DepartmentC>
        return observableListOf()
    }
}