package regres;

import lib.SQLOracleUtils;
import lib.StartParameters;
import lib.StartSuid;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.sql.Connection;
import java.sql.SQLException;

import static junit.framework.TestCase.assertTrue;

/**
 * Класс для проверки работы механизма кадрового загрузчика
 *
 * @author VO Losev
 */

public class Z_HR_Data_Loader_3 {

    private static WebDriver driverSys;
    private  Connection conn;
    private int DepartmentsFulllength;

    @BeforeClass
    public static void start() {

        StartParameters.getStartParameters();
        driverSys = StartSuid.startSys();
    }

    @Test(timeout=10000000)
    public void verifyHRLoader() {
        try {

            String connStr = StartParameters.connStr;
            String connUser = StartParameters.connUser;
            String connPass = StartParameters.connPass;
            conn = SQLOracleUtils.connect(connStr, connUser, connPass);

            /**
             * Изменяем данные в схеме SUID_INTMDT и GPN_OIM, осуществляем проверку их соответсвия
             */

            String[] VerifiedValue = new String[6];

            VerifiedValue[0] = "ЯЭР_TEST_HR_SUCCSESS_5555";   //Company
            String DepartmentsFull = "TEST5555 Направление по сопровождению производственного и оперативного учета";
            DepartmentsFulllength = DepartmentsFull.length();
            VerifiedValue[1] = DepartmentsFull.substring(0, DepartmentsFulllength);  //Departments
            VerifiedValue[2] = "Коля5555";  //Persons
            VerifiedValue[3] = "Ростов5555";  //Employments
            VerifiedValue[4] = "TO_DATE('2019-08-05', 'YYYY-MM-DD')";  //Vacation
            VerifiedValue[5] = "Техник по учету автотранспорта ТЕСТОВЫЙ5555";  //Titles

            changeDataDB(VerifiedValue, conn);


            /**
             * Возвращаем данные в схеме SUID_INTMDT и GPN_OIM в исходное состояние, осуществляем проверку их соответсвия
             */

            driverSys.quit();
            Thread.sleep(5000);
            driverSys = StartSuid.startSys();

            VerifiedValue[0] = "ЯЭР_TEST_HR_SUCCSESS";   //Company
            DepartmentsFull = "Направление по сопровождению производственного и оперативного учета";
            DepartmentsFulllength = DepartmentsFull.length();
            VerifiedValue[1] = DepartmentsFull.substring(0, DepartmentsFulllength); //Departments
            VerifiedValue[2] = "Николай";  //Persons
            VerifiedValue[3] = "Москва";  //Employments
            VerifiedValue[4] = "TO_DATE('2018-01-01', 'YYYY-MM-DD')";  //Vacation
            VerifiedValue[5] = "Техник по учету автотранспорта";  //Titles

            changeDataDB(VerifiedValue, conn);

            conn.close();
            System.out.println("+++");

        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        catch (SQLException sqle){
            sqle.printStackTrace();
        }
    }


    @AfterClass
    public static void close() {

        driverSys.quit();
    }


    /**
     * метод осуществляющий изменение данных указанных в качестве параметра для в схеме SUID_INTMDT и их сравнеие с данными в схеме
     * GPN_OIM после окончания работы шедулера Z HR Data Loader 3.
     *
     */

    public void changeDataDB(String[] VerifiedValue, Connection conn) throws InterruptedException {


        /**
         * Изменяем данные в схеме SUID_INTMDT и контролируем прокидывание измененных данных в схему GPN_OIM
         */


        String updStr  = "update SUID_INTMDT.COMPANIES t set t.COMPANY_NAME = '" + VerifiedValue[0] + "', t.done = 0 where COMPANY_NAME like 'ЯЭР%'";
        String updStr1 = "update SUID_INTMDT.DEPARTMENTS t set T.DEPARTMENT_NAME = '" + VerifiedValue[1] + "', t.done = 0 where T.DEPARTMENT_ID_FULL = '[1C-34][00000000081]'";
        String updStr2 = "update SUID_INTMDT.PERSONS t set t.FIRST_NAME = '" + VerifiedValue[2] + "', t.done = 0 where t.UNQ_ID = '0AC6C3A2396050DEE053F21E320A9B90'";
        String updStr3 = "update SUID_INTMDT.EMPLOYMENTS t set t.CITY = '" + VerifiedValue[3] + "', t.done = 0 where t.EMPLOYEE_ID = '[_РП0001269][29022902-34DC-11E4-A82C-0025B3DEFD14]'";
        String updStr4 = "update SUID_INTMDT.VACATION t set t.VACATION_END = " + VerifiedValue[4] + ", t.done = 0, t.deleted = 0  where VACATION_ID = '8AD0AA0F-6572-11E4-A815-0025B3DEFD14                                                                '";
        String updStr5 = "update SUID_INTMDT.TITLES t set t.TITLE_NAME = '" + VerifiedValue[5] + "', t.done = 0  where t.TITLE_ID_FULL = '[1C-10][000000145]'";


        SQLOracleUtils.changeDataDB(updStr, conn);
        SQLOracleUtils.changeDataDB(updStr1, conn);
        SQLOracleUtils.changeDataDB(updStr2, conn);
        SQLOracleUtils.changeDataDB(updStr3, conn);
        SQLOracleUtils.changeDataDB(updStr4, conn);
        SQLOracleUtils.changeDataDB(updStr5, conn);


        /**
         * Запускаем  Z HR Data Loader 3
         */

        Thread.sleep(2000);
        StartSuid.startshedulers("Z HR Data Loader 3");

        /**
         * Ждем когда закончится выполнение  Z HR Data Loader 3
         */

        String statusHRLoader = "1";
        String statusSQL = "select status from GPN_OIM.JOB_HISTORY job where id in (select MAX(ID) from GPN_OIM.job_history where JOB_NAME = 'Z HR Data Loader 3')";


        while(!statusHRLoader.equals("2") ) {

            statusHRLoader = SQLOracleUtils.selectStrValueDB(statusSQL, conn);
            Thread.sleep(10000);
            //System.out.print("statusHRLoader ----- " + statusHRLoader);

        }

        /**
         * Выбираем данные из схемы GPN_OIM
         */

        String selectSTR = "select t.COMPANY_NAME from GPN_OIM.Z_COMPANIES t where t.COMPANY_NAME = '" + VerifiedValue[0] + "'";
        String selectSTR1Full = "select t.ACT_NAME from GPN_OIM.ACT t where t.ORG_UDF_DEPARTMENT_ID_FULL = '[1C-34][00000000081]'";
        String selectSTR2 = "select t.USR_FIRST_NAME from GPN_OIM.USR t where t.USR_UDF_UNIQUE_ID = '0AC6C3A2396050DEE053F21E320A9B90'";
        String selectSTR3 = "select t.CITY from GPN_OIM.Z_EMPLOYMENTS t where t.EMPLOYEE_ID = '[_РП0001269][29022902-34DC-11E4-A82C-0025B3DEFD14]'";
        String selectSTR4 = "select t.VACATION_END from GPN_OIM.Z_VACATIONS t where VACATION_ID = '8AD0AA0F-6572-11E4-A815-0025B3DEFD14                                                                '";
        String selectSTR41 = "select t.VACATION_END from SUID_INTMDT.VACATION t where VACATION_ID = '8AD0AA0F-6572-11E4-A815-0025B3DEFD14                                                                '";
        String selectSTR5 = "select t.TITLE_NAME from GPN_OIM.Z_TITLES t  where t.TITLE_ID_FULL = '[1C-10][000000145]'";

        String verifyValueSelect = SQLOracleUtils.selectStrValueDB(selectSTR, conn);

        String selectSTR1 = SQLOracleUtils.selectStrValueDB(selectSTR1Full, conn);
        String verifyValueSelect1 = selectSTR1.substring(0,DepartmentsFulllength);

        String verifyValueSelect2 = SQLOracleUtils.selectStrValueDB(selectSTR2, conn);
        String verifyValueSelect3 = SQLOracleUtils.selectStrValueDB(selectSTR3, conn);
        String verifyValueSelect4 = SQLOracleUtils.selectStrValueDB(selectSTR4, conn);
        String verifyValueSelect41 = SQLOracleUtils.selectStrValueDB(selectSTR41, conn);
        String verifyValueSelect5 = SQLOracleUtils.selectStrValueDB(selectSTR5, conn);

        /**
         * Проверяем данные в схеме GPN_OIM на соответствие  данным в схеме SUID_INTMDT
         */



        assertTrue(VerifiedValue[0].equals(verifyValueSelect));

        assertTrue(VerifiedValue[1].equals(verifyValueSelect1));

        assertTrue(VerifiedValue[2].equals(verifyValueSelect2));

        assertTrue(VerifiedValue[3].equals(verifyValueSelect3));

        assertTrue(verifyValueSelect41.equals(verifyValueSelect4));

        assertTrue(VerifiedValue[5].equals(verifyValueSelect5));

        


    }

}
