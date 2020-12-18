package com.pet001kambala.controller

import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.utils.DateUtil.Companion.thisYear
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.BorderPane
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

                piechart("December 2020 - Least efficient vehicles") {
                    data("H01", 400.0)
                    data("H02", 900.0)
                    data("L30", 1200.0)
                    data("H12", 780.0)
                    data("H04", 248.0)
                }

//                GlobalScope.launch {
//                    val results = transactionRepo.loadMostEfficientVehicle(thisYearFirstDate())
//                    if(results is Results.Success<*>){
//                        val data = results.data as List<*>
//                        Platform.runLater {
//                            piechart("${thisYear()} - Most efficient vehicles (Km/Litre)"){
//
//                            }
//                        }
//                    }
//                }
//
//                GlobalScope.launch {
//                    val results = transactionRepo.loadLeastEfficientVehicle(thisYearFirstDate())
//                    if(results is Results.Success<*>){
//                        val data = results.data as List<*>
//                        Platform.runLater {
//                            piechart("${thisYear()} - Least efficient vehicles (Km/Litre)"){
//
//                            }
//                        }
//                    }
//                }

                piechart("December 2020 - Most efficient vehicles") {
                    data("H03", 271.0)
                    data("H05", 417.0)
                    data("H07", 271.0)
                    data("H11", 142.0)
                    data("H10", 21.0)
                }
            }
        }
        bottom {
            barchart("${thisYear()} - Monthly Fuel Usage", CategoryAxis(), NumberAxis()) {


//
//                GlobalScope.launch {
//                    val resultsList = transactionRepo.loadMonthlyFuelUsage()
//
//                    if(resultsList is Results.Success<*>){
//                        val data = resultsList.data as List<*>
//                        val lastYearData = data[0] as List<*>
//                        val thisYearData = data[1] as List<*>
//
//                        Platform.runLater {
//                            series(lastYear()) {
//                                lastYearData.forEach {
//                                    println(it.toString())
//                                }
//                            }
//
//                            series(thisYear()){
//                                thisYearData.forEach {
//                                    println(it.toString())
//                                }
//                            }
//                        }
//                    }
//                    else parseResults(resultsList)
//                }

                series("2019") {
                    data("Jan", 3221.0)
                    data("Feb", 2100.0)
                    data("Mar", 1800.0)
                    data("Apr", 2500.0)
                    data("May", 4200.0)
                    data("Jun", 1000.0)
                    data("Jul", 6520.0)
                    data("Aug", 4950.0)
                    data("Sep", 3210.0)
                    data("Oct", 4508.0)
                    data("Nov", 5300.0)
                    data("Dec", 6000.0)
                }
                series("2020") {
                    data("Jan", 7800.0)
                    data("Feb", 2510.0)
                    data("Mar", 2511.0)
                    data("Apr", 6000.0)
                    data("May", 8000.0)
                    data("Jun", 4650.0)
                    data("Jul", 3511.0)
                    data("Aug", 5000.0)
                    data("Sep", 3200.0)
                    data("Oct", 5200.0)
                    data("Nov", 6300.0)
                    data("Dec", 5320.0)
                }
            }
        }
    }
}