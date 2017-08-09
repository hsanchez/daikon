package daikon.mints;

/**
 * @author Huascar Sanchez
 */
class CommandLineConst {

  // modes
  static final String MINE_MODE = "mine";
  static final String PREP_MODE = "prep";
  static final String SIM_MODE  = "sim";

  // run strategies for sim mode
  static final String KM     = "km"; // kmeans
  static final String DBC    = "dbc";// distributed based clustering

  // run strategies for mine mode
  static final String MLCS   = "mlcs";
  static final String PIM    = "pim";

  // data files
  static final String PATTERNS = "patterns.json";
  static final String DATA     = "data.json";

  private CommandLineConst(){}
}
