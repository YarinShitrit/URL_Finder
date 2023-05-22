import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents a node in the URL tree structure.
 */
public class URLTreeNode {
    private int depth;
    private String urlString;
    private String dirPath;
    private ArrayList<URLTreeNode> children;

    /**
     * Constructs a URLTreeNode object with the specified URL, directory path, and depth.
     *
     * @param url      The URL string.
     * @param dirPath  The directory path for the URL.
     * @param depth    The depth of the URL in the tree structure.
     */
    public URLTreeNode(String url, String dirPath, int depth) {
        this.depth = depth;
        this.urlString = url;
        this.dirPath = dirPath;
        this.children = new ArrayList<>();
    }

    /**
     * Returns the URL string of the URLTreeNode.
     *
     * @return The URL string.
     */
    public String getUrlString() {
        return urlString;
    }

    /**
     * Returns the directory path of the URLTreeNode, including the depth.
     *
     * @return The directory path.
     */
    public String getDirPath() {
        return dirPath + "/" + depth;
    }

    /**
     * Returns the depth of the URLTreeNode.
     *
     * @return The depth.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Adds a child URLTreeNode to the current URLTreeNode.
     *
     * @param child The child URLTreeNode to add.
     */
    public void addChild(URLTreeNode child) {
        children.add(child);
    }

    /**
     * Removes a child URLTreeNode from the current URLTreeNode.
     *
     * @param child The child URLTreeNode to remove.
     */
    public void removeChild(URLTreeNode child) {
        Iterator<URLTreeNode> iterator = children.iterator();
        while (iterator.hasNext()) {
            URLTreeNode currentChild = iterator.next();
            if (currentChild == child) {
                iterator.remove();
                return; // Child node found and removed
            }
        }
    }

    /**
     * Returns a string representation of the URLTreeNode and its children in a tree-like format.
     *
     * @return The string representation of the URLTreeNode.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Recursive helper function
        toStringHelper(this, "", sb);

        return sb.toString();
    }

    /**
     * Recursive helper function to build the string representation of the URLTreeNode and its children.
     *
     * @param node   The current URLTreeNode.
     * @param indent The indentation string for formatting.
     * @param sb     The StringBuilder to append the string representation.
     */
    private void toStringHelper(URLTreeNode node, String indent, StringBuilder sb) {
        sb.append(indent);
        sb.append("|-- ");
        sb.append("URL: ").append(node.urlString).append(", Depth: ").append(node.depth).append("\n");

        for (URLTreeNode child : node.children) {
            String childIndent = indent + "|   ";
            toStringHelper(child, childIndent, sb);
        }
    }
}
