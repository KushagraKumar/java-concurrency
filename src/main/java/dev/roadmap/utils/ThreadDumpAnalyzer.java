package dev.roadmap.utils;
import java.io.IOException; import java.nio.file.Files; import java.nio.file.Path;
import java.util.regex.Matcher; import java.util.regex.Pattern;

public class ThreadDumpAnalyzer {
    private static final Pattern THREAD_STATE = Pattern.compile("^\"(.+?)\".*State: (BLOCKED|WAITING|TIMED_WAITING)");
    private static final Pattern LOCK_WAIT = Pattern.compile("waiting to lock <(0x[0-9a-f]+)>");
    private static final Pattern LOCK_HELD = Pattern.compile("locked <(0x[0-9a-f]+)>");
    public static void analyze(Path dumpFile) throws IOException {
        System.out.println("🔍 Analyzing thread dump: " + dumpFile);
        Files.lines(dumpFile).forEach(line -> {
            Matcher state = THREAD_STATE.matcher(line);
            if (state.find()) System.out.printf("Thread: %-30s State: %s%n", state.group(1), state.group(2));
            Matcher wait = LOCK_WAIT.matcher(line);
            if (wait.find()) System.out.println("  ⏳ waiting to lock: " + wait.group(1));
            Matcher held = LOCK_HELD.matcher(line);
            if (held.find()) System.out.println("  🔒 holds lock: " + held.group(1));
        });
        System.out.println("\n💡 Staff+ tip: Look for cycles where Thread A waits for lock held by B, and B waits for lock held by A.");
    }
    public static void main(String[] args) throws IOException {
        if (args.length == 0) { System.err.println("Usage: java ThreadDumpAnalyzer <thread-dump.txt>"); System.exit(1); }
        analyze(Path.of(args[0]));
    }
}
