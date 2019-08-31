import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JDK6CompileTest {
  public static void main(String args[]) {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("javascript");
    engine.put("counter", 0);
    if (engine instanceof Compilable) {
      Compilable compEngine = (Compilable) engine;
      try {
        CompiledScript script = compEngine.compile("function count(){counter=counter+1;return counter;}; count();");
        System.out.println(script.eval());
        System.out.println(script.eval());
        System.out.println(script.eval());
      } catch (ScriptException e) {
        System.err.println(e);
      }
    } else {
      System.err.println("Engine can't compile code");
    }
  }
}