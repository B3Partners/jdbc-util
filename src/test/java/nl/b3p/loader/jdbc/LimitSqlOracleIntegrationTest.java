/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.loader.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import nl.b3p.AbstractDatabaseIntegrationTest;
import oracle.jdbc.OracleConnection;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Meine Toonen
 */
public class LimitSqlOracleIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(LimitSqlOracleIntegrationTest.class);

    @Before
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    /**
     * moet op Oracle 12 en hoger passeren.
     * @throws SQLException soms
     */
    @Test
    public void checkLimitQuery() throws SQLException {
        Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"));

        String q = "Select * from bericht where id > 0 order by id";
        GeometryJdbcConverter converter = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
        String finalquery = converter.buildLimitSql(new StringBuilder(q), 10).toString();
        LOG.info("Final query" + finalquery);
        MapListHandler h = new MapListHandler();

        QueryRunner run = new QueryRunner();

        try {
            List<Map<String, Object>> result = run.query(c, finalquery, h);
        } catch (SQLException e) {
            LOG.error("Error executing query " + finalquery, e);
            fail(e.getLocalizedMessage());
        } finally {
            DbUtils.close(c);
        }
    }

    /**
     * moet op Oracle 11 en hoger passeren.
     * @throws SQLException soms
     */
    @Test
    public void checkLimitQueryOracle11() throws SQLException {
        if (isOracle) {
            Connection c = DriverManager.getConnection(
                    params.getProperty("staging.jdbc.url"),
                    params.getProperty("staging.user"),
                    params.getProperty("staging.passwd"));

            String q = "Select * from bericht where id > 0 order by id";
            OracleConnection oc = OracleConnectionUnwrapper.unwrap(c);
            GeometryJdbcConverter converter = new Oracle11JdbcConverter(oc);
            String finalquery = converter.buildLimitSql(new StringBuilder(q), 10).toString();
            LOG.info("Final query" + finalquery);
            MapListHandler h = new MapListHandler();

            QueryRunner run = new QueryRunner();

            try {
                List<Map<String, Object>> result = run.query(c, finalquery, h);
            } catch (SQLException e) {
                LOG.error("Error executing query " + finalquery, e);
                fail(e.getLocalizedMessage());
            } finally {
                DbUtils.close(c);
            }
        }
    }

}
