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
package nl.b3p.loader.jdbc;

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
    private static final Log LOG = LogFactory.getLog(OracleConnectionUnwrapper.class);

    public static OracleConnection unwrap(Connection c) throws SQLException {
        LOG.trace("Unwrapping Oracle connection, isWrapperFor(OracleConnection.class): " + c.isWrapperFor(OracleConnection.class));
        LOG.trace("Connection class: " + c.getClass().getName());
        // Sometimes isWrapperFor() does not work for certain JDBC drivers. The
        // MetaData connection is always unwrapped, trick learned from Spring's
        // org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor
        Connection mdC = c.getMetaData().getConnection();
        LOG.trace("MetaData connection class: " + mdC.getClass().getName());

        OracleConnection oc;

        if(c.isWrapperFor(OracleConnection.class)) {
            LOG.trace("Unwrap Connection voor OracleConnection");
            oc = c.unwrap(OracleConnection.class);
        } else if(mdC instanceof OracleConnection) {
            LOG.trace("Cast MetaData Connection naar OracleConnection");
            oc = (OracleConnection)mdC;
        } else if (mdC instanceof org.apache.tomcat.dbcp.dbcp2.PoolableConnection) {
            LOG.trace("Cast naar OracleConnection via cast naar tomcat DelegatingConnection");
            oc = (OracleConnection) ((org.apache.tomcat.dbcp.dbcp2.DelegatingConnection) mdC).getDelegate();
        } else {
            throw new SQLException(
                    "Kan connectie niet unwrappen naar OracleConnection van meta connectie: "
                            + mdC.getClass().getName()
                            + ", connection: " + c.getClass().getName()
            );
        }

        return oc;
    }
}
