/*
 * Copyright (C) 2018 B3Partners B.V.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * testcases voor {@link GeometryJdbcConverterFactory}. Gebruik
 * {@code mvn -Dit.test=GeometryJdbcConverterFactoryIntegrationTest verify -Poracle -Dtest.onlyITs=true}
 * om deze test te draaien tegen oracle of
 * {@code mvn -Dit.test=GeometryJdbcConverterFactoryIntegrationTest verify -Ppostgresql -Dtest.onlyITs=true}
 * tegen postgresql.
 */
public class GeometryJdbcConverterFactoryIntegrationTest extends AbstractDatabaseIntegrationTest {

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    @Test
    public void testGetGeometryJdbcConverter() {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            if (isMsSQL) {
                assertThat("Onjuiste geometry converter gekregen.", conv, instanceOf(MssqlJdbcConverter.class));
            } else if (isOracle) {
                assertThat("Onjuiste geometry converter gekregen.", conv, instanceOf(OracleJdbcConverter.class));
            } else if (isPostgis) {
                assertThat("Onjuiste geometry converter gekregen.", conv, instanceOf(PostgisJdbcConverter.class));
            } else if (isHSQLDB) {
                assertThat("Onjuiste geometry converter gekregen.", conv, instanceOf(HSQLJdbcConverter.class));
            } else {
                fail("Onbekende (en niet ondersteunde) database");
            }
        } catch (SQLException | UnsupportedOperationException e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void testGetGeotoolsDBTypeName() {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            String actual = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c).getGeotoolsDBTypeName();
            if (isMsSQL) {
                assertEquals("sqlserver", actual, "Onjuiste database smaak gekregen.");
            } else if (isOracle) {
                assertEquals("oracle", actual, "Onjuiste database smaak gekregen.");
            } else if (isPostgis) {
                assertEquals("postgis", actual, "Onjuiste database smaak gekregen.");
            } else if (isHSQLDB) {
                Assertions.assertNull(actual, "Onjuiste database smaak gekregen.");
            } else {
                fail("Onbekende (en niet ondersteunde) database");
            }
        } catch (SQLException | UnsupportedOperationException e) {
            fail(e.getLocalizedMessage());
        }
    }
}
