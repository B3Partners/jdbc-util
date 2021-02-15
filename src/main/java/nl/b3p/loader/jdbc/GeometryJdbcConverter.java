/*
 * Copyright (C) 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.loader.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 *
 * @author Matthijs Laan
 * @author Meine Toonen
 * @author mprins
 */
public abstract class GeometryJdbcConverter {
    private static final Log LOG = LogFactory.getLog(GeometryJdbcConverter.class);

    static public Object convertToSQLObject(String stringValue, ColumnMetadata cm,
            String tableName, String column) {
        Object param;
        stringValue = stringValue.trim();
        switch (cm.getDataType()) {
            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
            case java.sql.Types.INTEGER:
                try {
                    param = new BigDecimal(stringValue);
                } catch (NumberFormatException nfe) {
                    throw new NumberFormatException(
                            String.format("Conversie van waarde \"%s\" naar type %s voor %s.%s niet mogelijk",
                                    stringValue,
                                    cm.getTypeName(),
                                    tableName,
                                    cm.getName()));
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
                            "Parsen van waarde " + stringValue + " als LocalDateTime is mislukt, probeer als " +
                                    "LocalDate.");
                    try {
                        param = LocalDate.parse(stringValue).atTime(0, 0);
                    } catch (DateTimeParseException e2) {
                        LOG.error("Fout tijdens parsen van waarde " + stringValue + " als LocalDate", e2);
                        param = null;
                    }
                }
                if (param != null) {
                    param = new java.sql.Date(
                            Date.from(((LocalDateTime) param).atZone(ZoneId.systemDefault()).toInstant()).getTime()
                    );
                }
                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("Data type %s (#%d) van kolom \"%s\" wordt niet ondersteund.",
                                cm.getTypeName(), cm.getDataType(), cm.getName())
                );
        }
        return param;
    }

    protected GeometryFactory gf = new GeometryFactory();
    protected final WKTReader wkt = new WKTReader();

    //definieer placeholder als ? wanneer object naar native geometry wordt
    //geconverteerd
    //defineer placeholder via native wkt-import functie als geometry als
    //wkt-string wordt doorgegeven
    public abstract Object convertToNativeGeometryObject(Geometry param) throws SQLException, ParseException;

    public abstract Object convertToNativeGeometryObject(Geometry param, int srid) throws SQLException, ParseException;

    public abstract Geometry convertToJTSGeometryObject(Object nativeObj);

    public abstract String createPSGeometryPlaceholder() throws SQLException;

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
     * @param nextVal the value to restart the sequence, some systems
     *                require this to be larger than the next value of the sequence.
     * @return SQL statement specific for the flavour of database
     */
    public String getUpdateSequenceSQL(String seqName, long nextVal) {
        // supported for postgres, ms sql, hsqldb, NB return values vary
        // https://www.postgresql.org/docs/11/sql-altersequence.html
        // https://docs.microsoft.com/en-us/sql/t-sql/statements/alter-sequence-transact-sql?view=sql-server-ver15

        return String.format("ALTER SEQUENCE %s RESTART WITH %d", seqName , nextVal);
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
        if (param != null) {
            o = wkt.read(param);
        }
        return convertToNativeGeometryObject(o);
    }

    public Object createNativePoint(double lat, double lon, int srid) throws SQLException, ParseException {
        if (lat == 0 || lon == 0) {
            return null;
        }
        Point p = gf.createPoint(new Coordinate(lon, lat));
        return convertToNativeGeometryObject(p, srid);
    }
}
