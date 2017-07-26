package daikon.mints;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Mints --from ssss --to xxxx --spawn
 * @author Huascar Sanchez
 */
class CommandLineParseResult {
  private final List<String>    options       = new ArrayList<>();
  private final List<Path>      paths         = new ArrayList<>();
  private final List<Throwable> parserErrors  = new ArrayList<>();

  CommandLineParseResult(){}

  /**
   * Returns options parsed from command line.
   */
  public List<String> getOptions() {
    return Immutable.listOf(options);
  }

  /**
   * Returns paths parsed from command line.
   */
  public List<Path> getPaths() {
    return Immutable.listOf(paths);
  }


  private static ParsingError unknownOption(String arg){
    return new ParsingError(String.format("Mints knows nothing about the %s option", arg));
  }

  private static ParsingError unspecifiedValue(String arg){
    return new ParsingError(String.format("%s value not specified", arg));
  }

  /**
   * Parses the arguments.
   *
   * @param args Arguments
   */
  public static CommandLineParseResult parse(String[] args) {
    CommandLineParseResult result = new CommandLineParseResult();

    result.parseArgs(args);

    return result;
  }

  private void parseArgs(String[] args) {
    parseParameters(parseOptions(args));
  }

  private String[] parseOptions(String... args) {
    for (int i = 0; i != args.length; ++i) {
      String arg = args[i];

      if ("--".equals(arg)) {
        return copyArgsArray(args, i + 1, args.length);
      } else if (arg.startsWith("--")) {
        if (arg.startsWith("--output=") || arg.equals("--output")) {
          String outputDir;
          if (arg.equals("--output")) {
            ++i;

            if (i < args.length) {
              outputDir = args[i];
            } else {
              parserErrors.add(unspecifiedValue(arg));
              break;
            }
          } else {
            outputDir = arg.substring(arg.indexOf('=') + 1);
          }

          options.add(outputDir);
        } else if (arg.startsWith("--input=") || arg.equals("--input")) {
          String inputDir;

          if (arg.equals("--input")) {
            ++i;

            if (i < args.length) {
              inputDir = args[i];
            } else {
              parserErrors.add(unspecifiedValue(arg));
              break;
            }
          } else {
            inputDir = arg.substring(arg.indexOf('=') + 1);
          }

          options.add(inputDir);

        } else {
          parserErrors.add(unknownOption(arg));
        }
      } else {
        return copyArgsArray(args, i, args.length);
      }
    }

    return new String[]{};
  }

  private String[] copyArgsArray(String[] args, int from, int to) {
    final String[] result = new String[to - from];

    for (int j = from; j != to; ++j) {
      result[j - from] = args[j];
    }

    return result;
  }


  private void parseParameters(String[] args) {
    for (String arg : args) {
      final Path path = Paths.get(arg);
      if(!Files.exists(path)) {
        parserErrors.add(new IllegalArgumentException("Could not find directory [" + arg + "]"));
      } else {
        paths.add(path);
      }
    }
  }

  private static class ParsingError extends Exception {
    ParsingError(String message) {
      super(message);
    }
  }

  void throwParsingErrors() {

  }


}
