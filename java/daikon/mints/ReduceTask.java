package daikon.mints;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author Huascar Sanchez
 */
public class ReduceTask extends Task {

  private final Log log;

  ReduceTask(Log log){
    super("merge files");
    this.log = log;
  }

  @Override protected TaskResult execute() throws Exception {
    final Path currentDir = Paths.get(".").toAbsolutePath().normalize();

    final List<Path> allJsons = collectFiles(currentDir);
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
      final Path dataFile = Paths.get("data.json");
      if(Files.exists(dataFile)){
        log.info("Deleting already created data.json file.");
        Utils.deleteFile(dataFile);
      }

      final byte[] dataBytes = data.toString().getBytes();

      Files.write(dataFile, dataBytes, CREATE, WRITE);
      log.info(String.format("Copied %d records", data.size()));
      return TaskResult.SUCCESS;

    } finally {
      allJsons.forEach(Utils::deleteFile);
    }

  }


  /**
   * Collect files in a given location.
   *
   * @param directory directory to access
   * @return the list of files matching a given extension.
   */
  private static List<Path> collectFiles(Path directory){
    final List<Path> data = new ArrayList<>();

    try {
     return getJsonFiles(directory);
    } catch (IOException e) {
      // ignored
    }

    return data;
  }


  private static List<Path> getJsonFiles(final Path classDir) throws IOException {

    final List<Path> data = new ArrayList<>();

    final Path        start             = Paths.get(classDir.toUri());
    final FileSystem  defaultFileSystem = FileSystems.getDefault();
    final PathMatcher matcher           = defaultFileSystem.getPathMatcher(
      "glob:*.json"
    );

    try {
      Files.walkFileTree(start, new SimpleFileVisitor<Path>(){
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws
          IOException {

          final Path fileName = file.getFileName();
          if(matcher.matches(fileName)){

            data.add(file);
          }

          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException ignored){}

    return data;

  }

}
