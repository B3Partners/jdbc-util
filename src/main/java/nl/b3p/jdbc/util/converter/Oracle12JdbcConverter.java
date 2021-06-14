/*
 * Copyright (C) 2020 B3Partners B.V.
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
