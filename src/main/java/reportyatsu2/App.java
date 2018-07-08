package reportyatsu2;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;

public class App {
    public static void main(String[] args) {
        boolean debugMode = false;
        String inputFilePathString = null;
        String outputFilePathString = null;

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
                    if (outputFilePathString != null || i == args.length - 1) {
                        abortAsArgumentsError();
                        return;
                    }

                    // 次の引数を出力ファイルパスとして認識
                    outputFilePathString = args[++i];
                } else if (arg.startsWith("-o=")) {
                    // すでに出力ファイルパスが指定されているなら不正
                    if (outputFilePathString != null) {
                        abortAsArgumentsError();
                        return;
                    }

                    // 「-o=」から始まるなら、その後ろが出力パス
                    outputFilePathString = arg.substring(3);
                } else if ("--debug".equals(arg)) {
                    // デバッグモード
                    debugMode = true;
                } else {
                    // 受理できないオプション
                    abortAsArgumentsError();
                    return;
                }
            } else if (inputFilePathString != null) {
                // すでに入力ファイルは指定されているのでエラー
                abortAsArgumentsError();
                return;
            } else {
                // この引数を入力ファイルパスとして認識
                inputFilePathString = arg;
            }
        }

        // 入力ファイルが指定されていないのでエラー
        if (inputFilePathString == null) {
            abortAsArgumentsError();
            return;
        }

        Path inputFilePath;
        Path outputFilePath;
        try {
            FileSystem fs = FileSystems.getDefault();
            inputFilePath = fs.getPath(inputFilePathString);

            // 出力ファイルパスが指定されていなければ、入力の拡張子を .odt に変えたものにする
            outputFilePath = outputFilePathString != null
                ? fs.getPath(outputFilePathString)
                : changeExtension(inputFilePath, ".odt");
        } catch (InvalidPathException e) {
            System.err.println("指定されたパスにエラーがありました。");
            System.err.println(e.getMessage());
            abort();
            return;
        }

        // 入力の読み込み
        Document inputDocument;
        try (InputStream inputStream = Files.newInputStream(inputFilePath)) {
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
        InputToIrTransformer irTransformer = new InputToIrTransformer(debugMode, inputFilePath.getParent());
        try {
            irTransformer.inputDocument(inputDocument);
        } catch (InputToIrTransformException e) {
            System.err.println("入力された XML にエラーがありました。");
            System.err.println(e.getMessage());
            abort();
            return;
        }

        // ODF として出力
        IrToOdfPackageTransformer odfTransformer = new IrToOdfPackageTransformer(debugMode);
        odfTransformer.inputIr(irTransformer);
        try (OutputStream outputStream = Files.newOutputStream(outputFilePath)) {
            odfTransformer.writeToStream(outputStream);
        } catch (Exception e) {
            System.err.println("出力ファイルの作成に失敗しました。");
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

    private static Path changeExtension(Path source, String newExtension) {
        String name = source.getFileName().toString();
        // 最後の「.」より後ろの newExtension で置き換える
        // 「.」がなければ付け加える
        int dotIndex = name.lastIndexOf('.');
        String newName = dotIndex < 0
            ? name + newExtension
            : name.substring(0, dotIndex) + newExtension;
        // ファイル名部を新しい名前に置き換え
        return source.resolveSibling(newName);
    }
}
