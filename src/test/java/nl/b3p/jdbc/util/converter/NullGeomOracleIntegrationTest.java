/*
 * Copyright (C) 2016 B3Partners B.V.
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

import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * gebruik {@code mvn -Dit.test=NullGeomOracleIntegrationTest verify -Poracle -Dtest.onlyITs=true}
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
  @ParameterizedTest(name = "#{index} - waarde: `{0}`")
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   "})
  public void testNullGeomXML(String testVal) throws Exception {
    if (isOracle) {
      Connection connection =
          DriverManager.getConnection(
              params.getProperty("staging.jdbc.url"),
              params.getProperty("staging.user"),
              params.getProperty("staging.passwd"));

      OracleConnection oc = OracleConnectionUnwrapper.unwrap(connection);
      OracleJdbcConverter c = new OracleJdbcConverter(oc);

      OracleStruct s = (OracleStruct) c.convertToNativeGeometryObject(testVal);
      for (Object o : s.getAttributes()) {
        assertNull(o, "verwacht een null object");
      }
    }
  }
}
