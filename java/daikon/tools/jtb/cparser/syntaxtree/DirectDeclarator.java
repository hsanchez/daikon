//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

/**
 * Grammar production:
 * f0 -> ( t=<IDENTIFIER> | "(" Declarator() ")" )
 * f1 -> ( "[" [ ConstantExpression() ] "]" | "(" ParameterTypeList() ")" | "(" [ IdentifierList() ] ")" )*
 */
public class DirectDeclarator implements Node {
  static final long serialVersionUID = 20050923L;

   public NodeChoice f0;
   public NodeListOptional f1;

   public DirectDeclarator(NodeChoice n0, NodeListOptional n1) {
      f0 = n0;
      f1 = n1;
   }

   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }
}
