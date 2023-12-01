package io.sim.repport;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import io.sim.bank.BankService;
import io.sim.driver.Driver;
import io.sim.driver.DrivingData;
import io.sim.simulation.EnvSimulator;

public class ExcelRepport 
{
    private static final String FILE_NAME_DD = "DrivingDataRepport.xlsx";
    private static final String FILE_NAME_BS = "BankServiceRepport.xlsx";
    
    public static void ssDrivingDataCreator() throws IOException
    {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Drivers");
        headerCreatorDD(sheet);
        FileOutputStream outputStream = new FileOutputStream(FILE_NAME_DD);
        workbook.write(outputStream);

        System.out.println("Planilha DrivingData criada");
        workbook.close();
    }
    
    private static void headerCreatorDD(Sheet _sheet )
    {
        Row row = _sheet.createRow(0);

        row.createCell(0).setCellValue("Timestamp");
        row.createCell(1).setCellValue("ID Car");
        row.createCell(2).setCellValue("ID Route");
        row.createCell(3).setCellValue("Speed");
        row.createCell(4).setCellValue("Distance");
        row.createCell(5).setCellValue("FuelConsumption");
        row.createCell(6).setCellValue("FuelType");
        row.createCell(7).setCellValue("CO2Emission");
        row.createCell(8).setCellValue("longitude (lon)");
        row.createCell(9).setCellValue("latitude (lat)");
    }

    public static synchronized void updateSSDrivingData(DrivingData _carRepport) throws EncryptedDocumentException, IOException
    {
        FileInputStream inputStream = new FileInputStream(FILE_NAME_DD);
        Workbook workbook = WorkbookFactory.create(inputStream);
        FileOutputStream outputStream = new FileOutputStream(FILE_NAME_DD);
        Sheet sheet = workbook.getSheetAt(0);

        int lastRowNum = sheet.getLastRowNum();
        Row ceil = sheet.createRow(lastRowNum + 1);

        ceil.createCell(0).setCellValue(_carRepport.getTimeStamp());
        ceil.createCell(1).setCellValue(_carRepport.getCarID());
        ceil.createCell(2).setCellValue(_carRepport.getRouteIDSUMO());
        ceil.createCell(3).setCellValue(_carRepport.getSpeed());
        ceil.createCell(4).setCellValue(_carRepport.getDistance()); 
        ceil.createCell(5).setCellValue(_carRepport.getFuelConsumption());
        ceil.createCell(6).setCellValue(_carRepport.getFuelType());
        ceil.createCell(7).setCellValue(_carRepport.getCo2Emission());
        ceil.createCell(8).setCellValue(_carRepport.getLongitude());
        ceil.createCell(9).setCellValue(_carRepport.getLatitude());

        workbook.write(outputStream);
    }

    public static void ssBankServiceCreator(String _companyLogin, ArrayList<Driver> _drivers, String _fStationLogin) throws IOException // TODO adicionar lista para rodar
    {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheetCompany = workbook.createSheet(_companyLogin); 
        headerCreatorBS(sheetCompany);

        Sheet sheetFstation = workbook.createSheet(_fStationLogin); 
        headerCreatorBS(sheetFstation);

        Sheet sheetDriver;
        for(Driver driver : _drivers)
        {
            sheetDriver = workbook.createSheet(driver.getAccountLogin()); // nome da aba
            headerCreatorBS(sheetDriver);
        }

        FileOutputStream outputStream = new FileOutputStream(FILE_NAME_BS);
        workbook.write(outputStream);

        System.out.println("Planilha BankService criada");
        workbook.close();
    }
    
    private static void headerCreatorBS(Sheet _sheet )
    {
        Row row = _sheet.createRow(0);

        row.createCell(0).setCellValue("Pagador");
        row.createCell(1).setCellValue("Valor");
        row.createCell(2).setCellValue("Recebedor");
        row.createCell(3).setCellValue("Timestamp");
    }

