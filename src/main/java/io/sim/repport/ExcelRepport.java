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

    public static void headerCreatorFlow(Sheet _sheet, int _nFlow)
    {
        Row row = _sheet.createRow(0);
        String time = "t";
        String distance = "d";
        for(int i=0; i<_nFlow; i++)
        {
            row.createCell(i).setCellValue(time + String.valueOf(i+1));
            row.createCell(i+_nFlow+2).setCellValue(distance + String.valueOf(i+1));
        }
        row.createCell(_nFlow).setCellValue("T");
        row.createCell(2*_nFlow +2).setCellValue("D");
    }

    public static void setFlowParam(int _edgesSize) throws EncryptedDocumentException, IOException
    {
        FileInputStream inputStream = new FileInputStream(FILE_NAME_DD);
        Workbook workbook = WorkbookFactory.create(inputStream);
        FileOutputStream outputStream = new FileOutputStream(FILE_NAME_DD);
        Sheet sheet = workbook.createSheet("Fluxos");

        int nFlow = _edgesSize/EnvSimulator.FLOW_SIZE;

        headerCreatorFlow(sheet, nFlow);
        int passo = nFlow + 1;
        int rownum = 1;

        for(int i=2; i<(passo*EnvSimulator.AV2_CICLE + 1); i+= passo)
        {
            Row row = sheet.createRow(rownum);
            for(int j=0; j<nFlow; j++)
            {
                String t_i = "Drivers!A" + String.valueOf(j + i + 1) + " - Drivers!A" + String.valueOf(j+i);
                String d_i = "Drivers!E" + String.valueOf(j + i + 1) + " - Drivers!E" + String.valueOf(j+i);
                row.createCell(j).setCellFormula("10^-7*(" + t_i + ")/" + String.valueOf(EnvSimulator.ACQUISITION_RATE));
                row.createCell(j+nFlow+2).setCellFormula(d_i);
            }
            String t_total = "Drivers!A" + String.valueOf(nFlow + i) + " - Drivers!A" + String.valueOf(i);
            String d_total = "Drivers!E" + String.valueOf(nFlow + i) + " - Drivers!E" + String.valueOf(i);
            row.createCell(nFlow).setCellFormula("10^-7*(" + t_total + ")/" + String.valueOf(EnvSimulator.ACQUISITION_RATE));
            row.createCell(2*nFlow + 2).setCellFormula(d_total);

            rownum++;
        }

        workbook.write(outputStream);
    }

    public static void setRecParam(int _edgesSize) throws EncryptedDocumentException, IOException
    {
        // Configuracoes iniciais
        FileInputStream inputStream = new FileInputStream(FILE_NAME_DD);
        Workbook workbook = WorkbookFactory.create(inputStream);
        FileOutputStream outputStream = new FileOutputStream(FILE_NAME_DD);
        Sheet sheet = workbook.getSheet("Fluxos");

        // Cabecalhos
        Row rowMed = sheet.createRow(EnvSimulator.AV2_CICLE + 2);
        rowMed.createCell(0).setCellValue("Média");
        Row rowDP = sheet.createRow(EnvSimulator.AV2_CICLE + 4);
        rowDP.createCell(0).setCellValue("Desvio Padrão");

        // Linhas para media e desvio padrao
        rowMed = sheet.createRow(EnvSimulator.AV2_CICLE + 3);
        rowDP = sheet.createRow(EnvSimulator.AV2_CICLE + 5);

        // Inicializando variaveis
        int nFlow = _edgesSize/EnvSimulator.FLOW_SIZE;
        String media = "AVERAGE(";
        String desvpad = "SQRT(VARP(";
            // Variaveis tempo
        char columT1 = 'A';
        char columT2 = 'A';
        boolean columT_ValDouble = false;
        String columT = "";
            // Variaveis distancia
        char columD1 = columT1;
        columD1 += (nFlow + 2);
        char columD2 = columT2;
        columD2 += (nFlow + 2);
        boolean columD_ValDouble = false;
        String columD = "";

        for(int i=0; i<nFlow; i++)
        {
            if(columT_ValDouble)
            {
                columT = String.valueOf(columT2 + columT1);
            }
            else
            {
                columT = String.valueOf(columT1);
            }

            if(columD_ValDouble)
            {
                columD = String.valueOf(columD2 + columD1);
            }
            else
            {
                columD = String.valueOf(columD1);
            }

            String med = media + columT + "2:" + columT + String.valueOf(1 + EnvSimulator.AV2_CICLE) + ")";
            String dp = desvpad + columT + "2:" + columT + String.valueOf(1 + EnvSimulator.AV2_CICLE) + "))";
            rowMed.createCell(i).setCellFormula(med); 
            rowDP.createCell(i).setCellFormula(dp);

            med = media + columD + "2:" + columD + String.valueOf(1 + EnvSimulator.AV2_CICLE) + ")";
            dp = desvpad + columD + "2:" + columD + String.valueOf(1 + EnvSimulator.AV2_CICLE) + "))";
            rowMed.createCell(i + nFlow + 2).setCellFormula(med); 
            rowDP.createCell(i + nFlow + 2).setCellFormula(dp);

            // Atualiza valor das colunas | nao considera que passe da coluna ZZ
            if(columT1 == 'Z')
            {
                columT1 = 'A';
                if(columT_ValDouble)
                {
                    columT2++;
                }
                else
                {
                    columT_ValDouble = true;
                }
            }
            else
            {
                columT1++;
            }

            if(columD1 == 'Z')
            {
                columD1 = 'A';
                if(columD_ValDouble)
                {
                    columD2++;
                }
                else
                {
                    columD_ValDouble = true;
                }
            }
            else
            {
                columD1++;
            }
            
        }
        String med_f = media + columT + "2:" + columT + String.valueOf(1 + EnvSimulator.AV2_CICLE) + ")";
        String dp_f = desvpad + columT + "2:" + columT + String.valueOf(1 + EnvSimulator.AV2_CICLE) + "))";
        rowMed.createCell(nFlow).setCellFormula(med_f);
        rowDP.createCell(nFlow).setCellFormula(dp_f);
        med_f = media + columD + "2:" + columD + String.valueOf(1 + EnvSimulator.AV2_CICLE) + ")";
        dp_f = desvpad + columD + "2:" + columD + String.valueOf(1 + EnvSimulator.AV2_CICLE) + "))";
        rowMed.createCell(2*nFlow + 2).setCellFormula(med_f);
        rowDP.createCell(2*nFlow + 2).setCellFormula(dp_f);

        workbook.write(outputStream);

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook); // atualiza o valor das formulas
    }

    
}
