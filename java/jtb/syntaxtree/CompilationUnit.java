//
// Generated by JTB 1.2.2
//

package jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> [ PackageDeclaration() ]
 * f1 -> ( ImportDeclaration() )*
 * f2 -> ( TypeDeclaration() )*
 * f3 -> <EOF>
 */
public class CompilationUnit implements Node {
   private Node parent;
   public NodeOptional f0;
   public NodeListOptional f1;
   public NodeListOptional f2;
   public NodeToken f3;

   public CompilationUnit(NodeOptional n0, NodeListOptional n1, NodeListOptional n2, NodeToken n3) {
      f0 = n0;
      if ( f0 != null ) f0.setParent(this);
      f1 = n1;
      if ( f1 != null ) f1.setParent(this);
      f2 = n2;
      if ( f2 != null ) f2.setParent(this);
      f3 = n3;
      if ( f3 != null ) f3.setParent(this);
   }

   public CompilationUnit(NodeOptional n0, NodeListOptional n1, NodeListOptional n2) {
      f0 = n0;
      if ( f0 != null ) f0.setParent(this);
      f1 = n1;
      if ( f1 != null ) f1.setParent(this);
      f2 = n2;
      if ( f2 != null ) f2.setParent(this);
      f3 = new NodeToken("");
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

