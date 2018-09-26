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

import org.locationtech.jts.io.ParseException;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.db.sqlserver.Encoders;


/**
 *
 * @author Matthijs Laan
 */
public class MssqlJdbcConverter extends GeometryJdbcConverter {

    private String schema = "dbo";

    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        //Error Code: 2627
        //Violation of %ls constraint '%.*ls'. Cannot insert duplicate key in object '%.*ls'.
        return message!=null && message.contains("Cannot insert duplicate key in object");
    }
    @Override
    public boolean isFKConstraintViolationMessage(String message) {
        return message != null && message.startsWith("The INSERT statement conflicted with the FOREIGN KEY constraint");
    }

    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        //return "geometry::STGeomFromText(?, 28992)";
        return "?";
    }
    
    @Override
    public Object convertToNativeGeometryObject(org.locationtech.jts.geom.Geometry g, int srid) throws SQLException, ParseException {
        if (g == null) {
            return null;
        }
        String param = g.toText();
        // return param;
        if (param == null || param.trim().length() == 0) {
            return null;
        }
        Geometry geom = Wkt.fromWkt("SRID=" +  srid + "; " + param);
        byte[] ret = Encoders.encode(geom);
        return ret;
    }

    @Override
    public Object convertToNativeGeometryObject(org.locationtech.jts.geom.Geometry g) throws SQLException, ParseException {
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
        //return true; // microsoft driver
        return false; // jtds driver
    }

    @Override
    public String getGeotoolsDBTypeName() {
        // see: http://docs.geotools.org/stable/userguide/library/jdbc/sqlserver.html
        final String name = "jtds-sqlserver";
        // we gebruiken altijd jtds, maar anders.. 
        // if(...){name = "sqlserver"}
        return name;
    }

    /**
     * return een dummy query omdat mssql geen materialized views kent.
     *
     * @return een dummy query omdat mssql geen materialized views kent.
     */
    @Override
    public String getMViewsSQL() {
        return "select 1 from brmo_metadata where 1 = 2";
    }

    /**
     * return een dummy query omdat mssql geen materialized views kent.
     *
     * @return een dummy query omdat mssql geen materialized views kent.
     */
    @Override
    public String getMViewRefreshSQL(String mview) {
        return getMViewsSQL();
    }

    @Override
    public org.locationtech.jts.geom.Geometry convertToJTSGeometryObject(Object nativeObj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
