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

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.oracle.sdo.GeometryConverter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.sql.SQLException;
import java.sql.Struct;

/**
 *
 * @author Matthijs Laan
 */
public class OracleJdbcConverter extends GeometryJdbcConverter {
    protected final static Log LOG = LogFactory.getLog(OracleJdbcConverter.class);
    private final GeometryConverter gc;
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
    public Object convertToNativeGeometryObject(Geometry g, int srid) throws SQLException {
        // geen (Object)null geven, dat levert in veel gevallen een
        // java.sql.SQLException: ORA-00932: inconsistent datatypes: expected MDSYS.SDO_GEOMETRY got CHAR
        // if (null ==g){
        //     return ((OracleStruct)null);
        // }
        return gc.toSDO(g, srid);
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
        } else if (OracleStruct.class.isAssignableFrom(nativeObj.getClass())) {
            try {
                jts = gc.asGeometry((OracleStruct) nativeObj);
            } catch (SQLException | NullPointerException e) {
                LOG.error("Error parsing OracleStruct to geometry", e);
            }
        } else {
            LOG.error("Native Oracle object can not be converted to JTS geometry");
        }
        return jts;
    }

    /**
     * Gets a statement to use in a prepared statement to restart a sequence.
     * This assumes no other interactions are going on with the sequence;
     * <em>can only be used to increase the value of the sequence, not decrease.</em>
     *
     * @param seqName name of sequence
     * @return SQL statement specific for the flavour of database
     */
    @Override
    public String getUpdateSequenceSQL(String seqName, long nextVal) {
        // zie: https://rogertroller.com/2018/02/20/oracle-18c-alter-sequence-restart/
        return String.format("ALTER SEQUENCE %s RESTART START WITH %d",seqName, nextVal);
    }
}
