package nl.b3p.loader.jdbc;

import oracle.jdbc.OracleConnection;

import java.sql.SQLException;

public class Oracle18JdbcConverter extends Oracle12JdbcConverter {

    public Oracle18JdbcConverter(OracleConnection oc) throws SQLException {
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
        // zie: https://rogertroller.com/2018/02/20/oracle-18c-alter-sequence-restart/
        return String.format("ALTER SEQUENCE %s RESTART START WITH %d",seqName, nextVal);
    }
}
