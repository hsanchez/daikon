//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

/**
 * Grammar production:
 * f0 -> [ Pointer() ]
 * f1 -> DirectDeclarator()
 */
public class Declarator implements Node {
  static final long serialVersionUID = 20050923L;

   public NodeOptional f0;
   public DirectDeclarator f1;

   public Declarator(NodeOptional n0, DirectDeclarator n1) {
      f0 = n0;
      f1 = n1;
   }

   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }
}
