//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

/**
 * Grammar production:
 * f0 -> ( <WHILE> "(" Expression() ")" Statement() | <DO> Statement() <WHILE> "(" Expression() ")" ";" | <FOR> "(" [ Expression() ] ";" [ Expression() ] ";" [ Expression() ] ")" Statement() )
 */
public class IterationStatement implements Node {
  static final long serialVersionUID = 20050923L;

   public NodeChoice f0;

   public IterationStatement(NodeChoice n0) {
      f0 = n0;
   }

   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }
}
