package regres;

import lib.SQLOracleUtils;
import lib.StartParameters;
import lib.StartSuid;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.sql.Connection;
import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;

        /*
        * Класс для проверки  прохождения маршрута согласования заявок типа "Запрос доступа" для различных систем
        * AD, SAP, 1C, Прочие.
        * @author VO Losev
        */

public class AgreementRoute {

    private static WebDriver driver;
    private static WebDriver driverSys;
    private Connection conn;
    private String requectStrState;
    private String requectStep;
    private String requectItem;
    private String requectID;
    private String requectIDM;
    private String requect_request_idm;

    @BeforeClass
    public static void start() {

        StartParameters.getStartParameters();

    }
    @Test(timeout = 100000000)
    public void verifyRoute() throws Exception {

        String system;

        /**
         *  Соединяемся с БД
         */

        conn = SQLOracleUtils.connect();

        /**
         * Тест по проверке маршрута прохождения заявок AD
         */

        system = "AD";
        this.createRequestAD();
        this.startShedulers(system);
        this.DBRequest();
        this.routeRequest(system);
        System.out.println("Тест по проверке маршрута прохождения заявок AD завершился успешно");

        /**
         * Тест по проверке маршрута прохождения заявок ПРОЧИЕ
         */

        system = "Other";
        this.createRequesOther();
        this.startShedulers(system);
        this.DBRequest();
        this.routeRequest(system);
        System.out.println("Тест по проверке маршрута прохождения заявок Other завершился успешно");

        /**
         * Тест по проверке маршрута прохождения заявок SAP
         */

        system = "SAP";
        this.createRequestSAP();
        this.startShedulers(system);
        this.DBRequest();
        this.routeRequest(system);
        System.out.println("Тест по проверке маршрута прохождения заявок SAP завершился успешно");

        /**
         * Тест по проверке маршрута прохождения заявок 1C
         */

        system = "1C";
        this.createRequest1C();
        this.startShedulers(system);
        this.DBRequest();
        this.routeRequest(system);
        System.out.println("Тест по проверке маршрута прохождения заявок 1C завершился успешно");

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
     * метод выполняющий ожидание завершения SOD для заявок SAP
     */

    public void responseSAPGRT() throws Exception {

        String stepSAP = "with zps0 as (select step_id, idm_process_id, task_id from gpn_oim.z_process_step)\n" +
                "select MAX(zps2.step_id) from gpn_oim.z_request_item t\n" +
                "  join gpn_oim.z_requests requ on requ.id = t.request_id\n" +
                "  join gpn_oim.z_misc_attrs ma on ma.ATTRNAME = requ.request_type\n" +
                "  join gpn_oim.usr u on u.usr_key=t.beneficiary_key\n" +
                "  join gpn_oim.request req on t.idm_request_id=req.request_key\n" +
                "  join (select step_id, idm_process_id from zps0) zps on t.idm_request_id=zps.idm_process_id\n" +
                "  join zps0 zps2 on zps2.step_id=zps.step_id\n" +
                "  join gpn_oim.z_resource_access ra on ra.id=t.requested_object_id\n" +
                "  join gpn_oim.z_resources r on r.id=ra.resource_id \n" +
                "  join gpn_oim.z_logical_systems l on l.id=r.system_id \n" +
                "  join gpn_oim.z_resource_access_type_info ri on ri.id=ra.resource_access_type_id\n" +
                "  join gpn_oim.z_process_step_attrs zpsa on zpsa.step_id=zps.step_id and zpsa.attrname in ('step_name','calculation:user_approvers','task_comment')\n" +
                "  left join gpn_oim.z_process_step_approvers apprvs on zpsa.attrvalue=apprvs.step_name\n" +
                "  left join gpn_soainfra.wftask wft on zps2.task_id=wft.TASKGROUPID and (wft.STATE='ASSIGNED' or wft.STATE is null)\n" +
                "  left join gpn_soainfra.wftask wft1 on zps2.task_id=wft1.TASKID and (wft1.STATE='ASSIGNED' or wft1.STATE is null)\n" +
                "  left join gpn_oim.usr u1 on nvl(wft.updatedby,wft1.updatedby) = lower(u1.usr_login)\n" +
                "  left join gpn_soainfra.wfcomments wfc on wft.TASKID=wfc.taskid or wft1.TASKGROUPID=wfc.taskid\n" +
                "  left join gpn_oim.z_request_item_attr zia on t.id=requet_item_id and zia.ATTRNAME='AccountContext'\n" +
                "  left join GPN_OIM.Z_ACCESS_CONTEXT ac on ac.id=zia.attrvalue\n" +
                "  left join gpn_oim.oiu o on o.oiu_key=ac.target_object_id and ac.context_type = 'account'\n" +
                "  left join gpn_oim.ud_sapuser su on su.orc_key=o.orc_key\n" +
                "  left join gpn_oim.orc orc on orc.orc_key=o.orc_key \n" +
                "  where t.request_id = '" + requectID + "'";


        String  stepIDSAP = SQLOracleUtils.selectStrValueDB(stepSAP, conn);

        String requectStepTypeSAP = "select t.STEP_TYPE from GPN_OIM.Z_PROCESS_STEP t where t.step_id = '" + stepIDSAP + "'";
        String requectSAPifSODres = SQLOracleUtils.selectStrValueDB(requectStepTypeSAP, conn);

        int i =0;
        while(requectSAPifSODres.equals("SOD")) {
            i++;
            if(i==1) {
                this.Z_HandleRequests();
            }
            Thread.sleep(30000);
            stepIDSAP = SQLOracleUtils.selectStrValueDB(stepSAP, conn);
            requectStepTypeSAP = "select t.STEP_TYPE from GPN_OIM.Z_PROCESS_STEP t where t.step_id = '" + stepIDSAP + "'";
            requectSAPifSODres = SQLOracleUtils.selectStrValueDB(requectStepTypeSAP, conn);
        }

    }

    /**
     * метод для запука шедулеров Z_ApprovalProcessInitiator, Z_HandleRequests
     */

    public void startShedulers(String system) throws Exception {

        driverSys = StartSuid.startSys();
        Thread.sleep(3000);
        StartSuid.startshedulersANDwait("Z_ApprovalProcessInitiator");
        if(system.equals("SAP")) {
            StartSuid.startshedulersANDwait("Z_HandleRequests");
        }
        Thread.sleep(2000);
        driverSys.quit();

    }

    /**
     * метод для запука шедулера Z_HandleRequests
     */

    public void Z_HandleRequests() throws Exception {

        driverSys = StartSuid.startSys();
        Thread.sleep(3000);
        StartSuid.startshedulersANDwait("Z_HandleRequests");
        Thread.sleep(2000);
        driverSys.quit();
    }

    /**
     * метод для создания заявки AD
     */

    public void createRequestAD() throws Exception {

        driver = StartSuid.startSD();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region0:0:link3")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:searchBtn")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:dc_it1::content")).clear();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:dc_it1::content")).sendKeys("Абатуров Илья");
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:searchBtn")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:pc1:j_id__ctru24pc21:24:dc_sbc1::content")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(4000);
        //driver.findElement(By.id("pt1:_d_reg:region1:1:z_fts:j_id__ctru22pc22:2:dc_sbc1::content")).click();
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_fts:j_id__ctru22pc22:5:dc_sbc3::content")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:dc6:dc_it1::content")).clear();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:dc6:dc_it1::content")).sendKeys("111");
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru13pc19")).click();
        Thread.sleep(5000);
        driver.quit();
    }

    /**
     * метод для создания заявки SAP
     */

    public void createRequestSAP() throws Exception {

        driver = StartSuid.startSD();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region0:0:link3")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:dc_it1::content")).clear();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:dc_it1::content")).sendKeys("Абатуров Илья");
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:searchBtn")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:pc1:j_id__ctru24pc21:1:dc_sbc1::content")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru15pc16::disAcr")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_saprolesearch:sf_01:searchBtn")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_saprolesearch:pc1:j_id__ctru53pc24:1:j_id__ctru57pc24::content")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(3000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:dc6:dc_it1::content")).clear();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:dc6:dc_it1::content")).sendKeys("111");
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru13pc19")).click();
        Thread.sleep(5000);
        driver.quit();

    }

    /**
     * метод для создания заявки 1С
     */

    public void createRequest1C() throws Exception {

        driver = StartSuid.startSD();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region0:0:link3")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:dc_it1::content")).clear();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:dc_it1::content")).sendKeys("Абатуров Илья");
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:searchBtn")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:pc1:j_id__ctru24pc21:1:dc_sbc1::content")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru17pc16::disAcr")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_othres_1c:sf_01:searchBtn")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_othres_1c:j_id__ctru41pc31:0:j_id__ctru45pc31::content")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:dc6:dc_it1::content")).clear();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:dc6:dc_it1::content")).sendKeys("5");
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru13pc19")).click();
        Thread.sleep(5000);
        driver.quit();
    }

    /**
     *  метод для создания заявки ПРОЧИЕ
     */

    public void createRequesOther() throws Exception {

        driver = StartSuid.startSD();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region0:0:link3")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:searchBtn")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:dc_it1::content")).clear();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:dc_it1::content")).sendKeys("Абатурова");
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:sf_01:searchBtn")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_rolessel:pc1:j_id__ctru24pc21:25:dc_sbc1")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru19pc16::disAcr")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_othres:sf_01:searchBtn")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:z_othres:j_id__ctru41pc38:0:j_id__ctru45pc38::content")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru12pc19")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:dc6:dc_it1::content")).sendKeys("111");
        Thread.sleep(2000);
        driver.findElement(By.id("pt1:_d_reg:region1:1:j_id__ctru2pc16:j_id__ctru15pc17:j_id__ctru7pc18:j_id__ctru13pc19")).click();
        Thread.sleep(5000);
        driver.quit();
    }

    /**
     * метод осуществляющий согласование заявки и проверку корректности
     */

    public void routeRequest(String system) throws Exception {

        driver = StartSuid.startSD();
        Thread.sleep(2000);
        Boolean requectState = true;
        Thread.sleep(3000);
        driver.findElement(By.id("pt1:_d_reg:region0:0:pc1:t1:0:clid2")).click();

        while(requectState) {
            if(system.equals("SAP")) {
                this.responseSAPGRT();
            }
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
            if(driver.findElement(By.id("d1::msgDlg::cancel")).isDisplayed()) {
                driver.findElement(By.id("d1::msgDlg::cancel")).click();
                Thread.sleep(2000);
            }

            Thread.sleep(2000);
            driver.findElement(By.id("pt1:_d_reg:region1:1:dc8:j_id__ctru4pc71::content")).sendKeys("5");

            Thread.sleep(2000);
            driver.findElement(By.id("pt1:_d_reg:region1:1:dc8:j_id__ctru3pc71::ok")).click();
            Thread.sleep(1000);

            if(driver.findElement(By.id("d1::msgDlg::cancel")).isDisplayed()) {
                driver.findElement(By.id("d1::msgDlg::cancel")).click();
                Thread.sleep(2000);
            }

            ArrayList selectStepArray1 = SQLOracleUtils.selectStrValueDBArray(requectStep, conn);
            assertTrue(String.valueOf(selectStepArray1.size()).equals("1"));

        }

        driver.quit();

    }

}
