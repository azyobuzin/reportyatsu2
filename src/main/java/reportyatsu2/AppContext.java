package reportyatsu2;

public class AppContext {
    private final boolean debugMode;

    public AppContext(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isDebugMode() { return debugMode; }

    public void log(String format, Object... args) {
        // デバッグモードのときだけログを出力
        if (isDebugMode()) System.err.println(String.format(format, args));
    }
}
