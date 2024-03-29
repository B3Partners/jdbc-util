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

import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @author Matthijs Laan
 */
public class ColumnMetadata implements Comparable<ColumnMetadata> {
  private final String defaultValue;
  private String name;
  private int position;
  private int dataType;
  private String typeName;
  private int size;
  private Integer decimalDigits;
  private boolean nullable;
  private Integer charOctetLength;
  private boolean autoIncrement;

  public ColumnMetadata(ResultSet columnMetadataRs) throws SQLException {
    // the resultset mmay be forward-only, therefor the order could be important (oracle 23.2
    // driver)
    // see
    // https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/DatabaseMetaData.html#getColumns(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
    // 4
    name = columnMetadataRs.getString("COLUMN_NAME");
    // 5
    dataType = columnMetadataRs.getInt("DATA_TYPE");
    // 6
    typeName = columnMetadataRs.getString("TYPE_NAME");
    // 7
    size = columnMetadataRs.getInt("COLUMN_SIZE");
    // 9
    Object o = columnMetadataRs.getObject("DECIMAL_DIGITS");
    if (o != null) {
      if (o instanceof BigDecimal) {
        decimalDigits = ((BigDecimal) o).intValue();
      } else if (o instanceof Integer) {
        decimalDigits = (Integer) o;
      }
    }
    // 11
    nullable = DatabaseMetaData.columnNullable == columnMetadataRs.getInt("NULLABLE");
    // 13
    defaultValue = columnMetadataRs.getString("COLUMN_DEF");
    // 16
    o = columnMetadataRs.getObject("CHAR_OCTET_LENGTH");
    if (o != null) {
      if (o instanceof BigDecimal) {
        charOctetLength = ((BigDecimal) o).intValue();
      } else if (o instanceof Integer) {
        charOctetLength = (Integer) o;
      }
    }
    // 17
    position = columnMetadataRs.getInt("ORDINAL_POSITION");
    // 23
    // autoIncrement = "YES".equals(columnMetadataRs.getString("IS_AUTOINCREMENT"));
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getDataType() {
    return dataType;
  }

  public void setDataType(int dataType) {
    this.dataType = dataType;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public Integer getDecimalDigits() {
    return decimalDigits;
  }

  public void setDecimalDigits(Integer decimalDigits) {
    this.decimalDigits = decimalDigits;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public Integer getCharOctetLength() {
    return charOctetLength;
  }

  public void setCharOctetLength(Integer charOctetLength) {
    this.charOctetLength = charOctetLength;
  }

  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  public void setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
  }

  @Override
  public String toString() {
    return toString("%s %4d %s %s bytes=%d %s %s");
  }

  public String typeToString() {
    return typeName + "(" + size + (decimalDigits == null ? "" : "," + decimalDigits) + ")";
  }

  public String toStringFixedWidth(Collection<ColumnMetadata> allColumns) {
    int nameWidth = name.length(), typeWidth = typeToString().length();
    for (ColumnMetadata c : allColumns) {
      nameWidth = Math.max(c.getName().length(), nameWidth);
      typeWidth = Math.max(c.typeToString().length(), typeWidth);
    }
    return toString("%-" + nameWidth + "s %4d %-" + typeWidth + "s %8s bytes=%d %s %s");
  }

  private String toString(String formatString) {

    return String.format(
        formatString,
        name,
        dataType,
        typeToString(),
        nullable ? "NULL" : "NOT NULL",
        charOctetLength,
        autoIncrement ? "AUTO INCREMENT" : "",
        defaultValue == null ? "" : "DEFAULT '" + defaultValue + "'");
  }

  @Override
  public int compareTo(ColumnMetadata rhs) {
    return Integer.compare(position, rhs.position);
  }
}
