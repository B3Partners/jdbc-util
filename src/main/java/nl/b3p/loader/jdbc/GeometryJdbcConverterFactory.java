/*
 * Copyright (C) 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.loader.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import oracle.jdbc.OracleConnection;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Chris
 */
public class GeometryJdbcConverterFactory {
    private static final Log LOG = LogFactory.getLog(GeometryJdbcConverterFactory.class);

    public static GeometryJdbcConverter getGeometryJdbcConverter(Connection conn) {
        String databaseProductName = null;
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
                String schema = new QueryRunner().query(conn, "select current_schema", new ScalarHandler<String>());
                geomToJdbc.setSchema(schema);
            } catch (SQLException ex) {
                throw new UnsupportedOperationException("Cannot get/set schema: " + databaseProductName, ex);
            }
            return geomToJdbc;
        } else if (databaseProductName.contains("Oracle")) {
            boolean oracle11 = false;
            boolean oracle12 = false;
            boolean oracle18 = false;
            try {
                oracle11 = (conn.getMetaData().getDatabaseMajorVersion() == 11);
            } catch (SQLException ex) {
                LOG.warn("Uitlezen database versie is mislukt.", ex);
            }
            try {
                oracle12 = (conn.getMetaData().getDatabaseMajorVersion() == 12);
            } catch (SQLException ex) {
                LOG.warn("Uitlezen database versie is mislukt.", ex);
            }
            try {
                oracle18 = (conn.getMetaData().getDatabaseMajorVersion() == 18);
            } catch (SQLException ex) {
                LOG.warn("Uitlezen database versie is mislukt.", ex);
            }
            try {
                OracleConnection oc = OracleConnectionUnwrapper.unwrap(conn);
                OracleJdbcConverter geomToJdbc;
                if (oracle11) {
                    geomToJdbc = new Oracle11JdbcConverter(oc);
                }else if(oracle12){
                    geomToJdbc = new Oracle12JdbcConverter(oc);
                }else if(oracle18){
                    geomToJdbc = new Oracle18JdbcConverter(oc);
                } else {
                    geomToJdbc = new OracleJdbcConverter(oc);
                }
                geomToJdbc.setSchema(oc.getCurrentSchema());
                return geomToJdbc;
            } catch (SQLException ex) {
                throw new UnsupportedOperationException("Cannot get connection: " + databaseProductName, ex);
            }
        } else if (databaseProductName.contains("Microsoft SQL Server")) {
            MssqlJdbcConverter geomToJdbc = new MssqlJdbcConverter();
            try {
                // vanwege Tomcat 7 / DBCP 1.4 die niet de getSchema() implementaties heeft
                // geomToJdbc.setSchema( conn.getSchema());
                String schema = new QueryRunner().query(conn, "SELECT SCHEMA_NAME()", new ScalarHandler<String>());
                geomToJdbc.setSchema(schema);
            } catch (SQLException ex) {
                throw new UnsupportedOperationException("Cannot get/set schema: " + databaseProductName, ex);
            }
            return geomToJdbc;
        } else if(databaseProductName.contains("HSQL Database Engine")){
            return new HSQLJdbcConverter();
        }else{
            throw new UnsupportedOperationException("Unknown database: " + databaseProductName);
        }
    }
}
