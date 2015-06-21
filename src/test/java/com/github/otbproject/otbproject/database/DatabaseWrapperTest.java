package com.github.otbproject.otbproject.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

public class DatabaseWrapperTest {
    public static final String path = "target/test.db";
    public static final HashMap<String, TableFields> tables = new HashMap<>();
    public static final String tableName = "tblTest";
    public static TableFields testFields;
    public static final HashMap<String, String> fields = new HashMap<>();
    public static DatabaseWrapper db;
    public static final HashMap<String, Object> testData = new HashMap<>();
    public static final HashMap<String, Object> testDataNew = new HashMap<>();
    public static final HashMap<String, Object> testDataInt = new HashMap<>();
    public static final String fieldName = "testField";
    public static final String fieldName2 = "testIntField";


    @BeforeClass
    public static void initialise() {
        org.apache.logging.log4j.core.Logger coreLogger
                = (org.apache.logging.log4j.core.Logger) LogManager.getLogger();
        LoggerContext context
                = coreLogger.getContext();
        org.apache.logging.log4j.core.config.Configuration config
                = context.getConfiguration();
        coreLogger.removeAppender(config.getAppender("Console-info"));
        coreLogger.removeAppender(config.getAppender("Console-debug"));
        coreLogger.removeAppender(config.getAppender("Routing"));
        coreLogger.removeAppender(config.getAppender("Routing-console-debug"));
        coreLogger.removeAppender(config.getAppender("Routing-console-info"));

        fields.put(fieldName, DataTypes.STRING);
        fields.put(fieldName2, DataTypes.INTEGER);
        HashSet<String> primaryKeys = new HashSet<>();
        primaryKeys.add(fieldName);
        testFields = new TableFields(fields, primaryKeys);
        tables.put(tableName, testFields);
        db = DatabaseWrapper.createDatabase(path, tables);
        testData.put(fieldName, "Test Data");
        testDataNew.put(fieldName, "Test Data New");
        testDataInt.put(fieldName2, 1);

    }
    @AfterClass
    public static void clean(){
        new File(path).delete();
    }

    @Test
    public void tableDataShouldBeDumped(){
        assertNotNull(db.tableDump(tableName));
    }
    @Test
    public void tableDataShouldNotBeDumped(){
        assertNull(db.tableDump(tableName + "poop"));
    }
    @Test
    public void listOfRecordsShouldBeRetrieved(){
        assertNotNull(db.getRecordsList(tableName, fieldName));
    }
    @Test
    public void listOfRecordsShouldNotBeRetrieved(){
        assertNull(db.getRecordsList(tableName+"poop",fieldName));
    }
    @Test
    public void stringDataShouldBeInsertedIntoAndRemovedFromDatabase() {
        assertFalse(db.exists(tableName, testData.get(fieldName), fieldName));
        assertTrue(db.insertRecord(tableName, testData));
        assertTrue(db.exists(tableName, testData.get(fieldName), fieldName));
        assertTrue(db.removeRecord(tableName, testData.get(fieldName), fieldName));
        assertFalse(db.exists(tableName, testData.get(fieldName), fieldName));
    }

    @Test
    public void integerDataShouldBeInsertedIntoAndRemovedFromDatabase(){
        assertFalse(db.exists(tableName, testDataInt.get(fieldName2), fieldName2));
        assertTrue(db.insertRecord(tableName, testDataInt));
        assertTrue(db.exists(tableName, testDataInt.get(fieldName2), fieldName2));
        assertTrue(db.removeRecord(tableName, testDataInt.get(fieldName2), fieldName2));
        assertFalse(db.exists(tableName, testDataInt.get(fieldName2), fieldName2));
    }

    @Test
    public void dataShouldNotBeInsertedIntoDatabase(){
        assertFalse(db.insertRecord("poop", testData));
    }
    @Test
    public void dataShouldNotBeRemovedFromDatabase(){
        assertFalse(db.removeRecord(tableName, testData.get(fieldName), fieldName));
    }

    @Test
    public void newDataShouldReplaceOldDataInDatabase(){
        assertFalse(db.exists(tableName, testData.get(fieldName), fieldName));
        assertTrue(db.insertRecord(tableName, testData));
        assertTrue(db.exists(tableName, testData.get(fieldName), fieldName));
        assertTrue(db.updateRecord(tableName, testData.get(fieldName), fieldName, testDataNew));
        assertFalse(db.exists(tableName, testData.get(fieldName), fieldName));
        assertTrue(db.exists(tableName, testDataNew.get(fieldName), fieldName));
        assertTrue(db.removeRecord(tableName, testDataNew.get(fieldName), fieldName));
    }

    @Test
    public void dataShouldBeInsertedIntoAndRetrievedFromDatabase() throws SQLException {
        assertTrue(db.insertRecord(tableName, testData));
        assertTrue(db.exists(tableName, testData.get(fieldName), fieldName));
        ResultSet rs = db.getRecord(tableName, testData.get(fieldName), fieldName);
        assertEquals(testData.get(fieldName), rs.getString(fieldName));
        assertTrue(db.removeRecord(tableName, testData.get(fieldName), fieldName));

    }
}
