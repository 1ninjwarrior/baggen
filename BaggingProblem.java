import java.util.*;
import java.io.*;

public class BaggingProblem {
    
    boolean[][] canPackWith;
    Vector<Bag> bags = new Vector<Bag>();

    HashMap<String, Item> items = new HashMap<String, Item>();

    public class Item implements Comparable<Item> {
        int id;
        String name;
        int size;
        Bag myBag = null;
        StringTokenizer st_constraints;

        public Item(String name, int size, StringTokenizer st_constraints) {
            this.id = items.size();
            this.name = name;
            this.size = size;
            this.st_constraints = st_constraints;
        }

        @Override
        public int compareTo(Item i) {
            return i.size - this.size;
        }
    }

    public class Bag  {
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

            for (Item j : packedInMe.values()) {
                if (!canPackWith[i.id][j.id]) return false;
            }

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
            if (items.get(name) != null) throw new RuntimeException("Duplicate item name in file");
            items.put(name, new Item(name, size, st));
        }

        canPackWith = new boolean[items.size()][items.size()];
        for (int x = 0; x < canPackWith.length; x++) Arrays.fill(canPackWith[x], true);

        for (Item i : items.values()) {
            if (i.st_constraints.hasMoreTokens()) {
                if (i.st_constraints.nextToken().equals("+")) {
                    Vector<String> goodStuff = new Vector<String>();
                    
                    while (i.st_constraints.hasMoreTokens()) {
                        goodStuff.add(i.st_constraints.nextToken());
                    }

                    for (String itemName : items.keySet()) {
                        if (!goodStuff.contains(itemName)) {
                            canPackWith[i.id][items.get(itemName).id] = false;
                            canPackWith[items.get(itemName).id][i.id] = false;
                        }
                    }
                } else {
                    while (i.st_constraints.hasMoreTokens()) {
                        Item j = items.get(i.st_constraints.nextToken());
                        canPackWith[i.id][j.id] = false;
                        canPackWith[j.id][i.id] = false;
                    }
                }
            }
        }
    }

    public boolean search() {
        PriorityQueue<Item> pq = new PriorityQueue<>(items.values());
        return search(pq);
    }

    private boolean search(PriorityQueue<Item> pq) {
        if (pq.isEmpty()) return true;

        Item i = pq.remove();

        for (Bag b : bags) {
            if (b.pack(i)) {
                if (search(pq)) return true;
                b.unpack(i);
            }
        }

        pq.add(i);
        return false;
    }

    public void printPacking() {
        System.out.println("success");
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
