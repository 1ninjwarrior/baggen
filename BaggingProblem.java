import java.util.*;
import java.io.*;

public class BaggingProblem {
    
    List<Bag> bags = new ArrayList<>();
    HashMap<String, Item> items = new HashMap<>();
    private long startTime;
    private static final long TIME_LIMIT = 10000; // 10 seconds
    HashMap<Item, List<Bag>> compatibleBags = new HashMap<>();
    HashMap<Item, HashSet<String>> itemConflicts = new HashMap<>();
    HashMap<Bag, HashSet<String>> bagConflicts = new HashMap<>();

    public class Item implements Comparable<Item> {
        int id;
        String name;
        int size;
        Bag myBag = null;
        Set<String> conflictingItems;
        Set<String> allowedWithItems;

        public Item(String name, int size) {
            this.id = items.size();
            this.name = name;
            this.size = size;
            this.conflictingItems = new HashSet<>();
            this.allowedWithItems = new HashSet<>();
        }

        @Override
        public int compareTo(Item i) {
            return i.size - this.size;
        }
    }

    public class Bag {
        int id;
        String name;
        int maxSize;
        int currSize = 0;
        HashMap<String, Item> packedInMe = new HashMap<>();

        public Bag(int maxBagSize) {
            this.id = bags.size();
            this.name = "bag" + id;
            this.maxSize = maxBagSize;
        }

        public boolean canPack(Item i) {
            if (i.size > maxSize - currSize) return false;

            for (Item j : packedInMe.values()) {
                if (i.conflictingItems.contains(j.name) || j.conflictingItems.contains(i.name)) {
                    return false;
                }
                if (!i.allowedWithItems.isEmpty() && !i.allowedWithItems.contains(j.name)) {
                    return false;
                }
                if (!j.allowedWithItems.isEmpty() && !j.allowedWithItems.contains(i.name)) {
                    return false;
                }
            }
            return true;
        }

        public boolean pack(Item i) {
            if (!canPack(i)) return false;

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

            if (st.hasMoreTokens()) {
                String constraintType = st.nextToken();
                while (st.hasMoreTokens()) {
                    String constraintItem = st.nextToken();
                    if (constraintType.equals("+")) {
                        item.allowedWithItems.add(constraintItem);
                    } else if (constraintType.equals("-")) {
                        item.conflictingItems.add(constraintItem);
                    }
                }
            }
        }
        br.close();

        // Initialize item conflicts
        for (Item item : items.values()) {
            itemConflicts.put(item, new HashSet<>(item.conflictingItems));
        }

        // Initialize bag conflicts
        for (Bag bag : bags) {
            bagConflicts.put(bag, new HashSet<>()); // Assuming bags have a way to track conflicts
        }

        // Sort items using MRV
        List<Item> sortedItems = new ArrayList<>(items.values());
        sortedItems.sort((i1, i2) -> {
            int conflictComparison = Integer.compare(itemConflicts.get(i2).size(), itemConflicts.get(i1).size());
            if (conflictComparison != 0) return conflictComparison;
            return Integer.compare(i1.size, i2.size);
        });

        // Update compatible bags based on constraints
        updateCompatibleBags();

        // Start search
        search(sortedItems, 0);
    }

    private void updateCompatibleBags() {
        for (Item item : items.values()) {
            Set<String> conflicts = itemConflicts.get(item);
            List<Bag> compatible = new ArrayList<>();

            for (Bag bag : bags) {
                if (bag.canPack(item) && !conflicts.contains(bag.name)) {
                    compatible.add(bag);
                }
            }

            // Sort bags using LCV
            compatible.sort((b1, b2) -> {
                int b1Constraints = bagConflicts.get(b1).size();
                int b2Constraints = bagConflicts.get(b2).size();
                return Integer.compare(b1Constraints, b2Constraints);
            });

            compatibleBags.put(item, compatible);
        }
    }

    public boolean search() {
        startTime = System.currentTimeMillis();
    
        //Make sure the weight of all items isn't more than the total capacity
        int totalItemSize = items.values().stream().mapToInt(item -> item.size).sum();
        int totalBagCapacity = bags.stream().mapToInt(bag -> bag.maxSize).sum();
        
        if (totalItemSize > totalBagCapacity) {
            System.out.println("Total item size exceeds total bag capacity. Packing is impossible.");
            return false;
        }
        List<Item> sortedItems = new ArrayList<>(items.values());
        Collections.sort(sortedItems);
        return search(sortedItems, 0);
    }

    private boolean search(List<Item> sortedItems, int index) {
        if (System.currentTimeMillis() - startTime > TIME_LIMIT) {
            System.out.println("Time limit exceeded");
            return false;
        }

        if (index == sortedItems.size()) return true;

        Item currentItem = sortedItems.get(index);
        List<Bag> compatible = compatibleBags.get(currentItem);

        for (Bag b : compatible) {
            if (b.pack(currentItem)) {
                if (search(sortedItems, index + 1)) return true;
                b.unpack(currentItem);
            }
        }

        return false;
    }

    public void printPacking() {
        for (Bag b : bags) {
            if (!b.packedInMe.isEmpty()) {
                System.out.println(String.join("\t", b.packedInMe.keySet()));
            }
        }
    }

    public static void main(String[] args) {
        try {
            BaggingProblem bp = new BaggingProblem(args[0]);
            if (bp.search()) {
                System.out.println("success");
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
