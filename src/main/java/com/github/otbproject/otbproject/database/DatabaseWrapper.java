package com.github.otbproject.otbproject.database;

import com.github.otbproject.otbproject.App;

import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DatabaseWrapper {
    final Connection connection;
    protected final Lock lock = new ReentrantLock();

    /**
     * Private constructor, should never be used directly. <br>
     * Instead use <code>createDatabase()</code>.
     *
     * @param path   The path to the database file, should already exist.
     * @param tables A HashMap of Table name to a HashSet of the field names.
     * @throws SQLException if a SQLException occurs in the construction of the object
     * @throws ClassNotFoundException if the SQLite JDBC class is not available at runtime
     */
    protected DatabaseWrapper(String path, HashMap<String, TableFields> tables) throws SQLException, ClassNotFoundException {
        lock.lock();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            for (String key : tables.keySet()) {
                if (!createTable(key, tables.get(key).map, tables.get(key).primaryKeys)) {
                    throw new SQLException("Failed to create table: " + key);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Static method for creation of a DataBase Wrapper Object. <br>
     * Will return a new DataBaseWrapper Object or null if either an <code>SQLException</code> or a <code>CLassNotFoundException</code>.
     *
     * @param path   The path to the database file, should already exist.
     * @param tables A HashMap of Table name to a HashSet of the field names.
     * @return a new DataBaseWrapper Object or null if either an <code>SQLException</code> or a <code>CLassNotFoundException</code>.
     * @see com.github.otbproject.otbproject.database.DatabaseHelper
     */
    public static DatabaseWrapper createDatabase(String path, HashMap<String, TableFields> tables) {
        try {
            return new DatabaseWrapper(path, tables);
        } catch (SQLException | ClassNotFoundException e) {
            App.logger.catching(e);
            return null;
        }
    }

    /**
     * Creates a table in the database with no primary key.
     *
     * @param name  The name of the table to create.
     * @param table A HashMap of field names  to Field Types for the table.
     * @return False if an <code>SQLException</code> is thrown, else it returns true.
     */
    private boolean createTable(String name, HashMap<String, String> table) {
        return createTable(name, table, null);
    }

    /**
     * Creates a table in the database with a primary key.
     *
     * @param name       The name of the table to create.
     * @param table      A HashSet of field names for the table.
     * @param primaryKeys The field name for the primary key.
     * @return False if an <code>SQLException</code> is thrown, else it returns true.
     */
    private boolean createTable(String name, HashMap<String, String> table, HashSet<String> primaryKeys) {
        PreparedStatement preparedStatement = null;
        String sql = "CREATE TABLE IF NOT EXISTS " + name + " ("
                + table.keySet().stream().map(key -> (key + " " + table.get(key))).collect(Collectors.joining(", "));
        if (!primaryKeys.isEmpty()) {
            sql += ", PRIMARY KEY (" + primaryKeys.stream().collect(Collectors.joining(", ")) + ")";
        }
        sql += ")";
        boolean bool = true;
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
            bool = false;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                App.logger.catching(e);
                bool = false;
            }

        }
        return bool;
    }

    public ResultSet getRecord(String table, List<Map.Entry<String, Object>> entryList) {
        PreparedStatement preparedStatement;
        String sql = "SELECT * FROM " + table + " WHERE ";
        sql += entryList.stream().map(entry -> (entry.getKey() + "= ?")).collect(Collectors.joining(", "));
        ResultSet rs = null;
        lock.lock();
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            int index = 1;
            for (Map.Entry entry : entryList) {
                setValue(preparedStatement, index, entry.getValue());
                index++;
            }
            rs = preparedStatement.executeQuery();
            connection.commit();
        } catch (SQLException e) {
            App.logger.catching(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                App.logger.catching(e);
            }
            lock.unlock();
        }
        return rs;
    }

    /**
     * Retrieves a <code>ResultSet</code> that contains all records that match the filter.
     * The Filter is defined as a field name and field value pair. <br>
     * i.e. CommandName and !test where CommandName is a field name and !test is a value in that field, <br>
     * this method would return a result set of all records that had !test in the CommandName field.
     *
     * @param table      The table name.
     * @param identifier what the filter should match.
     * @param fieldName  the field you want to filter with.
     * @return a <code>ResultSet</code> that contains the records that match the Identifier in the field specified.
     * @see java.sql.ResultSet
     */
    public ResultSet getRecord(String table, Object identifier, String fieldName) {
        List<Map.Entry<String, Object>> list = new ArrayList<>();
        list.add(new AbstractMap.SimpleEntry<>(fieldName, identifier));
        return getRecord(table, list);
    }

    public ResultSet getRandomRecord(String table) {
        String sql = "SELECT * FROM " + table + " ORDER BY RANDOM() LIMIT 1";
        ResultSet rs = null;
        lock.lock();
        try {
            connection.setAutoCommit(false);
            rs = connection.createStatement().executeQuery(sql);
            connection.commit();
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                App.logger.catching(e);
            }
            lock.unlock();
        }
        return rs;
    }

    public boolean exists(String table, List<Map.Entry<String, Object>> entryList) {
        PreparedStatement preparedStatement = null;
        String sql = "SELECT COUNT() FROM " + table + " WHERE ";
        sql += entryList.stream().map(entry -> (entry.getKey() + "= ?")).collect(Collectors.joining(", "));
        ResultSet rs;
        lock.lock();
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            int index = 1;
            for (Map.Entry entry : entryList) {
                setValue(preparedStatement, index, entry.getValue());
                index++;
            }
            rs = preparedStatement.executeQuery();
            connection.commit();
            return  (rs.getInt(1) > 0);
        } catch (SQLException e) {
            App.logger.catching(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                App.logger.catching(e);
            }
            lock.unlock();
        }
        return false;
    }

    /**
     * Checks to see if a record with a Field Name, to Field Value pair exists in the table.
     *
     * @param table      The table name.
     * @param identifier what the filter should match.
     * @param fieldName  the field you want to filter with.
     * @return True if the identifier exists in the field in the table.
     */
    public boolean exists(String table, Object identifier, String fieldName) {
        List<Map.Entry<String, Object>> list = new ArrayList<>();
        list.add(new AbstractMap.SimpleEntry<>(fieldName, identifier));
        return exists(table, list);
    }

    /**
     * Updates an existing record in the database.
     *
     * @param table      The table name.
     * @param identifier what the filter should match.
     * @param fieldName  the field you want to filter with.
     * @param map        A HashMap of Field Name to Field Value.
     * @return True if one or more records were updated in the database.
     */
    public boolean updateRecord(String table, Object identifier, String fieldName, HashMap<String, Object> map) {
        PreparedStatement preparedStatement = null;
        String sql = "UPDATE " + table + " SET ";
        List<Map.Entry<String, Object>> entryList = new ArrayList<>(map.entrySet()); // Guarantee ordering
        sql += entryList.stream().map(entry -> (entry.getKey() + "=?")).collect(Collectors.joining(", "));
        sql += " WHERE " + fieldName + "= ?";
        lock.lock();
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            int index = 1;
            for (Map.Entry entry : entryList) {
                setValue(preparedStatement, index, entry.getValue());
                index++;
            }
            if (identifier instanceof String) {
                preparedStatement.setString(index, (String) identifier);
            } else if (identifier instanceof Integer) {
                preparedStatement.setInt(index, (Integer) identifier);
            }
            int i = preparedStatement.executeUpdate();
            connection.commit();
            return  (i > 0);
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                App.logger.catching(e);
            }
            lock.unlock();
        }
        return false;
    }

    /**
     * Inserts a record into the Database.
     *
     * @param table The table name.
     * @param map   A HashMap of Field Name to Field Value.
     * @return True if the record was inserted into database.
     */
    public boolean insertRecord(String table, HashMap<String, Object> map) {
        PreparedStatement preparedStatement = null;
        String sql = "INSERT OR IGNORE INTO " + table + " (";
        List<Map.Entry<String, Object>> entryList = new ArrayList<>(map.entrySet()); // Guarantee ordering
        sql += entryList.stream().map(Map.Entry::getKey).collect(Collectors.joining(", "));
        sql += ") VALUES (";
        sql += Collections.nCopies(map.keySet().size(), "?").stream().collect(Collectors.joining(", "));
        sql += ")";
        lock.lock();
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            int index = 1;
            for (Map.Entry entry : entryList) {
                setValue(preparedStatement, index, entry.getValue());
                index++;
            }
            int i = preparedStatement.executeUpdate();
            connection.commit();
            return  (i > 0);
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                App.logger.catching(e);
            }
            lock.unlock();
        }
        return false;
    }

    public boolean removeRecord(String table, List<Map.Entry<String, Object>> entryList) {
        PreparedStatement preparedStatement = null;
        String sql = "DELETE FROM " + table + " WHERE ";
        sql += entryList.stream().map(entry -> (entry.getKey() + "=?")).collect(Collectors.joining(", "));
        lock.lock();
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            int index = 1;
            for (Map.Entry entry : entryList) {
                setValue(preparedStatement, index, entry.getValue());
                index++;
            }
            int i = preparedStatement.executeUpdate();
            connection.commit();
            return (i > 0);
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                App.logger.catching(e);
            }
            lock.unlock();
        }
        return false;
    }

    /**
     * Removes a record from the Database.
     *
     * @param table      The table name.
     * @param identifier what the filter should match.
     * @param fieldName  the field you want to filter with.
     * @return True if one or more records are removed.
     */
    public boolean removeRecord(String table, Object identifier, String fieldName) {
        List<Map.Entry<String, Object>> list = new ArrayList<>();
        list.add(new AbstractMap.SimpleEntry<>(fieldName, identifier));
        return removeRecord(table, list);
    }

    /**
     * Retrieves all records from a table.
     *
     * @param table The Table name.
     * @return result set that contains all records of the table.
     */
    public ResultSet tableDump(String table) {
        String sql = "SELECT * FROM " + table;
        lock.lock();
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves a list of entries in a singular field in a specific table.<br>
     * The list may contain duplicate elements, if the field specified is neither the <code>PRIMARY KEY</code>, or marked as <code>UNIQUE</code>.
     *
     * @param table The table name that you want the record list from.
     * @param key   The field in the table you want to get.
     * @return an <code>ArrayList&lt;String&gt;</code> that contains all entries in the table specified for the field key or <code>null</code> if an <code>SQLException</code> is thrown.
     */
    public ArrayList<Object> getRecordsList(String table, String key) {
        String sql = "";
        lock.lock();
        try {
            ArrayList<Object> set = new ArrayList<>();
            sql = "SELECT " + key + " FROM " + table;
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                set.add(resultSet.getString(key));
            }
            return set;
        } catch (SQLException e) {
            App.logger.error("SQL: " + sql);
            App.logger.catching(e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    protected static void setValue(PreparedStatement statement, int index, Object identifier) throws SQLException {
        if (identifier instanceof String || identifier == null) {
            statement.setString(index, (String) identifier);
        } else if (identifier instanceof Integer) {
            statement.setInt(index, (Integer) identifier);
        } else if (identifier instanceof Long) {
            statement.setLong(index, (Long) identifier);
        } else {
            throw new SQLException("Invalid value to set in prepared statement: " + identifier.toString());
        }
    }
}
