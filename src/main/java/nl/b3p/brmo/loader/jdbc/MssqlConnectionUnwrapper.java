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
        Connection mdC = c.getMetaData().getConnection();
        LOG.trace("MetaData connection class: " + mdC.getClass().getName());

        JtdsConnection oc = null;

        try {
            if (mdC.isWrapperFor(JtdsConnection.class)) {
                oc = mdC.unwrap(JtdsConnection.class);
            }
        } catch (AbstractMethodError e) {
            LOG.trace("Connection ondersteund geen 'isWrapperFor' of 'unwrap' - dit is verwacht voor een DelegatingConnection|PoolableConnection.");
        }
        if (oc == null) {
            if (mdC instanceof JtdsConnection) {
                oc = (JtdsConnection) mdC;
            } else if (mdC instanceof PoolableConnection) {
                oc = (JtdsConnection) ((DelegatingConnection) mdC).getDelegate();
            } else {
                throw new SQLException("Kan connectie niet unwrappen naar JtdsConnection!");
            }
        }
        return oc;
    }
}
