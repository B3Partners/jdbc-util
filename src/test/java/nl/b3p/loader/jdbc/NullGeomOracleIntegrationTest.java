/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.loader.jdbc;

import nl.b3p.AbstractDatabaseIntegrationTest;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * gebruik
 * {@code mvn -Dit.test=NullGeomOracleIntegrationTest verify -Poracle -Dtest.onlyITs=true}
 * om deze test te draaien.
 *
 * @author mprins
 */
public class NullGeomOracleIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(NullGeomOracleIntegrationTest.class);

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    /**
     * Test NULL geometrie.
     *
     * @throws Exception if any
     */
    @ParameterizedTest(name = "#{index} - waarde: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    public void testNullGeomXML(String testVal) throws Exception {
        if (isOracle) {
            Connection connection = DriverManager.getConnection(
                    params.getProperty("staging.jdbc.url"),
                    params.getProperty("staging.user"),
                    params.getProperty("staging.passwd"));

            OracleConnection oc = OracleConnectionUnwrapper.unwrap(connection);
            OracleJdbcConverter c = new OracleJdbcConverter(oc);

            OracleStruct s = (OracleStruct) c.convertToNativeGeometryObject(testVal);
            assertNull(s, "verwacht een null sdo geometry");
        }
    }
}
