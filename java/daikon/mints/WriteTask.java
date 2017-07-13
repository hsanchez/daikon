package daikon.mints;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * @author Huascar Sanchez
 */
public class WriteTask extends Task {
  private final Path filepath;
  private final List<SequenceSegments> partition;

  /**
   * Construct a new write task object.
   *
   */
  WriteTask(Path filepath, List<SequenceSegments> partition) {
    super("write to file");
    this.filepath   = filepath;
    this.partition  = Objects.requireNonNull(partition);
  }

  @Override protected Result execute() throws Exception {
    if(this.partition.isEmpty()) return Result.SKIPPED;


    final JsonArray main = Json.array().asArray();

    for(SequenceSegments each : partition){

      final Segment src = each.source();
      final JsonObject source    = Json.object();

    }



    final JsonObject  file      = Json.object();
    final JsonArray   sources   = Json.array().asArray();

    for(SequenceSegments eachSequence : partition){

      final Segment src = eachSequence.source();

      final JsonObject source    = Json.object();
      source.add("source", String.format("%s#%s", src.className(), src.methodName()));

      final JsonArray actualInvariants = Json.array().asArray();
      final JsonArray entrySpace       = Json.array().asArray();
      final JsonArray exitSpace        = Json.array().asArray();

      final boolean isEntry       = eachSequence.source().isEntry();

      for(LikelyInvariant each : eachSequence.content()){

        actualInvariants.add(each.invariantObject().format());
        if(isEntry){
          entrySpace.add(each.typeOfInvariant());
        } else {
          exitSpace.add(each.typeOfInvariant());
        }

      }


      source.add("invariants",  actualInvariants);
      source.add("entryspace",  entrySpace);
      source.add("exitspace",   exitSpace);

      sources.add(source);

    }

    file.add("sources", sources);

    final byte[] content = file.toString().getBytes();

    Files.write(filepath, content, APPEND, CREATE);

    return Result.SUCCESS;
  }
}
