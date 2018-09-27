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
import static org.junit.Assert.assertThat;
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
}
