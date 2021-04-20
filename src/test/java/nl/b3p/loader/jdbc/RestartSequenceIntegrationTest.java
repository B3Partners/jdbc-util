// Copyright (C) 2020 B3Partners B.V.
//
// SPDX-License-Identifier: MIT

package nl.b3p.loader.jdbc;

import nl.b3p.AbstractDatabaseIntegrationTest;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author mprins
 */
public class RestartSequenceIntegrationTest extends AbstractDatabaseIntegrationTest {
    private static final Log LOG = LogFactory.getLog(RestartSequenceIntegrationTest.class);

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    @Test
    public void testRestartSequence() throws SQLException {
        Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"));

        GeometryJdbcConverter converter = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
        QueryRunner run = new QueryRunner(converter.isPmdKnownBroken());
        try {
            int updated = run.update(c, converter.getUpdateSequenceSQL(
                    params.getProperty("staging.sequence.name", "testing_seq"), 99999)
            );
            LOG.info("Update query returned: " + updated + " voor " + converter);
            if (converter instanceof MssqlJdbcConverter) {
                assertEquals(-1, updated, "expected -1 rows to be updated");
            } else if (converter instanceof OracleJdbcConverter) {
                assertEquals(0, updated, "expected 0 rows to be updated");
            }  else if (converter instanceof Oracle12JdbcConverter ) {
                assertEquals(1, updated, "expected 1 rows to be updated");
            } else {
                assertEquals(0, updated, "expected 0 rows to be updated");
            }

            Number seqVal = run.query(c, converter.getSelectNextValueFromSequenceSQL(
                    params.getProperty("staging.sequence.name", "testing_seq")),
                    new ScalarHandler<>()
            );
            assertEquals(99999L, seqVal.longValue(), "next value should be 99999");
        } catch (SQLException e) {
            fail(e.getLocalizedMessage());
        } finally {
            DbUtils.close(c);
        }
    }
}
