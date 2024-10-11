public class BagIt {
    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.err.println("Usage: java BagIt <filename>");
                System.exit(1);
            }
            
            BaggingProblem bp = new BaggingProblem(args[0]);
            
            long startTime = System.nanoTime();
            boolean result = bp.search();
            long endTime = System.nanoTime();
            
            if (result) {
                System.out.println("Success");
                bp.printPacking();
            } else {
                System.out.println("Failure");
            }
            
            double durationInSeconds = (endTime - startTime) / 1e9;
            System.out.printf("Time taken: %.6f seconds%n", durationInSeconds);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
