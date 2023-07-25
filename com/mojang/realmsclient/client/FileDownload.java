package com.mojang.realmsclient.client;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.ContentValidationException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;

public class FileDownload {
   static final Logger LOGGER = LogUtils.getLogger();
   volatile boolean cancelled;
   volatile boolean finished;
   volatile boolean error;
   volatile boolean extracting;
   @Nullable
   private volatile File tempFile;
   volatile File resourcePackPath;
   @Nullable
   private volatile HttpGet request;
   @Nullable
   private Thread currentThread;
   private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
   private static final String[] INVALID_FILE_NAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

   public long contentLength(String s) {
      CloseableHttpClient closeablehttpclient = null;
      HttpGet httpget = null;

      long var5;
      try {
         httpget = new HttpGet(s);
         closeablehttpclient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
         CloseableHttpResponse closeablehttpresponse = closeablehttpclient.execute(httpget);
         return Long.parseLong(closeablehttpresponse.getFirstHeader("Content-Length").getValue());
      } catch (Throwable var16) {
         LOGGER.error("Unable to get content length for download");
         var5 = 0L;
      } finally {
         if (httpget != null) {
            httpget.releaseConnection();
         }

         if (closeablehttpclient != null) {
            try {
               closeablehttpclient.close();
            } catch (IOException var15) {
               LOGGER.error("Could not close http client", (Throwable)var15);
            }
         }

      }

      return var5;
   }

   public void download(WorldDownload worlddownload, String s, RealmsDownloadLatestWorldScreen.DownloadStatus realmsdownloadlatestworldscreen_downloadstatus, LevelStorageSource levelstoragesource) {
      if (this.currentThread == null) {
         this.currentThread = new Thread(() -> {
            CloseableHttpClient closeablehttpclient = null;

            try {
               this.tempFile = File.createTempFile("backup", ".tar.gz");
               this.request = new HttpGet(worlddownload.downloadLink);
               closeablehttpclient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
               HttpResponse httpresponse = closeablehttpclient.execute(this.request);
               realmsdownloadlatestworldscreen_downloadstatus.totalBytes = Long.parseLong(httpresponse.getFirstHeader("Content-Length").getValue());
               if (httpresponse.getStatusLine().getStatusCode() == 200) {
                  OutputStream outputstream1 = new FileOutputStream(this.tempFile);
                  FileDownload.ProgressListener filedownload_progresslistener = new FileDownload.ProgressListener(s.trim(), this.tempFile, levelstoragesource, realmsdownloadlatestworldscreen_downloadstatus);
                  FileDownload.DownloadCountingOutputStream filedownload_downloadcountingoutputstream1 = new FileDownload.DownloadCountingOutputStream(outputstream1);
                  filedownload_downloadcountingoutputstream1.setListener(filedownload_progresslistener);
                  IOUtils.copy(httpresponse.getEntity().getContent(), filedownload_downloadcountingoutputstream1);
                  return;
               }

               this.error = true;
               this.request.abort();
            } catch (Exception var93) {
               LOGGER.error("Caught exception while downloading: {}", (Object)var93.getMessage());
               this.error = true;
               return;
            } finally {
               this.request.releaseConnection();
               if (this.tempFile != null) {
                  this.tempFile.delete();
               }

               if (!this.error) {
                  if (!worlddownload.resourcePackUrl.isEmpty() && !worlddownload.resourcePackHash.isEmpty()) {
                     try {
                        this.tempFile = File.createTempFile("resources", ".tar.gz");
                        this.request = new HttpGet(worlddownload.resourcePackUrl);
                        HttpResponse httpresponse4 = closeablehttpclient.execute(this.request);
                        realmsdownloadlatestworldscreen_downloadstatus.totalBytes = Long.parseLong(httpresponse4.getFirstHeader("Content-Length").getValue());
                        if (httpresponse4.getStatusLine().getStatusCode() != 200) {
                           this.error = true;
                           this.request.abort();
                           return;
                        }

                        OutputStream outputstream4 = new FileOutputStream(this.tempFile);
                        FileDownload.ResourcePackProgressListener filedownload_resourcepackprogresslistener3 = new FileDownload.ResourcePackProgressListener(this.tempFile, realmsdownloadlatestworldscreen_downloadstatus, worlddownload);
                        FileDownload.DownloadCountingOutputStream filedownload_downloadcountingoutputstream4 = new FileDownload.DownloadCountingOutputStream(outputstream4);
                        filedownload_downloadcountingoutputstream4.setListener(filedownload_resourcepackprogresslistener3);
                        IOUtils.copy(httpresponse4.getEntity().getContent(), filedownload_downloadcountingoutputstream4);
                     } catch (Exception var91) {
                        LOGGER.error("Caught exception while downloading: {}", (Object)var91.getMessage());
                        this.error = true;
                     } finally {
                        this.request.releaseConnection();
                        if (this.tempFile != null) {
                           this.tempFile.delete();
                        }

                     }
                  } else {
                     this.finished = true;
                  }
               }

               if (closeablehttpclient != null) {
                  try {
                     closeablehttpclient.close();
                  } catch (IOException var90) {
                     LOGGER.error("Failed to close Realms download client");
                  }
               }

            }

         });
         this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
         this.currentThread.start();
      }
   }

