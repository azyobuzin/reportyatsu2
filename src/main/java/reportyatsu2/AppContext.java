package reportyatsu2;

public class AppContext {
    private final boolean _debugMode;

    public AppContext(boolean debugMode) {
        _debugMode = debugMode;
    }

    public boolean isDebugMode() { return _debugMode; }

    public void log(String format, Object... args) {
        // デバッグモードのときだけログを出力
        if (isDebugMode()) System.err.println(String.format(format, args));
    }
}
