//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

/**
 * Grammar production:
 * f0 -> ( PostfixExpression() | "++" UnaryExpression() | "--" UnaryExpression() | UnaryOperator() CastExpression() | <SIZEOF> ( UnaryExpression() | "(" TypeName() ")" ) )
 */
public class UnaryExpression implements Node {
  static final long serialVersionUID = 20050923L;

   public NodeChoice f0;

   public UnaryExpression(NodeChoice n0) {
      f0 = n0;
   }

   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }
}
