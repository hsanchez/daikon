//
// Generated by JTB 1.2.2
//

package jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> "("
 * f1 -> [ ArgumentList() ]
 * f2 -> ")"
 */
public class Arguments implements Node {
   private Node parent;
   public NodeToken f0;
   public NodeOptional f1;
   public NodeToken f2;

   public Arguments(NodeToken n0, NodeOptional n1, NodeToken n2) {
      f0 = n0;
      if ( f0 != null ) f0.setParent(this);
      f1 = n1;
      if ( f1 != null ) f1.setParent(this);
      f2 = n2;
      if ( f2 != null ) f2.setParent(this);
   }

   public Arguments(NodeOptional n0) {
      f0 = new NodeToken("(");
      if ( f0 != null ) f0.setParent(this);
      f1 = n0;
      if ( f1 != null ) f1.setParent(this);
      f2 = new NodeToken(")");
      if ( f2 != null ) f2.setParent(this);
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