    public static synchronized void updateSSBankService(BankService _service) throws EncryptedDocumentException, IOException
    {
        FileInputStream inputStream = new FileInputStream(FILE_NAME_BS);
        Workbook workbook = WorkbookFactory.create(inputStream);
        FileOutputStream outputStream = new FileOutputStream(FILE_NAME_BS);
        Sheet sheet = workbook.getSheet(_service.getOrigem());

        int lastRowNum = sheet.getLastRowNum();
        Row ceil = sheet.createRow(lastRowNum + 1);

        ceil.createCell(0).setCellValue(_service.getOrigem());
        ceil.createCell(1).setCellValue(_service.getValor());
        ceil.createCell(2).setCellValue(_service.getDestino());
        ceil.createCell(3).setCellValue(_service.getTimestamp());

        workbook.write(outputStream);
    }

    public static void setFlowParam(int _edgesSize) throws EncryptedDocumentException, IOException
    {
        FileInputStream inputStream = new FileInputStream(FILE_NAME_DD);
        Workbook workbook = WorkbookFactory.create(inputStream);
        FileOutputStream outputStream = new FileOutputStream(FILE_NAME_DD);
        Sheet sheet = workbook.createSheet("Fluxos");

        // TODO cabecalho

        int nFlow = _edgesSize/EnvSimulator.FLOW_SIZE;
        int passo = nFlow + 1;
        int rownum = 1;

        for(int i=2; i<(passo*EnvSimulator.AV2_CICLE + 1); i+= passo)
        {
            Row row = sheet.createRow(rownum);
            for(int j=0; j<nFlow; j++)
            {
                String t_i = "Drivers!A" + String.valueOf(j + i + 1) + " - Drivers!A" + String.valueOf(j+i);
                row.createCell(j).setCellFormula("10^-6*(" + t_i + ")/" + String.valueOf(EnvSimulator.ACQUISITION_RATE));
            }
            String t_i = "Drivers!A" + String.valueOf(nFlow + i) + " - Drivers!A" + String.valueOf(i);
            row.createCell(nFlow).setCellFormula("10^-6*(" + t_i + ")/" + String.valueOf(EnvSimulator.ACQUISITION_RATE));
            rownum++;
        }

        workbook.write(outputStream);
    }

    public static void setRecParam(int _edgesSize) throws EncryptedDocumentException, IOException
    {
        FileInputStream inputStream = new FileInputStream(FILE_NAME_DD);
        Workbook workbook = WorkbookFactory.create(inputStream);
        FileOutputStream outputStream = new FileOutputStream(FILE_NAME_DD);
        Sheet sheet = workbook.getSheet("Fluxos");

        // FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        Row rowMed = sheet.createRow(EnvSimulator.AV2_CICLE + 2);
        Row rowDP = sheet.createRow(EnvSimulator.AV2_CICLE + 3);

        int nFlow = _edgesSize/EnvSimulator.FLOW_SIZE;
        char colum = 'A';
        String media = "AVERAGE("; // MÉDIA
        String desvpad = "SQRT(VARP(";
        // Cell aux_Cell;
        for(int i=0; i<nFlow; i++)
        {
            String med = media + colum + "2:" + colum + String.valueOf(1 + EnvSimulator.AV2_CICLE) + ")";
            String dp = desvpad + colum + "2:" + colum + String.valueOf(1 + EnvSimulator.AV2_CICLE) + "))";
            rowMed.createCell(i).setCellFormula(med); 
            rowDP.createCell(i).setCellFormula(dp);

            // teste
            // aux_Cell = rowMed.getCell(i);
            // evaluator.evaluateFormulaCell(aux_Cell);
            // aux_Cell = rowDP.getCell(i);
            // evaluator.evaluateFormulaCell(aux_Cell);
            colum ++;
        }
        String med_f = media + colum + "2:" + colum + String.valueOf(1 + EnvSimulator.AV2_CICLE) + ")";
        String dp_f = desvpad + colum + "2:" + colum + String.valueOf(1 + EnvSimulator.AV2_CICLE) + "))";
        rowMed.createCell(nFlow).setCellFormula(med_f);
        rowDP.createCell(nFlow).setCellFormula(dp_f);

        workbook.write(outputStream);

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
    }
}
