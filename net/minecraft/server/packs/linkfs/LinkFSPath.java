package net.minecraft.server.packs.linkfs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

class LinkFSPath implements Path {
   private static final BasicFileAttributes DIRECTORY_ATTRIBUTES = new DummyFileAttributes() {
      public boolean isRegularFile() {
         return false;
      }

      public boolean isDirectory() {
         return true;
      }
   };
   private static final BasicFileAttributes FILE_ATTRIBUTES = new DummyFileAttributes() {
      public boolean isRegularFile() {
         return true;
      }

      public boolean isDirectory() {
         return false;
      }
   };
   private static final Comparator<LinkFSPath> PATH_COMPARATOR = Comparator.comparing(LinkFSPath::pathToString);
   private final String name;
   private final LinkFileSystem fileSystem;
   @Nullable
   private final LinkFSPath parent;
   @Nullable
   private List<String> pathToRoot;
   @Nullable
   private String pathString;
   private final PathContents pathContents;

   public LinkFSPath(LinkFileSystem linkfilesystem, String s, @Nullable LinkFSPath linkfspath, PathContents pathcontents) {
      this.fileSystem = linkfilesystem;
      this.name = s;
      this.parent = linkfspath;
      this.pathContents = pathcontents;
   }

   private LinkFSPath createRelativePath(@Nullable LinkFSPath linkfspath, String s) {
      return new LinkFSPath(this.fileSystem, s, linkfspath, PathContents.RELATIVE);
   }

   public LinkFileSystem getFileSystem() {
      return this.fileSystem;
   }

   public boolean isAbsolute() {
      return this.pathContents != PathContents.RELATIVE;
   }

   public File toFile() {
      PathContents var2 = this.pathContents;
      if (var2 instanceof PathContents.FileContents pathcontents_filecontents) {
         return pathcontents_filecontents.contents().toFile();
      } else {
         throw new UnsupportedOperationException("Path " + this.pathToString() + " does not represent file");
      }
   }

   @Nullable
   public LinkFSPath getRoot() {
      return this.isAbsolute() ? this.fileSystem.rootPath() : null;
   }

   public LinkFSPath getFileName() {
      return this.createRelativePath((LinkFSPath)null, this.name);
   }

   @Nullable
   public LinkFSPath getParent() {
      return this.parent;
   }

   public int getNameCount() {
      return this.pathToRoot().size();
   }

   private List<String> pathToRoot() {
      if (this.name.isEmpty()) {
         return List.of();
      } else {
         if (this.pathToRoot == null) {
            ImmutableList.Builder<String> immutablelist_builder = ImmutableList.builder();
            if (this.parent != null) {
               immutablelist_builder.addAll(this.parent.pathToRoot());
            }

            immutablelist_builder.add(this.name);
            this.pathToRoot = immutablelist_builder.build();
         }

         return this.pathToRoot;
      }
   }

   public LinkFSPath getName(int i) {
      List<String> list = this.pathToRoot();
      if (i >= 0 && i < list.size()) {
         return this.createRelativePath((LinkFSPath)null, list.get(i));
      } else {
         throw new IllegalArgumentException("Invalid index: " + i);
      }
   }

