package daikon.mints;

/**
 * Mints: Mining Interesting Likely Invariants.
 *
 * <code>Mints</code> is a facade for processing .gz files
 * produced by Daikon. It supports two processes:
 * prep, and mine. To run prep from the command line, run
 * <code>java daikon.mints.Mints --mode prep &lt;path/to/gz-files&gt;
 * &lt;path/to/output.json&gt;</code>.
 * For running the mine mode with the baseline mining strategy, use
 * <code>java daikon.mints.Mints --mode mine &lt;path/to/input.json&gt;
 * &lt;path/to/pattern.json&gt;</code>.
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
