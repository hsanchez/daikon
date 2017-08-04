package daikon.mints;

import java.nio.file.Path;

/**
 * @author Huascar Sanchez
 */
class Errors {

  private static final String MISSING_INPUT     = "Missing input %s %s";
  private static final String MISSING_MODE      = "Missing %s mode.";
  private static final String INVALID_STRATEGY  = "Invalid %s mining strategy.";
  private static final String INVALID_FILE      = "Invalid %s file extension.";


  private Errors(){}

  static void throwInvalidStrategy(boolean trueCondition, String strategy){
    if(!trueCondition) throw new ConfigurationError(newInvalidStrategy(strategy));
  }

  static void throwInvalidFile(boolean trueCondition, Path file){
    if(!trueCondition) throw new ConfigurationError(newInvalidFileExtension(file));
  }

  static void throwNewMissingSetupMode(boolean trueCondition){
    if(!trueCondition) throw new ConfigurationError(newMissingMode("setup"));
  }

  static void throwNewMissingInputs(boolean trueCondition){
    throwNewMissingInput(trueCondition, "(s)", ".");
  }

  static void throwNewMissingInputFile(boolean trueCondition){
    throwNewMissingInput(trueCondition, "data.json", "file");
  }

  static void throwNewMissingInputDir(boolean trueCondition){
    throwNewMissingInput(trueCondition, "data", "dir");
  }

  private static void throwNewMissingInput(boolean trueCondition, String name, String leftover){
    if(!trueCondition) throw new ConfigurationError(newMissingInputException("<" + name + ">", leftover));
  }

  private static NullPointerException newMissingInputException(String name, String leftover){
    return new NullPointerException(format(MISSING_INPUT, name, leftover));
  }

  private static NullPointerException newMissingMode(String mode){
    return new NullPointerException(format(MISSING_MODE, mode));
  }

  private static IllegalArgumentException newInvalidStrategy(String strategy){
    return new IllegalArgumentException(format(INVALID_STRATEGY, strategy));
  }

  private static IllegalArgumentException newInvalidFileExtension(Path file){
    return new IllegalArgumentException(format(INVALID_FILE, file));
  }


  public static String format(String messageFormat, Object... arguments) {
    for (int i = 0; i < arguments.length; i++) {
      arguments[i] = String.valueOf(arguments[i]);
    }

    return String.format(messageFormat, arguments);
  }

  private static class ConfigurationError extends RuntimeException {
    ConfigurationError(Throwable cause){
      super(cause);
    }
  }
}
