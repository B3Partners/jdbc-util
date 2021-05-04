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

package nl.b3p.loader.jdbc;

import com.microsoft.sqlserver.jdbc.Geometry;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.locationtech.jts.io.ParseException;
import java.sql.SQLException;


/**
 * @author Matthijs Laan
 * @author mprins
 */
public class MssqlJdbcConverter extends GeometryJdbcConverter {
    private final static Log LOG = LogFactory.getLog(MssqlJdbcConverter.class);
    private String schema = "dbo";
    // a select query that returns empty result any time any place
    private static final String NOT_IMPLEMENTED_DUMMY_SQL_SELECT = "select 1 where 1 = 2";
    // an update / delete query that returns 0 results for update
    private static final String NOT_IMPLEMENTED_DUMMY_SQL_UPDATE = "while 1 = 0 break";


    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        //Error Code: 2627
        //Violation of %ls constraint '%.*ls'. Cannot insert duplicate key in object '%.*ls'.
        return message != null && message.contains("Cannot insert duplicate key in object");
    }

    @Override
    public boolean isFKConstraintViolationMessage(String message) {
        return message != null && message.startsWith("The INSERT statement conflicted with the FOREIGN KEY constraint");
    }

    @Override
    public String createPSGeometryPlaceholder() {
        //return "geometry::STGeomFromText(?, 28992)";
        return "?";
    }

    @Override
    public Object convertToNativeGeometryObject(org.locationtech.jts.geom.Geometry g, int srid) throws SQLException {
        if (g == null) {
            return null;
        }
        String param = g.toText();
        // geotools
        // import org.geotools.geometry.jts.WKTWriter2;
        // import org.locationtech.jts.io.WKTWriter;
        // String param = (new WKTWriter2(2)).write(g);
        if (param == null || param.trim().length() == 0) {
            return null;
        }
        LOG.trace("Converted geom WKT: " + param + ", SRID: " + srid);
        Geometry sqlGeom = Geometry.STGeomFromText(param, srid);
        LOG.trace("mssql geom: " + sqlGeom);
        return sqlGeom;
    }

    @Override
    public Object convertToNativeGeometryObject(org.locationtech.jts.geom.Geometry g) throws SQLException {
        return convertToNativeGeometryObject(g, 28992);
    }

    @Override
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getGeomTypeName() {
        return "geometry";
    }

    /*
    QUERY USING "ROW_NUMBER"
    DECLARE @PageNumber AS INT, @RowspPage AS INT
    SET @PageNumber = 2
    SET @RowspPage = 10 
    SELECT * FROM (
                 SELECT ROW_NUMBER() OVER(ORDER BY ID_EXAMPLE) AS Numero,
                        ID_EXAMPLE, NM_EXAMPLE , DT_CREATE FROM TB_EXAMPLE
                   ) AS TBL
    WHERE Numero BETWEEN ((@PageNumber - 1) * @RowspPage + 1) AND (@PageNumber * @RowspPage)
    ORDER BY ID_EXAMPLE
    GO

    QUERY USING "OFFSET" AND "FETCH NEXT" (SQL SERVER 2012)
    DECLARE @PageNumber AS INT, @RowspPage AS INT
    SET @PageNumber = 2
    SET @RowspPage = 10
    
    SELECT ID_EXAMPLE, NM_EXAMPLE, DT_CREATE
    FROM TB_EXAMPLE
    ORDER BY ID_EXAMPLE
    OFFSET ((@PageNumber - 1) * @RowspPage) ROWS
    FETCH NEXT @RowspPage ROWS ONLY
    GO    
    */
    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        StringBuilder builder = new StringBuilder(sql);
        if (!StringUtils.containsIgnoreCase(sql, "ORDER BY")) {
            // OFFSET ... FETCH queries require order by,
            // see https://msdn.microsoft.com/en-us/library/gg699618.aspx?f=255&MSPPError=-2147217396
            builder.append(" ORDER BY id ");
        }
        builder.append(" OFFSET ");
        builder.append(offset);
        builder.append(" ROWS FETCH NEXT ");
        builder.append(limit);
        builder.append(" ROWS ONLY ");
        return builder.toString();
    }

    @Override
    public StringBuilder buildLimitSql(StringBuilder sql, int limit) {
        String s = buildPaginationSql(sql.toString(), 0, limit);
        return new StringBuilder(s);
    }

    @Override
    public boolean useSavepoints() {
        return false;
    }

    @Override
    public boolean isPmdKnownBroken() {
        return false;
    }

    @Override
    public String getGeotoolsDBTypeName() {
        // see: http://docs.geotools.org/stable/userguide/library/jdbc/sqlserver.html
        return "sqlserver";
    }

    /**
     * return een dummy query omdat mssql geen materialized views kent.
     *
     * @return een dummy select query omdat mssql geen materialized views kent.
     */
    @Override
    public String getMViewsSQL() { return NOT_IMPLEMENTED_DUMMY_SQL_SELECT; }

    /**
     * return een dummy query omdat mssql geen materialized views kent.
     *
     * @return een dummy update query omdat mssql geen materialized views kent.
     */
    @Override
    public String getMViewRefreshSQL(String mview) { return NOT_IMPLEMENTED_DUMMY_SQL_UPDATE; }

    @Override
    public String getSelectNextValueFromSequenceSQL(String seqName) {
        return String.format("SELECT NEXT VALUE FOR %s", seqName);
    }

    @Override
    public org.locationtech.jts.geom.Geometry convertToJTSGeometryObject(Object nativeObj) {
        org.locationtech.jts.geom.Geometry jts = null;
        if (nativeObj == null) {
            return jts;
        } else if (Geometry.class.isAssignableFrom(nativeObj.getClass())) {
            try {
                jts = wkt.read(((Geometry) nativeObj).STAsText());
                jts.setSRID(((Geometry) nativeObj).getSrid());
            } catch (ParseException | SQLServerException e) {
                LOG.error("Error converting SQL Server to JTS geometry", e);
            }
        }
        return jts;
    }
}
