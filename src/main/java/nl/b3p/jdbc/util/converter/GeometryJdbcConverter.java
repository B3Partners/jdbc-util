/*
 * Copyright (C) 2017 B3Partners B.V.
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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * @author Matthijs Laan
 * @author Meine Toonen
 * @author mprins
 */
public abstract class GeometryJdbcConverter {
  private static final Log LOG = LogFactory.getLog(GeometryJdbcConverter.class);
  protected final WKTReader wkt = new WKTReader();
  protected GeometryFactory gf = new GeometryFactory();

  public static Object convertToSQLObject(
      String stringValue, ColumnMetadata cm, String tableName, String column) {
    Object param;
    stringValue = stringValue.trim();
    switch (cm.getDataType()) {
      case java.sql.Types.DECIMAL:
      case java.sql.Types.NUMERIC:
      case java.sql.Types.INTEGER:
        try {
          param = new BigDecimal(stringValue);
        } catch (NumberFormatException nfe) {
          // try to convert to 1/0 for Oracle boolean
          if ("true".equalsIgnoreCase(stringValue)) {
            param = BigDecimal.ONE;
          } else if ("false".equalsIgnoreCase(stringValue)) {
            param = BigDecimal.ZERO;
          } else {
            throw new NumberFormatException(
                String.format(
                    "Conversie van waarde \"%s\" naar type %s voor %s.%s niet mogelijk",
                    stringValue, cm.getTypeName(), tableName, cm.getName()));
          }
        }
        break;
      case java.sql.Types.CHAR:
      case java.sql.Types.VARCHAR:
      case java.sql.Types.NVARCHAR:
      case java.sql.Types.LONGNVARCHAR:
      case java.sql.Types.LONGVARCHAR:
        param = stringValue;
        break;
      case java.sql.Types.DATE:
      case java.sql.Types.TIMESTAMP:
        try {
          param = LocalDateTime.parse(stringValue);
        } catch (DateTimeParseException e) {
          LOG.debug(
              "Parsen van waarde "
                  + stringValue
                  + " als LocalDateTime is mislukt, probeer als "
                  + "LocalDate.");
          try {
            param = LocalDate.parse(stringValue).atTime(0, 0);
          } catch (DateTimeParseException e2) {
            LOG.error("Fout tijdens parsen van waarde " + stringValue + " als LocalDate", e2);
            param = null;
          }
        }
        if (param != null) {
          param =
              new java.sql.Date(
                  Date.from(((LocalDateTime) param).atZone(ZoneId.systemDefault()).toInstant())
                      .getTime());
        }
        break;
      case java.sql.Types.BOOLEAN:
      case java.sql.Types.BIT:
        param = Boolean.parseBoolean(stringValue);
        break;
        //            case java.sql.Types.BIT:
        //                PostgreSQL boolean kolom komt uit de JDBC drivar als BIT / -7 en niet als
        // BOOLEAN / 16
        //                // try to convert to 1/0 for MSSQL boolean
        //                if ("true".equalsIgnoreCase(stringValue)) {
        //                    param = 1;
        //                } else if ("false".equalsIgnoreCase(stringValue)) {
        //                    param = 0;
        //                } else {
        //                    param = Integer.parseInt(stringValue);
        //                }
        //                break;
      default:
        throw new UnsupportedOperationException(
            String.format(
                "Data type %s (#%d) van kolom \"%s\" wordt niet ondersteund.",
                cm.getTypeName(), cm.getDataType(), cm.getName()));
    }
    return param;
  }

  // definieer placeholder als ? wanneer object naar native geometry wordt
  // geconverteerd
  // defineer placeholder via native wkt-import functie als geometry als
  // wkt-string wordt doorgegeven
  public abstract Object convertToNativeGeometryObject(Geometry param)
      throws SQLException, ParseException;

  public abstract Object convertToNativeGeometryObject(Geometry param, int srid)
      throws SQLException, ParseException;

  public abstract Geometry convertToJTSGeometryObject(Object nativeObj);

  public String createPSGeometryPlaceholder() throws SQLException {
    return "?";
  }

  public abstract String getSchema();

  public abstract String getGeomTypeName();

  public abstract boolean isDuplicateKeyViolationMessage(String message);

  /**
   * bepaal of een melding een constraint violation betreft.
   *
   * @param message de melding uit de database
   * @return {@code true} als de melding een contraint violation betreft
   */
  public abstract boolean isFKConstraintViolationMessage(String message);

  public abstract String buildPaginationSql(String sql, int offset, int limit);

  public abstract StringBuilder buildLimitSql(StringBuilder sql, int limit);

  public abstract boolean useSavepoints();

  public abstract boolean isPmdKnownBroken();

  public abstract String getMViewsSQL();

  public abstract String getMViewRefreshSQL(String mview);

  /**
   * Gets a statement to use in a {@link java.sql.PreparedStatement } to restart a sequence.
   *
   * @param seqName name of sequence
   * @param nextVal the value to restart the sequence, some systems require this to be larger than
   *     the next value of the sequence.
   * @return SQL statement specific for the flavour of database
   */
  public String getUpdateSequenceSQL(String seqName, long nextVal) {
    // supported for postgres, ms sql, hsqldb, NB return values vary
    // https://www.postgresql.org/docs/11/sql-altersequence.html
    // https://docs.microsoft.com/en-us/sql/t-sql/statements/alter-sequence-transact-sql?view=sql-server-ver15

    return String.format("ALTER SEQUENCE %s RESTART WITH %d", seqName, nextVal);
  }

  /**
   * get the database flavour specific SQL statement to get the next value from a sequence.
   *
   * @param seqName name of sequence
   * @return SQL statement specific for the flavour of database
   */
  public abstract String getSelectNextValueFromSequenceSQL(String seqName);

  public abstract String getGeotoolsDBTypeName();

  public Object convertToNativeGeometryObject(String param) throws ParseException, SQLException {
    Geometry o = null;
    if (param != null && param.trim().length() > 0) {
      o = wkt.read(param);
    }
    return convertToNativeGeometryObject(o);
  }

  public Object createNativePoint(double lat, double lon, int srid)
      throws SQLException, ParseException {
    if (lat == 0 || lon == 0) {
      return null;
    }
    Point p = gf.createPoint(new Coordinate(lon, lat));
    return convertToNativeGeometryObject(p, srid);
  }
}
