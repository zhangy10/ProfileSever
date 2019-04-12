package mbluenet.zac.com.profilesever.utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

public class UtilHelper {

    private static SecureRandom random = new SecureRandom();

    public static String getSecret() {
        return new BigInteger(130, random).toString(32);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }


    public static String getExceptionLog(Thread t, String msg, Throwable e) {
        StringBuilder br = new StringBuilder(msg);
        StackTraceElement[] stack = e.getStackTrace();
        if (t != null) {
            br.append(t.getName() + "-");
        }
        br.append(e + FileUtils.NEW_LINE);
        for (StackTraceElement er : stack) {
            br.append(er.toString() + FileUtils.NEW_LINE);
        }
        return br.toString();
    }

    public static String getExceptionLog(String msg, Throwable e) {
        return getExceptionLog(null, msg, e);
    }

    /**
     * @param s
     * @return true: empty; false: not
     */
    public static boolean isEmptyStr(String s) {
        if (s != null && s.length() != 0) {
            return false;
        }
        return true;
    }

}