   public void cancel() {
      if (this.request != null) {
         this.request.abort();
      }

      if (this.tempFile != null) {
         this.tempFile.delete();
      }

      this.cancelled = true;
   }

   public boolean isFinished() {
      return this.finished;
   }

   public boolean isError() {
      return this.error;
   }

   public boolean isExtracting() {
      return this.extracting;
   }

   public static String findAvailableFolderName(String s) {
      s = s.replaceAll("[\\./\"]", "_");

      for(String s1 : INVALID_FILE_NAMES) {
         if (s.equalsIgnoreCase(s1)) {
            s = "_" + s + "_";
         }
      }

      return s;
   }

   void untarGzipArchive(String s, @Nullable File file, LevelStorageSource levelstoragesource) throws IOException {
      Pattern pattern = Pattern.compile(".*-([0-9]+)$");
      int i = 1;

      for(char c0 : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
         s = s.replace(c0, '_');
      }

      if (StringUtils.isEmpty(s)) {
         s = "Realm";
      }

      s = findAvailableFolderName(s);

      try {
         for(LevelStorageSource.LevelDirectory levelstoragesource_leveldirectory : levelstoragesource.findLevelCandidates()) {
            String s1 = levelstoragesource_leveldirectory.directoryName();
            if (s1.toLowerCase(Locale.ROOT).startsWith(s.toLowerCase(Locale.ROOT))) {
               Matcher matcher = pattern.matcher(s1);
               if (matcher.matches()) {
                  int j = Integer.parseInt(matcher.group(1));
                  if (j > i) {
                     i = j;
                  }
               } else {
                  ++i;
               }
            }
         }
      } catch (Exception var43) {
         LOGGER.error("Error getting level list", (Throwable)var43);
         this.error = true;
         return;
      }

      String s3;
      if (levelstoragesource.isNewLevelIdAcceptable(s) && i <= 1) {
         s3 = s;
      } else {
         s3 = s + (i == 1 ? "" : "-" + i);
         if (!levelstoragesource.isNewLevelIdAcceptable(s3)) {
            boolean flag = false;

            while(!flag) {
               ++i;
               s3 = s + (i == 1 ? "" : "-" + i);
               if (levelstoragesource.isNewLevelIdAcceptable(s3)) {
                  flag = true;
               }
            }
         }
      }

      TarArchiveInputStream tararchiveinputstream = null;
      File file1 = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath(), "saves");

