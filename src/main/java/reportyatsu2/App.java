package reportyatsu2;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class App {
    public static void main(String[] args) throws IOException {
        boolean debugMode = false;
        String inputFilePath = null;
        String outputFilePath = null;

        // コマンドライン引数の読み取り
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            // 1文字目が「-」で、それに続く文字があるならオプション
            if (arg.length() >= 2 && arg.charAt(0) == '-') {
                if (arg.equals("-h") || arg.equals("--help")) {
                    // 使い方を表示して終了
                    showUsage();
                    return;
                } else if (arg.equals("-o")) {
                    // 1. すでに出力ファイルパスが指定されているなら不正
                    // 2. 「-o」が最後の引数なら、次の引数がないので不正
                    if (outputFilePath != null || i == args.length - 1)
                        exitAsArgumentsError();

                    // 次の引数を出力ファイルパスとして認識
                    outputFilePath = args[++i];
                } else if (arg.startsWith("-o=")) {
                    // すでに出力ファイルパスが指定されているなら不正
                    if (outputFilePath != null) exitAsArgumentsError();

                    // 「-o=」から始まるなら、その後ろが出力パス
                    outputFilePath = arg.substring(3);
                } else if (arg.equals("--debug")) {
                    // デバッグモード
                    debugMode = true;
                } else {
                    // 受理できないオプション
                    exitAsArgumentsError();
                }
            } else if (inputFilePath != null) {
                // すでに入力ファイルは指定されているのでエラー
                exitAsArgumentsError();
            } else {
                // この引数を入力ファイルパスとして認識
                inputFilePath = arg;
            }
        }

        // 入力ファイルが指定されていないのでエラー
        if (inputFilePath == null)
            exitAsArgumentsError();

        boolean fromStdin = inputFilePath.equals("-");

        // 入力が標準入力だと出力ファイル名を作れないのでエラー
        if (fromStdin && outputFilePath == null)
            exitAsArgumentsError();

        // 入力の読み込み
        Document inputDocument;
        try (InputStream inputStream = fromStdin ? System.in : new FileInputStream(inputFilePath)) {
            inputDocument = new InputLoader().loadToDom(inputStream);
        } catch (SAXException e) {
            // 入力された XML にエラーがあった
            System.err.println("入力された XML にエラーがありました。");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void showUsage() {
        System.out.println("Usage: reportyatsu2 [--debug] [-o 出力ファイル] 入力ファイル");
    }

    private static void exitAsArgumentsError() {
        showUsage();
        System.exit(1);
    }
}