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

import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.oracle.sdo.GeometryConverter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.sql.SQLException;

/**
 *
 * @author Matthijs Laan
 */
public class OracleJdbcConverter extends GeometryJdbcConverter {
    protected final static Log LOG = LogFactory.getLog(OracleJdbcConverter.class);
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
    public boolean isFKConstraintViolationMessage(String message) {
        return message != null && message.startsWith("ORA-02291:");
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
        return true;
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
    public String getSelectNextValueFromSequenceSQL(String seqName) {
        return String.format("SELECT %s.nextval FROM dual", seqName);
    }

    /**
     * de geotools converter is niet round-trip safe, er treed een NPE op als een
     * 'lege' geometrie wordt aangeboden:
     * <pre>
     *    at org.geotools.data.oracle.sdo.SDO.ETYPE(SDO.java:1681)
     *    at org.geotools.data.oracle.sdo.SDO.create(SDO.java:1933)
     * 	  at org.geotools.data.oracle.sdo.GeometryConverter.asGeometry(GeometryConverter.java:121)
     * </pre>
     * dat is een bug in geotools.
     *
     * @param nativeObj uit de database
     * @return jts geom of {@code null}
     */
    @Override
    public Geometry convertToJTSGeometryObject(Object nativeObj) {
        org.locationtech.jts.geom.Geometry jts = null;
        if (nativeObj == null) {
            return jts;
        } else if (STRUCT.class.isAssignableFrom(nativeObj.getClass())) {
            try {
                jts = gc.asGeometry((STRUCT) nativeObj);
            } catch (SQLException | NullPointerException e) {
                LOG.error("Error parsing Oracle STRUCT to geometry", e);
            }
        } else {
            LOG.error("Native Oracle object can not be converted to JTS geometry");
        }
        return jts;
    }

    public String getUpdateSequenceSQL(String seqName, long nextVal){
        throw new UnsupportedOperationException("Update sequence not supported for this database version.");
    }
}
