/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.brmo.loader.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import nl.b3p.AbstractDatabaseIntegrationTest;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Meine Toonen
 */
public class LimitSqlIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Before
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    @Test
    public void checkLimitQuery() throws SQLException {
        Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"));

        
        String q = "Select * from bericht where id > 0 order by id";
        GeometryJdbcConverter converter = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
        String finalquery = converter.buildLimitSql(new StringBuilder(q), 10).toString();

        MapListHandler h = new MapListHandler();

        QueryRunner run = new QueryRunner();

        try {
            List<Map<String,Object>> result = run.query(c, finalquery, h);
        }catch(SQLException e){
            fail(e.getLocalizedMessage());
        } finally {
            // Use this helper method so we don't have to check for null
            DbUtils.close(c);
        }
    }

}
