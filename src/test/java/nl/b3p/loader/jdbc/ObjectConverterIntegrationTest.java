package nl.b3p.loader.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;
import nl.b3p.AbstractDatabaseIntegrationTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ObjectConverterIntegrationTest extends AbstractDatabaseIntegrationTest {
    private static final Log LOG = LogFactory.getLog(ObjectConverterIntegrationTest.class);

    static Stream<Arguments> dateProvider() {
        return Stream.of(
                // tabel, kolom, expected epoch millis localtime, input
                arguments("bericht", "datum", /*GMT+1: 1607063255000L*/ /*GMT*/ 1607066855000L, "2020-12-04T07:27:35"),
                arguments("job", "datum", /*GMT+1: 1607018340000L*/ /*GMT*/ 1607021940000L, "2020-12-03T18:59:00")
        );
    }

    static Stream<Arguments> stringProvider() {
        return Stream.of(
                // tabel, kolom, input
                arguments("bericht", "soort", "input"),
                // CLOB kolom in Oracle, niet ondersteund
                //arguments("job", "br_xml",
                //        "And that's when it becomes fun - you don't have to spend your time thinking about what's " +
                //                "happening - you just let it happen. Let's give him a friend too. Everybody needs a " +
                //                "friend. You gotta think like a tree. That's what makes life fun. That you can make " +
                //                "these decisions. That you can create the world that you want. Fluff that up. " +
                //                "Everything is happy if you choose to make it that way.")
                arguments("bericht", "object_ref",
                        "And that's when it becomes fun - you don't have to spend your time thinking about what's " +
                                "happening - you just let it happen. Let's give him a friend too. Everybody needs a " +
                                "friend. You gotta think like a tree. That's what makes life fun. That you can make " +
                                "these decisions. That you can create the world that you want. Fluff that up. " +
                                "Everything is happy if you choose to make it that way.")
        );
    }


    @BeforeEach
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    @DisplayName("Test conversie van tekstuele datum naar database type")
    @ParameterizedTest(name = "{index}: tabel: {0}, kolom: {1}")
    @MethodSource("dateProvider")
    public void testConvertToSQLObjectDatum(String tableName, String colName, long expected, String input) {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            Object o = GeometryJdbcConverter.convertToSQLObject(
                    input,
                    getColumnMetadata(c, tableName, colName),
                    tableName,
                    colName
            );
            assertTrue(o instanceof java.sql.Date);
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            cal.setTimeInMillis(expected);
            cal.setTimeZone(TimeZone.getDefault());

            assertEquals(
                    cal.getTime().getTime()-cal.getTimeZone().getOffset(Instant.now().toEpochMilli()),
                    ((Date) o).getTime(),
                    "datum is ongelijk, kan een tijdzone verschil zijn"
            );

        } catch (SQLException e) {
            LOG.error("Fout tijdens uitlezen kolom informatie", e);
            fail(e.getLocalizedMessage());
        }
    }

    @ParameterizedTest(name = "{index}: tabel: {0}, kolom: {1}")
    @MethodSource("stringProvider")
    public void testConvertToSQLObjectString(String tableName, String colName, String input) {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            Object o = GeometryJdbcConverter.convertToSQLObject(
                    input,
                    getColumnMetadata(c, tableName, colName),
                    tableName,
                    colName
            );
            assertTrue(o instanceof java.lang.String);
            assertEquals(input, o);

        } catch (SQLException e) {
            LOG.error("Fout tijdens uitlezen kolom informatie", e);
            fail(e.getLocalizedMessage());
        }
    }

    private String getCasedName(String name) {
        if (this.isHSQLDB) {
            return name.toUpperCase(Locale.ROOT);
        }
        if (this.isOracle) {
            return name.toUpperCase(Locale.ROOT);
        }
        return name;
    }

    private ColumnMetadata getColumnMetadata(Connection c, String tableName, String colName) throws SQLException {
        ColumnMetadata columnMetadata = null;
        try (ResultSet columnsRs = c.getMetaData().getColumns(null, c.getSchema(), getCasedName(tableName),
                getCasedName(colName))) {
            while (columnsRs.next()) {
                columnMetadata = new ColumnMetadata(columnsRs);
            }
        }
        LOG.debug("metadata voor: " + colName + " is: " + columnMetadata);
        return columnMetadata;
    }

}
