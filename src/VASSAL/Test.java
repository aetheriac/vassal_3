package VASSAL;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

public class Test {
  
  JXTreeTable tree;
  DefaultTreeTableModel model;
  
  public Test() {
    ModuleNode root = new ModuleNode(new NodeInfo("root"));
    ModuleNode child1 = new ModuleNode(new NodeInfo("child 1"));
    root.add(child1);
    ModuleNode child11 = new ModuleNode(new NodeInfo("child 1-1"));
    child1.add(child11);
    ModuleNode child2 = new ModuleNode(new NodeInfo("child 2"));
    root.add(child2);
    
    model = new DefaultTreeTableModel(root);
    tree = new JXTreeTable(model);
    tree.setEditable(false);
    tree.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        TreePath path = tree.getPathForLocation(e.getPoint().x, e.getPoint().y);
        if (path == null) {
          return;
        }
              
        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) path.getLastPathComponent();
        NodeInfo target = (NodeInfo) node.getUserObject();

        if (e.isMetaDown()) {
          int row = tree.getRowForPath(path);          
          if (row >= 0) {
            tree.clearSelection();
            tree.addRowSelectionInterval(row, row);
            //node.setValueAt(target.getName() + "-", 0);
            //tree.tableChanged(new TableModelEvent(tree.getModel(), row));
            model.setValueAt(target.getName() + "-", node, 0);
          }
        }
      }
    });
  }

  public static void main(String[] args) {
    Test test = new Test();
    JFrame f = new JFrame();
    f.add(new JScrollPane(test.tree));
    f.pack();
    f.setVisible(true);
  }
  
  private static class NodeInfo {
    
    String name;
    
    public NodeInfo(String n) {
      name = n;
    }
    
    public String getName() {
      return name;
    }
    
    public void setName(String n) {
      name = n;
    }
    
    public String toString() {
      return name;
    }
  }
  
  private static class ModuleNode extends DefaultMutableTreeTableNode {
    
    public ModuleNode(NodeInfo nodeInfo) {
      super(nodeInfo);
    }

    public NodeInfo getNodeInfo() {
      return (NodeInfo) getUserObject();
    }
    public void setValueAt(Object aValue, int column) {
      getNodeInfo().setName((String) aValue);
    }
    
    public Object getValueAt(int column) {
      return getNodeInfo().getName();
    }
    
  }
  
}