package edu.purdue.cybercenter.dm.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author xu222
 */
@Service
public class CrisScriptEngine {

    public static final String KEY_CONTENT = "content";
    public static final String KEY_INIT = "init";
    public static final String KEY_EXPECTED = "expected";
    public static final String KEY_RESULT = "result";

    protected static final ScriptEngine engine;

    static {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("nashorn");

        // load initial script
        Resource scriptResource = new ClassPathResource("/test_script.js");
        try {
            engine.eval(new InputStreamReader(scriptResource.getInputStream()));
        } catch (ScriptException | IOException ex) {
            // problem with script execution
            // file does not exist
        }

    }

    public Bindings createEngineScope(Map<String, Object> init, Map<String, Object> expected, Map<String, Object> result) {
        Map<String, Object> scope = new HashMap<>();
        scope.put(KEY_INIT, init);
        scope.put(KEY_EXPECTED, expected);
        scope.put(KEY_RESULT, result);
        return createEngineScope(scope);
    }

    public Bindings createEngineScope(Map<String, Object> scope) {
        Bindings engineScopeBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        if (scope != null && !scope.isEmpty()) {
            engineScopeBindings.clear();
            engineScopeBindings.putAll(scope);
        }

        return engineScopeBindings;
    }

    public void evaluateScript(String expression) throws ScriptException {
        engine.eval(expression);
    }

    public <T> T evaluateScript(String expression, Class<T> clazz) throws ScriptException {
        T result = (T) engine.eval(expression);
        return result;
    }

    public Boolean evaluateBooleanExpression(String expression) throws ScriptException {
        Boolean result = (Boolean) engine.eval(expression);
        return result;
    }

    public String evaluateStringExpression(String expression) throws ScriptException {
        String result = (String) engine.eval(expression);
        return result;
    }

    public Number evaluateNumberExpression(String expression) throws ScriptException {
        Number result = (Number) engine.eval(expression);
        return result;
    }
}
