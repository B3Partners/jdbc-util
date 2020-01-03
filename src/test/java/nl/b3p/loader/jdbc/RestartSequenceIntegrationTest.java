package nl.b3p.loader.jdbc;

import nl.b3p.AbstractDatabaseIntegrationTest;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author mprins
 */
public class RestartSequenceIntegrationTest extends AbstractDatabaseIntegrationTest {
    private static final Log LOG = LogFactory.getLog(RestartSequenceIntegrationTest.class);

    @Before
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
            assertEquals("expected 0 rows to be updated", 0, updated);

            Number seqVal = run.query(c, converter.getSelectNextValueFromSequenceSQL(
                    params.getProperty("staging.sequence.name", "testing_seq")),
                    new ScalarHandler<>()
            );
            assertEquals("next value should be 99999", 99999L, seqVal.longValue());
        } catch (SQLException e) {
            fail(e.getLocalizedMessage());
        } finally {
            DbUtils.close(c);
        }
    }
}
