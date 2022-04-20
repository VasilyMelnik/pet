import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Row row = Row.withNames();

        scriptEngine.put("x","123");
        scriptEngine.put("y","123");
        String expression = "if (x==y) [0.5,1]; else 1; "; //Да, разные ветки if могут возвращать разное. Тут только рантайме
        Object evaluated = scriptEngine.eval(expression);


        Object result = null;
        if (evaluated instanceof Integer)
            result = (Integer) evaluated;
        else if (evaluated instanceof Double)
            result = (Double) evaluated;
        else if (evaluated instanceof String)
            result = (String) evaluated;
        else if (evaluated instanceof ScriptObjectMirror)
        {
            ScriptObjectMirror mirror = (ScriptObjectMirror) evaluated;
            result = new ArrayList(mirror.values());
        }
        row.setField("val1",result);
        System.out.println(row.getField("val1").getClass());
        System.out.println(row.getFieldNames(true));
        System.out.println(row);


    }
}
