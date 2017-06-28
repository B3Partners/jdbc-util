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

package nl.b3p.brmo.loader.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Matthijs Laan
 */
public class OracleConnectionUnwrapper {
    private static final Log log = LogFactory.getLog(OracleConnectionUnwrapper.class);

    public static OracleConnection unwrap(Connection c) throws SQLException {
        log.trace("Unwrapping Oracle connection, isWrapperFor(OracleConnection.class): " + c.isWrapperFor(OracleConnection.class));
        log.trace("Connection class: " + c.getClass().getName());
        // Sometimes isWrapperFor() does not work for certain JDBC drivers. The
        // MetaData connection is always unwrapped, trick learned from Spring's
        // org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor
        Connection mdC = c.getMetaData().getConnection();
        log.trace("MetaData connection class: " + mdC.getClass().getName());

        OracleConnection oc;

        if(c.isWrapperFor(OracleConnection.class)) {
            oc = c.unwrap(OracleConnection.class);
        } else if(mdC instanceof OracleConnection) {
            oc = (OracleConnection)mdC;
        } else {
            throw new SQLException("Kan connectie niet unwrappen naar OracleConnection!");
        }

        return oc;
    }
}
