//
// Generated by JTB 1.2.2
//

package jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> <INTEGER_LITERAL>
 *       | <FLOATING_POINT_LITERAL>
 *       | <CHARACTER_LITERAL>
 *       | <STRING_LITERAL>
 *       | BooleanLiteral()
 *       | NullLiteral()
 */
public class Literal implements Node {
   private Node parent;
   public NodeChoice f0;

   public Literal(NodeChoice n0) {
      f0 = n0;
      if ( f0 != null ) f0.setParent(this);
   }

   public void accept(jtb.visitor.Visitor v) {
      v.visit(this);
   }
   public Object accept(jtb.visitor.ObjectVisitor v, Object argu) {
      return v.visit(this,argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}

