import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.json.simple.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Array;
import java.util.*;

public class Main {

    public static void main(String args[]) throws Exception
    {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");

        // Кажется, то что нужно)
        //String expression = " if (x==y) Math.cos(0.5); else 1; ";
       // Double result = (Double) scriptEngine.eval(expression);
        //System.out.println(result);
        //А теперь попробуем массив
        //Так писать сложно, или придется делать постобработку
       // expression = "load(\"nashorn:mozilla_compat.js\"); importPackage(java.util); if (x==y) Arrays.asList([Math.cos(0.5),11]); else Arrays.asList([1]); ";
        //System.out.println(scriptEngine.eval(expression));

        //Давайте попробуем вовне
       // expression = "if (x==y) [Math.cos(0.5),11]; else [1]; ";
        //Хуй там
        //System.out.println(scriptEngine.eval(expression).getClass());

        //У mirror есть рефлексивные методы!
      /*  ScriptObjectMirror mirror = (ScriptObjectMirror) scriptEngine.eval(expression);
        List list = new ArrayList(mirror.values());
        System.out.println(list); */

        //А теперь соберем в общем виде

        System.out.println("JS");
        long startTime = System.currentTimeMillis();
        for (Integer i=1; i<=100000; i++) {
            JSONObject msgInJson = new JSONObject();
            msgInJson.put("x1", i);
            msgInJson.put("y1", i.toString());

            JSONObject msgOutJson = null;

            Map<String, String> exprs = new HashMap();

            exprs.put("x2", "(x1+100).toString()+'_'+y1");
            exprs.put("y2", "if (y1.length()<4) 'short_'+y1; else y1");
            String where = "(x1+ parseInt(y1))%7 != 0";

            //Заполняем переменные из входного сообщения
            for (Object key : msgInJson.keySet()) {
                scriptEngine.put((String) key, msgInJson.get(key));
            }

            Boolean whereEvaluated = (Boolean) scriptEngine.eval(where);
            if (whereEvaluated) {

                msgOutJson = new JSONObject();
                for (Map.Entry<String, String> entry : exprs.entrySet()) {

                    String expression = entry.getValue(); //Да, разные ветки if могут возвращать разное. Тут только рантайме
                    Object evaluated = scriptEngine.eval(expression);

                    Object result = null;
                    if (evaluated instanceof Integer)
                        result = (Integer) evaluated;
                    else if (evaluated instanceof Double)
                        result = (Double) evaluated;
                    else if (evaluated instanceof String)
                        result = (String) evaluated;
                    else if (evaluated instanceof Boolean)
                        result = (Boolean) evaluated;
                    else if (evaluated instanceof ScriptObjectMirror) {
                        ScriptObjectMirror mirror = (ScriptObjectMirror) evaluated;
                        result = new ArrayList(mirror.values());
                    }
                    msgOutJson.put(entry.getKey(), result);

                }
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime-startTime);

    }
}
