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
