package daikon.mints;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * A class of methods useful when reading arguments
 * from the command line
 */
public class CommandLineParseResult {

  private static final Set<String> NON_BOOLEAN_OPTIONS;
  private static final Set<String> BOOLEAN_OPTIONS;

  static {
    final Set<String> bo = new HashSet<>();
    bo.add("-p");
    bo.add("--patterns");
    bo.add("-h");
    bo.add("--help");
    bo.add("-q");
    bo.add("--quiet");

    BOOLEAN_OPTIONS = Immutable.setOf(bo);

    final Set<String> nbo = new HashSet<>();
    nbo.add("-f");
    nbo.add("--from");

    nbo.add("-i");
    nbo.add("--in");

    // TODO(Huascar) finish this once we have a stable implementation
    //nbo.add("-t");
    //nbo.add("--to");

    NON_BOOLEAN_OPTIONS = Immutable.setOf(nbo);
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
    return new CommandLineParseResult(Arrays.asList(args).iterator());
  }

  /**
   *
   * Creates a new Mints request.
   *
   * @return Either a Data request or a patterns discovery request.
   */
  Request createRequest(){

    final Log log = quiteLogging() ? Log.quiet() : Log.verbose();

    if(containsPatternsOption()){
      if(!containsInputFileOption()){
        throw new ParsingException(
          Collections.singletonList(new IllegalStateException("Missing (-i | --input) input file."))
        );
      }

      final Path jsonFile = Paths.get(getValue("-i", "--input"));
      if(!Files.exists(jsonFile)){
        throw new ParsingException(
          Collections.singletonList(new IllegalStateException("Not found input file."))
        );
      }

      return Request.patterns(jsonFile, log);
    } else {

      if(containsRequiredOptions()){

        final Path from = Paths.get(getValue("-f", "--from"));
        //final Path to   = Paths.get(getValue("-t", "--to"));

        final Path dataFile = Paths.get("data.json");
        if(Files.exists(dataFile)){
          log.info("Deleting content already created data.json file.");
          Utils.deleteFile(dataFile);
        }

        return Request.invariantsData(from, log);
      }

      throw new ParsingException(
        Collections.singletonList(
          new IllegalStateException(
            "Missing (-f | --from) and (-t | --to) options"
          )
        )
      );

    }

  }

  private boolean quiteLogging(){
    return (arguments.containsKey("--quiet")
      || arguments.containsKey("-q"));
  }

  private boolean containsPatternsOption(){
    return (arguments.containsKey("--patterns")
      || arguments.containsKey("-p"));
  }

  private boolean containsInputFileOption(){
    return (arguments.containsKey("--in")
      || arguments.containsKey("-i"));
  }

  private boolean containsRequiredOptions(){
    return ((arguments.containsKey("--from")
      || arguments.containsKey("-f")));
//      && (arguments.containsKey("--to")
//      || arguments.containsKey("-t")));
  }

  /**
   * check to see if a key exists in the list arguments
   *
   * @param  key   the key to lookup
   * @return value the value of the arguments
   */
  public boolean containsKey(String key) {

    // check to ensure the key is valid
    if(isValid(key) && (BOOLEAN_OPTIONS.contains(key) || NON_BOOLEAN_OPTIONS.contains(key))) {
      return arguments.get(key) != null;
    }

    // invalid key so return null
    return false;

  }

  /**
   * @return a list of the positional arguments left over after processing all options.
   */
  public List<String> getLeftovers(){
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
      if (i + 1 < arg.length()) { // similar to args.hasNext()
        value = arg.substring(i + 1);
        i = arg.length() - 1;
      } else {
        value = grabNextValue(args, name);
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

  private static class ParsingException extends RuntimeException {

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
