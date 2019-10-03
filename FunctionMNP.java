package regres;

import lib.SQLOracleUtils;
import lib.StartSuid;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.sql.Connection;
import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;

/*
 * Класс для проверки МНП
 *
 * @author VO Losev
 */

public class FunctionMNP {

    private Connection conn;
    private static WebDriver driver;
    private static WebDriver driverSys;
    private String requectStrState;
    private String requectStep;
    private String requectItem;
    private String requectID;
    private String requectIDM;
    private String requect_request_idm;

    @Test(timeout = 10000000)
    public void reviewAccessRights() throws Exception {

        /**
         *  Соединяемся с БД
         */

        conn = SQLOracleUtils.connect();

        /**
         * формируем первую заявку типа "Пересмотр прав" с доступами СКИБ для пользователя Абатурова
         */

        String updHRsource = "update hrsource.hremp h\n" +
                "set h.department_id = '50457279', h.department_id_full = '[SAPHR][50457279]', h.done = 0, h.source_id = 'SAPHR', h.employee_status = 'w', h.employment_weight = 100,\n" +
                "h.end_date = to_date ('10.04.2051 12:00:00','dd.mm.yyyy hh24:mi:ss'), h.title_id= '50447836', h.title_id_full = '[SAPHR][50447836]'\n" +
                "where h.person_id = '[ТЮРДЕНЕВ][КИРИЛЛ][ВЛАДИМИРОВИЧ][19041977]'";

        SQLOracleUtils.changeDataDB(updHRsource, conn);

        this.startShedulers();
        //this.DBRequest();
        //this.routeRequest();

        Thread.sleep(10000);

        /**
         * формируем вторую заявку типа "Пересмотр прав" с доступами СКИБ для пользователя Абатурова (возвращаем title и department в исходное состояние)
         */

        updHRsource = "update hrsource.hremp h\n" +
                "set h.department_id = '50048215', h.department_id_full = '[SAPHR][50048215]', h.done = 0, h.source_id = 'SAPHR', h.employee_status = 'w', h.employment_weight = 100,\n" +
                "h.end_date = to_date ('10.04.2051 12:00:00','dd.mm.yyyy hh24:mi:ss'), h.title_id= '51592296', h.title_id_full = '[SAPHR][51592296]'\n" +
                "where h.person_id = '[ТЮРДЕНЕВ][КИРИЛЛ][ВЛАДИМИРОВИЧ][19041977]'";

        SQLOracleUtils.changeDataDB(updHRsource, conn);

        this.startShedulers();
       // this.DBRequest();
       // this.routeRequest();


    }



    /**
     * метод для запука шедулеров Z HR Data Loader 3, Z_ApprovalProcessInitiator, Z_HandleRequests и ожидания окончания их работы
     */

    public void startShedulers() throws Exception {

        driverSys = StartSuid.startSys();
        Thread.sleep(2000);

        StartSuid.startshedulersANDwait("Z HR Data Loader 3");
       // StartSuid.startshedulersANDwait("Z_ApprovalProcessInitiator");
       // StartSuid.startshedulersANDwait("Z_HandleRequests");

        driverSys.quit();

    }


    /**
     * метод выполняющий запросы к БД для определения шага согласования
     */

    public void DBRequest() throws Exception {

        requectItem = "select t.REQUEST_ID from GPN_OIM.Z_REQUEST_ITEM t where id in (select MAX(ID) from GPN_OIM.Z_REQUEST_ITEM)";
        requectID = SQLOracleUtils.selectStrValueDB(requectItem, conn);
        requectIDM = "select t.IDM_REQUEST_ID from GPN_OIM.Z_REQUEST_ITEM t where t.REQUEST_ID = '" + requectID + "'";
        requect_request_idm = SQLOracleUtils.selectStrValueDB(requectIDM, conn);
        requectStep = "select distinct t.STEP_NAME from GPN_OIM.JIVS_CURENT_STEP t where t.idm_request_id = '" + requect_request_idm + "'";
        requectStrState = "select t.APPROVAL_STATE from GPN_OIM.Z_REQUEST_ITEM t where t.REQUEST_ID = '" + requectID + "'";

    }


    /**
     * метод осуществляющий согласование заявки и проверку корректности
     */

    public void routeRequest() throws Exception {

        driver = StartSuid.startSD();
        Thread.sleep(2000);
        Boolean requectState = true;
        Thread.sleep(3000);
        driver.findElement(By.id("pt1:_d_reg:region0:0:pc1:t1:0:clid2")).click();

        while(requectState) {

            Thread.sleep(30000);
            WebElement el = driver.findElement(By.id("pt1:_d_reg:region1:1:pc2:t3:0:sbc2::content"));
            el.click();
            Thread.sleep(2000);
            driver.findElement(By.xpath("//div[@id='pt1:_d_reg:region1:1:pc2:dc9:j_id__ctru16pc62']/a/span")).click();

            String requectStateStr = SQLOracleUtils.selectStrValueDB(requectStrState, conn);
            Assert.assertNotEquals(requectStateStr, "Failed");

            if(requectStateStr != null) {
                if (requectStateStr.equals("Approved")) {
                    requectState = false;
                    break;
                }
            }

            Thread.sleep(2000);
            driver.findElement(By.id("pt1:_d_reg:region1:1:dc8:j_id__ctru4pc71::content")).sendKeys("5");

            Thread.sleep(2000);
            driver.findElement(By.id("pt1:_d_reg:region1:1:dc8:j_id__ctru3pc71::ok")).click();
            Thread.sleep(1000);

            ArrayList selectStepArray1 = SQLOracleUtils.selectStrValueDBArray(requectStep, conn);
            assertTrue(String.valueOf(selectStepArray1.size()).equals("1"));

        }

        driver.quit();
    }


}
