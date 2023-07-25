package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.packs.PackResources;
import org.slf4j.Logger;

public class ResourceLoadStateTracker {
   private static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   private ResourceLoadStateTracker.ReloadState reloadState;
   private int reloadCount;

   public void startReload(ResourceLoadStateTracker.ReloadReason resourceloadstatetracker_reloadreason, List<PackResources> list) {
      ++this.reloadCount;
      if (this.reloadState != null && !this.reloadState.finished) {
         LOGGER.warn("Reload already ongoing, replacing");
      }

      this.reloadState = new ResourceLoadStateTracker.ReloadState(resourceloadstatetracker_reloadreason, list.stream().map(PackResources::packId).collect(ImmutableList.toImmutableList()));
   }

   public void startRecovery(Throwable throwable) {
      if (this.reloadState == null) {
         LOGGER.warn("Trying to signal reload recovery, but nothing was started");
         this.reloadState = new ResourceLoadStateTracker.ReloadState(ResourceLoadStateTracker.ReloadReason.UNKNOWN, ImmutableList.of());
      }

      this.reloadState.recoveryReloadInfo = new ResourceLoadStateTracker.RecoveryInfo(throwable);
   }

   public void finishReload() {
      if (this.reloadState == null) {
         LOGGER.warn("Trying to finish reload, but nothing was started");
      } else {
         this.reloadState.finished = true;
      }

   }

   public void fillCrashReport(CrashReport crashreport) {
      CrashReportCategory crashreportcategory = crashreport.addCategory("Last reload");
      crashreportcategory.setDetail("Reload number", this.reloadCount);
      if (this.reloadState != null) {
         this.reloadState.fillCrashInfo(crashreportcategory);
      }

   }

   static class RecoveryInfo {
      private final Throwable error;

      RecoveryInfo(Throwable throwable) {
         this.error = throwable;
      }

      public void fillCrashInfo(CrashReportCategory crashreportcategory) {
         crashreportcategory.setDetail("Recovery", "Yes");
         crashreportcategory.setDetail("Recovery reason", () -> {
            StringWriter stringwriter = new StringWriter();
            this.error.printStackTrace(new PrintWriter(stringwriter));
            return stringwriter.toString();
         });
      }
   }

   public static enum ReloadReason {
      INITIAL("initial"),
      MANUAL("manual"),
      UNKNOWN("unknown");

      final String name;

      private ReloadReason(String s) {
         this.name = s;
      }
   }

   static class ReloadState {
      private final ResourceLoadStateTracker.ReloadReason reloadReason;
      private final List<String> packs;
      @Nullable
      ResourceLoadStateTracker.RecoveryInfo recoveryReloadInfo;
      boolean finished;

      ReloadState(ResourceLoadStateTracker.ReloadReason resourceloadstatetracker_reloadreason, List<String> list) {
         this.reloadReason = resourceloadstatetracker_reloadreason;
         this.packs = list;
      }

      public void fillCrashInfo(CrashReportCategory crashreportcategory) {
         crashreportcategory.setDetail("Reload reason", this.reloadReason.name);
         crashreportcategory.setDetail("Finished", this.finished ? "Yes" : "No");
         crashreportcategory.setDetail("Packs", () -> String.join(", ", this.packs));
         if (this.recoveryReloadInfo != null) {
            this.recoveryReloadInfo.fillCrashInfo(crashreportcategory);
         }

      }
   }
}
