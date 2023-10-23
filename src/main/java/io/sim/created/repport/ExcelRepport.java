package io.sim.created.repport;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import io.sim.DrivingData;
import io.sim.created.BankService;
import io.sim.created.Driver;

public class ExcelRepport 
{
    private static final String FILE_NAME_DD = "DrivingDataRepport.xlsx";
    private static final String FILE_NAME_BS = "BankServiceRepport.xlsx";
    
    public static void ssDrivingDataCreator(ArrayList<Driver> _drivers) throws IOException
    {
        Workbook workbook = new XSSFWorkbook();
        for(Driver driver : _drivers)
        {
            Sheet sheet = workbook.createSheet(driver.getCarID()); // nome da aba
            headerCreatorDD(sheet);
        }
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
        Sheet sheet = workbook.getSheet(_carRepport.getCarID());

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
}
