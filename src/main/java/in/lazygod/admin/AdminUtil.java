package in.lazygod.admin;

import com.sun.management.OperatingSystemMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUtil {

    public static Map<String, Object> systemMetrics() {
        Map<String, Object> map = new HashMap<>();
        Runtime rt = Runtime.getRuntime();

        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        map.put("memUsed", formatBytes(usedMemory));
        map.put("memTotal", formatBytes(totalMemory));

        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemCpuLoad(); // value between 0.0 and 1.0
        map.put("cpuLoad", String.format("%.2f", cpuLoad * 100));

        return map;
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.2f %s", bytes / Math.pow(1024, exp), unit);
    }

    public static List<String> tailLog(Path path, int lines) throws IOException {
        if (!Files.exists(path)) return List.of();
        List<String> all = Files.readAllLines(path);
        int start = Math.max(0, all.size() - lines);
        return all.subList(start, all.size());
    }
}
