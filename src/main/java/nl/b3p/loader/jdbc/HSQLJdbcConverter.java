/*
 * Copyright (C) 2016 - 2017 B3Partners B.V.
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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
    public String createPSGeometryPlaceholder() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getGeomTypeName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Geometry convertToJTSGeometryObject(Object nativeObj) {

        try {
            Clob c = (Clob)nativeObj;
            if(c == null){
                return null;
            }
            InputStream in = c.getAsciiStream();
            StringWriter w = new StringWriter();
            IOUtils.copy(in, w);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isFKConstraintViolationMessage(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StringBuilder buildLimitSql(StringBuilder sql, int limit) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean useSavepoints() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMViewsSQL() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMViewRefreshSQL(String mview) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getGeotoolsDBTypeName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object convertToNativeGeometryObject(Geometry param, int srid) throws SQLException, ParseException {
        if(param == null){
            return null;
        }
        return param.toText();
    }
}
