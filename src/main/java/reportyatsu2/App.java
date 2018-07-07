package reportyatsu2;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class App {
    public static void main(String[] args) {
        boolean debugMode = false;
        String inputFilePath = null;
        String outputFilePath = null;

        // コマンドライン引数の読み取り
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            // 1文字目が「-」で、それに続く文字があるならオプション
            if (arg.length() >= 2 && arg.charAt(0) == '-') {
                if ("-h".equals(arg) || "--help".equals(arg) || "-?".equals(arg)) {
                    // 使い方を表示して終了
                    showUsage(false);
                    return;
                } else if ("-o".equals(arg)) {
                    // 1. すでに出力ファイルパスが指定されているなら不正
                    // 2. 「-o」が最後の引数なら、次の引数がないので不正
                    if (outputFilePath != null || i == args.length - 1) {
                        abortAsArgumentsError();
                        return;
                    }

                    // 次の引数を出力ファイルパスとして認識
                    outputFilePath = args[++i];
                } else if (arg.startsWith("-o=")) {
                    // すでに出力ファイルパスが指定されているなら不正
                    if (outputFilePath != null) {
                        abortAsArgumentsError();
                        return;
                    }

                    // 「-o=」から始まるなら、その後ろが出力パス
                    outputFilePath = arg.substring(3);
                } else if ("--debug".equals(arg)) {
                    // デバッグモード
                    debugMode = true;
                } else {
                    // 受理できないオプション
                    abortAsArgumentsError();
                    return;
                }
            } else if (inputFilePath != null) {
                // すでに入力ファイルは指定されているのでエラー
                abortAsArgumentsError();
                return;
            } else {
                // この引数を入力ファイルパスとして認識
                inputFilePath = arg;
            }
        }

        // 入力ファイルが指定されていないのでエラー
        if (inputFilePath == null) {
            abortAsArgumentsError();
            return;
        }

        boolean fromStdin = "-".equals(inputFilePath);

        // 入力が標準入力だと出力ファイル名を作れないのでエラー
        if (fromStdin && outputFilePath == null) {
            abortAsArgumentsError();
            return;
        }

        // 入力の読み込み
        Document inputDocument;
        try (InputStream inputStream = fromStdin ? System.in : new FileInputStream(inputFilePath)) {
            inputDocument = new InputLoader().loadToDom(inputStream);
        } catch (SAXException e) {
            System.err.println("入力された XML にエラーがありました。");
            System.err.println(e.getMessage());
            abort();
            return;
        } catch (IOException e) {
            System.err.println("入力ファイルの読み込みに失敗しました。");
            e.printStackTrace();
            abort();
            return;
        }

        // 中間表現へ変換
        InputToIrTransformer transformer = new InputToIrTransformer(debugMode);
        try {
            transformer.inputDocument(inputDocument);
        } catch (TransformException e) {
            System.err.println("入力された XML にエラーがありました。");
            System.err.println(e.getMessage());
            abort();
            return;
        }
    }

    private static void showUsage(boolean isError) {
        (isError ? System.err : System.out)
            .println("Usage: reportyatsu2 [--debug] [-o 出力ファイル] 入力ファイル");
    }

    private static void abort() {
        System.exit(1);
    }

    private static void abortAsArgumentsError() {
        showUsage(true);
        abort();
    }
}
