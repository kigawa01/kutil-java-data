package net.kigawa.data.database;

import net.kigawa.data.data.IntData;
import net.kigawa.data.data.StringData;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

class DatabaseTest extends AbstractDatabaseTest {

    protected Database database;


    public DatabaseTest() {
        database = manager.getDatabase(url, DB_NAME, true);
    }

    @Test
    void update() throws SQLException {
        database.createConnection();
        database.update(TABLE_NAME, new String[]{"name"}, "id=0", new StringData("update"));
        database.close();

        var result = connection.prepareStatement("SELECT name FROM " + TABLE_NAME + " WHERE id=0").executeQuery();

        assertTrue(result.next());
        assertEquals("update", result.getString("name"));
    }

    @Test
    void insert() throws SQLException {
        database.createConnection();
        database.insert(TABLE_NAME, new String[]{"id", "name"}, new IntData(1), new StringData("insert"));
        database.close();

        var result = connection.prepareStatement("SELECT name FROM " + TABLE_NAME + " WHERE id=1").executeQuery();

        assertTrue(result.next());
        assertEquals("insert", result.getString("name"));
    }

    @Test
    void select() throws SQLException {
        database.createConnection();
        var result = database.select(TABLE_NAME, new String[]{"id", "name"}, "id=0");

        assertTrue(result.next());
        assertEquals("name", result.getString("name"));

        database.close();
    }

    @Test
    void delete() throws SQLException {
        database.createConnection();
        database.delete(TABLE_NAME, "id=0");
        database.close();

        var result = connection.prepareStatement("SELECT name FROM " + TABLE_NAME + " WHERE id=0").executeQuery();

        assertFalse(result.next());
    }

    @Test
    void getTable() throws SQLException {
        connection.prepareStatement("DROP TABLE " + TABLE_NAME);

        database.remove(table);

        table = database.getTable(TABLE_NAME, columns, true);

        var result = connection.prepareStatement("SHOW TABLES LIKE '" + TABLE_NAME+"'" ).executeQuery();

        assertTrue(result.next());
        assertEquals(result.getString(1), TABLE_NAME);
    }
}