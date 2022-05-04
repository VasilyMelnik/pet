import org.json.simple.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Main_h2 {
    private static Connection h2Connection = null;
    private static String select = null;
    private static String whereClause = null;

    private static String substFieldsToSql(String sql, JSONObject value, List<Object> vars) throws SQLException {
        List<String> fieldsList = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        Set<String> fieldsSet = value.keySet();
        @SuppressWarnings("rawtypes")
        Iterator iterator = fieldsSet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            fieldsList.add(key);
        }
        String resultClause = String.valueOf(sql);

        for (String fieldName : fieldsList) {
            resultClause = resultClause.replaceAll("(?i)\\bAS " + fieldName + "\\b", "__##AS##__" + fieldName);
        }
        String[] words = resultClause.split("\\W+");
        for (String fieldName : fieldsList) {
            resultClause = resultClause.replaceAll("(?i)\\b" + fieldName + "\\b", "?");
            resultClause = resultClause.replaceAll("\\b__##AS##__" + fieldName + "\\b", "AS " + fieldName);
        }
        for (String word : words) {
            if (fieldsSet.contains(word)) {
                vars.add(value.get(word));
            }
        }
        return resultClause;
    }

    private static JSONObject selectH2(String selectSubst, String whereClauseSubst, List<Object> vars) throws SQLException, ClassNotFoundException {
        ResultSet rs = null;
        String query = "SELECT " + selectSubst + " WHERE " + whereClauseSubst;

        List<String> types = new ArrayList<>(); // DEBUG

        try {
            PreparedStatement prepSt = h2Connection.prepareStatement(query);
            int varNum = 0;
            for (Object var : vars) {
                varNum++;
                if (var instanceof String || var == null) {
                    prepSt.setString(varNum, (String)var);
                    types.add("String"); // DEBUG
                } else if (var instanceof Long) {
                    prepSt.setLong(varNum, (Long)var);
                    types.add("Long"); // DEBUG
                } else if (var instanceof Integer) {
                    prepSt.setInt(varNum, (Integer)var);
                    types.add("Int"); // DEBUG
                } else if (var instanceof Double) {
                    prepSt.setDouble(varNum, (Double)var);
                    types.add("Double"); // DEBUG
                } else if (var instanceof Boolean) {
                    prepSt.setBoolean(varNum, (Boolean)var);
                    types.add("Boolean"); // DEBUG
                }
            }


            rs = prepSt.executeQuery();
//            rs = this.h2Statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return resultSetToJson(rs);
    }

    private static JSONObject resultSetToJson(ResultSet rs) throws SQLException {
        JSONObject json = new JSONObject();
        ResultSetMetaData rsmd = rs.getMetaData();
        if (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                json.put(column_name, rs.getObject(column_name));
            }
            return json;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {

        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:");

        select = "cast(x1+100 as nvarchar)||'_'||y1 as x2, case when length(y1) <4 then 'short_'||y1 else y1 end as y2";
        whereClause = "(x1+cast(y1 as integer))%7 <>0";

        System.out.println("H2");
        long startTime = System.currentTimeMillis();
        for (Integer i=1; i<=100000; i++) {
            JSONObject msgJson = new JSONObject();
            msgJson.put("x1", i);
            msgJson.put("y1", i.toString());

            List<Object> vars = new ArrayList<>();
            String selectSubst = substFieldsToSql(select, msgJson, vars);
            String whereClauseSubst = substFieldsToSql(whereClause, msgJson, vars);
            JSONObject res = selectH2(selectSubst, whereClauseSubst, vars);
            //System.out.println(res.toJSONString());
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime-startTime);
    }
}

