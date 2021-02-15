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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import java.sql.SQLException;
import org.postgis.PGgeometry;

/**
 *
 * @author Matthijs Laan
 */
public class PostgisJdbcConverter extends GeometryJdbcConverter {

    private String schema = null;

    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        return message!=null && message.startsWith("ERROR: duplicate key value violates unique constraint");
    }

    @Override
    public boolean isFKConstraintViolationMessage(String message) {
        return message != null && message.startsWith("ERROR: insert or update on table") && message.contains("violates foreign key constraint");
    }

    @Override
    public String createPSGeometryPlaceholder() {
        //return "ST_GeomFromText(?, 28992)";
        return "?";
    }
   
    @Override
    public Object convertToNativeGeometryObject(Geometry g) throws SQLException {
      return convertToNativeGeometryObject(g, 28992);
    }
    
    @Override
    public Object convertToNativeGeometryObject(Geometry g, int srid) throws SQLException {
        if(g == null){
            return null;
        }
        String param = g.toText();
        if (param == null || param.trim().length() == 0) {
            return null;
        }
        return new PGgeometry("SRID=" + srid + ";" + param);
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
    
    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        StringBuilder builder = new StringBuilder(sql);
        builder.append(" LIMIT ");
        builder.append(limit);
        builder.append(" OFFSET ");
        builder.append(offset);
        return builder.toString();
    }

    @Override
    public StringBuilder buildLimitSql(StringBuilder sql, int limit) {
        String s = buildPaginationSql(sql.toString(), 0, limit);
        return new StringBuilder(s);
    }

    @Override
    public boolean useSavepoints() {
        return true;
    }

     @Override
    public boolean isPmdKnownBroken() {
        return false;
    }
    @Override
    public String getGeotoolsDBTypeName() {
        return "postgis";
    }

    @Override
    public String getMViewsSQL() {
        return "SELECT oid::regclass::text FROM pg_class WHERE relkind = 'm'";
    }

    @Override
    public String getMViewRefreshSQL(String mview) {
        return String.format("REFRESH MATERIALIZED VIEW %s;", mview);
    }

    @Override
    public String getSelectNextValueFromSequenceSQL(String seqName) {
        return String.format("SELECT nextval('%s')", seqName);
    }

    @Override
    public Geometry convertToJTSGeometryObject(Object nativeObj) {
        PGgeometry geom = (PGgeometry)nativeObj;
        StringBuffer sb = new StringBuffer();
        if(geom == null){
            return null;
        }
        geom.getGeometry().outerWKT(sb);
        try {
            return wkt.read(sb.toString());
        } catch (ParseException ex) {
            return null;
        }
    }
}
