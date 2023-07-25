package net.minecraft.world.level.validation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DirectoryValidator {
   private final PathAllowList symlinkTargetAllowList;

   public DirectoryValidator(PathAllowList pathallowlist) {
      this.symlinkTargetAllowList = pathallowlist;
   }

   public void validateSymlink(Path path, List<ForbiddenSymlinkInfo> list) throws IOException {
      Path path1 = Files.readSymbolicLink(path);
      if (!this.symlinkTargetAllowList.matches(path1)) {
         list.add(new ForbiddenSymlinkInfo(path, path1));
      }

   }

   public List<ForbiddenSymlinkInfo> validateSave(Path path, boolean flag) throws IOException {
      final List<ForbiddenSymlinkInfo> list = new ArrayList<>();

      BasicFileAttributes basicfileattributes;
      try {
         basicfileattributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      } catch (NoSuchFileException var6) {
         return list;
      }

      if (!basicfileattributes.isRegularFile() && !basicfileattributes.isOther()) {
         if (basicfileattributes.isSymbolicLink()) {
            if (!flag) {
               this.validateSymlink(path, list);
               return list;
            }

            path = Files.readSymbolicLink(path);
         }

         Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            private void validateSymlink(Path path, BasicFileAttributes basicfileattributes) throws IOException {
               if (basicfileattributes.isSymbolicLink()) {
                  DirectoryValidator.this.validateSymlink(path, list);
               }

            }

            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicfileattributes) throws IOException {
               this.validateSymlink(path, basicfileattributes);
               return super.preVisitDirectory(path, basicfileattributes);
            }

            public FileVisitResult visitFile(Path path, BasicFileAttributes basicfileattributes) throws IOException {
               this.validateSymlink(path, basicfileattributes);
               return super.visitFile(path, basicfileattributes);
            }
         });
         return list;
      } else {
         throw new IOException("Path " + path + " is not a directory");
      }
   }
}
