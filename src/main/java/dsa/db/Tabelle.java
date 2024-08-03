package dsa.db;

import java.util.ArrayList;

public class Tabelle {
    private String name;

    public Tabelle(String name) {
        this.name = name;
    }

    /**
     * Einfügen von Werten in die Tabelle
     * @param attribute: enthält die Namen der Spalten
     * @param values: enthält den Wert für die jeweilige Spalte
     * @param primaryKeys: beinhaltet indices der Spalten, die Primärschlüssel sind
     */
    public String insertInto(String[] attribute, Object[] values, int[] primaryKeys) {
        String query = "INSERT INTO " + name + " ";
        String spalten = "(";
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                spalten += ", ";
            }
            spalten += "\"" + attribute[i] + "\"";
        }
        spalten += ")";
        query += spalten + " VALUES ";
        String werte = "(";
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                werte += ", ";
            }
            if (values[i] instanceof String) {
                werte += "'";
                werte += StringService.escapeApostrophs((String) values[i]);
                werte += "'";
            } else {
                werte += values[i];
            }
        }
        werte += ")";
        query += werte + "\n";
        if (primaryKeys.length > 0) {
            query += "ON CONFLICT (";
            for (int i = 0; i < primaryKeys.length; i++) {
                int index = primaryKeys[i];
                query += "\"" + attribute[index] + "\"";
                if (i < primaryKeys.length - 1) {
                    query += ", ";
                }
            } query += ") DO UPDATE SET " + spalten + " = " + werte;
        }
        query += ";";
        return query;
    }

    public String selectAll() {
        String query = "SELECT * FROM " + name + ";";
        return query;
    }

    public String selectAllOrdered(String[] orderPriority,
                                   String[] orderTypes) {
        String query = "SELECT * FROM " + name + " ORDER BY ";
        for (int i = 0; i < orderPriority.length; i++) {
            if (i == 0) {
                query += "\"" + orderPriority[i] + "\"";
            } else {
                query += ", " + "\"" + orderPriority[i] + "\"";
            }
            query += " " + orderTypes[i];
        }
        query += ";";
        return query;
    }

    public String selectWhere(String[] wantedAttribute,
                              String[] attribute,
                              String[] coparator,
                              Object[] values) {
        String query = "SELECT ";
        for (int i = 0; i < wantedAttribute.length; i++) {
            if (i > 0) {
                query += ", ";
            }
            if (wantedAttribute[i].equals("*")) {
                query += wantedAttribute[i];
            } else {
                query += "\"" + wantedAttribute[i] + "\"";
            }
        }
        query += " FROM " + name + " WHERE ";

        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                query += " AND ";
            }
            query += "\"" + attribute[i] + "\" " + coparator[i] + " ";
            if (values[i] instanceof String) {
                query += "'";
                query += StringService.escapeApostrophs((String) values[i]);
                query += "'";
            } else {
                query += values[i];
            }
        }
        query += ";";
        return query;
    }

    public String selectWhereOrderBy(String[] wantedAttribute,
                                     String[] attribute,
                                     String[] coparator,
                                     Object[] values,
                                     String[] orderBy) {
        String query = "SELECT ";
        for (int i = 0; i < wantedAttribute.length; i++) {
            if (i > 0) {
                query += ", ";
            }
            if (wantedAttribute[i].equals("*")) {
                query += wantedAttribute[i];
            } else {
                query += "\"" + wantedAttribute[i] + "\"";
            }
        }
        query += " FROM " + name + " WHERE ";

        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                query += " AND ";
            }
            query += "\"" + attribute[i] + "\" " + coparator[i] + " ";
            if (values[i] instanceof String) {
                query += "'";
                query += StringService.escapeApostrophs((String) values[i]);
                query += "'";
            } else {
                query += values[i];
            }
        }
        query += " ORDER BY ";
        for (int i = 0; i < orderBy.length; i++) {
            if (i > 0) {
                query += ", ";
            }
            query += "\"" + orderBy[i] + "\"";
        }
        query += ";";
        return query;
    }

    public String selectAllWhere(String[] attribute,
                                 String[] coparator,
                                 Object[] values) {
        String query = "SELECT * FROM " + name + " WHERE ";
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                query += " AND ";
            }
            query += "\"" + attribute[i] + "\" " + coparator[i] + " ";
            if (values[i] instanceof String) {
                query += "'";
                query += StringService.escapeApostrophs((String) values[i]);
                query += "'";
            } else {
                query += values[i];
            }
        }
        query += ";";
        return query;
    }

    public String selectWhereLike(String[] wantedAttribute,
                                  String[] attribute,
                                  String[] values) {
        String query = "SELECT ";
        for (int i = 0; i < wantedAttribute.length; i++) {
            if (i > 0) {
                query += ", ";
            }
            if (wantedAttribute[i].equals("*")) {
                query += wantedAttribute[i];
            } else {
                query += "\"" + wantedAttribute[i] + "\"";
            }
        }
        query += " FROM " + name + " WHERE ";

        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                query += " AND ";
            }
            query += "\"" + attribute[i] + "\" LIKE ";
            query += "'";
            query += values[i];
            query += "'";
        }
        query += ";";
        return query;
    }

    public String clearTable() {
        String query = "DELETE  FROM " + name + ";";
        return query;
    }

    public String dropTable() {
        String query = "DROP TABLE " + name + ";";
        return query;
    }

    public String deleteWhere(String[] attribute,
                              String[] coparator,
                              Object[] values) {
        String query = "DELETE  FROM " + name + " WHERE ";

        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                query += " AND ";
            }
            query += "\"" + attribute[i] + "\" " + coparator[i] + " ";
            if (values[i] instanceof String) {
                query += "'";
                query += StringService.escapeApostrophs((String) values[i]);
                query += "'";
            } else {
                query += values[i];
            }
        }
        query += ";";
        return query;
    }

    public String modificateWhere(String[] columnNames,
                                  String[] newValues,
                                  String[] attribute,
                                  String[] comparator,
                                  Object[] values) {
        String query = "UPDATE " + name + "\n" +
                "    SET ";

        for (int i = 0; i < columnNames.length; i++) {
            query += "\n        " + columnNames[i] + " = " + newValues[i];
            if (i > 0 && i < columnNames.length - 1) {
                query += ",";
            }
        }

        query += "    WHERE";
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                query += " AND ";
            }
            query += "\n        \"" + attribute[i] + "\" " + comparator[i] + " ";
            if (values[i] instanceof String) {
                query += "'";
                query += StringService.escapeApostrophs((String) values[i]);
                query += "'";
            } else {
                query += values[i];
            }
        }
        query += ";";
        return query;
    }

    public String selectDistinct(String[] selectedColumns) {
        String query = "SELECT DISTINCT " + encloseIn(selectedColumns[0], "\"");
        for (int i = 1; i < selectedColumns.length; i++) {
            query += ", " + encloseIn(selectedColumns[i],"\"");
        }
        query += " FROM " + name + " ORDER BY " + encloseIn(selectedColumns[0], "\"") + ";";
        return query;
    }

    public String selectDistinctOrderBy(String[] selectedColumns, String[] orderBy) {
        String query = "SELECT DISTINCT " + encloseIn(selectedColumns[0], "\"");
        for (int i = 1; i < selectedColumns.length; i++) {
            query += ", " + encloseIn(selectedColumns[i],"\"");
        }
        query += " FROM " + name;
        query += " ORDER BY ";
        for (int i = 0; i < orderBy.length; i++) {
            if (i > 0) {
                query += ", ";
            }
            query += "\"" + orderBy[i] + "\"";
        }
        return query;
    }

    public String selectDistinctWhere(String[] selectedColumns,
                                      String[] attribute,
                                      String[] coparator,
                                      Object[] values) {
        String query = "SELECT DISTINCT " + encloseIn(selectedColumns[0], "\"");
        for (int i = 1; i < selectedColumns.length; i++) {
            query += ", " + encloseIn(selectedColumns[i],"\"");
        }
        query += " FROM " + name + " WHERE ";
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                query += " AND ";
            }
            query += encloseIn(attribute[i], "\"") + coparator[i] + " ";
            if (values[i] instanceof String) {
                query += "'";
                query += StringService.escapeApostrophs((String) values[i]);
                query += "'";
            } else {
                query += values[i];
            }
        }
        query += ";";
        return query;
    }

    public String exists(String[] attribute,
                         String[] comparator,
                         Object[] values) {
        String query = "SELECT EXISTS(SELECT 1 FROM " + name + " WHERE ";
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                query += " AND ";
            }
            query += encloseIn(attribute[i], "\"") + comparator[i] + " ";
            if (values[i] instanceof String) {
                query += "'";
                query += StringService.escapeApostrophs((String) values[i]);
                query += "'";
            } else {
                query += values[i];
            }
        }
        query += ");";
        return query;
    }

    private String encloseIn(String input, String enclose) {
        return enclose + input + enclose;
    }


    public String selectRegex(String[] attribute,
                              String[] values) {
        // encloseIn(selectedColumns[0], "\"")
        String query = "SELECT * FROM " + name + " WHERE ";
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                query += " AND ";
            }
            query += attribute[i] + " ~ " + encloseIn(values[i], "'");
        }
        query += ";";
        return query;
    }

    /**
     * create table katalog.table_name
     * (
     *     column_name  integer,
     *     column_name2 integer
     * );
     */
    public String createTableOfVarchars(ArrayList<String> list) {
        String query = "CREATE TABLE " + name + " (\n";
        query += "  " + list.get(0) + " varchar";
        for (int i = 1; i < list.size(); i++) {
            query += ",\n  " + list.get(i) + " varchar";
        }
        query += "\n);";
        return query;
    }
}
