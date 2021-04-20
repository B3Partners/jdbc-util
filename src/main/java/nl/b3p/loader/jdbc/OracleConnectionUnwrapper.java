// Copyright (C) 2017 B3Partners B.V.
//
// SPDX-License-Identifier: MIT

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
        } else if (mdC instanceof org.apache.tomcat.dbcp.dbcp.PoolableConnection) {
            LOG.trace("Cast naar OracleConnection via cast naar tomcat DelegatingConnection");
            oc = (OracleConnection) ((org.apache.tomcat.dbcp.dbcp.DelegatingConnection) mdC).getDelegate();
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
