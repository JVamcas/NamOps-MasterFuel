package com.pet001kambala.controller.charts

import com.pet001kambala.controller.AbstractView
import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.utils.DateUtil.Companion.lastYear
import com.pet001kambala.utils.DateUtil.Companion.monthByName
import com.pet001kambala.utils.DateUtil.Companion.thisMonthBeginning
import com.pet001kambala.utils.DateUtil.Companion.thisMonthByName
import com.pet001kambala.utils.DateUtil.Companion.thisYear
import com.pet001kambala.utils.Results
import javafx.application.Platform
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.BorderPane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class ChartsController : AbstractView("Fuel Statistics") {

    private val transactionRepo = FuelTransactionRepo()

    init {
        disableCreate()
        disableSave()
        disableDelete()
    }

    override val root: BorderPane = borderpane {

        right {
            vbox {
                GlobalScope.launch {

                    val leastEfficientResult = transactionRepo.loadLeastEfficientVehicle(thisMonthBeginning())
                    if (leastEfficientResult is Results.Success<*>) {
                        val dataList = leastEfficientResult.data as List<*>

                        Platform.runLater {
                            piechart("${thisMonthByName()} ${thisYear()} - Five Least efficient vehicles") {
                                dataList.forEach {
                                    val entry = it as Array<*>
                                    val vehicle = "${entry[0].toString()} - ${entry[1].toString()}"
                                    val average = entry[2] as Double
                                    data(vehicle, average)
                                }
                            }
                        }
                    }
                }

                GlobalScope.launch {

                    val mostEfficientResult = transactionRepo.loadMostEfficientVehicle(thisMonthBeginning())
                    if (mostEfficientResult is Results.Success<*>) {
                        val dataList = mostEfficientResult.data as List<*>

                        Platform.runLater {
                            piechart("${thisMonthByName()} ${thisYear()} - Five Most efficient vehicles") {
                                dataList.forEach {
                                    val entry = it as Array<*>
                                    val vehicle = "${entry[0].toString()} - ${entry[1].toString()}"
                                    val average = entry[2] as Double
                                    data(vehicle, average)
                                }
                            }
                        }
                    }
                }
            }
        }
        bottom {
            barchart("${lastYear()} - ${thisYear()} - Monthly Fuel Usage", CategoryAxis(), NumberAxis()) {

                GlobalScope.launch {
                    val resultsList = transactionRepo.loadMonthlyFuelUsage()

                    if (resultsList is Results.Success<*>) {
                        val data = resultsList.data as List<*>
                        val lastYearData = data[0] as List<*>
                        val thisYearData = data[1] as List<*>

                        Platform.runLater {

                            series(lastYear()) {
                                val usageMap = lastYearData.map {
                                    val result = it as Array<*>
                                    result[1] as Int to result[0] as Double
                                }.toMap()

                                (1..12).map {
                                    data(monthByName(it), usageMap.getOrDefault(it, 0))
                                }
                            }

                            series(thisYear()) {
                                val usageMap = thisYearData.map {
                                    val result = it as Array<*>
                                    result[1] as Int to result[0] as Double
                                }.toMap()

                                (1..12).map {
                                    data(monthByName(it), usageMap.getOrDefault(it, 0))
                                }
                            }
                        }
                    } else parseResults(resultsList)
                }
            }
        }
    }
}