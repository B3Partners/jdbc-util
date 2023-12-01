/*
 * Copyright (C) 2021 B3Partners B.V.
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
module nl.b3p.jdbc.util {
  requires java.sql;
  requires org.apache.commons.logging;
  requires org.apache.commons.dbutils;
  requires org.apache.commons.io;
  requires static org.apache.tomcat.dbcp;
  requires org.apache.commons.lang3;
  requires org.locationtech.jts;
  requires static com.oracle.database.jdbc;
  requires static org.hsqldb;
  requires static org.postgresql.jdbc;
  requires static net.postgis.jdbc;
  requires static net.postgis.jdbc.geometry;
  requires static com.microsoft.sqlserver.jdbc;
  requires org.geotools.jdbc.jdbc_oracle;

  exports nl.b3p.jdbc.util.converter;
  exports nl.b3p.jdbc.util.dbutils;

  // for integration tests
  opens nl.b3p.jdbc.util.converter;
// opens nl.b3p.jdbc.util.dbutils;
}
