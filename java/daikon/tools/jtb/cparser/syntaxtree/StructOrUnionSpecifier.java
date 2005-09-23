//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

/**
 * Grammar production:

 * f0 -> StructOrUnion()
 * f1 -> ( [ <IDENTIFIER> ] "{" StructDeclarationList() "}" | <IDENTIFIER> )
 */
public class StructOrUnionSpecifier implements Node {
  static final long serialVersionUID = 20050923L;

   public StructOrUnion f0;
   public NodeChoice f1;

   public StructOrUnionSpecifier(StructOrUnion n0, NodeChoice n1) {
      f0 = n0;
      f1 = n1;
   }

   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }
}
