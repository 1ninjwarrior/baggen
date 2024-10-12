import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

public class BaggingProblem {
    private List<Bag> bags = new ArrayList<>();
    private Map<String, Item> items = new HashMap<>();
    private Map<Item, List<Bag>> domains = new HashMap<>();

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

        initializeDomains();
    }

    private void initializeDomains() {
        domains = new HashMap<>();
        for (Item item : items.values()) {
            List<Bag> compatibleBags = bags.stream()
                .filter(bag -> bag.canPack(item))
                .collect(Collectors.toList());
            domains.put(item, compatibleBags);
        }
    }

    public boolean search() {
        if (getTotalItemSize() > getTotalBagCapacity()) {
            System.out.println("Total item size exceeds total bag capacity. Packing is impossible.");
            return false;
        }

        if (!nodeConsistency()) {
            return false;
        }

        if (!arcConsistency()) {
            return false;
        }

        List<Item> unassignedItems = new ArrayList<>(items.values());
        return backtrack(unassignedItems);
    }

    private int getTotalItemSize() {
        return items.values().stream().mapToInt(item -> item.size).sum();
    }

    private int getTotalBagCapacity() {
        return bags.stream().mapToInt(bag -> bag.maxSize).sum();
    }

    private boolean nodeConsistency() {
        for (Item item : items.values()) {
            domains.get(item).removeIf(bag -> !bag.canPack(item));
            if (domains.get(item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean arcConsistency() {
        Queue<Arc> queue = new LinkedList<>();
        for (Item item1 : items.values()) {
            for (Item item2 : items.values()) {
                if (item1 != item2) {
                    queue.add(new Arc(item1, item2));
                }
            }
        }

        while (!queue.isEmpty()) {
            Arc arc = queue.poll();
            if (revise(arc)) {
                if (domains.get(arc.item1).isEmpty()) {
                    return false;
                }
                for (Item neighbor : items.values()) {
                    if (neighbor != arc.item1 && neighbor != arc.item2) {
                        queue.add(new Arc(neighbor, arc.item1));
                    }
                }
            }
        }
        return true;
    }

    private boolean revise(Arc arc) {
        boolean revised = false;
        List<Bag> domain1 = domains.get(arc.item1);
        List<Bag> toRemove = new ArrayList<>();

        for (Bag bag1 : domain1) {
            boolean hasSupport = false;
            for (Bag bag2 : domains.get(arc.item2)) {
                if (bag1 != bag2 || bag1.canPack(arc.item1) && bag1.canPack(arc.item2)) {
                    hasSupport = true;
                    break;
                }
            }
            if (!hasSupport) {
                toRemove.add(bag1);
                revised = true;
            }
        }

        domain1.removeAll(toRemove);
        return revised;
    }

    private boolean backtrack(List<Item> unassignedItems) {
        if (unassignedItems.isEmpty()) {
            return true;
        }

        Item item = selectUnassignedItem(unassignedItems);
        for (Bag bag : orderDomainValues(item)) {
            if (bag.pack(item)) {
                List<Item> newUnassignedItems = new ArrayList<>(unassignedItems);
                newUnassignedItems.remove(item);
                if (backtrack(newUnassignedItems)) {
                    return true;
                }
                bag.unpack(item);
            }
        }

        return false;
    }

    private Item selectUnassignedItem(List<Item> unassignedItems) {
        return Collections.min(unassignedItems, Comparator
            .<Item>comparingInt(item -> domains.get(item).size())
            .thenComparingInt(item -> -item.conflictingItems.size())
            .thenComparingInt(item -> -item.allowedWithItems.size())
            .thenComparingInt(item -> -item.size));
    }

    private List<Bag> orderDomainValues(Item item) {
        return domains.get(item).stream()
            .sorted(Comparator
                .comparingInt((Bag b) -> -b.packedInMe.size())
                .thenComparingInt(b -> b.maxSize - b.currSize)) 
            .collect(Collectors.toList());
    }

    private static class Arc {
        Item item1;
        Item item2;

        Arc(Item item1, Item item2) {
            this.item1 = item1;
            this.item2 = item2;
        }
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
            e.printStackTrace();
        }
    }
}
