package daikon.mints;

/**
 * Mints: Mining Interesting Likely Invariants.
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
    result.throwParsingErrors();

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
