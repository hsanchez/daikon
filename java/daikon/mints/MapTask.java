package daikon.mints;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author Huascar Sanchez
 */
public class MapTask extends Task {
  private final Path  filepath;
  private final Log   log;
  private final List<SequenceSummary> partition;

  /**
   * Construct a new write task object.
   *
   */
  MapTask(Path filepath, List<SequenceSummary> partition, Log log) {
    super("write to file");
    this.filepath   = filepath;
    this.log        = log;

    if(Files.exists(this.filepath)){
      FileUtils.deleteFile(this.filepath);
    }

    this.partition  = Objects.requireNonNull(partition);
  }

  @Override protected TaskResult execute() throws Exception {
    if(this.partition.isEmpty()) return TaskResult.SKIPPED;

    final List<JsonObject> objects = new CopyOnWriteArrayList<>();

    for(SequenceSummary eachSummary : partition){

      final SummaryDescriptor seqEntry  = eachSummary.source();
      final JsonObject    item      = Json.object();

      item.add("name", String.format("%s#%s", seqEntry.className(), seqEntry.methodName()));

      final JsonArray invs  = Json.array().asArray();

      for(LikelyInvariant eachInv : eachSummary.content()){
        final JsonObject eachObject = Json.object();

        eachObject.add("i", eachInv.invariantObject().format()); // i: invariant
        eachObject.add("t", eachInv.typeOfInvariant()); // t: type of invariant

        invs.add(eachObject);
      }

      item.add("invs", invs);

      log.info(item.toString());

      objects.add(item);

    }

    // [
    //   {name: ..., invs: [{i:..,t:...}, ...]}, {name: ..., invs: [{i:..,t:...}, ...]}, ...
    // ]
    JsonArray data = Json.array().asArray();
    objects.forEach(data::add);

    final byte[] dataBytes = data.toString().getBytes();

    Files.write(filepath, dataBytes, CREATE, WRITE);

    return TaskResult.SUCCESS;
  }


}

