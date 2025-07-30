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
        map.put("memUsed", rt.totalMemory() - rt.freeMemory());
        map.put("memTotal", rt.totalMemory());
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        map.put("cpuLoad", String.format("%.2f", osBean.getSystemCpuLoad() * 100));
        return map;
    }

    public static List<String> tailLog(Path path, int lines) throws IOException {
        if (!Files.exists(path)) return List.of();
        List<String> all = Files.readAllLines(path);
        int start = Math.max(0, all.size() - lines);
        return all.subList(start, all.size());
    }
}
