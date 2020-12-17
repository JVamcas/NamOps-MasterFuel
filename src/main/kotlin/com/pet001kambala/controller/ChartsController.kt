package com.pet001kambala.controller

import com.pet001kambala.repo.FuelTransactionRepo
import com.pet001kambala.utils.DateUtil.Companion.lastYear
import com.pet001kambala.utils.DateUtil.Companion.lastYearFirstDate
import com.pet001kambala.utils.DateUtil.Companion.thisYearFirstDate
import com.pet001kambala.utils.Results
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.BorderPane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class ChartsController: AbstractView("Fuel Statistics") {

    private val transactionRepo = FuelTransactionRepo()

    init {

        disableCreate()
        disableSave()
        disableDelete()
    }

    override val root: BorderPane = borderpane {
        right {
            vbox {

                piechart("December 2020 - Least efficient vehicles"){
                    data("H01", 400.0)
                    data("H02", 900.0)
                    data("L30",1200.0)
                    data("H12",780.0)
                    data("H04",248.0)
                }
                
                piechart("December 2020 - Most efficient vehicles") {
                    data("H03",271.0)
                    data("H05",417.0)
                    data("H07",271.0)
                    data("H11",142.0)
                    data("H10",21.0)

                }
            }
        }
        bottom {
            barchart("Total Fuel Usage", CategoryAxis(), NumberAxis()){
                series(lastYear()){
                    val startDate = lastYearFirstDate()
                    val endDate = thisYearFirstDate()

                    GlobalScope.launch {
                        val results = transactionRepo.loadMonthlyFuelUsage(startDate = startDate,endDate = endDate)
                        if(results is Results.Success<*>){
                            val list = results.data as List<*>
                            list.forEach {
                               //todo this to be moded
                            }
                        }
                    }

                }
                series("2019"){
                    data("Jan",3221.0)
                    data("Feb",2100.0)
                    data("Mar",1800.0)
                    data("Apr",2500.0)
                    data("May",4200.0)
                    data("Jun",1000.0)
                    data("Jul",6520.0)
                    data("Aug",4950.0)
                    data("Sep",3210.0)
                    data("Oct",4508.0)
                    data("Nov",5300.0)
                    data("Dec",6000.0)
                }
                series("2020"){
                    data("Jan",7800.0)
                    data("Feb",2510.0)
                    data("Mar",2511.0)
                    data("Apr",6000.0)
                    data("May",8000.0)
                    data("Jun",4650.0)
                    data("Jul",3511.0)
                    data("Aug",5000.0)
                    data("Sep",3200.0)
                    data("Oct",5200.0)
                    data("Nov",6300.0)
                    data("Dec",5320.0)
                }
            }
        }
    }
}