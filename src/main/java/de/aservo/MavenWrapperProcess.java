package de.aservo;

import java.io.File;

public class MavenWrapperProcess {

    public static final String ATLASSIAN_PLUGIN = "atlassian-plugin";

    ProcessBuilder getProcessBuilder(
            final File file) {

        final File directory = file.isDirectory() ? file : file.getParentFile();

        final ProcessBuilder processBuilder = new ProcessBuilder();

        if (directory != null && directory.isDirectory()) {
            processBuilder.directory(directory);
        }

        processBuilder.redirectOutput(getNullFile());

        if (isWindows()) {
            processBuilder.command("bat", "mvnw.cmd");
        } else {
            processBuilder.command("mvn", "clean", "package");
        }

        return processBuilder;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    private static File getNullFile() {
        return new File(isWindows() ? "NUL" : "/dev/null");
    }

    /*
    StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
    Executors.newSingleThreadExecutor().submit(streamGobbler);

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }
     */

}
