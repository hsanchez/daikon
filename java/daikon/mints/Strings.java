package daikon.mints;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Huascar Sanchez
 */
public class Strings {
  private static final String EMPTY = "";
  private static final String SPLIT_PATTERN = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";

  private Strings(){}


  static String empty(){
    return EMPTY;
  }

  static boolean isEmpty(String text){
    if(empty().equals(text)) return true;

    final Optional<String> optional = Optional.ofNullable(text);
    return optional.map(s -> !s.isEmpty()).orElse(false);
  }


  static List<String> generateLabel(String guessedName){
    return Immutable.listOf(Arrays.stream(
      guessedName.split(SPLIT_PATTERN))
      .map(String::toLowerCase));
  }

  static String joinParameters(Map<String, String> env, List<String> params){

    if(Objects.isNull(params) || params.isEmpty()) return empty();

    final StringBuilder toString = new StringBuilder(params.size() * 100);

    final Iterator<String> iterator = params.iterator();
    while (iterator.hasNext()){

      final String each = iterator.next().trim();
      if(env.containsKey(each)){
        toString.append(env.get(each));
        if(iterator.hasNext()){
          toString.append(", ");
        }
      } else {
        toString.append(each);
        if(iterator.hasNext()){
          toString.append(", ");
        }
      }

    }

    return toString.toString();
  }

  private static String readFromInputStream(InputStream inputStream) throws IOException {
    StringBuilder content = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        content.append(line).append("\n");
      }
    }

    return content.toString();
  }

  static String fetchContent(URL url) {
    try {

      final InputStream is = (InputStream) url.getContent();

      assert !Objects.isNull(is);

      return readFromInputStream(is);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
