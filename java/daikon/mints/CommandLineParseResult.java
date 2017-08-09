package daikon.mints;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static daikon.mints.CommandLineConst.DATA;
import static daikon.mints.CommandLineConst.KM;
import static daikon.mints.CommandLineConst.MINE_MODE;
import static daikon.mints.CommandLineConst.MLCS;
import static daikon.mints.CommandLineConst.PATTERNS;
import static daikon.mints.CommandLineConst.PIM;
import static daikon.mints.CommandLineConst.PREP_MODE;
import static daikon.mints.CommandLineConst.SAC;
import static daikon.mints.CommandLineConst.SIM_MODE;

/**
 * A class of methods that are useful when reading arguments
 * from the command line
 *
 * @author Huascar Sanchez
 */
public class CommandLineParseResult {

  private static final Set<String> NON_BOOLEAN_OPTIONS;
  private static final Set<String> BOOLEAN_OPTIONS;
  private static final Set<String> MODES;
  private static final Map<String, Set<String>> STRATEGIES;

  static {
    BOOLEAN_OPTIONS = Immutable.setOf(Arrays.asList("-v", "--verbose", "-p", "--prune-seqs"));
    NON_BOOLEAN_OPTIONS = Immutable.setOf(Arrays.asList("-m", "--mode", "-i", "--invoke-with"));
    MODES = Immutable.setOf(Arrays.asList(PREP_MODE, MINE_MODE, SIM_MODE));

    final Map<String, Set<String>> map = new HashMap<>();
    map.put(MINE_MODE, Immutable.setOf(Arrays.asList(MLCS, PIM)));
    map.put(SIM_MODE, Immutable.setOf(Arrays.asList(KM, SAC)));

    STRATEGIES = Collections.unmodifiableMap(map);
  }

  // declare private class level variables
  private final Map<String,String> arguments;
  private final List<String>       leftovers;
  private final List<Throwable>    parsingErrors;

  private CommandLineParseResult(Iterator<String> args){
    this.arguments      = new HashMap<>();
    this.leftovers      = new ArrayList<>();
    this.parsingErrors  = new ArrayList<>();

    // Scan 'args'.
    while (args.hasNext()) {
      final String arg = args.next();
      if (arg.equals("--")) {

        // "--" marks the end of options and the beginning of positional arguments.
        break;
      } else if (arg.startsWith("--")) {
        // A long option.
        parseLongOption(arg, args);
      } else if (arg.startsWith("-")) {
        // A short option.
        parseGroupedShortOptions(arg, args);
      } else {
        // The first non-option marks the end of options.
        leftovers.add(arg);
        break;
      }
    }

    // Package up the leftovers.
    while (args.hasNext()) {
      leftovers.add(args.next());
    }
  }

  /**
   * Parses the command-line arguments 'args'.
   * Returns a list of the positional arguments left over after processing all options.
   */
  public static CommandLineParseResult parse(String[] args) {
    final List<String> argList = Arrays.asList(args);
    final CommandLineParseResult result = new CommandLineParseResult(argList.iterator());
    if(argList.isEmpty()) result.parsingErrors.add(new IllegalArgumentException("Missing Mints [options]"));
    return result;
  }

  void printUsage(){
    reset();

    System.out.println("Usage: Mints [options]... [args]...");
    System.out.println();
    System.out.println("  <args>: arguments passed to the target mode: ");
    System.out.println("      .json input file (or directory containing .gz files), and ");
    System.out.println("      an output JSON file.");
    System.out.println();
    System.out.println("      This should a file containing previously-generated Daikon invariants, ");
    System.out.println("      a directory containing .gz files produced by Daikon,");
    System.out.println("      and an output JSON file persisting.");
    System.out.println();
    System.out.println("OPTIONS");
    System.out.println("  --mode <prep|mine|sim>: specify which mode to run in.");
    System.out.println("      prep: persists content of daikon-produced .gz files into a JSON data file.");
    System.out.println("      mine: examines JSON data file to discover invariant patterns.");
    System.out.println("      sim:  examines multiple JSON data files to find similar methods.");
    System.out.println();
    System.out.println("      E.g.,  ");
    System.out.println("      --mode prep data/ to/data.json");
    System.out.println("      --mode mine from/data.json dir/to/patterns.json");
    System.out.println("      --mode sim [from/data.json]... dir/to/sims.json");
    System.out.println();
    System.out.println("  --prune-seqs: prune method invariants. Examples:");
    System.out.println("      --mode prep --prune-seqs from/data/ to/data.json");
    System.out.println();
    System.out.println("      Default value: false");
    System.out.println();
    System.out.println("  --invoke-with <mlcs|pim|km|dbc>: invoke a strategy for (mine|sim) modes. Examples:");
    System.out.println("      --mode mine --invoke-with mlcs");
    System.out.println("      --mode sim --invoke-with km");
    System.out.println();
    System.out.println("      <mlcs|pim>: available strategies for the mine mode.");
    System.out.println("      <km|dbc>: available strategies for the sim mode.");
    System.out.println();
    System.out.println("      Default value for mine mode: mlcs");
    System.out.println("      Default value for sim mode: km");
    System.out.println();
    System.out.println("  --verbose: turn on persistent verbose output.");
    System.out.println();
  }

