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

import java.sql.SQLException;
import oracle.jdbc.OracleConnection;

/**
 * Oracle 11g specifieke overrides.
 *
 * @author mprins
 */
public class Oracle11JdbcConverter extends OracleJdbcConverter {

    public Oracle11JdbcConverter(OracleConnection oc) throws SQLException {
        super(oc);
    }

    /**
     * Oracle 11 specifieke versie om aantal rijen van een insert/select te
     * beperken. Maakt gebruik van de ROWNUM functie omdat
     * {@code FETCH FIRST ... ROWS ONLY} alleen in Oracle 12 werkt. Zie ook:
     * <a href="https://github.com/B3Partners/brmo/issues/294">GH 294</a>.
     *
     *
     * @param sql query zonder limiet
     * @param limit max aantal op te halen records dat voldoet aan query
     * @return query met limiet
     * @since 1.4.3
     * @see OracleJdbcConverter#buildLimitSql(java.lang.StringBuilder, int)
     */
    @Override
    public StringBuilder buildLimitSql(StringBuilder sql, int limit) {
        sql.append(" AND ROWNUM <= ").append(limit);
        return sql;
    }
}
