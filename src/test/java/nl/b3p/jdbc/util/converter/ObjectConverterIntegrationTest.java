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
package nl.b3p.jdbc.util.converter;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.sql.*;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ObjectConverterIntegrationTest extends AbstractDatabaseIntegrationTest {
  private static final Log LOG = LogFactory.getLog(ObjectConverterIntegrationTest.class);

  static Stream<Arguments> dateProvider() {
    return Stream.of(
        // tabel, kolom, expected epoch millis localtime, input
        arguments(
            "bericht",
            "datum", /*GMT+1: 1607063255000L*/ /*GMT*/
            1607066855 * 1000L,
            "2020-12-04T07:27:35"),
        arguments("bericht", "datum", /*GMT*/ 1591255655 * 1000L, "2020-06-04T07:27:35"),
        arguments(
            "job",
            "datum", /*GMT+1: 1607018340000L*/ /*GMT*/
            1607021940 * 1000L,
            "2020-12-03T18:59:00"));
  }

  static Stream<Arguments> stringProvider() {
    return Stream.of(
        // tabel, kolom, input
        arguments("bericht", "soort", "input"),
        arguments(
            "bericht",
            "object_ref",
            "And that's when it becomes fun - you don't have to spend your time thinking about what's "
                + "happening - you just let it happen. Let's give him a friend too. Everybody needs a "
                + "friend. You gotta think like a tree. That's what makes life fun. That you can make "
                + "these decisions. That you can create the world that you want. Fluff that up. "
                + "Everything is happy if you choose to make it that way."));
  }

  @BeforeEach
  @Override
  public void setUp() throws Exception {
    loadProps();
  }

  @DisplayName("Test conversie van tekstuele datum naar database type")
  @ParameterizedTest(name = "{index}: tabel: {0}, kolom: {1}")
  @MethodSource("dateProvider")
  public void testConvertToSQLObjectDatum(
      String tableName, String colName, long expected, String input) {
    try (Connection c =
        DriverManager.getConnection(
            params.getProperty("staging.jdbc.url"),
            params.getProperty("staging.user"),
            params.getProperty("staging.passwd"))) {

      Object o =
          GeometryJdbcConverter.convertToSQLObject(
              input, getColumnMetadata(c, tableName, colName), tableName, colName);

      assertTrue(o instanceof java.sql.Date);

      LOG.debug(
          "converted datum object (input: "
              + input
              + "): "
              + (new java.text.SimpleDateFormat()).format(((Date) o)));

      Calendar cal = Calendar.getInstance();
      cal.setTimeZone(TimeZone.getTimeZone("GMT"));
      cal.setTimeInMillis(expected);
      cal.setTimeZone(TimeZone.getDefault());

      long utcOffset = cal.getTimeZone().getOffset(expected);

      LOG.debug(
          "calendar timezone '"
              + cal.getTimeZone().getDisplayName()
              + "', is DST ?: "
              + cal.getTimeZone().inDaylightTime(new Date(expected))
              + ", DST offset : "
              + cal.getTimeZone().getDSTSavings()
              + ", UTC offset : "
              + utcOffset);

      assertEquals(
          cal.getTime().getTime() - utcOffset,
          ((Date) o).getTime(),
          "datum is ongelijk, dat kan een tijdzone/zomertijd verschil zijn");

    } catch (SQLException e) {
      LOG.error("Fout tijdens uitlezen kolom informatie", e);
      fail(e.getLocalizedMessage());
    }
  }

  @ParameterizedTest(name = "{index}: tabel: {0}, kolom: {1}")
  @MethodSource("stringProvider")
  public void testConvertToSQLObjectString(String tableName, String colName, String input) {
    try (Connection c =
        DriverManager.getConnection(
            params.getProperty("staging.jdbc.url"),
            params.getProperty("staging.user"),
            params.getProperty("staging.passwd"))) {

      Object o =
          GeometryJdbcConverter.convertToSQLObject(
              input, getColumnMetadata(c, tableName, colName), tableName, colName);
      assertTrue(o instanceof java.lang.String);
      assertEquals(input, o);

    } catch (SQLException e) {
      LOG.error("Fout tijdens uitlezen kolom informatie", e);
      fail(e.getLocalizedMessage());
    }
  }
}
