package regres;

import lib.AssertUtils;
import lib.SQLOracleUtils;
import lib.StartParameters;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;

/**
 * Класс для проверки состояния доступов пользователей в управляемых системах (пока без получения ответа из самих систем,
 * то есть проверкой соответствующих view GPN_OIM.Z_ENTS_TO_REMOVE, GPN_OIM.Z_ENTS_TO_CHANGE_DATE, GPN_OIM.Z_ENTS_TO_ADD)
 *
 * @author VO Losev
 */

public class EntrancesRevision {

    private Connection conn;


    @Test(timeout = 10000000)
    public void RevisionENTS() {

        /**
         * Получаем стартовые парметры. Соединяемся с БД
         */

        StartParameters.getStartParameters();
        String connStr = StartParameters.connStr;
        String connUser = StartParameters.connUser;
        String connPass = StartParameters.connPass;
        conn = SQLOracleUtils.connect(connStr, connUser, connPass);

        // Строка запроса для Z_ENTS_TO_REMOVE
        String slctENTtoRmvSTR = "select t.DEPROVISIONING_STATUS from GPN_OIM.Z_ENTS_TO_REMOVE t";
        // Строка запроса для Z_ENTS_TO_CHANGE_DATE
        String slctENTtoDateSTR = "select t.PROVISIONING_STATUS from GPN_OIM.Z_ENTS_TO_CHANGE_DATE t";
        // Строка запроса для GPN_OIM.Z_ENTS_TO_ADD

        String selectStr = "(\n" +
                "SELECT USR_KEY, ENTITLEMENT_ID\n" +
                "FROM gpn_oim.z_vw_usr_target_ent target_ent\n" +
                "WHERE not exists\n" +
                "(\n" +
                "select 1 from gpn_oim.z_user_entitlements eff_ent\n" +
                "where is_deleted=0 and eff_ent.usr_key=target_ent.usr_key\n" +
                "and eff_ent.entitlement_id=target_ent.entitlement_id\n" +
                "and (\n" +
                "nvl(eff_ent.oiu_key, -1)=nvl(target_ent.account_id, -1) or\n" +
                "(nvl(target_ent.account_id, -1)<0 and nvl(eff_ent.oiu_key, -1)>0)\n" +
                ")\n" +
                ")\n" +
                "and nvl(target_ent.end_date, to_date('31.12.2999', 'dd.mm.yyyy'))>trunc(sysdate, 'dd')\n" +
                "union\n" +
                "select usr_key, entitlement_id\n" +
                "from gpn_oim.Z_VW_USR_TARGET_ENT_WITH_CTX\n" +
                "where user_access_id in (select user_access_id from gpn_oim.itsk_access_request_repeat)\n" +
                "group by usr_key, entitlement_id, account_id\n" +
                ")";

        /**
         * Инициализируем объекты коллекций для помещения результатов SQL запросов
         */

        ArrayList slctENTtoRmv = new ArrayList<String>();
        ArrayList slctENTtoDate = new ArrayList<String>();
        ArrayList slctENTtoAdd = new ArrayList<String>();

        /**
         * Выыполняем запрос, получаем выборки полей
         * PROVISIONING_STATUS (view GPN_OIM.Z_ENTS_TO_CHANGE_DATE), DEPROVISIONING_STATUS (view GPN_OIM.Z_ENTS_TO_REMOVE)
         * и помещаем их в коллекции.
         */


        slctENTtoRmv = SQLOracleUtils.selectStrValueDBArray(slctENTtoRmvSTR, conn);
        slctENTtoDate = SQLOracleUtils.selectStrValueDBArray(slctENTtoDateSTR, conn);

        /**
         * Проверяем view GPN_OIM.Z_ENTS_TO_REMOVE и GPN_OIM.Z_ENTS_TO_CHANGE_DATE на предмет наличия записей "New" для
         * полей  PROVISIONING_STATUS (view GPN_OIM.Z_ENTS_TO_CHANGE_DATE), DEPROVISIONING_STATUS (view GPN_OIM.Z_ENTS_TO_REMOVE)
         */

         AssertUtils.assertNotEqualsArray(slctENTtoRmv, "New");
         AssertUtils.assertNotEqualsArray(slctENTtoDate, "New");

        /**
         * Проверяем view GPN_OIM.Z_ENTS_TO_ADD, выводимые ей поля USR_KEY, ENTITLEMENT_ID используем в качестве параметров
         * для запроса к таблице GPN_OIM.Z_USER_ENTITLEMENTS и в полученной выборке с помощью метода assertNotEqualsArray проверяем
         * наличие записей "New" для поля  PROVISIONING_STATUS
         */

        ArrayList ResultValueArr = SQLOracleUtils.selectStrValueDBArrayList(selectStr, conn);


        for (int j = 0; j < ResultValueArr.size(); j++) {

            String[] str = (String[])ResultValueArr.get(j);
            String slctENTtoAddSTR = "select PROVISIONING_STATUS from GPN_OIM.Z_USER_ENTITLEMENTS t where t.USR_KEY = " + str[0] + "and t.ENTITLEMENT_ID = " + str[1];
            slctENTtoAdd = SQLOracleUtils.selectStrValueDBArray(slctENTtoAddSTR, conn);
            AssertUtils.assertNotEqualsArray(slctENTtoAdd, "New");
        }

    }
}
