public class BagIt {
    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.err.println("Usage: java BagIt <filename>");
                System.exit(1);
            }
            
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