  private void reset(){
    arguments.clear();
    leftovers.clear();
    parsingErrors.clear();
  }


  /**
   *
   * Creates a new Mints request.
   *
   * @return Either a Data request or a patterns discovery request.
   */
  Request createRequest(){

    final Log log = verboseLogging() ? Log.verbose() : Log.quiet();
    Errors.throwNewMissingInputs(!getLeftovers().isEmpty());

    List<Path> optionArgs = Immutable.listOf(
      getLeftovers().stream().map(a -> Paths.get(a)));

    if(inMiningMode()){

      // this option supports up to 2 leftovers.
      optionArgs = Immutable.listOf(optionArgs.stream().limit(2));

      if(pruningEnabled()){
        log.warn("Ignoring unnecessary (-p|--prune-seqs) option.");
      }


      final String invokedWith = (containsInvokeStrategy())
        ? getValue("-i", "--invoke-with") : MLCS;

      Errors.throwInvalidStrategy(inMineStrategies(invokedWith), MINE_MODE + " " + invokedWith);

      final Path inputFile = optionArgs.get(0);

      Errors.throwInvalidFile(FileUtils.isJsonfile(inputFile), inputFile);
      Errors.throwNewMissingInputFile(Files.exists(inputFile));

      if(optionArgs.size() == 1){
        optionArgs.add(inputFile.getParent().resolve(PATTERNS));
      }

      final Path outputFile = optionArgs.get(1);
      Errors.throwInvalidFile(FileUtils.isJsonfile(outputFile), outputFile);

      if(Files.exists(outputFile)){
        log.info(String.format("Deleting %s file", outputFile));
        FileUtils.deleteFile(outputFile);
      }

      return Request.interestingPatterns(invokedWith, inputFile, outputFile, log);

    } else if (inPrepMode()){

      final Path inputDir = optionArgs.get(0);
      Errors.throwNewMissingInputDir(Files.isDirectory(inputDir) && Files.exists(inputDir));

      if(optionArgs.size() == 1){
        optionArgs.add(Paths.get(DATA));
      }

      final Path outputFile = optionArgs.get(1);
      Errors.throwInvalidFile(FileUtils.isJsonfile(outputFile), outputFile);

      if(Files.exists(outputFile)){
        log.info(String.format("Deleting %s file", outputFile));
        FileUtils.deleteFile(outputFile);
      }

      final boolean pruningEnabled = pruningEnabled();

      return Request.invariantsData(inputDir, outputFile, pruningEnabled, log);
    } else {
      Errors.throwNewMissingSimMode(isSimMode());

      final String invokedWith = (containsInvokeStrategy())
        ? getValue("-i", "--invoke-with") : KM;

      Errors.throwInvalidStrategy(inSimStrategies(invokedWith), SIM_MODE + " " + invokedWith);

      Errors.throwNewMissingInput(optionArgs.size() > 2, "(s)", "--2 inputs required.");

      if(pruningEnabled()){
        log.warn("Ignoring unnecessary (-p|--prune-seqs) option.");
      }

      if(optionArgs.size() > 2){
        if(Files.exists(optionArgs.get(optionArgs.size() - 1))){
          log.info(String.format("Deleting %s file", optionArgs.get(optionArgs.size() - 1)));
          FileUtils.deleteFile(optionArgs.get(optionArgs.size() - 1));
        }
      }

      return Request.similarMethods(invokedWith, optionArgs, log);
    }

  }

  private static boolean inMineStrategies(String command){
    return STRATEGIES.get(MINE_MODE).contains(command);
  }

  private static boolean inSimStrategies(String command){
    return STRATEGIES.get(SIM_MODE).contains(command);
  }

  private boolean verboseLogging(){
    final boolean keyExist = (containsKey("--verbose") || containsKey("-v"));

    if(keyExist){
      return Boolean.valueOf(getValue("-v", "--verbose"));
    } else {
      return false;
    }
  }

  private boolean pruningEnabled(){
    final boolean keyExist = (containsKey("--prune-seqs") || containsKey("-p"));

    if(keyExist){
      return Boolean.valueOf(getValue("-p", "--prune-seqs"));
    } else {
      return false;
    }
  }

  private boolean inPrepMode(){
    final String value = getValue("-m", "--mode");
    return containsRequiredOptions()
      && (MODES.contains(value) && value.equals("prep"));
  }

  private boolean inMiningMode(){
    final String value = getValue("-m", "--mode");
    return containsRequiredOptions()
      && (MODES.contains(value) && value.equals("mine"));
  }

  private boolean isSimMode(){
    return true;
  }


  private boolean containsRequiredOptions(){
    return ((containsKey("--mode")
      || containsKey("-m")));
  }

