//
// Generated by JTB 1.1.2
//

package daikon.tools.jtb.cparser.syntaxtree;

import java.util.*;

/**
 * Represents a grammar list, e.g. ( A )+
 */
public class NodeList implements NodeListInterface {
  static final long serialVersionUID = 20050923L;

   public NodeList() {
      nodes = new Vector();
   }

   public NodeList(Node firstNode) {
      nodes = new Vector();
      addNode(firstNode);
   }

   public void addNode(Node n) {
      nodes.addElement(n);
   }

   public Enumeration elements() { return nodes.elements(); }
   public Node elementAt(int i)  { return (Node)nodes.elementAt(i); }
   public int size()             { return nodes.size(); }
   public void accept(daikon.tools.jtb.cparser.visitor.Visitor v) {
      v.visit(this);
   }

   public Vector nodes;
}
