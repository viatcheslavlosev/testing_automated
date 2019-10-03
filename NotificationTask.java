package regres;

import lib.AssertUtils;
import lib.SQLOracleUtils;
import lib.StartParameters;
import lib.StartSuid;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Класс для  проверки формирования и отправки нотификаций
 *
 * @author VO Losev
 */

public class NotificationTask {

    private static WebDriver driverSys;
    private  Connection conn;

    @BeforeClass
    public static void start() {

        StartParameters.getStartParameters();
        driverSys = StartSuid.startSys();
    }

    @Test(timeout=10000000)
    public void verifyNotificationTask() {
        try {

            /**
             * Получаем connection object
             *
             */

            String connStr = StartParameters.connStr;
            String connUser = StartParameters.connUser;
            String connPass = StartParameters.connPass;
            conn = SQLOracleUtils.connect(connStr, connUser, connPass);

            /**
             * Изменяем данные в gpn_oim.Z_NOTIFICATION_EVENTS, осуществляем проверку  после завершения работы
             * шедулера Z_NotificationTask
             */

            changeDataDB(conn);

            conn.close();
            System.out.println("+++");

        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }


    @AfterClass
    public static void close() {

        driverSys.quit();
    }

    /**
     * Метод осуществляющий установку для определенных событий (от 06.03.2019, уведомление для HRLoader) флага IS_NOTIFIED в "0" в таблице gpn_oim.Z_NOTIFICATION_EVENTS,
     * запуск и контроль завершения шедулера Z_NotificationTask,
     * проверку установки флага IS_NOTIFIED в "1" после окончания работы шедулера.
     *
     */

    public void changeDataDB(Connection conn) throws InterruptedException {


        /**
         * Изменяем данные в gpn_oim.Z_NOTIFICATION_EVENTS (установка флага IS_NOTIFIED в "0")
         */


        String updStrHRLoader  = "update GPN_OIM.Z_NOTIFICATION_EVENTS t set t.IS_NOTIFIED = 0 where t.MESSAGE_TYPE = 'HRLoader' and ADDITION_DATE > TO_DATE('2019-03-06', 'YYYY-MM-DD') and ADDITION_DATE < TO_DATE('2019-03-07', 'YYYY-MM-DD')";
        String updStrHPSMReq  = "update GPN_OIM.Z_NOTIFICATION_EVENTS t set t.IS_NOTIFIED = 0 where t.MESSAGE_TYPE = 'HPSMRequest' and ADDITION_DATE = TO_DATE('09.11.2018 16:27:59', 'DD-MM-YYYY hh24:mi:ss')";
        String updStrNewAccount  = "update GPN_OIM.Z_NOTIFICATION_EVENTS t set t.IS_NOTIFIED = 0 where t.MESSAGE_TYPE = 'NewAccount' and ADDITION_DATE > TO_DATE('2018-04-27', 'YYYY-MM-DD') and ADDITION_DATE < TO_DATE('2018-04-28', 'YYYY-MM-DD')";
        String updStrOverdueTask  = "update GPN_OIM.Z_NOTIFICATION_EVENTS t set t.IS_NOTIFIED = 0 where t.MESSAGE_TYPE = 'OverdueTask' and ADDITION_DATE = TO_DATE('24.12.2018 17:45:47', 'DD-MM-YYYY hh24:mi:ss')";
        String updStrRequestFinish  = "update GPN_OIM.Z_NOTIFICATION_EVENTS t set t.IS_NOTIFIED = 0  where t.MESSAGE_TYPE = 'RequestFinish' and ADDITION_DATE = TO_DATE('27.09.2018 14:15:22', 'DD-MM-YYYY hh24:mi:ss')";
        String updStrResourceCreation  = "update GPN_OIM.Z_NOTIFICATION_EVENTS t set t.IS_NOTIFIED = 0 where t.MESSAGE_TYPE = 'ResourceCreation' and ADDITION_DATE > TO_DATE('2018-10-01', 'YYYY-MM-DD') and ADDITION_DATE < TO_DATE('2018-10-31', 'YYYY-MM-DD')";
        String updStrSAPNewAccount  = "update GPN_OIM.Z_NOTIFICATION_EVENTS t set t.IS_NOTIFIED = 0 where t.MESSAGE_TYPE = 'SAPNewAccount' and ADDITION_DATE > TO_DATE('2017-06-01', 'YYYY-MM-DD') and ADDITION_DATE < TO_DATE('2018-12-01', 'YYYY-MM-DD')";
        String updStrTaskAssignment  = "update GPN_OIM.Z_NOTIFICATION_EVENTS t set t.IS_NOTIFIED = 0 where t.MESSAGE_TYPE = 'TaskAssignment' and ADDITION_DATE = TO_DATE('09.11.2018 14:53:19', 'DD-MM-YYYY hh24:mi:ss')";


        SQLOracleUtils.changeDataDB(updStrHRLoader, conn);
        SQLOracleUtils.changeDataDB(updStrHPSMReq, conn);
        SQLOracleUtils.changeDataDB(updStrNewAccount, conn);
        SQLOracleUtils.changeDataDB(updStrOverdueTask, conn);
        SQLOracleUtils.changeDataDB(updStrRequestFinish, conn);
        SQLOracleUtils.changeDataDB(updStrResourceCreation, conn);
        SQLOracleUtils.changeDataDB(updStrSAPNewAccount, conn);
        SQLOracleUtils.changeDataDB(updStrTaskAssignment, conn);


        /**
         * Запускаем  Z_NotificationTask"
         */

        Thread.sleep(2000);
        StartSuid.startshedulers("Z_NotificationTask");

        /**
         * Ждем когда закончится выполнение Z_NotificationTask"
         */

        String statusHRLoader = "1";
        String statusSQL = "select status from GPN_OIM.JOB_HISTORY job where id in (select MAX(ID) from GPN_OIM.job_history where JOB_NAME = 'Z_NotificationTask')";


        while(!statusHRLoader.equals("2") ) {

            statusHRLoader = SQLOracleUtils.selectStrValueDB(statusSQL, conn);
            Thread.sleep(10000);

        }

        /**
         * Выбираем данные из gpn_oim.Z_NOTIFICATION_EVENTS
         */

        String selectSTRHRLoader = "select t.IS_NOTIFIED from GPN_OIM.Z_NOTIFICATION_EVENTS t where t.MESSAGE_TYPE = 'HRLoader' and ADDITION_DATE > TO_DATE('2019-03-06', 'YYYY-MM-DD') and ADDITION_DATE < TO_DATE('2019-03-07', 'YYYY-MM-DD')";
        String selectSTRHPSMReq = "select t.IS_NOTIFIED from gpn_oim.Z_NOTIFICATION_EVENTS t where t.MESSAGE_TYPE = 'HPSMRequest' and ADDITION_DATE = TO_DATE('09.11.2018 16:27:59', 'DD-MM-YYYY hh24:mi:ss')";
        String selectSTRNewAccount = "select t.IS_NOTIFIED from gpn_oim.Z_NOTIFICATION_EVENTS t where t.MESSAGE_TYPE = 'NewAccount' and ADDITION_DATE > TO_DATE('2018-04-27', 'YYYY-MM-DD') and ADDITION_DATE < TO_DATE('2018-04-28', 'YYYY-MM-DD')";
        String selectSTROverdueTask = "select t.IS_NOTIFIED from gpn_oim.Z_NOTIFICATION_EVENTS t where t.MESSAGE_TYPE = 'OverdueTask' and ADDITION_DATE = TO_DATE('24.12.2018 17:45:47', 'DD-MM-YYYY hh24:mi:ss')";
        String selectSTRRequestFinish = "select t.IS_NOTIFIED from gpn_oim.Z_NOTIFICATION_EVENTS t where t.MESSAGE_TYPE = 'RequestFinish' and ADDITION_DATE = TO_DATE('27.09.2018 14:15:22', 'DD-MM-YYYY hh24:mi:ss')";
        String selectSTRResourceCreation = "select t.IS_NOTIFIED from gpn_oim.Z_NOTIFICATION_EVENTS t where t.MESSAGE_TYPE = 'ResourceCreation' and ADDITION_DATE > TO_DATE('2018-10-01', 'YYYY-MM-DD') and ADDITION_DATE < TO_DATE('2018-10-31', 'YYYY-MM-DD')";
        String selectSTRSAPNewAccount = "select t.IS_NOTIFIED from gpn_oim.Z_NOTIFICATION_EVENTS t where t.MESSAGE_TYPE = 'SAPNewAccount' and ADDITION_DATE > TO_DATE('2017-06-01', 'YYYY-MM-DD') and ADDITION_DATE < TO_DATE('2018-12-01', 'YYYY-MM-DD')";
        String selectSTRTaskAssignment = "select t.IS_NOTIFIED from gpn_oim.Z_NOTIFICATION_EVENTS t where t.MESSAGE_TYPE = 'TaskAssignment' and ADDITION_DATE = TO_DATE('09.11.2018 14:53:19', 'DD-MM-YYYY hh24:mi:ss')";

        ArrayList selectStrSHRLoader = new ArrayList<String>();
        ArrayList selectStrSHPSMReq = new ArrayList<String>();
        ArrayList selectStrSNewAccount = new ArrayList<String>();
        ArrayList selectStrSOverdueTask = new ArrayList<String>();
        ArrayList selectStrSRequestFinish = new ArrayList<String>();
        ArrayList selectStrSResourceCreation = new ArrayList<String>();
        ArrayList selectStrSSAPNewAccount = new ArrayList<String>();
        ArrayList selectStrSTaskAssignment = new ArrayList<String>();

        selectStrSHRLoader  = SQLOracleUtils.selectStrValueDBArray(selectSTRHRLoader, conn);
        selectStrSHPSMReq  = SQLOracleUtils.selectStrValueDBArray(selectSTRHPSMReq, conn);
        selectStrSNewAccount  = SQLOracleUtils.selectStrValueDBArray(selectSTRNewAccount, conn);
        selectStrSOverdueTask  = SQLOracleUtils.selectStrValueDBArray(selectSTROverdueTask, conn);
        selectStrSRequestFinish  = SQLOracleUtils.selectStrValueDBArray(selectSTRRequestFinish, conn);
        selectStrSResourceCreation  = SQLOracleUtils.selectStrValueDBArray(selectSTRResourceCreation, conn);
        selectStrSSAPNewAccount  = SQLOracleUtils.selectStrValueDBArray(selectSTRSAPNewAccount, conn);
        selectStrSTaskAssignment  = SQLOracleUtils.selectStrValueDBArray(selectSTRTaskAssignment, conn);

        /**
         * Проверяем данные в gpn_oim.Z_NOTIFICATION_EVENTS (флаг IS_NOTIFIED должен быть установлен в "1")
         */

        AssertUtils.assertTrueArray(selectStrSHRLoader, "1");
        AssertUtils.assertTrueArray(selectStrSHPSMReq, "1");
        AssertUtils.assertTrueArray(selectStrSNewAccount, "1");
        AssertUtils.assertTrueArray(selectStrSOverdueTask, "1");
        AssertUtils.assertTrueArray(selectStrSRequestFinish, "1");
        AssertUtils.assertTrueArray(selectStrSResourceCreation, "1");
        AssertUtils.assertTrueArray(selectStrSSAPNewAccount, "1");
        AssertUtils.assertTrueArray(selectStrSTaskAssignment, "1");


        /**
         * Выводим в консоль сообщение, если тест завешился успешно
         */

        System.out.println();
        System.out.println("Проверка отправки нотификаций, завершилась успешно!!!");
        System.out.println();

    }

}
