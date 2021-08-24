/*
 * Copyright (C) 2021 B3Partners B.V.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class PGConnectionUnwrapper {
    private static final Log LOG = LogFactory.getLog(PGConnectionUnwrapper.class);

    public static PGConnection unwrap(Connection connection) throws SQLException {
        LOG.trace("Connection class: " + connection.getClass().getName());
        LOG.trace("isWrapperFor(PGConnection.class): " + connection.isWrapperFor(PGConnection.class));
        // Sometimes isWrapperFor() does not work for certain JDBC drivers. The
        // MetaData connection is always unwrapped, trick learned from Spring's
        // org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor
        Connection metadataConnection = connection.getMetaData().getConnection();

        LOG.trace("MetaData connection class: " + metadataConnection.getClass().getName());
        LOG.trace("MetaData isWrapperFor(PGConnection.class): " + metadataConnection.isWrapperFor(PGConnection.class));

        if (connection.isWrapperFor(PGConnection.class)) {
            LOG.trace("Unwrap Connection voor PGConnection");
            return connection.unwrap(PGConnection.class);
        } else if (metadataConnection instanceof PGConnection) {
            LOG.trace("Cast MetaData Connection naar PGConnection");
            return (PGConnection) metadataConnection;
        } else if (metadataConnection instanceof org.apache.tomcat.dbcp.dbcp2.PoolableConnection) {
            LOG.trace("Cast naar matadataConnection PGConnection via cast naar tomcat DelegatingConnection");
            return (PGConnection) ((org.apache.tomcat.dbcp.dbcp2.DelegatingConnection) metadataConnection).getDelegate();
        } else if (org.apache.tomcat.dbcp.dbcp2.DelegatingConnection.class.isAssignableFrom(connection.getClass())) {
            //org.apache.tomcat.dbcp.dbcp2.PoolingDataSource.PoolGuardConnectionWrapper is private maar extends DelegatingConnection
            LOG.trace("Cast InnermostDelegate van DelegatingConnection connection");
            return (PGConnection) ((org.apache.tomcat.dbcp.dbcp2.DelegatingConnection) connection).getInnermostDelegate();
        } else {
            throw new SQLException(
                    "Kan connectie niet unwrappen naar PGConnection van meta connectie: "
                            + metadataConnection.getClass().getName()
                            + ", connection: " + connection.getClass().getName()
            );
        }
    }
}
