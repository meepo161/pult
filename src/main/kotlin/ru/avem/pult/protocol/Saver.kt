package ru.avem.pult.protocol

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.usermodel.charts.AxisPosition
import org.apache.poi.ss.usermodel.charts.ChartDataSource
import org.apache.poi.ss.usermodel.charts.DataSources
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFChart
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean
import ru.avem.pult.app.MainApp
import ru.avem.pult.database.entities.Protocol
import ru.avem.pult.utils.SHEET_PASSWORD
import ru.avem.pult.utils.copyFileFromStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException

object Saver {
    fun saveProtocolAsWorkbook(protocol: Protocol, path: String = "temp.xlsx") {
        val template = File(path)
        copyFileFromStream(MainApp::class.java.getResource("form2.xlsx").openStream(), template)

        try {
            XSSFWorkbook(template).use { wb ->
                val sheet = wb.getSheetAt(0)
                for (iRow in 0 until 100) {
                    val row = sheet.getRow(iRow)
                    if (row != null) {
                        for (iCell in 0 until 100) {
                            val cell = row.getCell(iCell)
                            if (cell != null && (cell.cellType == CellType.STRING)) {
                                when (cell.stringCellValue) {
                                    "#PROTOCOL_NUMBER#" -> cell.setCellValue(protocol.id.toString())
                                    "#OBJECT#" -> cell.setCellValue(protocol.objectName)
                                    "#SERIAL_NUMBER#" -> cell.setCellValue(protocol.factoryNumber)
                                    "#U_SET_1#" -> cell.setCellValue(protocol.specifiedU)
                                    "#U_OBJ_1#" -> cell.setCellValue(protocol.objectU0)
                                    "#I_SET_1#" -> cell.setCellValue(protocol.specifiedI)
                                    "#I_OBJ_1#" -> cell.setCellValue(protocol.objectI0)
                                    "#U_SET_2#" -> cell.setCellValue(protocol.specifiedU)
                                    "#U_OBJ_2#" -> cell.setCellValue(protocol.objectU1)
                                    "#I_SET_2#" -> cell.setCellValue(protocol.specifiedI)
                                    "#I_OBJ_2#" -> cell.setCellValue(protocol.objectI1)
                                    "#U_SET_3#" -> cell.setCellValue(protocol.specifiedU)
                                    "#U_OBJ_3#" -> cell.setCellValue(protocol.objectU2)
                                    "#I_SET_3#" -> cell.setCellValue(protocol.specifiedI)
                                    "#I_OBJ_3#" -> cell.setCellValue(protocol.objectI2)
                                    "#U_SET_4#" -> cell.setCellValue(protocol.specifiedU)
                                    "#U_OBJ_4#" -> cell.setCellValue(protocol.objectU3)
                                    "#I_SET_4#" -> cell.setCellValue(protocol.specifiedI)
                                    "#I_OBJ_4#" -> cell.setCellValue(protocol.objectI3)
                                    "#TIME_1#" -> cell.setCellValue(protocol.experimentTime0)
                                    "#TIME_2#" -> cell.setCellValue(protocol.experimentTime1)
                                    "#TIME_3#" -> cell.setCellValue(protocol.experimentTime2)
                                    "#TIME_4#" -> cell.setCellValue(protocol.experimentTime3)
                                    "#RESULT_1#" -> cell.setCellValue(protocol.result0)
                                    "#RESULT_2#" -> cell.setCellValue(protocol.result1)
                                    "#RESULT_3#" -> cell.setCellValue(protocol.result2)
                                    "#RESULT_4#" -> cell.setCellValue(protocol.result3)
                                    "#POS_1_NAME#" -> cell.setCellValue(protocol.tester)
                                    "#DATE#" -> cell.setCellValue(protocol.date)
                                    "#RESULT#" -> cell.setCellValue(protocol.result0)
                                    else -> {
                                        if (cell.stringCellValue.contains("#")) {
                                            cell.setCellValue("")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                fillParameters(wb, protocol.graphU, protocol.graphI)
                drawLineChart(wb)
                sheet.protectSheet(SHEET_PASSWORD)
                val outStream = ByteArrayOutputStream()
                wb.write(outStream)
                outStream.close()
            }
        } catch (e: FileNotFoundException) {
        }
    }

    fun fillParameters(wb: XSSFWorkbook, dots1: String, dots2: String) {
        var valuesU = dots1.removePrefix("[").removePrefix("'").removeSuffix("]")
            .split(", ").map { it.replace(',', '.') }.map(String::toDouble)
        var valuesI = dots2.removePrefix("[").removePrefix("'").removeSuffix("]")
            .split(", ").map { it.replace(',', '.') }.map(String::toDouble)
        val sheet = wb.getSheetAt(0)
        var row: Row
        var cellStyle: XSSFCellStyle = generateStyles(wb) as XSSFCellStyle
        var rowNum = 24
        row = sheet.createRow(rowNum)
        var columnNum = 0
        for (i in valuesU.indices) {
            fillOneCell(row, columnNum, cellStyle, i)
            fillOneCell(row, columnNum + 1, cellStyle, valuesU[i])
            fillOneCell(row, columnNum + 2, cellStyle, i)
            fillOneCell(row, columnNum + 3, cellStyle, valuesI[i])
            row = sheet.createRow(++rowNum)
        }
    }

    private fun generateStyles(wb: XSSFWorkbook): CellStyle {
        val headStyle: CellStyle = wb.createCellStyle()
        headStyle.wrapText = true
        headStyle.borderBottom = BorderStyle.THIN
        headStyle.borderTop = BorderStyle.THIN
        headStyle.borderLeft = BorderStyle.THIN
        headStyle.borderRight = BorderStyle.THIN
        headStyle.alignment = HorizontalAlignment.CENTER
        headStyle.verticalAlignment = VerticalAlignment.CENTER
        return headStyle
    }

    private fun fillOneCell(row: Row, columnNum: Int, cellStyle: XSSFCellStyle, points: Double): Int {
        val cell: Cell = row.createCell(columnNum)
        cell.cellStyle = cellStyle
        cell.setCellValue(points)
        return columnNum + 1
    }

    private fun fillOneCell(row: Row, columnNum: Int, cellStyle: XSSFCellStyle, points: Int): Int {
        val cell: Cell = row.createCell(columnNum)
        cell.cellStyle = cellStyle
        cell.setCellValue(points.toString())
        return columnNum + 1
    }

    private fun drawLineChart(workbook: XSSFWorkbook) {
        val sheet = workbook.getSheet("Sheet1")
        val lastRowIndex = sheet.lastRowNum - 1

        val timeDataU = DataSources.fromNumericCellRange(sheet, CellRangeAddress(24, lastRowIndex, 0, 0))
        val valueDataU = DataSources.fromNumericCellRange(sheet, CellRangeAddress(24, lastRowIndex, 1, 1))

        val timeDataI = DataSources.fromNumericCellRange(sheet, CellRangeAddress(24, lastRowIndex, 2, 2))
        val valueDataI = DataSources.fromNumericCellRange(sheet, CellRangeAddress(24, lastRowIndex, 3, 3))

        var lineChartU = createLineChart(sheet, 24, 34)
        drawLineChart(lineChartU, timeDataU, valueDataU)
        var lineChartI = createLineChart(sheet, 35, 45)
        drawLineChart(lineChartI, timeDataI, valueDataI)
    }

    private fun createLineChart(sheet: XSSFSheet, rowStart: Int, rowEnd: Int): XSSFChart {
        val drawing = sheet.createDrawingPatriarch()
        val anchor = drawing.createAnchor(0, 0, 0, 0, 5, rowStart, 18, rowEnd)

        return drawing.createChart(anchor)
    }

    private fun drawLineChart(
        lineChart: XSSFChart,
        xAxisData: ChartDataSource<Number>,
        yAxisData: ChartDataSource<Number>
    ) {
        val data = lineChart.chartDataFactory.createLineChartData()

        val xAxis = lineChart.chartAxisFactory.createCategoryAxis(AxisPosition.BOTTOM)
        val yAxis = lineChart.createValueAxis(AxisPosition.LEFT)
        yAxis.crosses = org.apache.poi.ss.usermodel.charts.AxisCrosses.AUTO_ZERO

        val series = data.addSeries(xAxisData, yAxisData)
        series.setTitle("График")
        lineChart.plot(data, xAxis, yAxis)

        val plotArea = lineChart.ctChart.plotArea
        plotArea.lineChartArray[0].smooth
        val ctBool = CTBoolean.Factory.newInstance()
        ctBool.`val` = false
        plotArea.lineChartArray[0].smooth = ctBool
        for (series in plotArea.lineChartArray[0].serArray) {
            series.smooth = ctBool
        }
    }

}