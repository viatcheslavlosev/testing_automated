package lib;

import org.junit.Assert;

import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;

public class AssertUtils {

    /**
     *
     *
     */

    public static void assertTrueArray(ArrayList arr, String str) {

        for(int i = 0; i < arr.size(); i++) {

            assertTrue(arr.get(i).equals(str));
        }

    }

    /**
     *
     *
     */

    public static void assertNotEqualsArray(ArrayList arr, String str) {

        for(int i = 0; i < arr.size(); i++) {

            Assert.assertNotEquals(arr.get(i), str);
        }

    }

}
