package daikon.mints;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author Huascar Sanchez
 */
public class ReduceTask extends Task {

  private final Path  output;
  private final Log   log;

  ReduceTask(Path output, Log log){
    super("merge files");
    this.output = output;
    this.log    = log;
  }

  @Override protected TaskResult execute() throws Exception {
    final Path currentDir = Paths.get(".").toAbsolutePath().normalize();

    final List<Path> allJsons = collectFiles(currentDir, log);
    log.info(String.format("Collected %d files", allJsons.size()));

    final JsonArray data = Json.array().asArray();
    allJsons.forEach(e -> {

      try {
        final String jsonContent   = new String(Files.readAllBytes(e));
        final JsonArray localArray = Json.parse(jsonContent).asArray();

        for(JsonValue eachValue : localArray){

          final JsonObject eachObject = eachValue.asObject();
          data.add(eachObject);

        }


      } catch (IOException ignored) {}

    });


    try {
      final byte[] dataBytes = data.toString().getBytes();

      Files.write(output, dataBytes, CREATE, WRITE);
      log.info(String.format("Copied %d records", data.size()));

      return TaskResult.SUCCESS;

    } finally {
      allJsons.forEach(FileUtils::deleteFile);
    }

  }


  /**
   * Collect files in a given location.
   *
   * @param directory directory to access
   * @param log log object
   * @return the list of files matching a given extension; or an empty file with no files.
   */
  private static List<Path> collectFiles(Path directory, Log log){
    return getJsonFiles(directory, log);
  }

  private static List<Path> getJsonFiles(final Path classDir, Log log){
    final File directory = classDir.toFile();
    return Immutable.listOf(
      FileUtils.findFiles(directory, log, "json")
        .stream().map(File::toPath)
    );
  }

}
