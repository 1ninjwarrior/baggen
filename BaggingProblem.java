import java.util.*;
import java.io.*;

public class BaggingProblem {
    
    Vector<Bag> bags = new Vector<Bag>();
    HashMap<String, Item> items = new HashMap<String, Item>();

    public class Item implements Comparable<Item> {
        int id;
        String name;
        int size;
        Bag myBag = null;

        public Item(String name, int size) {
            this.id = items.size();
            this.name = name;
            this.size = size;
        }

        @Override
        public int compareTo(Item i) {
            return i.size - this.size; // Sort by size (descending)
        }
    }

    public class Bag {
        int id;
        int maxSize;
        int currSize = 0;
        HashMap<String, Item> packedInMe = new HashMap<String, Item>();

        public Bag(int maxBagSize) {
            this.id = bags.size();
            this.maxSize = maxBagSize;
        }

        public boolean pack(Item i) {
            if (i.size > maxSize - currSize) return false;
            currSize += i.size;
            packedInMe.put(i.name, i);
            i.myBag = this;
            return true;
        }

        public void unpack(Item i) {
            packedInMe.remove(i.name);
            currSize -= i.size;
            i.myBag = null;
        }
    }

    public BaggingProblem(String filename) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int nb = Integer.parseInt(br.readLine());
        int maxBagSize = Integer.parseInt(br.readLine());
        for (int x = 0; x < nb; x++) bags.add(new Bag(maxBagSize));

        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            String name = st.nextToken();
            int size = Integer.parseInt(st.nextToken());
            if (items.containsKey(name)) throw new RuntimeException("Duplicate item name in file");

            Item item = new Item(name, size);
            items.put(name, item);
        }
        br.close();
    }

    public boolean search() {
        PriorityQueue<Item> priorityQueue = new PriorityQueue<>(items.values());
        return search(priorityQueue);
    }

    private boolean search(PriorityQueue<Item> priorityQueue) {
        if (priorityQueue.isEmpty()) return true;

        Item mostRestrictiveItem = priorityQueue.poll();
        for (Bag b : bags) {
            if (b.pack(mostRestrictiveItem)) {
                if (search(priorityQueue)) return true;
                b.unpack(mostRestrictiveItem);
            }
        }

        priorityQueue.add(mostRestrictiveItem);
        return false;
    }

    public void printPacking() {
        System.out.println("Success:");
        for (Bag b : bags) {
            if (!b.packedInMe.isEmpty()) {
                System.out.println("Bag " + b.id + ": " + String.join(", ", b.packedInMe.keySet()));
            }
        }
    }

    public static void main(String[] args) {
        try {
            BaggingProblem bp = new BaggingProblem(args[0]);
            if (bp.search()) {
                bp.printPacking();
            } else {
                System.out.println("failure");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
