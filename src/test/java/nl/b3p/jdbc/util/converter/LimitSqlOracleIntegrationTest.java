/*
 * Copyright (C) 2016 B3Partners B.V.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 */
package nl.b3p.jdbc.util.converter;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import oracle.jdbc.OracleConnection;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Meine Toonen
 */
public class LimitSqlOracleIntegrationTest extends AbstractDatabaseIntegrationTest {

  private static final Log LOG = LogFactory.getLog(LimitSqlOracleIntegrationTest.class);

  @BeforeEach
  @Override
  public void setUp() throws Exception {
    loadProps();
  }

  /**
   * moet op Oracle 12 en hoger passeren.
   *
   * @throws SQLException soms
   */
  @Test
  public void checkLimitQuery() throws SQLException {
    Connection c =
        DriverManager.getConnection(
            params.getProperty("staging.jdbc.url"),
            params.getProperty("staging.user"),
            params.getProperty("staging.passwd"));

    String q = "select * from bericht where id > 0 order by id";
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
   *
   * @throws SQLException soms
   */
  @Test
  public void checkLimitQueryOracle11() throws SQLException {
    if (isOracle) {
      Connection c =
          DriverManager.getConnection(
              params.getProperty("staging.jdbc.url"),
              params.getProperty("staging.user"),
              params.getProperty("staging.passwd"));

      String q = "Select * from bericht where id > 0 order by id";
      OracleConnection oc = OracleConnectionUnwrapper.unwrap(c);
      GeometryJdbcConverter converter = new OracleJdbcConverter(oc);
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
