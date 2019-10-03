package lib;

import org.ini4j.Ini;
import java.io.File;
import java.io.IOException;

public class StartParameters {


    public static String connStr;
    public static String connUser;
    public static String connPass;
    public static String driverpath;
    public static String webdriver;
    public static String suidURL;
    public static String sysadmURL;
    public static String nameUser;
    public static String passUser;
    public static String nameSysadm;
    public static String passSysadm;

    public static void getStartParameters() {
        try {
            Ini ini = new Ini(new File("param.ini"));

            connStr = ini.get("strtparam", "connStr");
            connUser = ini.get("strtparam", "connUser");
            connPass = ini.get("strtparam", "connPass");
            driverpath = ini.get("strtparam", "driverpath");
            webdriver = ini.get("strtparam", "webdriver");
            suidURL = ini.get("strtparam", "suidURL");
            sysadmURL = ini.get("strtparam", "sysadmURL");
            nameUser = ini.get("strtparam", "nameUser");
            passUser = ini.get("strtparam", "passUser");
            nameSysadm = ini.get("strtparam", "nameSysadm");
            passSysadm = ini.get("strtparam", "passSysadm");

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}