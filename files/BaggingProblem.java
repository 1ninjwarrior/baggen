package files;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;


public class BaggingProblem {
    private List<Bag> bags = new ArrayList<>();
    private Map<String, Item> items = new HashMap<>();
    private Map<Item, List<Bag>> itemToCompatibleBags = new HashMap<>();

    public class Item implements Comparable<Item> {
        String name;
        int size;
        Bag assignedBag = null;
        Set<String> conflictingItems;
        Set<String> compatibleItems;

        public Item(String name, int size) {
            this.name = name;
            this.size = size;
            this.conflictingItems = new HashSet<>();
            this.compatibleItems = new HashSet<>();
        }

        @Override
        public int compareTo(Item otherItem) {
            return otherItem.size - this.size;
        }
    }

    public class Bag {
        String name;
        int maxCapacity;
        int currentCapacity = 0;
        Map<String, Item> itemsPacked = new HashMap<>();

        public Bag(String name, int maxCapacity) {
            this.name = name;
            this.maxCapacity = maxCapacity;
        }

        public boolean canPack(Item item) {
            if (item.size > maxCapacity - currentCapacity) return false;

            for (Item packedItem : itemsPacked.values()) {
                if (item.conflictingItems.contains(packedItem.name) || packedItem.conflictingItems.contains(item.name)) {
                    return false;
                }
                if (!item.compatibleItems.isEmpty() && !item.compatibleItems.contains(packedItem.name)) {
                    return false;
                }
                if (!packedItem.compatibleItems.isEmpty() && !packedItem.compatibleItems.contains(item.name)) {
                    return false;
                }
            }
            return true;
        }

        public boolean packItem(Item item) {
            if (!canPack(item)) return false;

            currentCapacity += item.size;
            itemsPacked.put(item.name, item);
            item.assignedBag = this;
            return true;
        }

        public void unpackItem(Item item) {
            itemsPacked.remove(item.name);
            currentCapacity -= item.size;
            item.assignedBag = null;
        }
    }

    public BaggingProblem(String filename) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int numberOfBags = Integer.parseInt(reader.readLine());
        int maxBagCapacity = Integer.parseInt(reader.readLine());
        for (int i = 0; i < numberOfBags; i++) bags.add(new Bag("bag" + i, maxBagCapacity));

        String line;
        while ((line = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            String itemName = tokenizer.nextToken();
            int itemSize = Integer.parseInt(tokenizer.nextToken());

            if (items.containsKey(itemName)) throw new RuntimeException("Duplicate item name in file");

            Item item = new Item(itemName, itemSize);
            items.put(itemName, item);

            if (tokenizer.hasMoreTokens()) {
                String constraintType = tokenizer.nextToken();
                while (tokenizer.hasMoreTokens()) {
                    String constraintItem = tokenizer.nextToken();
                    if (constraintType.equals("+")) {
                        item.compatibleItems.add(constraintItem);
                    } else if (constraintType.equals("-")) {
                        item.conflictingItems.add(constraintItem);
                    }
                }
            }
        }
        reader.close();

        initializeItemDomains();
    }

    private void initializeItemDomains() {
        itemToCompatibleBags = new HashMap<>();
        for (Item item : items.values()) {
            List<Bag> compatibleBags = bags.stream()
                .filter(bag -> bag.canPack(item))
                .collect(Collectors.toList());
            itemToCompatibleBags.put(item, compatibleBags);
        }
    }

    public boolean solve() {
        if (getTotalItemSize() > getTotalBagCapacity()) {
            return false;
        }

        if (!ensureNodeConsistency()) {
            return false;
        }

        if (!ensureArcConsistency()) {
            return false;
        }

        List<Item> unassignedItems = new ArrayList<>(items.values());
        return backtrack(unassignedItems);
    }

    private int getTotalItemSize() {
        return items.values().stream().mapToInt(item -> item.size).sum();
    }

    private int getTotalBagCapacity() {
        return bags.stream().mapToInt(bag -> bag.maxCapacity).sum();
    }

    private boolean ensureNodeConsistency() {
        for (Item item : items.values()) {
            itemToCompatibleBags.get(item).removeIf(bag -> !bag.canPack(item));
            if (itemToCompatibleBags.get(item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean ensureArcConsistency() {
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
            if (reviseArc(arc)) {
                if (itemToCompatibleBags.get(arc.item1).isEmpty()) {
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

    private boolean reviseArc(Arc arc) {
        boolean revised = false;
        List<Bag> domain1 = itemToCompatibleBags.get(arc.item1);
        List<Bag> toRemove = new ArrayList<>();

        for (Bag bag1 : domain1) {
            boolean hasSupport = false;
            for (Bag bag2 : itemToCompatibleBags.get(arc.item2)) {
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

        Item item = selectNextItem(unassignedItems);
        for (Bag bag : orderBagsForItem(item)) {
            if (bag.packItem(item)) {
                List<Item> newUnassignedItems = new ArrayList<>(unassignedItems);
                newUnassignedItems.remove(item);
                if (backtrack(newUnassignedItems)) {
                    return true;
                }
                bag.unpackItem(item);
            }
        }

        return false;
    }

    private Item selectNextItem(List<Item> unassignedItems) {
        return Collections.min(unassignedItems, Comparator
            .<Item>comparingInt(item -> itemToCompatibleBags.get(item).size())
            .thenComparingInt(item -> -item.conflictingItems.size())
            .thenComparingInt(item -> -item.compatibleItems.size())
            .thenComparingInt(item -> -item.size));
    }

    private List<Bag> orderBagsForItem(Item item) {
        return itemToCompatibleBags.get(item).stream()
            .sorted(Comparator
                .comparingInt((Bag b) -> -b.itemsPacked.size())
                .thenComparingInt(b -> b.maxCapacity - b.currentCapacity)) 
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
        for (Bag bag : bags) {
            if (!bag.itemsPacked.isEmpty()) {
                System.out.println(String.join("\t", bag.itemsPacked.keySet()));
            }
        }
    }

    public static void main(String[] args) {
        try {
            BaggingProblem baggingProblem = new BaggingProblem(args[0]);
            if (baggingProblem.solve()) {
                System.out.println("success");
                baggingProblem.printPacking();
            } else {
                System.out.println("failure");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}