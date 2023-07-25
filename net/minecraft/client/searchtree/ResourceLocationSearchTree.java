package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;

public interface ResourceLocationSearchTree<T> {
   static <T> ResourceLocationSearchTree<T> empty() {
      return new ResourceLocationSearchTree<T>() {
         public List<T> searchNamespace(String s) {
            return List.of();
         }

         public List<T> searchPath(String s) {
            return List.of();
         }
      };
   }

   static <T> ResourceLocationSearchTree<T> create(List<T> list, Function<T, Stream<ResourceLocation>> function) {
      if (list.isEmpty()) {
         return empty();
      } else {
         final SuffixArray<T> suffixarray = new SuffixArray<>();
         final SuffixArray<T> suffixarray1 = new SuffixArray<>();

         for(T object : list) {
            function.apply(object).forEach((resourcelocation) -> {
               suffixarray.add(object, resourcelocation.getNamespace().toLowerCase(Locale.ROOT));
               suffixarray1.add(object, resourcelocation.getPath().toLowerCase(Locale.ROOT));
            });
         }

         suffixarray.generate();
         suffixarray1.generate();
         return new ResourceLocationSearchTree<T>() {
            public List<T> searchNamespace(String s) {
               return suffixarray.search(s);
            }

            public List<T> searchPath(String s) {
               return suffixarray1.search(s);
            }
         };
      }
   }

   List<T> searchNamespace(String s);

   List<T> searchPath(String s);
}
