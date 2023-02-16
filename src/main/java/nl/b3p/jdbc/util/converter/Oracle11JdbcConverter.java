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
     * @param sql query zonder limiet
     * @param limit max aantal op te halen records dat voldoet aan query
     * @return query met limiet
     * @since 1.4.3
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
