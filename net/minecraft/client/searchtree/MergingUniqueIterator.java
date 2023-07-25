package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;

public class MergingUniqueIterator<T> extends AbstractIterator<T> {
   private final PeekingIterator<T> firstIterator;
   private final PeekingIterator<T> secondIterator;
   private final Comparator<T> comparator;

   public MergingUniqueIterator(Iterator<T> iterator, Iterator<T> iterator1, Comparator<T> comparator) {
      this.firstIterator = Iterators.peekingIterator(iterator);
      this.secondIterator = Iterators.peekingIterator(iterator1);
      this.comparator = comparator;
   }

   protected T computeNext() {
      boolean flag = !this.firstIterator.hasNext();
      boolean flag1 = !this.secondIterator.hasNext();
      if (flag && flag1) {
         return this.endOfData();
      } else if (flag) {
         return this.secondIterator.next();
      } else if (flag1) {
         return this.firstIterator.next();
      } else {
         int i = this.comparator.compare(this.firstIterator.peek(), this.secondIterator.peek());
         if (i == 0) {
            this.secondIterator.next();
         }

         return (T)(i <= 0 ? this.firstIterator.next() : this.secondIterator.next());
      }
   }
}