  private boolean containsInvokeStrategy(){
    return ((containsKey("--invoke-with")
      || containsKey("-i")));
  }

  /**
   * check to see if a key exists in the list arguments
   *
   * @param  key   the key to lookup
   * @return value the value of the arguments
   */
  public boolean containsKey(String key) {

    // check to ensure the key is valid
    if(isValid(key) && (BOOLEAN_OPTIONS.contains(key)
      || NON_BOOLEAN_OPTIONS.contains(key))) {

      return arguments.get(key) != null;
    }

    // invalid key so return null
    return false;

  }

  /**
   * @return a list of the positional arguments left over after processing all options.
   */
  private List<String> getLeftovers(){
    return Immutable.listOf(leftovers);
  }

  /**
   * get the value of the first key found in the list of arguments
   *
   * @param  key   the key to lookup
   * @param orElse the alternative key
   * @return value the value of the arguments
   */
  public String getValue(String key, String... orElse) {

    // check to ensure the key is valid
    if(containsKey(key)) {
      // return the key if found or null if it isn't
      return arguments.get(key);
    } else {
      final List<String> alts = Arrays.asList(orElse);
      if(!alts.isEmpty() && !alts.contains(null)){
        final String kalt = alts.get(0);
        if(containsKey(kalt)){
          return arguments.get(kalt);
        }
      }
    }

    throw new NoSuchElementException(String.format("unable to find %s", key));
  }

  private static boolean isValid(String value) {

    // check on the parameter value
    if(value == null) {
      return false;
    } else {
      value = value.trim();
      if("".equals(value)) {
        return false;
      }
    }

    // passed validation
    return true;
  }

  private void parseLongOption(String arg, Iterator<String> args) {
    String name = arg.replaceFirst("^--no-", "--");
    String value = null;

    // Support "--name=value" as well as "--name value".
    final int equalsIndex = name.indexOf('=');
    if (equalsIndex != -1) {
      value = name.substring(equalsIndex + 1);
      name  = name.substring(0, equalsIndex);
    }

    if (value == null) {

      if(BOOLEAN_OPTIONS.contains(name)){
        value = arg.startsWith("--no-") ? "false" : "true";
      } else {
        value = grabNextValue(args, name);
      }

    }

    if(arguments.containsKey(name)){
      parsingErrors.add(new IllegalStateException("Option already entered."));
    } else {
      arguments.put(name, value);
    }

  }

  // Given boolean options a and b, and non-boolean option f, we want to allow:
  // -ab
  // -abf out.txt
  // -abfout.txt
  // (But not -abf=out.txt --- POSIX doesn't mention that either way, but GNU
  // expressly forbids it.)
  private void parseGroupedShortOptions(String arg, Iterator<String> args) {
    final int len = arg.length();
    for (int i = 1; i < len; ++i) {
      final String name = "-" + arg.charAt(i);
      // We need a value. If there's anything left, we take the rest of this "short option".
      String value;

      if(BOOLEAN_OPTIONS.contains(name)){
        value = "true";
      } else {
        if (i + 1 < arg.length()) { // similar to args.hasNext()
          value = arg.substring(i + 1);
          i = arg.length() - 1;
        } else {
          value = grabNextValue(args, name);
        }
      }

      arguments.put(name, value);
    }
  }

  // Returns the next element of 'args' if there is one.
  private String grabNextValue(Iterator<String> args, String name) {
    if (!args.hasNext()) {
      throw new RuntimeException(String.format("no value found for %s", name));
    }

    return args.next();
  }

  void throwParsingErrors() {
    if(!parsingErrors.isEmpty()){
      throw new ParsingException(parsingErrors);
    }
  }

  static class ParsingException extends RuntimeException {

    static final long serialVersionUID = 1L;

    ParsingException(Collection<Throwable> throwables){
      super(createErrorMessage(throwables));

    }

    private static String createErrorMessage(Collection<Throwable> errorMessages) {


      final List<Throwable> encounteredErrors = Immutable.listOf(errorMessages.stream());
      if(!encounteredErrors.isEmpty()){
        encounteredErrors.sort(new ThrowableComparator());
      }

      final java.util.Formatter messageFormatter = new java.util.Formatter();
      messageFormatter.format("Parsing errors:%n%n");
      int index = 1;

      for (Throwable errorMessage : encounteredErrors) {
        final String    message = errorMessage.getLocalizedMessage();
        final String    line    = "line " + message.substring(
          message.lastIndexOf("line") + 5, message.lastIndexOf("line") + 6
        );

        messageFormatter.format("%s) Error at %s:%n", index++, line)
          .format(" %s%n%n", message);
      }

      return messageFormatter.format("%s error[s]", encounteredErrors.size()).toString();
    }
  }


  static class ThrowableComparator implements Comparator<Throwable> {
    @Override public int compare(Throwable a, Throwable b) {
      return a.getMessage().compareTo(b.getMessage());
    }
  }

}
