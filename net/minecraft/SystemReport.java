package net.minecraft;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;

public class SystemReport {
   public static final long BYTES_PER_MEBIBYTE = 1048576L;
   private static final long ONE_GIGA = 1000000000L;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String OPERATING_SYSTEM = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version");
   private static final String JAVA_VERSION = System.getProperty("java.version") + ", " + System.getProperty("java.vendor");
   private static final String JAVA_VM_VERSION = System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor");
   private final Map<String, String> entries = Maps.newLinkedHashMap();

   public SystemReport() {
      this.setDetail("Minecraft Version", SharedConstants.getCurrentVersion().getName());
      this.setDetail("Minecraft Version ID", SharedConstants.getCurrentVersion().getId());
      this.setDetail("Operating System", OPERATING_SYSTEM);
      this.setDetail("Java Version", JAVA_VERSION);
      this.setDetail("Java VM Version", JAVA_VM_VERSION);
      this.setDetail("Memory", () -> {
         Runtime runtime = Runtime.getRuntime();
         long i = runtime.maxMemory();
         long j = runtime.totalMemory();
         long k = runtime.freeMemory();
         long l = i / 1048576L;
         long i1 = j / 1048576L;
         long j1 = k / 1048576L;
         return k + " bytes (" + j1 + " MiB) / " + j + " bytes (" + i1 + " MiB) up to " + i + " bytes (" + l + " MiB)";
      });
      this.setDetail("CPUs", () -> String.valueOf(Runtime.getRuntime().availableProcessors()));
      this.ignoreErrors("hardware", () -> this.putHardware(new SystemInfo()));
      this.setDetail("JVM Flags", () -> {
         List<String> list = Util.getVmArguments().collect(Collectors.toList());
         return String.format(Locale.ROOT, "%d total; %s", list.size(), String.join(" ", list));
      });
   }

   public void setDetail(String s, String s1) {
      this.entries.put(s, s1);
   }

   public void setDetail(String s, Supplier<String> supplier) {
      try {
         this.setDetail(s, supplier.get());
      } catch (Exception var4) {
         LOGGER.warn("Failed to get system info for {}", s, var4);
         this.setDetail(s, "ERR");
      }

   }

   private void putHardware(SystemInfo systeminfo) {
      HardwareAbstractionLayer hardwareabstractionlayer = systeminfo.getHardware();
      this.ignoreErrors("processor", () -> this.putProcessor(hardwareabstractionlayer.getProcessor()));
      this.ignoreErrors("graphics", () -> this.putGraphics(hardwareabstractionlayer.getGraphicsCards()));
      this.ignoreErrors("memory", () -> this.putMemory(hardwareabstractionlayer.getMemory()));
   }

   private void ignoreErrors(String s, Runnable runnable) {
      try {
         runnable.run();
      } catch (Throwable var4) {
         LOGGER.warn("Failed retrieving info for group {}", s, var4);
      }

   }

   private void putPhysicalMemory(List<PhysicalMemory> list) {
      int i = 0;

      for(PhysicalMemory physicalmemory : list) {
         String s = String.format(Locale.ROOT, "Memory slot #%d ", i++);
         this.setDetail(s + "capacity (MB)", () -> String.format(Locale.ROOT, "%.2f", (float)physicalmemory.getCapacity() / 1048576.0F));
         this.setDetail(s + "clockSpeed (GHz)", () -> String.format(Locale.ROOT, "%.2f", (float)physicalmemory.getClockSpeed() / 1.0E9F));
         this.setDetail(s + "type", physicalmemory::getMemoryType);
      }

   }

   private void putVirtualMemory(VirtualMemory virtualmemory) {
      this.setDetail("Virtual memory max (MB)", () -> String.format(Locale.ROOT, "%.2f", (float)virtualmemory.getVirtualMax() / 1048576.0F));
      this.setDetail("Virtual memory used (MB)", () -> String.format(Locale.ROOT, "%.2f", (float)virtualmemory.getVirtualInUse() / 1048576.0F));
      this.setDetail("Swap memory total (MB)", () -> String.format(Locale.ROOT, "%.2f", (float)virtualmemory.getSwapTotal() / 1048576.0F));
      this.setDetail("Swap memory used (MB)", () -> String.format(Locale.ROOT, "%.2f", (float)virtualmemory.getSwapUsed() / 1048576.0F));
   }

   private void putMemory(GlobalMemory globalmemory) {
      this.ignoreErrors("physical memory", () -> this.putPhysicalMemory(globalmemory.getPhysicalMemory()));
      this.ignoreErrors("virtual memory", () -> this.putVirtualMemory(globalmemory.getVirtualMemory()));
   }

   private void putGraphics(List<GraphicsCard> list) {
      int i = 0;

      for(GraphicsCard graphicscard : list) {
         String s = String.format(Locale.ROOT, "Graphics card #%d ", i++);
         this.setDetail(s + "name", graphicscard::getName);
         this.setDetail(s + "vendor", graphicscard::getVendor);
         this.setDetail(s + "VRAM (MB)", () -> String.format(Locale.ROOT, "%.2f", (float)graphicscard.getVRam() / 1048576.0F));
         this.setDetail(s + "deviceId", graphicscard::getDeviceId);
         this.setDetail(s + "versionInfo", graphicscard::getVersionInfo);
      }

   }

   private void putProcessor(CentralProcessor centralprocessor) {
      CentralProcessor.ProcessorIdentifier centralprocessor_processoridentifier = centralprocessor.getProcessorIdentifier();
      this.setDetail("Processor Vendor", centralprocessor_processoridentifier::getVendor);
      this.setDetail("Processor Name", centralprocessor_processoridentifier::getName);
      this.setDetail("Identifier", centralprocessor_processoridentifier::getIdentifier);
      this.setDetail("Microarchitecture", centralprocessor_processoridentifier::getMicroarchitecture);
      this.setDetail("Frequency (GHz)", () -> String.format(Locale.ROOT, "%.2f", (float)centralprocessor_processoridentifier.getVendorFreq() / 1.0E9F));
      this.setDetail("Number of physical packages", () -> String.valueOf(centralprocessor.getPhysicalPackageCount()));
      this.setDetail("Number of physical CPUs", () -> String.valueOf(centralprocessor.getPhysicalProcessorCount()));
      this.setDetail("Number of logical CPUs", () -> String.valueOf(centralprocessor.getLogicalProcessorCount()));
   }

   public void appendToCrashReportString(StringBuilder stringbuilder) {
      stringbuilder.append("-- ").append("System Details").append(" --\n");
      stringbuilder.append("Details:");
      this.entries.forEach((s, s1) -> {
         stringbuilder.append("\n\t");
         stringbuilder.append(s);
         stringbuilder.append(": ");
         stringbuilder.append(s1);
      });
   }

   public String toLineSeparatedString() {
      return this.entries.entrySet().stream().map((map_entry) -> (String)map_entry.getKey() + ": " + (String)map_entry.getValue()).collect(Collectors.joining(System.lineSeparator()));
   }
}
