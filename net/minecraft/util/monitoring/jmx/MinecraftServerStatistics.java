package net.minecraft.util.monitoring.jmx;

import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public final class MinecraftServerStatistics implements DynamicMBean {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MinecraftServer server;
   private final MBeanInfo mBeanInfo;
   private final Map<String, MinecraftServerStatistics.AttributeDescription> attributeDescriptionByName = Stream.of(new MinecraftServerStatistics.AttributeDescription("tickTimes", this::getTickTimes, "Historical tick times (ms)", long[].class), new MinecraftServerStatistics.AttributeDescription("averageTickTime", this::getAverageTickTime, "Current average tick time (ms)", Long.TYPE)).collect(Collectors.toMap((minecraftserverstatistics_attributedescription) -> minecraftserverstatistics_attributedescription.name, Function.identity()));

   private MinecraftServerStatistics(MinecraftServer minecraftserver) {
      this.server = minecraftserver;
      MBeanAttributeInfo[] ambeanattributeinfo = this.attributeDescriptionByName.values().stream().map(MinecraftServerStatistics.AttributeDescription::asMBeanAttributeInfo).toArray((i) -> new MBeanAttributeInfo[i]);
      this.mBeanInfo = new MBeanInfo(MinecraftServerStatistics.class.getSimpleName(), "metrics for dedicated server", ambeanattributeinfo, (MBeanConstructorInfo[])null, (MBeanOperationInfo[])null, new MBeanNotificationInfo[0]);
   }

   public static void registerJmxMonitoring(MinecraftServer minecraftserver) {
      try {
         ManagementFactory.getPlatformMBeanServer().registerMBean(new MinecraftServerStatistics(minecraftserver), new ObjectName("net.minecraft.server:type=Server"));
      } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException var2) {
         LOGGER.warn("Failed to initialise server as JMX bean", (Throwable)var2);
      }

   }

   private float getAverageTickTime() {
      return this.server.getAverageTickTime();
   }

   private long[] getTickTimes() {
      return this.server.tickTimes;
   }

   @Nullable
   public Object getAttribute(String s) {
      MinecraftServerStatistics.AttributeDescription minecraftserverstatistics_attributedescription = this.attributeDescriptionByName.get(s);
      return minecraftserverstatistics_attributedescription == null ? null : minecraftserverstatistics_attributedescription.getter.get();
   }

   public void setAttribute(Attribute attribute) {
   }

   public AttributeList getAttributes(String[] astring) {
      List<Attribute> list = Arrays.stream(astring).map(this.attributeDescriptionByName::get).filter(Objects::nonNull).map((minecraftserverstatistics_attributedescription) -> new Attribute(minecraftserverstatistics_attributedescription.name, minecraftserverstatistics_attributedescription.getter.get())).collect(Collectors.toList());
      return new AttributeList(list);
   }

   public AttributeList setAttributes(AttributeList attributelist) {
      return new AttributeList();
   }

   @Nullable
   public Object invoke(String s, Object[] aobject, String[] astring) {
      return null;
   }

   public MBeanInfo getMBeanInfo() {
      return this.mBeanInfo;
   }

   static final class AttributeDescription {
      final String name;
      final Supplier<Object> getter;
      private final String description;
      private final Class<?> type;

      AttributeDescription(String s, Supplier<Object> supplier, String s1, Class<?> oclass) {
         this.name = s;
         this.getter = supplier;
         this.description = s1;
         this.type = oclass;
      }

      private MBeanAttributeInfo asMBeanAttributeInfo() {
         return new MBeanAttributeInfo(this.name, this.type.getSimpleName(), this.description, true, false, false);
      }
   }
}
