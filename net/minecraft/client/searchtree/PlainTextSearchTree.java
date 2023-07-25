package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

public interface PlainTextSearchTree<T> {
   static <T> PlainTextSearchTree<T> empty() {
      return (s) -> List.of();
   }

   static <T> PlainTextSearchTree<T> create(List<T> list, Function<T, Stream<String>> function) {
      if (list.isEmpty()) {
         return empty();
      } else {
         SuffixArray<T> suffixarray = new SuffixArray<>();

         for(T object : list) {
            function.apply(object).forEach((s) -> suffixarray.add(object, s.toLowerCase(Locale.ROOT)));
         }

         suffixarray.generate();
         return suffixarray::search;
      }
   }

   List<T> search(String s);
}
