//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

/**
 * Grammar production:
 * f0 -> [ Expression() ]
 * f1 -> ";"
 */
public class ExpressionStatement implements Node {
  static final long serialVersionUID = 20050923L;

   public NodeOptional f0;
   public NodeToken f1;

   public ExpressionStatement(NodeOptional n0, NodeToken n1) {
      f0 = n0;
      f1 = n1;
   }

   public ExpressionStatement(NodeOptional n0) {
      f0 = n0;
      f1 = new NodeToken(";");
   }

   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }
}