   public LinkFSPath subpath(int i, int j) {
      List<String> list = this.pathToRoot();
      if (i >= 0 && j <= list.size() && i < j) {
         LinkFSPath linkfspath = null;

         for(int k = i; k < j; ++k) {
            linkfspath = this.createRelativePath(linkfspath, list.get(k));
         }

         return linkfspath;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public boolean startsWith(Path path) {
      if (path.isAbsolute() != this.isAbsolute()) {
         return false;
      } else if (path instanceof LinkFSPath) {
         LinkFSPath linkfspath = (LinkFSPath)path;
         if (linkfspath.fileSystem != this.fileSystem) {
            return false;
         } else {
            List<String> list = this.pathToRoot();
            List<String> list1 = linkfspath.pathToRoot();
            int i = list1.size();
            if (i > list.size()) {
               return false;
            } else {
               for(int j = 0; j < i; ++j) {
                  if (!list1.get(j).equals(list.get(j))) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public boolean endsWith(Path path) {
      if (path.isAbsolute() && !this.isAbsolute()) {
         return false;
      } else if (path instanceof LinkFSPath) {
         LinkFSPath linkfspath = (LinkFSPath)path;
         if (linkfspath.fileSystem != this.fileSystem) {
            return false;
         } else {
            List<String> list = this.pathToRoot();
            List<String> list1 = linkfspath.pathToRoot();
            int i = list1.size();
            int j = list.size() - i;
            if (j < 0) {
               return false;
            } else {
               for(int k = i - 1; k >= 0; --k) {
                  if (!list1.get(k).equals(list.get(j + k))) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public LinkFSPath normalize() {
      return this;
   }

   public LinkFSPath resolve(Path path) {
      LinkFSPath linkfspath = this.toLinkPath(path);
      return path.isAbsolute() ? linkfspath : this.resolve(linkfspath.pathToRoot());
   }

   private LinkFSPath resolve(List<String> list) {
      LinkFSPath linkfspath = this;

      for(String s : list) {
         linkfspath = linkfspath.resolveName(s);
      }

      return linkfspath;
   }

   LinkFSPath resolveName(String s) {
      if (isRelativeOrMissing(this.pathContents)) {
         return new LinkFSPath(this.fileSystem, s, this, this.pathContents);
      } else {
         PathContents linkfspath = this.pathContents;
         if (linkfspath instanceof PathContents.DirectoryContents) {
            PathContents.DirectoryContents pathcontents_directorycontents = (PathContents.DirectoryContents)linkfspath;
            LinkFSPath linkfspath = pathcontents_directorycontents.children().get(s);
            return linkfspath != null ? linkfspath : new LinkFSPath(this.fileSystem, s, this, PathContents.MISSING);
         } else if (this.pathContents instanceof PathContents.FileContents) {
            return new LinkFSPath(this.fileSystem, s, this, PathContents.MISSING);
         } else {
            throw new AssertionError("All content types should be already handled");
         }
      }
   }

   private static boolean isRelativeOrMissing(PathContents pathcontents) {
      return pathcontents == PathContents.MISSING || pathcontents == PathContents.RELATIVE;
   }

   public LinkFSPath relativize(Path path) {
      LinkFSPath linkfspath = this.toLinkPath(path);
      if (this.isAbsolute() != linkfspath.isAbsolute()) {
         throw new IllegalArgumentException("absolute mismatch");
      } else {
         List<String> list = this.pathToRoot();
         List<String> list1 = linkfspath.pathToRoot();
         if (list.size() >= list1.size()) {
            throw new IllegalArgumentException();
         } else {
            for(int i = 0; i < list.size(); ++i) {
               if (!list.get(i).equals(list1.get(i))) {
                  throw new IllegalArgumentException();
               }
            }

            return linkfspath.subpath(list.size(), list1.size());
         }
      }
   }

   public URI toUri() {
      try {
         return new URI("x-mc-link", this.fileSystem.store().name(), this.pathToString(), (String)null);
      } catch (URISyntaxException var2) {
         throw new AssertionError("Failed to create URI", var2);
      }
   }

   public LinkFSPath toAbsolutePath() {
      return this.isAbsolute() ? this : this.fileSystem.rootPath().resolve(this);
   }

   public LinkFSPath toRealPath(LinkOption... alinkoption) {
      return this.toAbsolutePath();
   }

   public WatchKey register(WatchService watchservice, WatchEvent.Kind<?>[] awatchevent_kind, WatchEvent.Modifier... awatchevent_modifier) {
      throw new UnsupportedOperationException();
   }

   public int compareTo(Path path) {
      LinkFSPath linkfspath = this.toLinkPath(path);
      return PATH_COMPARATOR.compare(this, linkfspath);
   }

   public boolean equals(Object object) {
      if (object == this) {
         return true;
      } else if (object instanceof LinkFSPath) {
         LinkFSPath linkfspath = (LinkFSPath)object;
         if (this.fileSystem != linkfspath.fileSystem) {
            return false;
         } else {
            boolean flag = this.hasRealContents();
            if (flag != linkfspath.hasRealContents()) {
               return false;
            } else if (flag) {
               return this.pathContents == linkfspath.pathContents;
            } else {
               return Objects.equals(this.parent, linkfspath.parent) && Objects.equals(this.name, linkfspath.name);
            }
         }
      } else {
         return false;
      }
   }

   private boolean hasRealContents() {
      return !isRelativeOrMissing(this.pathContents);
   }

   public int hashCode() {
      return this.hasRealContents() ? this.pathContents.hashCode() : this.name.hashCode();
   }

   public String toString() {
      return this.pathToString();
   }

   private String pathToString() {
      if (this.pathString == null) {
         StringBuilder stringbuilder = new StringBuilder();
         if (this.isAbsolute()) {
            stringbuilder.append("/");
         }

         Joiner.on("/").appendTo(stringbuilder, this.pathToRoot());
         this.pathString = stringbuilder.toString();
      }

      return this.pathString;
   }

   private LinkFSPath toLinkPath(@Nullable Path path) {
      if (path == null) {
         throw new NullPointerException();
      } else {
         if (path instanceof LinkFSPath) {
            LinkFSPath linkfspath = (LinkFSPath)path;
            if (linkfspath.fileSystem == this.fileSystem) {
               return linkfspath;
            }
         }

         throw new ProviderMismatchException();
      }
   }

   public boolean exists() {
      return this.hasRealContents();
   }

   @Nullable
   public Path getTargetPath() {
      PathContents var2 = this.pathContents;
      Path var10000;
      if (var2 instanceof PathContents.FileContents pathcontents_filecontents) {
         var10000 = pathcontents_filecontents.contents();
      } else {
         var10000 = null;
      }

      return var10000;
   }

   @Nullable
   public PathContents.DirectoryContents getDirectoryContents() {
      PathContents var2 = this.pathContents;
      PathContents.DirectoryContents var10000;
      if (var2 instanceof PathContents.DirectoryContents pathcontents_directorycontents) {
         var10000 = pathcontents_directorycontents;
      } else {
         var10000 = null;
      }

      return var10000;
   }

   public BasicFileAttributeView getBasicAttributeView() {
      return new BasicFileAttributeView() {
         public String name() {
            return "basic";
         }

         public BasicFileAttributes readAttributes() throws IOException {
            return LinkFSPath.this.getBasicAttributes();
         }

         public void setTimes(FileTime filetime, FileTime filetime1, FileTime filetime2) {
            throw new ReadOnlyFileSystemException();
         }
      };
   }

   public BasicFileAttributes getBasicAttributes() throws IOException {
      if (this.pathContents instanceof PathContents.DirectoryContents) {
         return DIRECTORY_ATTRIBUTES;
      } else if (this.pathContents instanceof PathContents.FileContents) {
         return FILE_ATTRIBUTES;
      } else {
         throw new NoSuchFileException(this.pathToString());
      }
   }
}