      try {
         file1.mkdir();
         tararchiveinputstream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(file))));

         for(TarArchiveEntry tararchiveentry = tararchiveinputstream.getNextTarEntry(); tararchiveentry != null; tararchiveentry = tararchiveinputstream.getNextTarEntry()) {
            File file2 = new File(file1, tararchiveentry.getName().replace("world", s3));
            if (tararchiveentry.isDirectory()) {
               file2.mkdirs();
            } else {
               file2.createNewFile();
               FileOutputStream fileoutputstream = new FileOutputStream(file2);

               try {
                  IOUtils.copy(tararchiveinputstream, fileoutputstream);
               } catch (Throwable var37) {
                  try {
                     fileoutputstream.close();
                  } catch (Throwable var36) {
                     var37.addSuppressed(var36);
                  }

                  throw var37;
               }

               fileoutputstream.close();
            }
         }
      } catch (Exception var41) {
         LOGGER.error("Error extracting world", (Throwable)var41);
         this.error = true;
      } finally {
         if (tararchiveinputstream != null) {
            tararchiveinputstream.close();
         }

         if (file != null) {
            file.delete();
         }

         try {
            LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess2 = levelstoragesource.validateAndCreateAccess(s3);

            try {
               levelstoragesource_levelstorageaccess2.renameLevel(s3.trim());
               Path path2 = levelstoragesource_levelstorageaccess2.getLevelPath(LevelResource.LEVEL_DATA_FILE);
               deletePlayerTag(path2.toFile());
            } catch (Throwable var38) {
               if (levelstoragesource_levelstorageaccess2 != null) {
                  try {
                     levelstoragesource_levelstorageaccess2.close();
                  } catch (Throwable var35) {
                     var38.addSuppressed(var35);
                  }
               }

               throw var38;
            }

            if (levelstoragesource_levelstorageaccess2 != null) {
               levelstoragesource_levelstorageaccess2.close();
            }
         } catch (IOException var39) {
            LOGGER.error("Failed to rename unpacked realms level {}", s3, var39);
         } catch (ContentValidationException var40) {
            LOGGER.warn("{}", (Object)var40.getMessage());
         }

         this.resourcePackPath = new File(file1, s3 + File.separator + "resources.zip");
      }

   }

   private static void deletePlayerTag(File file) {
      if (file.exists()) {
         try {
            CompoundTag compoundtag = NbtIo.readCompressed(file);
            CompoundTag compoundtag1 = compoundtag.getCompound("Data");
            compoundtag1.remove("Player");
            NbtIo.writeCompressed(compoundtag, file);
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }

   }

   static class DownloadCountingOutputStream extends CountingOutputStream {
      @Nullable
      private ActionListener listener;

      public DownloadCountingOutputStream(OutputStream outputstream) {
         super(outputstream);
      }

      public void setListener(ActionListener actionlistener) {
         this.listener = actionlistener;
      }

      protected void afterWrite(int i) throws IOException {
         super.afterWrite(i);
         if (this.listener != null) {
            this.listener.actionPerformed(new ActionEvent(this, 0, (String)null));
         }

      }
   }

   class ProgressListener implements ActionListener {
      private final String worldName;
      private final File tempFile;
      private final LevelStorageSource levelStorageSource;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

      ProgressListener(String s, File file, LevelStorageSource levelstoragesource, RealmsDownloadLatestWorldScreen.DownloadStatus realmsdownloadlatestworldscreen_downloadstatus) {
         this.worldName = s;
         this.tempFile = file;
         this.levelStorageSource = levelstoragesource;
         this.downloadStatus = realmsdownloadlatestworldscreen_downloadstatus;
      }

      public void actionPerformed(ActionEvent actionevent) {
         this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)actionevent.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
            try {
               FileDownload.this.extracting = true;
               FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
            } catch (IOException var3) {
               FileDownload.LOGGER.error("Error extracting archive", (Throwable)var3);
               FileDownload.this.error = true;
            }
         }

      }
   }

   class ResourcePackProgressListener implements ActionListener {
      private final File tempFile;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
      private final WorldDownload worldDownload;

      ResourcePackProgressListener(File file, RealmsDownloadLatestWorldScreen.DownloadStatus realmsdownloadlatestworldscreen_downloadstatus, WorldDownload worlddownload) {
         this.tempFile = file;
         this.downloadStatus = realmsdownloadlatestworldscreen_downloadstatus;
         this.worldDownload = worlddownload;
      }

      public void actionPerformed(ActionEvent actionevent) {
         this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)actionevent.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
            try {
               String s = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
               if (s.equals(this.worldDownload.resourcePackHash)) {
                  FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
                  FileDownload.this.finished = true;
               } else {
                  FileDownload.LOGGER.error("Resourcepack had wrong hash (expected {}, found {}). Deleting it.", this.worldDownload.resourcePackHash, s);
                  FileUtils.deleteQuietly(this.tempFile);
                  FileDownload.this.error = true;
               }
            } catch (IOException var3) {
               FileDownload.LOGGER.error("Error copying resourcepack file: {}", (Object)var3.getMessage());
               FileDownload.this.error = true;
            }
         }

      }
   }
}
