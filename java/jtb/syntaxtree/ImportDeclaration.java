//
// Generated by JTB 1.2.2
//

package jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> "import"
 * f1 -> Name()
 * f2 -> [ "." "*" ]
 * f3 -> ";"
 */
public class ImportDeclaration implements Node {
   private Node parent;
   public NodeToken f0;
   public Name f1;
   public NodeOptional f2;
   public NodeToken f3;

   public ImportDeclaration(NodeToken n0, Name n1, NodeOptional n2, NodeToken n3) {
      f0 = n0;
      if ( f0 != null ) f0.setParent(this);
      f1 = n1;
      if ( f1 != null ) f1.setParent(this);
      f2 = n2;
      if ( f2 != null ) f2.setParent(this);
      f3 = n3;
      if ( f3 != null ) f3.setParent(this);
   }

   public ImportDeclaration(Name n0, NodeOptional n1) {
      f0 = new NodeToken("import");
      if ( f0 != null ) f0.setParent(this);
      f1 = n0;
      if ( f1 != null ) f1.setParent(this);
      f2 = n1;
      if ( f2 != null ) f2.setParent(this);
      f3 = new NodeToken(";");
      if ( f3 != null ) f3.setParent(this);
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

