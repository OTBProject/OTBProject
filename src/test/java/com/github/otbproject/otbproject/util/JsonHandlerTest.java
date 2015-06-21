package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.config.Account;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JsonHandlerTest {
    public static final String path ="target/test.json";
    public static final Account testObject = new Account();
    public static final String testAccountName= "Test1";
    public static final String testPasskey = "Test2";
    public static final File testFile = new File(path);

    @BeforeClass
    public static void initilise(){
        testObject.setName(testAccountName);
        testObject.setPasskey(testPasskey);
    }

    @Test
    public void dataShouldBeWrittenAsJsonAndReadBack(){
        JsonHandler.writeValue(path, testObject);
        Account newObject = JsonHandler.readValue(path, Account.class);
        assertNotNull(newObject);
        assertTrue(compareAccount(testObject, newObject));
    }

    @Test
    public void fileShouldNotBeFound(){
        JsonHandler.writeValue(path, testObject);
        assertNull(JsonHandler.readValue(path + 1, Account.class));
    }
    @Test
    public void shouldCauseAnIOExceptionOnWrite(){
        assertTrue(testFile.setWritable(false, false));
        JsonHandler.writeValue(path, testObject);
        assertTrue(testFile.setWritable(true, false));
    }

    private boolean compareAccount(Account original, Account read){
        return original.getName().equals(read.getName()) && original.getPasskey().equals(read.getPasskey());
    }
}
