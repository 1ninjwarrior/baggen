package files;

public class BagIt {
    public static void main(String[] args) {
        try {
            if (args.length != 1 || args[0].equals("")) {
                System.err.println("Usage: java BagIt <filename>");
                System.exit(1);
            }
            
            BaggingProblem bp = new BaggingProblem(args[0]);
            
            boolean result = bp.solve();
            
            if (result) {
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
