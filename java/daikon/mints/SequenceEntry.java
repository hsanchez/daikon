package daikon.mints;

import daikon.PptName;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Huascar Sanchez
 */
class SequenceEntry {

  private static final Map<String, String> ENV;

  static {
    final Map<String, String> lookUpTable = new HashMap<>();
    //Primitive types
    lookUpTable.put("byte", "java.lang.Byte");
    lookUpTable.put("short", "java.lang.Short");
    lookUpTable.put("int", "java.lang.Integer");
    lookUpTable.put("long", "java.lang.Long");
    lookUpTable.put("float", "java.lang.Float");
    lookUpTable.put("double", "java.lang.Double");
    lookUpTable.put("boolean", "java.lang.Boolean");
    lookUpTable.put("char", "java.lang.Character");
    lookUpTable.put("byte[]", "java.lang.Byte[]");
    lookUpTable.put("short[]", "java.lang.Short[]");
    lookUpTable.put("int[]", "java.lang.Integer[]");
    lookUpTable.put("long[]", "java.lang.Long[]");
    lookUpTable.put("float[]", "java.lang.Float[]");
    lookUpTable.put("double[]", "java.lang.Double[]");
    lookUpTable.put("boolean[]", "java.lang.Boolean[]");
    lookUpTable.put("char[]", "java.lang.Character[]");

    ENV = Collections.unmodifiableMap(lookUpTable);
  }


  private final String        fullClassName;
  private final boolean       isConstructor;
  private final List<String>  classLabels;
  private final String        parametersString;
  private final String        methodName;
  private final boolean       caughtAsPrecondition;

  private SequenceEntry(String className, String methodName,
                        String joinedParameters, boolean isConstructor,
                        boolean isEntry) {

    this.fullClassName        = Objects.requireNonNull(className);
    this.parametersString     = Objects.requireNonNull(joinedParameters);
    this.isConstructor        = isConstructor;
    this.classLabels          = Strings.generateLabel(methodName);
    this.methodName           = methodName;
    this.caughtAsPrecondition = isEntry;
  }

  /**
   * Creates an object that holds info about the origin of these
   * likely invariants.
   *
   * @param pointName the program point name
   * @return a new source object.
   */
  static SequenceEntry from(PptName pointName, boolean isEntry) {

    final String signature = pointName.getSignature();
    if (Objects.isNull(signature)) {
      return null;
    }

    final Matcher matcher = Pattern.compile("\\((.*?)\\)")
      .matcher(signature);

    List<String> parameters   = null;
    String guessedMethodName  = null;

    final String NOTHING = Strings.empty();

    if (matcher.find()) {

      final String[] signatureElements = matcher.group(1).split(",");

      parameters        = Immutable.listOf(Arrays.stream(signatureElements));
      guessedMethodName = signature.replace(matcher.group(1), NOTHING);
      guessedMethodName = guessedMethodName.replace("()", NOTHING);
    }

    final String shortClassname = Objects.requireNonNull(pointName.getShortClassName());
    final boolean isConstructor = shortClassname.equals(guessedMethodName);

    final String paramsString = Strings.joinParameters(ENV, parameters);

    return new SequenceEntry(
      pointName.getFullClassName(),
      guessedMethodName,
      paramsString,
      isConstructor,
      isEntry
    );
  }

  String className() {
    return fullClassName;
  }

  boolean isConstructor() {
    return isConstructor;
  }

  List<String> labelList() {
    return classLabels;
  }

  String paramString() {
    return parametersString;
  }

  String methodName() {
    return methodName;
  }

  boolean isEntry() {
    return caughtAsPrecondition;
  }

  @Override public int hashCode() {
    return Objects.hash(fullClassName, methodName, parametersString);
  }

  @Override public boolean equals(Object obj) {

    if (!(obj instanceof SequenceEntry)) return false;

    final SequenceEntry other = (SequenceEntry) obj;

    final boolean sameClassName   = other.fullClassName.equals(fullClassName);
    final boolean sameMethodName  = other.methodName.equals(methodName);
    final boolean sameParams      = other.parametersString.equals(parametersString);

    return sameClassName && sameMethodName && sameParams;
  }

  @Override public String toString() {

    final String classLabels = labelList().toString().replace("[", "").replace("]", "");

    return ("(" + className() + ",	" +
      (isConstructor()
        ? ("(new,	" + classLabels + ")")
        : (("(" + classLabels + ")")))
      +
      ((Objects.isNull(paramString()) || paramString().isEmpty())
        ? ""
        : (",	" + paramString())) +
      ")");
  }

}
