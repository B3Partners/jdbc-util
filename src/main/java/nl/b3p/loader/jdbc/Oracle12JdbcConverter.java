// Copyright (C) 2020 B3Partners B.V.
//
// SPDX-License-Identifier: MIT

package nl.b3p.loader.jdbc;

import oracle.jdbc.OracleConnection;

import java.sql.SQLException;

/**
 * Oracle 12c specifieke overrides.
 *
 * @author mprins
 */
public class Oracle12JdbcConverter extends OracleJdbcConverter {

    public Oracle12JdbcConverter(OracleConnection oc) throws SQLException {
        super(oc);
    }

    /**
     * Gets a statement to use in a prepared statement to restart a sequence.
     * This assumes no other interactions are going on with the sequence;
     * <em>can only be used to increase the value of the sequence, not decrease.</em>
     *
     * @param seqName name of sequence
     * @return SQL statement specific for the flavour of database
     */
    @Override
    public String getUpdateSequenceSQL(String seqName, long nextVal) {
        return String.format("DECLARE curr_seq NUMBER;\n"
                + "BEGIN\n"
                + "LOOP\n"
                + "SELECT %s.NEXTVAL INTO curr_seq FROM dual;\n"
                + "IF curr_seq >= (%d-1) THEN EXIT;\n"
                + "END IF;\n"
                + "END LOOP;\n"
                + "END;\n",seqName, nextVal);
    }
}
