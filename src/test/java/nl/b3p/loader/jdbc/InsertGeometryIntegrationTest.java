package nl.b3p.loader.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import nl.b3p.AbstractDatabaseIntegrationTest;

import nl.b3p.brmo.test.util.database.HSQLDBDriverBasedFailures;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import static org.junit.Assert.*;

@Category(HSQLDBDriverBasedFailures.class)
public class InsertGeometryIntegrationTest extends AbstractDatabaseIntegrationTest {

    private final String wktString = "POLYGON((0 0, 10 0, 5 5, 0 0))";
    private final int srid = 28992;
    private final String geomName = "test";
    private final String ewktString = "epsg:28992;POLYGON((0 0, 10 0, 5 5, 0 0))";
    private final String insertStatement = "INSERT INTO geometries (geom, naam) VALUES (";
    private Geometry geom = null;

    @Before
    @Override
    public void setUp() throws Exception {
        loadProps();
        final WKTReader r = new WKTReader();
        geom = r.read(wktString);
    }

    @Test
    public void testGeometryInsertFromGeom() throws ParseException {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            Object o = conv.convertToNativeGeometryObject(geom);

            PreparedStatement ps = c.prepareStatement(insertStatement + conv.createPSGeometryPlaceholder() + ", ?)");
            ps.setObject(1, o);
            ps.setString(2, geomName + 1);
            assertEquals("", 1, ps.executeUpdate());
        } catch (SQLException sqle) {
            fail("Insert failed, msg: " + sqle.getLocalizedMessage());
        }
    }

    @Test
    public void testGeometryInsertFromGeomNoSrid() throws ParseException {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            Object o = conv.convertToNativeGeometryObject(geom, srid);

            PreparedStatement ps = c.prepareStatement(insertStatement + conv.createPSGeometryPlaceholder() + ", ?)");
            ps.setObject(1, o);
            ps.setString(2, geomName + 2);
            assertEquals("", 1, ps.executeUpdate());
        } catch (SQLException sqle) {
            fail("Insert failed, msg: " + sqle.getLocalizedMessage());
        }
    }
}
