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

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 * Utility om database properties te laden en methods te loggen.
 *
 * @author mprins
 */
public abstract class AbstractDatabaseIntegrationTest {

  private static final Log LOG = LogFactory.getLog(AbstractDatabaseIntegrationTest.class);

  /**
   * properties uit {@code <DB smaak>.properties} en {@code local.<DB smaak>.properties}.
   *
   * @see #loadProps()
   */
  protected final Properties params = new Properties();

  /** {@code true} als we met een Oracle database bezig zijn. */
  protected boolean isOracle;

  /** {@code true} als we met een MS SQL Server database bezig zijn. */
  protected boolean isMsSQL;

  /** {@code true} als we met een Postgis database bezig zijn. */
  protected boolean isPostgis;

  /** {@code true} als we met een HSQLDB database bezig zijn. */
  protected boolean isHSQLDB;

  /**
   * test of de database properties zijn aangegeven, zo niet dan skippen we alle tests in deze test.
   */
  @BeforeAll
  public static void checkDatabaseIsProvided() {
    // als je vanuit de IDE wilt draaien kun je hier de database property instellen
    // System.setProperty("database.properties.file", "postgis.properties");
    // System.setProperty("database.properties.file", "sqlserver.properties");
    // System.setProperty("database.properties.file", "oracle.properties");
    assumeFalse(
        null == System.getProperty("database.properties.file"),
        "Verwacht database omgeving te zijn aangegeven.");
  }

  /**
   * subklassen dienen zelf een setup te hebben.
   *
   * @throws Exception if any
   */
  @BeforeEach
  public abstract void setUp() throws Exception;

  /**
   * Laadt de database propery file en eventuele overrides.
   *
   * @throws IOException als laden van property file mislukt
   */
  public void loadProps() throws IOException {
    // de `database.properties.file` is in de pom.xml of via commandline ingesteld
    params.load(
        AbstractDatabaseIntegrationTest.class
            .getClassLoader()
            .getResourceAsStream(System.getProperty("database.properties.file")));
    try {
      // probeer een local (override) versie te laden als die bestaat
      params.load(
          AbstractDatabaseIntegrationTest.class
              .getClassLoader()
              .getResourceAsStream("local." + System.getProperty("database.properties.file")));
    } catch (IOException | NullPointerException e) {
      // negeren; het override bestand is normaal niet aanwezig
    }
    isOracle = "oracle".equalsIgnoreCase(params.getProperty("dbtype"));
    isMsSQL = "sqlserver".equalsIgnoreCase(params.getProperty("dbtype"));
    isPostgis = "postgis".equalsIgnoreCase(params.getProperty("dbtype"));
    isHSQLDB = "hsqldb".equalsIgnoreCase(params.getProperty("dbtype"));

    try {
      Class.forName(params.getProperty("staging.jdbc.driverClassName"));
    } catch (ClassNotFoundException ex) {
      LOG.error("Database driver niet gevonden.", ex);
    }
  }

  /** Log de naam van de test als deze begint. */
  @BeforeEach
  public void startTest(TestInfo testInfo) {
    LOG.info("==== Start test methode: " + testInfo.getDisplayName());
  }

  /** Log de naam van de test als deze eindigt. */
  @AfterEach
  public void endTest(TestInfo testInfo) {
    LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
  }

  public ColumnMetadata getColumnMetadata(Connection c, String tableName, String colName)
      throws SQLException {
    ColumnMetadata columnMetadata = null;
    try (ResultSet columnsRs =
        c.getMetaData()
            .getColumns(null, c.getSchema(), getCasedName(tableName), getCasedName(colName))) {
      while (columnsRs.next()) {
        columnMetadata = new ColumnMetadata(columnsRs);
      }
    }
    LOG.debug("metadata voor kolom: " + colName + " is: " + columnMetadata);
    return columnMetadata;
  }

  private String getCasedName(String name) {
    if (this.isHSQLDB || this.isOracle) {
      return name.toUpperCase(Locale.ROOT);
    }
    return name;
  }
}
