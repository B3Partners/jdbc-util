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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import java.math.BigDecimal;
import com.vividsolutions.jts.io.WKTReader;
import java.sql.SQLException;
import java.util.Calendar;

/**
 *
 * @author Matthijs Laan
 * @author Meine Toonen
 */
public abstract class GeometryJdbcConverter {

    static public Object convertToSQLObject(String stringValue, ColumnMetadata cm,
            String tableName, String column) {
        Object param = null;
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
                param = stringValue;
                break;
            case java.sql.Types.DATE:
            case java.sql.Types.TIMESTAMP:
                param = javax.xml.bind.DatatypeConverter.parseDateTime(stringValue);
                if (param != null) {
                    Calendar cal = (Calendar) param;
                    param = new java.sql.Date(cal.getTimeInMillis());
                }
                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("Data type %s (#%d) van kolom \"%s\" wordt niet ondersteund.", cm.getTypeName(), cm.getDataType(), column));
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

    public abstract String buildPaginationSql(String sql, int offset, int limit);

    public abstract StringBuilder buildLimitSql(StringBuilder sql, int limit);

    public abstract boolean useSavepoints();

    public abstract boolean isPmdKnownBroken();

    public abstract String getMViewsSQL();

    public abstract String getMViewRefreshSQL(String mview);

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
