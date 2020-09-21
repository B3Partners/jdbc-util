/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.loader.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Struct;
import java.util.Arrays;
import java.util.Collection;
import nl.b3p.AbstractDatabaseIntegrationTest;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNull;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * gebruik
 * {@code mvn -Dit.test=NullGeomOracleIntegrationTest verify -Poracle -Dtest.onlyITs=true}
 * om deze test te draaien.
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class NullGeomOracleIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(NullGeomOracleIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: testwaarde: '{0}'")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
            {""},
            {null}
        });
    }

    private final String testVal;

    public NullGeomOracleIntegrationTest(String testVal) {
        this.testVal = testVal;
    }

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @Before
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    /**
     * Test NULL geometrie.
     *
     * @throws Exception if any
     */
    @Test
    public void testNullGeomXML() throws Exception {
        if (isOracle) {
            Connection connection = DriverManager.getConnection(
                    params.getProperty("staging.jdbc.url"),
                    params.getProperty("staging.user"),
                    params.getProperty("staging.passwd"));

            OracleConnection oc = OracleConnectionUnwrapper.unwrap(connection);
            OracleJdbcConverter c = new OracleJdbcConverter(oc);

            OracleStruct s = (OracleStruct) c.convertToNativeGeometryObject(this.testVal);
            assertEquals("verwacht een sdo geometry", "MDSYS.SDO_GEOMETRY", s.getSQLTypeName());
            for (Object o : s.getAttributes()) {
                assertNull("verwacht 'null'", o);
            }
        }
    }
}
