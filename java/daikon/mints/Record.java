package daikon.mints;

/**
 * @author Huascar Sanchez
 */
class Record {
  private final String format;
  private final String type;

  Record(String format, String type) {
    this.format = format;
    this.type = type;
  }

  String format() {
    return format;
  }

  String type() {
    return type;
  }

  @Override public boolean equals(Object obj) {
    if (!(obj instanceof Record)) return false; // optimization

    final Record that = (Record) obj;

    return this.type().equals(that.type());
  }

  @Override public int hashCode() {
    return type().hashCode();
  }

  @Override public String toString() {
    return format() + ": " + type();
  }
}
