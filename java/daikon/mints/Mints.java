package daikon.mints;

import com.google.common.collect.Lists;
import daikon.PptMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * Mints: Mining Interesting Likely Invariants.
 *
 * @author Huascar A. Sanchez
 **/
class Mints {

  private static final String OUTPUT_DIRECTORY =
    "/Users/hsanchez/dev/vesperin/benchtop/output/";

  /** get patterns **/
  public static void main(String... args) throws IOException {

    final CommandLineParseResult result = CommandLineParseResult.parse(args);
    result.throwParsingErrors();

    final Path output = Paths.get(OUTPUT_DIRECTORY);

    final List<SequenceSummary> summaries = new Mints().summaries(output);

    final int k = (int) Math.floor(Math.sqrt(summaries.size()));

    final List<List<SequenceSummary>> partitions = Lists.partition(summaries, k);

    final Log verbose = Log.verbose();
    final TaskQueue queue = new TaskQueue(verbose, 20);

    int count = 1; for(List<SequenceSummary> each : partitions){
      final Path file = Paths.get(Integer.toString(count) + ".json");
      final MapTask unit = new MapTask(file, each);

      queue.enqueue(unit);
      count++;
    }

    final List<Task> prerequisites = Immutable.listOf(queue.getTasks());

    final ReduceTask reduce = new ReduceTask();
    reduce.afterSuccess(prerequisites);

    queue.enqueue(reduce);

    queue.calibrateAndRunTask();
  }

  /**
   * Returns a list of non empty sequence summary objects.
   *
   * @param fromDirectory the source of all these summary objects.
   * @return an immutable list of sequence summaries.
   */
  private List<SequenceSummary> summaries(Path fromDirectory){
    if(Objects.isNull(fromDirectory) || !Files.exists(fromDirectory)){
      return Collections.emptyList();
    }

    final List<PptMap>          containers  = Utils.mapList(fromDirectory);
    final Set<SequenceSummary> result      = new HashSet<>();

    containers.forEach(c -> result.addAll(Summaries.from(c)));

    return Immutable.listOf(result.stream()
      .filter(i -> !i.isEmpty()));
  }



}
