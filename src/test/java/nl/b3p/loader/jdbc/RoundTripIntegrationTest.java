/*
 * Copyright (C) 2019 B3Partners B.V.
 */
package nl.b3p.loader.jdbc;

import nl.b3p.AbstractDatabaseIntegrationTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.fail;

// mislukt op hsqldb, die is niet round-trip safe voor geometrie
//@Category(HSQLDBDriverBasedFailures.class)
public class RoundTripIntegrationTest extends AbstractDatabaseIntegrationTest {
    private static final Log LOG = LogFactory.getLog(RoundTripIntegrationTest.class);
    private final String wktString = "POLYGON((0 0, 10 0, 5 5, 0 0))";
    private final int srid = 28992;
    private Geometry testJtsGeometry = null;

    @Before
    @Override
    public void setUp() throws Exception {
        loadProps();
        final WKTReader r = new WKTReader();
        testJtsGeometry = r.read(wktString);
        testJtsGeometry.setSRID(srid);
    }

    @Test
    public void testGeomRoundTrip() {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {
            GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            Object nativeGeometryObject = conv.convertToNativeGeometryObject(testJtsGeometry, srid);
            assertNotNull("native geom mag niet 'null' zijn", nativeGeometryObject);
            assertEquals(testJtsGeometry, conv.convertToJTSGeometryObject(nativeGeometryObject));
        } catch (SQLException | ParseException e) {
            LOG.error(e);
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void testGeomRoundTripNull() {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {
            GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            Object nativeGeometryObject = conv.convertToNativeGeometryObject((String) null);
            LOG.debug("nativeGeometryObject: " + nativeGeometryObject);
            assertEquals("verwacht 'null' na round-trip", null, conv.convertToJTSGeometryObject(nativeGeometryObject));
        } catch (SQLException | ParseException e) {
            LOG.error(e);
            fail(e.getLocalizedMessage());
        }
    }
}
