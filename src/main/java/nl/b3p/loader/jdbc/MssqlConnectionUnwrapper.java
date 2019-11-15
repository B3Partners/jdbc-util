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
import java.sql.SQLException;
import net.sourceforge.jtds.jdbc.JtdsConnection;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.PoolableConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mprins
 */
public class MssqlConnectionUnwrapper {

    private static final Log LOG = LogFactory.getLog(MssqlConnectionUnwrapper.class);

    /**
     * <strong>LET OP</strong> JtdsConnection#isWrapperFor werpt altijd een
     * AbstractMethodError want niet geimplementeerd!
     *
     * @param c de wrapped JtdsConnection
     * @return de unwrapped JtdsConnection
     * @throws SQLException Als unwrappen naar een JtdsConnection is mislukt
     */
    public static JtdsConnection unwrap(Connection c) throws SQLException {
        LOG.trace("Unwrapping jTDS connection, connection class: " + c.getClass().getName());
        // Sometimes isWrapperFor() does not work for certain JDBC drivers. The
        // MetaData connection is always unwrapped, trick learned from Spring's
        // org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor
        Connection metadataConnection = c.getMetaData().getConnection();
        LOG.trace("MetaData connection class: " + metadataConnection.getClass().getName());

        JtdsConnection oc = null;

        try {
            if (metadataConnection.isWrapperFor(JtdsConnection.class)) {
                oc = metadataConnection.unwrap(JtdsConnection.class);
            }
        } catch (AbstractMethodError e) {
            LOG.trace("Connection ondersteund geen 'isWrapperFor' of 'unwrap' - dit is verwacht voor een DelegatingConnection|PoolableConnection.");
        }
        if (oc == null) {
            if (metadataConnection instanceof JtdsConnection) {
                LOG.trace("Cast naar JtdsConnection");
                oc = (JtdsConnection) metadataConnection;
            } else if (metadataConnection instanceof PoolableConnection) {
                LOG.trace("Cast naar JtdsConnection via cast naar dbcp DelegatingConnection");
                oc = (JtdsConnection) ((DelegatingConnection) metadataConnection).getDelegate();
            } else if (metadataConnection instanceof org.apache.tomcat.dbcp.dbcp.PoolableConnection) {
                LOG.trace("Cast naar JtdsConnection via cast naar tomcat DelegatingConnection");
                oc = (JtdsConnection) ((org.apache.tomcat.dbcp.dbcp.DelegatingConnection) metadataConnection).getDelegate();
            } else {
                throw new SQLException(
                        "Kan connectie niet unwrappen naar JtdsConnection van meta connectie: "
                        + metadataConnection.getClass().getName()
                        + ", connection: " + c.getClass().getName()
                );
            }
        }
        return oc;
    }
}
