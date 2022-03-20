package net.kigawa.data.database;

import net.kigawa.data.data.Data;
import net.kigawa.data.data.StringData;
import net.kigawa.kutil.kutil.KutilString;
import net.kigawa.kutil.kutil.list.GenerateMap;
import net.kigawa.kutil.log.log.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Table {
    private final Database database;
    private final String name;
    private final Columns columns;
    private final Logger logger;
    private final GenerateMap<Data, Record> recordMap;

    protected Table(Logger logger, Database dataBase, String name, Columns columns, boolean migrate) {
        this.database = dataBase;
        this.name = name;
        this.columns = columns;
        this.logger = logger;
        recordMap = new GenerateMap<>(data -> new Record(logger, this, columns, data));

        database.createConnection();
        if (migrate) migrate();
        database.close();
    }

    public int update(String[] columns, String where, Data... data) {
        return database.update(name, columns, where, data);
    }

    public int insert(String[] columns, Data[] data) {
        return database.insert(name, columns, data);
    }

    public ResultSet select(String[] columns, String where, Data[] data) {
        return database.select(name, columns, where, data);
    }

    public boolean canUse() {
        if (!isExist()) return false;

        return true;
    }

    public boolean isExist() {
        var result = database.executeQuery("SHOW TABLES LIKE ?", new StringData(name));
        if (result == null) return false;
        try {
            if (!result.next()) return false;
        } catch (SQLException e) {
            logger.warning(e);
            return false;
        }
        return true;
    }

    public void migrate() {
        logger.info("migrate table: " + name);
        if (!canUse()) return;
        createTable();
    }

    public void createTable() {
        logger.info("create table: " + name);
        var sb = new StringBuffer("CREATE TABLE IF NOT EXISTS " + name + " (");

        KutilString.insertSymbol(sb, ",", columns, column -> column.getName() + " " + column.getDataType().getSql() + " " + column.getOptionSql());

        sb.append(")");

        database.executeUpdate(sb.toString());
        database.close();
    }

    public void deleteTable() {
        database.executeUpdate("DROP TABLE " + name);
    }

    public void removeRecord(Data key) {
        recordMap.remove(key);
    }

    public Record getRecord(Data key) {
        return recordMap.get(key);
    }

    public Database getDatabase() {
        return database;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        if (o instanceof Table) return equalsTable((Table) o);
        return false;
    }

    public boolean equalsTable(Table table) {
        return equalsDatabase(table.database) && equalsName(table.name);
    }

    public boolean equalsName(String name) {
        return this.name.equals(name);
    }

    public boolean equalsColumn(Columns columns) {
        return this.columns.equals(columns);
    }

    public boolean equalsDatabase(Database database) {
        return this.database.equals(database);
    }
}
