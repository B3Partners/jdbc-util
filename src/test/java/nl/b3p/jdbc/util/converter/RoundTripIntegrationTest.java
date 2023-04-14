/*
 * Copyright (C) 2019 B3Partners B.V.
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
package nl.b3p.jdbc.util.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * test of de converters round-trip safe zijn, dus JTS-input naar native naar JTS-output moet gelijk
 * blijven.
 *
 * <p>gebruik bijvoorbeeld: {@code mvn -Dit.test=RoundTripIntegrationTest verify -Poracle
 * -Dtest.onlyITs=true} om deze test te draaien.
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
    try (Connection c =
        DriverManager.getConnection(
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
    try (Connection c =
        DriverManager.getConnection(
            params.getProperty("staging.jdbc.url"),
            params.getProperty("staging.user"),
            params.getProperty("staging.passwd"))) {
      GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
      Object nativeGeometryObject = conv.convertToNativeGeometryObject((String) null);
      LOG.debug("nativeGeometryObject: " + nativeGeometryObject);
      assertNull(
          conv.convertToJTSGeometryObject(nativeGeometryObject), "verwacht 'null' na round-trip");
    } catch (SQLException | ParseException | NullPointerException e) {
      LOG.error(e);
      fail(e.getLocalizedMessage());
    }
  }
}
