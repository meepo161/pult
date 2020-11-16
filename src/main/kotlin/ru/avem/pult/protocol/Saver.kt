package ru.avem.pult.protocol

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
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
        copyFileFromStream(MainApp::class.java.getResource("form1.xlsx").openStream(), template)

        try {
            XSSFWorkbook(template).use {
                val sheet = it.getSheetAt(0)
                for (iRow in 0 until 37) {
                    val row = sheet.getRow(iRow)
                    if (row != null) {
                        for (iCell in 0 until 6) {
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
                sheet.protectSheet(SHEET_PASSWORD)
                val outStream = ByteArrayOutputStream()
                it.write(outStream)
                outStream.close()
            }
        } catch (e: FileNotFoundException) {
        }
    }
}