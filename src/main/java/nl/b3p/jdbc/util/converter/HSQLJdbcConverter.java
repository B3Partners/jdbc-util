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

import org.hsqldb.jdbc.JDBCClob;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.SQLException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class HSQLJdbcConverter  extends GeometryJdbcConverter {

    protected final static Log LOG = LogFactory.getLog(HSQLJdbcConverter.class);
    
    @Override
    public Object convertToNativeGeometryObject(Geometry param) throws SQLException, ParseException {
        return convertToNativeGeometryObject(param, 28992);
    }

    @Override
    public String createPSGeometryPlaceholder() {
        return "?";
    }

    @Override
    public String getGeomTypeName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Geometry convertToJTSGeometryObject(Object nativeObj) {

        try {
            Clob c = (Clob) nativeObj;
            if(c == null){
                return null;
            }
            InputStream in = c.getAsciiStream();
            StringWriter w = new StringWriter();
            IOUtils.copy(in, w, Charset.defaultCharset());
            Geometry g;
            
            g = wkt.read(w.toString());
            
            return g;
        } catch (IOException | ParseException | SQLException ex) {
            LOG.error("Error parsing clob to geometry", ex);
            return null;
        }
    }

    @Override
    public boolean isPmdKnownBroken() {
        return false;
    }

    @Override
    public String getSchema() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isFKConstraintViolationMessage(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StringBuilder buildLimitSql(StringBuilder sql, int limit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean useSavepoints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getMViewsSQL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getMViewRefreshSQL(String mview) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSelectNextValueFromSequenceSQL(String seqName) {
        return "CALL NEXT VALUE FOR " + seqName;
    }

    @Override
    public String getGeotoolsDBTypeName() {
        // no hsqldb datastore in geotools
        return null;
    }

    @Override
    public Object convertToNativeGeometryObject(Geometry param, int srid) throws SQLException {
        if(param == null){
            return null;
        }
        return new JDBCClob(param.toText());
    }
}
