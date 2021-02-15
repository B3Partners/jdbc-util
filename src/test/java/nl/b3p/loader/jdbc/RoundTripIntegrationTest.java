/*
 * Copyright (C) 2019 B3Partners B.V.
 */
package nl.b3p.loader.jdbc;

import nl.b3p.AbstractDatabaseIntegrationTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * test of de converters round-trip safe zijn, dus JTS-input naar native naar JTS-output moet gelijk blijven.
 * 
 *  gebruik bijvoorbeeld: 
 *  {@code mvn -Dit.test=RoundTripIntegrationTest verify -Poracle -Dtest.onlyITs=true}
 *  om deze test te draaien.
 *  
 * @author mark
 */
public class RoundTripIntegrationTest extends AbstractDatabaseIntegrationTest {
    private static final Log LOG = LogFactory.getLog(RoundTripIntegrationTest.class);
    private final String wktString = "POLYGON((0 0, 10 0, 5 5, 0 0))";
    private final int srid = 28992;
    private Geometry testJtsGeometry = null;

    @BeforeEach
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
            assertNotNull(nativeGeometryObject, "native geom mag niet 'null' zijn");
            assertEquals(testJtsGeometry, conv.convertToJTSGeometryObject(nativeGeometryObject));
        } catch (SQLException | ParseException | NullPointerException e) {
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
            assertNull(conv.convertToJTSGeometryObject(nativeGeometryObject),
                "verwacht 'null' na round-trip");
        } catch (SQLException | ParseException | NullPointerException e) {
            LOG.error(e);
            fail(e.getLocalizedMessage());
        }
    }
}
