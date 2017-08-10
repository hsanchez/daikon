package daikon.mints;

/**
 * Mints: Mining Interesting Likely Invariants.
 *
 * <code>Mints</code> is a facade for processing .gz files
 * produced by Daikon. It supports three processes:
 * prep, mine, and sim.
 *
 * <p/>
 *
 * To run prep (or data preprocessing) from the command line, run
 * <code>java daikon.mints.Mints --mode prep &lt;path/to/gz-files&gt;
 * &lt;path/to/output.json&gt;</code>.
 *
 * <p/>
 *
 * Mine focuses on discovering a representative sequence of invariants (signature)
 * of a Java project. For running the mine mode with the baseline mining strategy, use
 * <code>java daikon.mints.Mints --mode mine &lt;path/to/input.json&gt;
 * &lt;path/to/pattern.json&gt;</code>.
 *
 * <p/>
 *
 * Sim focuses on discovering groups of similar methods, which share many likely invariants.
 * These groups are good candidate inputs for SRI's SimProg. To run the sim mode with the
 * baseline clustering strategy, use
 * <code>java daikon.mints.Mints --mode sim  &lt;path/to/data1.json&gt
 * &lt;path/to/data2.json&gt &lt;path/to/output.json&gt</code>
 *
 * <p/>
 *
 *
 * @author Huascar A. Sanchez
 **/
class Mints {

  /**
   * Run a Mints operation mentioned in the <code>args</code>.
   * If all operations run successfully, exit with a status of 0. Otherwise exit
   * with a status of 1.
   *
   * @param args Mints' options
   */
  public static void main(String... args) {
    try {
      final boolean wasSuccessful = new Mints().runMain(args);
      System.exit(wasSuccessful ? 0 : 1);
    } catch (Exception e){
      System.exit(1);
    }
  }

  /**
   * Run the main operations.
   *
   * @param args from main()
   * @return either a successful or fail operation.
   */
  private boolean runMain(String... args){
    final CommandLineParseResult result = CommandLineParseResult.parse(args);

    try {
      result.throwParsingErrors();
    } catch (Exception ignored){
      result.printUsage();
      return false;
    }


    return run(result.createRequest());
  }

  private boolean run(Request request){
    try {
      request.fulfill();
      return true;
    } catch (Exception e){
      request.getLog().error("Unable to fulfill " + request, e);
      return false;
    }
  }

}
