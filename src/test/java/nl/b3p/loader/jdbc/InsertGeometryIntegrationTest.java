package nl.b3p.loader.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nl.b3p.AbstractDatabaseIntegrationTest;
import static org.junit.Assert.fail;

import nl.b3p.brmo.test.util.database.HSQLDBDriverBasedFailures;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

@Category(HSQLDBDriverBasedFailures.class)
public class InsertGeometryIntegrationTest extends AbstractDatabaseIntegrationTest {

    private Geometry geom;
    private final String wktString = "POLYGON((0 0, 10 0, 5 5, 0 0))";
    private final String ewktString = "epsg:28995;POLYGON((0 0, 10 0, 5 5, 0 0))";
    private final String insertStatement = "INSERT INTO geometries (geom, naam) VALUES ";

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

            PreparedStatement ps = c.prepareStatement(insertStatement + conv.createPSGeometryPlaceholder());
            ps.setObject(1, o);

            ResultSet rs = ps.executeQuery();
            c.commit();
        } catch (SQLException sqle) {
            fail("Insert failed, msg: " + sqle.getLocalizedMessage());
        }
    }

    @Test
    @Ignore("TODO")
    public void testGeometryInsertFromString() throws SQLException, ParseException {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            Object o = conv.convertToNativeGeometryObject(wktString);
        }
    }

}
