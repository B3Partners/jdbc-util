/*
 * Copyright (C) 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.loader.jdbc;

import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author Matthijs Laan
 */
public class ColumnMetadata implements Comparable {
    private String name;
    private int position;
    private int dataType;
    private String typeName;
    private int size;
    private Integer decimalDigits;
    private boolean nullable;
    private Integer charOctetLength;
    private String defaultValue;
    private boolean autoIncrement;
    
    public ColumnMetadata(ResultSet columnMetadataRs) throws SQLException {
        name = columnMetadataRs.getString("COLUMN_NAME");
        position = columnMetadataRs.getInt("ORDINAL_POSITION");
        dataType = columnMetadataRs.getInt("DATA_TYPE");
        typeName = columnMetadataRs.getString("TYPE_NAME");
        size = columnMetadataRs.getInt("COLUMN_SIZE");
        Object o = columnMetadataRs.getObject("DECIMAL_DIGITS");
        if(o != null) {
            if(o instanceof BigDecimal) {
                decimalDigits = ((BigDecimal)o).intValue();
            } else if(o instanceof Integer) {
                decimalDigits = (Integer)o;
            }
        }
        nullable = DatabaseMetaData.columnNullable == columnMetadataRs.getInt("NULLABLE");
        o = columnMetadataRs.getObject("CHAR_OCTET_LENGTH");
        if(o != null) {
            if(o instanceof BigDecimal) {
                charOctetLength = ((BigDecimal)o).intValue();
            } else if(o instanceof Integer) {
                charOctetLength = (Integer)o;
            }
        }
        defaultValue = columnMetadataRs.getString("COLUMN_DEF");
        //autoIncrement = "YES".equals(columnMetadataRs.getString("IS_AUTOINCREMENT"));
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
        return typeName + "(" + size + 
                (decimalDigits == null ? "" : "," + decimalDigits) + ")";
    }
    
    public String toStringFixedWidth(Collection<ColumnMetadata> allColumns) {
        int nameWidth = name.length(), typeWidth = typeToString().length();
        for(ColumnMetadata c: allColumns) {
            nameWidth = Math.max(c.getName().length(), nameWidth);
            typeWidth = Math.max(c.typeToString().length(), typeWidth);
        }
        return toString("%-" + nameWidth + "s %4d %-" + typeWidth + "s %8s bytes=%d %s %s");
    }
    
    private String toString(String formatString) {
        
        return String.format(formatString,
                name,
                dataType,
                typeToString(),
                nullable ? "NULL" : "NOT NULL",
                charOctetLength,
                autoIncrement ? "AUTO INCREMENT" : "",
                defaultValue == null ? "" : "DEFAULT '" + defaultValue + "'");
    }

    public int compareTo(Object rhs) {
        return new Integer(position).compareTo(((ColumnMetadata)rhs).position);
    }
}
