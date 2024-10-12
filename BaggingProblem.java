import java.util.*;
import java.io.*;

public class BaggingProblem {
    private List<Bag> bags = new ArrayList<>();
    private Map<String, Item> items = new HashMap<>();
    private Map<Item, List<Bag>> compatibleBags = new HashMap<>();

    public class Item implements Comparable<Item> {
        String name;
        int size;
        Bag myBag = null;
        Set<String> conflictingItems;
        Set<String> allowedWithItems;

        public Item(String name, int size) {
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
        String name;
        int maxSize;
        int currSize = 0;
        Map<String, Item> packedInMe = new HashMap<>();

        public Bag(String name, int maxBagSize) {
            this.name = name;
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
        for (int x = 0; x < nb; x++) bags.add(new Bag("bag" + x, maxBagSize));

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

        updateCompatibleBags();
    }

    private void updateCompatibleBags() {
        for (Item item : items.values()) {
            List<Bag> compatible = new ArrayList<>();

            for (Bag bag : bags) {
                if (bag.canPack(item)) {
                    compatible.add(bag);
                }
            }

            compatible.sort(Comparator.comparingInt(b -> b.packedInMe.size()));
            compatibleBags.put(item, compatible);
        }
    }

    public boolean search() {
    
        if (getTotalItemSize() > getTotalBagCapacity()) {
            System.out.println("Total item size exceeds total bag capacity. Packing is impossible.");
            return false;
        }
        List<Item> sortedItems = sortItems();
        return search(sortedItems, 0);
    }

    private List<Item> sortItems() {
        List<Item> sortedItems = new ArrayList<>(items.values());
        sortedItems.sort((i1, i2) -> {
            int conflictComparison = Integer.compare(i2.conflictingItems.size(), i1.conflictingItems.size());
            return (conflictComparison != 0) ? conflictComparison : Integer.compare(i1.size, i2.size);
        });
        return sortedItems;
    }

    private boolean search(List<Item> sortedItems, int index) {

        if (index == sortedItems.size()) return true;

        Item currentItem = sortedItems.get(index);
        for (Bag bag : compatibleBags.get(currentItem)) {
            if (bag.pack(currentItem)) {
                if (search(sortedItems, index + 1)) return true;
                bag.unpack(currentItem);
            }
        }

        return false;
    }

    private int getTotalItemSize() {
        return items.values().stream().mapToInt(item -> item.size).sum();
    }

    private int getTotalBagCapacity() {
        return bags.stream().mapToInt(bag -> bag.maxSize).sum();
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