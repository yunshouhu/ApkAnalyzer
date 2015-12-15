package sk.styk.martin.bakalarka;

import sk.styk.martin.bakalarka.files.ApkFile;
import sk.styk.martin.bakalarka.files.FileFinder;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Martin Styk on 23.11.2015.
 */
public class Main {

    private static String APK_TEST = "D:\\Projects\\ApkToolTest";
    private static String APK_TORRENT = "D:\\APK\\APK_torrent";
    private static String APK_PLAY = "D:\\APK\\APK_playdrone";
    private static String APK_ZIPPY = "D:\\APK\\APK_zippyshare";
    private static String APK_ZIPPY2 = "D:\\APK\\APK_zippyshare2";
    private static String METADATA_DIR = "D:\\APK\\metadata";

    private static String APK_DIR = "D:\\APK";

    public static void main(String[] args) throws Exception {

        FileFinder ff = new FileFinder(new File(APK_DIR));
        List<ApkFile> apks = ff.getApkFilesInDirectories();
        List<Exception> decompileExceptions = new ArrayList<Exception>();
        List<Exception> compileExceptions = new ArrayList<Exception>();

        PrintWriter decompileWriter = new PrintWriter("decompilationErrors.txt", "UTF-8");
        PrintWriter compileWriter = new PrintWriter("compilationErrors.txt", "UTF-8");

        List<ApkFile> filesToDelete = new LinkedList<ApkFile>();

        for(ApkFile apk : apks){

            apk.decompile();
            Exception e = apk.getDecompilationException();
            if(e != null){
                decompileExceptions.add(e);
                decompileWriter.println("*["+e.getClass()+ "]*" + " -> " +e.getMessage() + "<-");
            }
            apk.compile();
            Exception e1 = apk.getCompilationException();
            if(e1 != null){
                compileExceptions.add(e);
                compileWriter.println("*["+e.getClass()+ "]*" + " -> " +e.getMessage() + "<-");
            }

            filesToDelete.add(apk);
            for (ApkFile a: filesToDelete) {
                if(a.cleanApkWorkingDirectory()){
                    filesToDelete.remove(a);
                }
            }

        }
        decompileWriter.close();
        compileWriter.close();

//        writeToFile(decompileExceptions,"decompilationErrors.txt");
//        writeToFile(compileExceptions,"compilationErrors.txt");

    }

    private static void writeToFile(List<Exception> exceptions, String fName) throws Exception{
        PrintWriter writer = new PrintWriter(fName, "UTF-8");

        for (Exception s : exceptions) {
            System.out.println("*["+s.getClass()+ "]*" + " -> " +s.getMessage() + "<-");
            writer.println(s);
        }
        writer.close();
    }
}

