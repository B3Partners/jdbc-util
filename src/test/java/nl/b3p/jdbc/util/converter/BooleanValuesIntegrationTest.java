/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */
package nl.b3p.jdbc.util.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class BooleanValuesIntegrationTest extends AbstractDatabaseIntegrationTest {
    private static final Log LOG = LogFactory.getLog(BooleanValuesIntegrationTest.class);
    private static final String INSERT_STATEMENT = "INSERT INTO booleantable (ishetwaar) VALUES (?)";
    private static final String CHECK_STATEMENT = "SELECT * FROM booleantable";
    private static final String TABLE_NAME = "booleantable";
    private static final String COLUMN_NAME = "ishetwaar";

    static Stream<Arguments> booleanProvider() {
        return Stream.of(arguments("true"), arguments("false"));
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"));
             PreparedStatement ps = c.prepareStatement("DELETE FROM booleantable")
        ) {
            c.setAutoCommit(true);
            ps.execute();
        }
    }

    @DisplayName("Test conversie van tekstuele boolean naar database type")
    @ParameterizedTest(name = "{index}: Test conversie van waarde: {0}")
    @MethodSource("booleanProvider")
    void testConvertBooleanToSQLObjectString(String stringValue) {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))
        ) {

            Object o = GeometryJdbcConverter.convertToSQLObject(stringValue, getColumnMetadata(c, TABLE_NAME, COLUMN_NAME), TABLE_NAME, COLUMN_NAME);
            if (this.isOracle) {
                assertTrue(o instanceof java.lang.Number);
                assertEquals(Objects.equals(stringValue, "true") ? BigDecimal.ONE : BigDecimal.ZERO, o);
            } else {
                assertTrue(o instanceof java.lang.Boolean);
                assertEquals(Boolean.parseBoolean(stringValue), o);
            }
        } catch (SQLException e) {
            LOG.error("Fout tijdens uitlezen kolom informatie", e);
            fail(e.getLocalizedMessage());
        }
    }

    @DisplayName("Test insert van tekstuele boolean")
    @ParameterizedTest(name = "{index}: Test insert van waarde: {0}")
    @MethodSource("booleanProvider")
    void insertBooleanValue(String stringValue) {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))
        ) {

            c.setAutoCommit(true);
            Object o = GeometryJdbcConverter.convertToSQLObject(stringValue, getColumnMetadata(c, TABLE_NAME, COLUMN_NAME), TABLE_NAME, COLUMN_NAME);

            PreparedStatement ps = c.prepareStatement(INSERT_STATEMENT);
            // gebruik setObject of de auto-conversie goed gaat
            ps.setObject(1, o);
            assertEquals(1, ps.executeUpdate(), "Er is geen rij toegevoegd");
            ps = c.prepareStatement(CHECK_STATEMENT, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "Er is geen rij gevonden");
            assertEquals(Boolean.parseBoolean(stringValue), rs.getBoolean(2), "Waarde komt niet overeen");
            rs.close();
        } catch (SQLException sqle) {
            fail("Insert or check failed, msg: " + sqle.getLocalizedMessage());
        }
    }
}
