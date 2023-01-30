package org.openhab.automation.fuzzylogicscripting.internal.script;

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.openhab.automation.fuzzylogicscripting.internal.compiler.CompilerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuzzyLogicScriptEngine implements ScriptEngine {
    private final Logger logger = LoggerFactory.getLogger(FuzzyLogicScriptEngine.class);

    private final CompilerService compilerService;

    private JFuzzyLogicScriptEngineFactory scriptEngineFactory;

    public FuzzyLogicScriptEngine(CompilerService compilerService, JFuzzyLogicScriptEngineFactory scriptEngineFactory) {
        this.compilerService = compilerService;
        this.scriptEngineFactory = scriptEngineFactory;
    }

    @Override
    public Bindings createBindings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object eval(String arg0) throws ScriptException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object eval(Reader arg0) throws ScriptException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object eval(String arg0, ScriptContext arg1) throws ScriptException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object eval(Reader arg0, ScriptContext arg1) throws ScriptException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object eval(String arg0, Bindings arg1) throws ScriptException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object eval(Reader arg0, Bindings arg1) throws ScriptException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object get(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bindings getBindings(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScriptContext getContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScriptEngineFactory getFactory() {
        // TODO Auto-generated method stub
        return scriptEngineFactory;
    }

    @Override
    public void put(String arg0, Object arg1) {
        // TODO Auto-generated method stub
        logger.info(arg0);
    }

    @Override
    public void setBindings(Bindings arg0, int arg1) {
        // TODO Auto-generated method stub
        logger.info("Ici");

    }

    @Override
    public void setContext(ScriptContext arg0) {
        // TODO Auto-generated method stub
        logger.info("Là");

    }
}
