/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.loader.jdbc;

import nl.b3p.AbstractDatabaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * testcases voor {@link GeometryJdbcConverterFactory}. Gebruik
 * {@code mvn -Dit.test=GeometryJdbcConverterFactoryIntegrationTest verify -Poracle -Dtest.onlyITs=true}
 * om deze test te draaien tegen oracle of
 * {@code mvn -Dit.test=GeometryJdbcConverterFactoryIntegrationTest verify -Ppostgresql -Dtest.onlyITs=true}
 * tegen postgresql.
 */
public class GeometryJdbcConverterFactoryIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Before
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
                assertEquals("Onjuiste database smaak gekregen.", "jtds-sqlserver", actual);
            } else if (isOracle) {
                assertEquals("Onjuiste database smaak gekregen.", "oracle", actual);
            } else if (isPostgis) {
                assertEquals("Onjuiste database smaak gekregen.", "postgis", actual);
            } else if (isHSQLDB) {
                assertNull("Onjuiste database smaak gekregen.", actual);
            } else {
                fail("Onbekende (en niet ondersteunde) database");
            }
        } catch (SQLException | UnsupportedOperationException e) {
            fail(e.getLocalizedMessage());
        }
    }
}
