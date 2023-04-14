/*
 * Copyright (C) 2017 B3Partners B.V.
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

import java.sql.Connection;
import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris
 */
public class GeometryJdbcConverterFactory {
  private static final Log LOG = LogFactory.getLog(GeometryJdbcConverterFactory.class);

  public static GeometryJdbcConverter getGeometryJdbcConverter(Connection conn) {
    String databaseProductName;
    try {
      databaseProductName = conn.getMetaData().getDatabaseProductName();
    } catch (SQLException ex) {
      throw new UnsupportedOperationException("Cannot get database product name", ex);
    }
    if (databaseProductName.contains("PostgreSQL")) {
      PostgisJdbcConverter geomToJdbc = new PostgisJdbcConverter();
      try {
        // DO NOT USE conn.getSchema(). This is a JDBC 4.1 method not
        // supported by older PostgreSQL drivers and NOT by DBCP 1.4
        // used by Tomcat 7!
        String schema =
            new QueryRunner().query(conn, "select current_schema", new ScalarHandler<>());
        geomToJdbc.setSchema(schema);
      } catch (SQLException ex) {
        throw new UnsupportedOperationException(
            "Cannot get/set schema: " + databaseProductName, ex);
      }
      return geomToJdbc;
    } else if (databaseProductName.contains("Oracle")) {
      int majorVersion = 19;
      try {
        majorVersion = conn.getMetaData().getDatabaseMajorVersion();
      } catch (SQLException ex) {
        LOG.warn("Uitlezen database versie is mislukt.", ex);
      }
      try {
        OracleConnection oc = OracleConnectionUnwrapper.unwrap(conn);
        OracleJdbcConverter geomToJdbc;
        switch (majorVersion) {
          case 11:
            throw new UnsupportedOperationException("Oracle 11 is niet ondersteund");
          case 12:
            throw new UnsupportedOperationException("Oracle 12 is niet ondersteund");
          case 18:
          case 19:
          case 21:
          case 23:
          default:
            geomToJdbc = new OracleJdbcConverter(oc);
        }
        geomToJdbc.setSchema(oc.getCurrentSchema());
        return geomToJdbc;
      } catch (SQLException ex) {
        throw new UnsupportedOperationException(
            "Cannot get connection: " + databaseProductName, ex);
      }
    } else if (databaseProductName.contains("Microsoft SQL Server")) {
      MssqlJdbcConverter geomToJdbc = new MssqlJdbcConverter();
      try {
        // vanwege Tomcat 7 / DBCP 1.4 die niet de getSchema() implementaties heeft
        // geomToJdbc.setSchema( conn.getSchema());
        String schema =
            new QueryRunner().query(conn, "SELECT SCHEMA_NAME()", new ScalarHandler<>());
        geomToJdbc.setSchema(schema);
      } catch (SQLException ex) {
        throw new UnsupportedOperationException(
            "Cannot get/set schema: " + databaseProductName, ex);
      }
      return geomToJdbc;
    } else if (databaseProductName.contains("HSQL Database Engine")) {
      return new HSQLJdbcConverter();
    } else {
      throw new UnsupportedOperationException("Unknown database: " + databaseProductName);
    }
  }
}
