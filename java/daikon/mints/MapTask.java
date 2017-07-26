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
  private final Path filepath;
  private final List<SequenceSummary> partition;

  /**
   * Construct a new write task object.
   *
   */
  MapTask(Path filepath, List<SequenceSummary> partition) {
    super("write to file");
    this.filepath   = filepath;

    if(Files.exists(this.filepath)){
      Utils.deleteFile(this.filepath);
    }

    this.partition  = Objects.requireNonNull(partition);
  }

  @Override protected Result execute() throws Exception {
    if(this.partition.isEmpty()) return Result.SKIPPED;

    final List<JsonObject> objects = new CopyOnWriteArrayList<>();

    for(SequenceSummary eachSummary : partition){

      final SequenceEntry seqEntry  = eachSummary.source();
      final JsonObject    item      = Json.object();

      item.add("name", String.format("%s#%s", seqEntry.className(), seqEntry.methodName()));

      final JsonArray invs  = Json.array().asArray();
      final JsonArray seq   = Json.array().asArray();

      for(LikelyInvariant eachInv : eachSummary.content()){
        invs.add(eachInv.invariantObject().format());
        seq.add(eachInv.typeOfInvariant());
      }


      item.add("invs", invs);
      item.add("seq", seq);

      Log.verbose().info(item.toString());

      objects.add(item);

    }

    // [
    //   {name: ..., invs: [], seq: [] }, {name: ..., invs: [], seq: [] }, ...
    // ]
    JsonArray data = Json.array().asArray();
    objects.forEach(data::add);

    final byte[] dataBytes = data.toString().getBytes();

    Files.write(filepath, dataBytes, CREATE, WRITE);

    return Result.SUCCESS;
  }


}

