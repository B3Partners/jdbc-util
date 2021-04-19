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
package nl.b3p.loader.util;

import org.locationtech.jts.geom.Geometry;
import java.sql.ResultSet;
import java.sql.SQLException;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import org.apache.commons.dbutils.BeanProcessor;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class DbUtilsGeometryColumnConverter extends BeanProcessor {

    private final GeometryJdbcConverter gjc;

    public DbUtilsGeometryColumnConverter(GeometryJdbcConverter gjc) {
        this.gjc = gjc;
    }

    @Override
    protected Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {
        if (Geometry.class.isAssignableFrom(propType)) {
            Object o = rs.getObject(index);
            return gjc.convertToJTSGeometryObject(o);
        } else {
            return super.processColumn(rs, index, propType);
        }
    }
}
