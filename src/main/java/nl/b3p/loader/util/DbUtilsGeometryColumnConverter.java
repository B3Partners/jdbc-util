// Copyright (C) 2017 B3Partners B.V.
//
// SPDX-License-Identifier: MIT

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
