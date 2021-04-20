// Copyright (C) 2017 B3Partners B.V.
//
// SPDX-License-Identifier: MIT

package nl.b3p.loader.jdbc;

import java.sql.SQLException;
import oracle.jdbc.OracleConnection;

/**
 * Oracle 11g specifieke overrides.
 * @deprecated Oracle 11 is EOL; niet meer gebruiken.
 * @author mprins
 */
@Deprecated
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
        int index = -1;
        String limitPart= " AND ROWNUM <= " + limit;
        String[] preInsertClauses = {"GROUP BY", "HAVING", "ORDER BY"};
        String upperCased = sql.toString().toUpperCase();
        for (String preInsertClause : preInsertClauses) {
            index = upperCased.indexOf(preInsertClause);
            if(index != -1){
                break;
            }
        }
        if(index != -1){
            sql.insert(index -1 , limitPart);
        }else{
            sql.append(limitPart);
        }
        return sql;
    }

    @Override
    public String getUpdateSequenceSQL(String seqName, long nextVal){
        throw new UnsupportedOperationException("Update sequence not supported for this database version.");
    }
}
