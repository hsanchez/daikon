package daikon.mints;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Huascar Sanchez
 */
class Jsons {

  static Map<String, List<Record>> readJson(Path jsonfile) {
    final JsonValue value = FileUtils.toJson(jsonfile);
    final JsonArray array = Objects.requireNonNull(value).asArray();

    final Map<String, List<Record>> container = new HashMap<>();

    for (JsonValue x : array) {

      final String key = x.asObject().getString("name", null);
      if (Objects.isNull(key)) continue;

      final JsonArray invs = x.asObject().get("invs").asArray();

      for (JsonValue y : invs) {
        final String rawInvariant = y.asObject().getString("i", null);
        final String typeInvariant = y.asObject().getString("t", null);

        if (Objects.isNull(rawInvariant) || Objects.isNull(typeInvariant)) {
          continue;
        }

        if (container.containsKey(key)) {
          final Record record = new Record(rawInvariant, typeInvariant);
          container.get(key).add(record);
        } else {
          final List<Record> records = new ArrayList<>();
          final Record record = new Record(rawInvariant, typeInvariant);
          records.add(record);

          container.put(key, records);
        }
      }


    }

    return container;
  }

}
