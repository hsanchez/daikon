//
// Generated by JTB 1.2.2
//

package jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> <IDENTIFIER>
 * f1 -> ":"
 * f2 -> Statement()
 */
public class LabeledStatement implements Node {
   private Node parent;
   public NodeToken f0;
   public NodeToken f1;
   public Statement f2;

   public LabeledStatement(NodeToken n0, NodeToken n1, Statement n2) {
      f0 = n0;
      if ( f0 != null ) f0.setParent(this);
      f1 = n1;
      if ( f1 != null ) f1.setParent(this);
      f2 = n2;
      if ( f2 != null ) f2.setParent(this);
   }

   public LabeledStatement(NodeToken n0, Statement n1) {
      f0 = n0;
      if ( f0 != null ) f0.setParent(this);
      f1 = new NodeToken(":");
      if ( f1 != null ) f1.setParent(this);
      f2 = n1;
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

