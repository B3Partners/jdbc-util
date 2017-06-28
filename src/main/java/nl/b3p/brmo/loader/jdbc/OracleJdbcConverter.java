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

package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.oracle.sdo.GeometryConverter;

/**
 *
 * @author Matthijs Laan
 */
public class OracleJdbcConverter extends GeometryJdbcConverter {

    private static final Log log = LogFactory.getLog(OracleJdbcConverter.class);
    
    private GeometryConverter gc;
    private String schema;

    public OracleJdbcConverter(OracleConnection oc) throws SQLException {
        gc = new GeometryConverter(oc, gf);
    }

    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        return message!=null && message.startsWith("ORA-00001:");
    }

    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        // return "SDO_GEOMETRY(?, 28992)";
        return "?";
    }
    
    @Override
    public Object convertToNativeGeometryObject(Geometry g, int srid) throws SQLException, ParseException {
        if(g == null){
            return gc.toSDO(null, srid);
        }
        String param = g.toText();
        // return param;
        WKTReader reader = new WKTReader(gf);
        Geometry geom = param == null || param.trim().length() == 0 ? null : reader.read(param);
        return gc.toSDO(geom, srid);
    }
    
    @Override
    public Object convertToNativeGeometryObject(Geometry g) throws SQLException, ParseException {
        return convertToNativeGeometryObject(g, 28992);
    }


    /**
     * @return the schema
     */
    @Override
    public String getSchema() {
        return schema;
    }

    /**
     * @param schema the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getGeomTypeName() {
        return "SDO_GEOMETRY";
    }
    
    
    /*
     * Check http://www.oracle.com/technetwork/issue-archive/2006/06-sep/o56asktom-086197.html
     * why just WHERE ROWNUM > x AND ROWNUM < x does not work.
     * Using subquery with optimizer.
    */
    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        StringBuilder builder = new StringBuilder();

        builder.append("select * from (");
        builder.append("select /*+ FIRST_ROWS(n) */");
        builder.append(" a.*, ROWNUM rnum from (");

        builder.append(sql);

        builder.append(" ) a where ROWNUM <=");

        builder.append(offset + limit);

        builder.append(" )");

        builder.append(" where rnum  > ");
        builder.append(offset);

        return builder.toString();
    }

    /**
     * Voegt een limiet toe aan een query te gebruiken in geval van insert met
     * select. Bijvoorbeeld zoals het plaatsen van stand-berichten in de job
     * tabel.
     * <b>NB</b> {@link #buildPaginationSql} is niet bruikbaar voor een
     * insert+select
     *
     * @param sql query zonder limiet
     * @param limit max aantal op te halen records dat voldoet aan query
     * @return query met limiet
     * @since 1.4.1
     */
    @Override
    public StringBuilder buildLimitSql(StringBuilder sql, int limit) {
        sql.append(" FETCH FIRST ").append(limit).append(" ROWS ONLY");
        return sql;
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
        return "oracle";
    }

    @Override
    public String getMViewsSQL() {
        return "SELECT MVIEW_NAME FROM ALL_MVIEWS";
    }

    @Override
    public String getMViewRefreshSQL(String mview) {
        //return String.format("DBMS_MVIEW.REFRESH('%s','?','',FALSE,TRUE,0,0,0,FALSE,FALSE)", mview);
        return String.format("begin\ndbms_mview.refresh('%s','C');\nend;", mview);
    }

    @Override
    public Geometry convertToJTSGeometryObject(Object nativeObj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
