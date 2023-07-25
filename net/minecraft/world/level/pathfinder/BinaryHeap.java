package net.minecraft.world.level.pathfinder;

public class BinaryHeap {
   private Node[] heap = new Node[128];
   private int size;

   public Node insert(Node node) {
      if (node.heapIdx >= 0) {
         throw new IllegalStateException("OW KNOWS!");
      } else {
         if (this.size == this.heap.length) {
            Node[] anode = new Node[this.size << 1];
            System.arraycopy(this.heap, 0, anode, 0, this.size);
            this.heap = anode;
         }

         this.heap[this.size] = node;
         node.heapIdx = this.size;
         this.upHeap(this.size++);
         return node;
      }
   }

   public void clear() {
      this.size = 0;
   }

   public Node peek() {
      return this.heap[0];
   }

   public Node pop() {
      Node node = this.heap[0];
      this.heap[0] = this.heap[--this.size];
      this.heap[this.size] = null;
      if (this.size > 0) {
         this.downHeap(0);
      }

      node.heapIdx = -1;
      return node;
   }

   public void remove(Node node) {
      this.heap[node.heapIdx] = this.heap[--this.size];
      this.heap[this.size] = null;
      if (this.size > node.heapIdx) {
         if (this.heap[node.heapIdx].f < node.f) {
            this.upHeap(node.heapIdx);
         } else {
            this.downHeap(node.heapIdx);
         }
      }

      node.heapIdx = -1;
   }

   public void changeCost(Node node, float f) {
      float f1 = node.f;
      node.f = f;
      if (f < f1) {
         this.upHeap(node.heapIdx);
      } else {
         this.downHeap(node.heapIdx);
      }

   }

   public int size() {
      return this.size;
   }

   private void upHeap(int i) {
      Node node = this.heap[i];

      int j;
      for(float f = node.f; i > 0; i = j) {
         j = i - 1 >> 1;
         Node node1 = this.heap[j];
         if (!(f < node1.f)) {
            break;
         }

         this.heap[i] = node1;
         node1.heapIdx = i;
      }

      this.heap[i] = node;
      node.heapIdx = i;
   }

   private void downHeap(int i) {
      Node node = this.heap[i];
      float f = node.f;

      while(true) {
         int j = 1 + (i << 1);
         int k = j + 1;
         if (j >= this.size) {
            break;
         }

         Node node1 = this.heap[j];
         float f1 = node1.f;
         Node node2;
         float f2;
         if (k >= this.size) {
            node2 = null;
            f2 = Float.POSITIVE_INFINITY;
         } else {
            node2 = this.heap[k];
            f2 = node2.f;
         }

         if (f1 < f2) {
            if (!(f1 < f)) {
               break;
            }

            this.heap[i] = node1;
            node1.heapIdx = i;
            i = j;
         } else {
            if (!(f2 < f)) {
               break;
            }

            this.heap[i] = node2;
            node2.heapIdx = i;
            i = k;
         }
      }

      this.heap[i] = node;
      node.heapIdx = i;
   }

   public boolean isEmpty() {
      return this.size == 0;
   }

   public Node[] getHeap() {
      Node[] anode = new Node[this.size()];
      System.arraycopy(this.heap, 0, anode, 0, this.size());
      return anode;
   }
}
