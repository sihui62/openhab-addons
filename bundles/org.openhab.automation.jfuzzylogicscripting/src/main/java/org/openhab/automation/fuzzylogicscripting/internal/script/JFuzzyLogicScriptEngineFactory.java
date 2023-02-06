package org.openhab.automation.fuzzylogicscripting.internal.script;

import static org.openhab.automation.fuzzylogicscripting.internal.FuzzyLogicConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.automation.fuzzylogicscripting.internal.compiler.FclCompilerService;

public class JFuzzyLogicScriptEngineFactory implements ScriptEngineFactory {
    private static final String LANGUAGE_NAME = "Fuzzy Control Language";
    private static final String SHORT_NAME = "FCL";
    private final FclCompilerService compilerService;

    public JFuzzyLogicScriptEngineFactory(@NonNull FclCompilerService compilerService) {
        this.compilerService = compilerService;
    }

    @Override
    public String getEngineName() {
        return SOFTWARE_NAME + " Scripting Engine";
    }

    @Override
    public String getEngineVersion() {
        return VERSION_MAJOR;
    }

    @Override
    public List<String> getExtensions() {
        return EXTENSIONS;
    }

    @Override
    public String getLanguageName() {
        return LANGUAGE_NAME;
    }

    @Override
    public String getLanguageVersion() {
        return VERSION_MAJOR;
    }

    @Override
    public String getMethodCallSyntax(String arg0, String arg1, String... arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getMimeTypes() {
        return MIME_TYPES;
    }

    @Override
    public List<String> getNames() {
        return NAMES;
    }

    @Override
    public String getOutputStatement(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProgram(String... arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new FuzzyLogicScriptEngine(compilerService, this);
    }

    private static final List<String> NAMES;
    private static final List<String> EXTENSIONS;
    private static final List<String> MIME_TYPES;

    static {
        List<String> n = new ArrayList<String>(2);
        n.add(SHORT_NAME);
        n.add(LANGUAGE_NAME);
        NAMES = Collections.unmodifiableList(n);

        n = new ArrayList<String>(1);
        n.add("fcl");
        EXTENSIONS = Collections.unmodifiableList(n);

        n = new ArrayList<String>(1);
        n.add("application/x-fcl");
        MIME_TYPES = Collections.unmodifiableList(n);
    }
}
