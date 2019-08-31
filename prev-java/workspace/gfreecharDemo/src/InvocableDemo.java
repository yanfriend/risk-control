import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class InvocableDemo {

  public static void main(String[] args) throws Exception {
    ScriptEngineManager manager = new ScriptEngineManager();

    ScriptEngine engine = manager.getEngineByName("js");

    engine.eval("function add (a, b) {c = a + b; return c; }");
    Invocable jsInvoke = (Invocable) engine;

    Object result1 = jsInvoke.invokeFunction("add", new Object[] { 10, 5 });
    System.out.println(result1);

    Adder adder = jsInvoke.getInterface(Adder.class);
    int result2 = adder.add(10, 5);
    System.out.println(result2);
  }
}

interface Adder {
  int add(int a, int b);
}

// engine.eval(new FileReader(jsfile));      
//  http://www.java2s.com/Code/Java/JDK-6/RunJavascriptfunctionwithScriptEngine.htm