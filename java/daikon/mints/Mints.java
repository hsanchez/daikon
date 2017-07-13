package daikon.mints;

import com.google.common.collect.Lists;
import daikon.PptMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * (Min)ing Interesting Likely Invarian(ts).
 *
 * @author Huascar A. Sanchez
 **/
class Mints {

  private static final String FILE = "data.json";
  private static final String OUTPUT_DIRECTORY = "/Users/hsanchez/dev/vesperin/benchtop/output/";

  /** get patterns **/
  public static void main(String... args) throws IOException {

    final Path output = Paths.get(OUTPUT_DIRECTORY);

    final List<SequenceSegments> sequences = new Mints()
      .sequenceList(output)
      .stream()
      .collect(Collectors.toList());

    final int k = (int) Math.floor(Math.sqrt(sequences.size()));

    final List<List<SequenceSegments>> partitions = Lists.partition(sequences, k);

    final Path filepath = Paths.get(FILE);

    if(Files.exists(filepath)){
      Files.delete(filepath);
    }

    final Log verbose = Log.verbose();
    final TaskQueue queue = new TaskQueue(verbose, 20);

    for(List<SequenceSegments> each : partitions){
      final WriteTask unit = new WriteTask(filepath, each);
      queue.enqueue(unit);
    }

    queue.runTasks();
  }

  /**
   * Returns a list of non empty segments.
   *
   * @param fromDirectory the source of all these segments.
   * @return a segment list.
   */
  private List<SequenceSegments> sequenceList(Path fromDirectory){
    if(Objects.isNull(fromDirectory) || !Files.exists(fromDirectory)){
      return Collections.emptyList();
    }

    final List<PptMap>            containers  = Utils.mapList(fromDirectory);
    final List<SequenceSegments> result      = new LinkedList<>();

    containers.forEach(c -> result.addAll(Sequences.filtered(c)));

    return result.stream()
      .filter(i -> !i.isEmpty())
      .collect(Collectors.toList());
  }



}
