package mycollections.map;

public class MyHashMap implements MyMap {

    private static int TABLELENGTHDEFAULT = 2;
    private static float LOADFACTORDEFAULT = 0.75f;
    private int tableLength;
    private float loadFactor;
    private Node[] table;
    private int threshold;
    private int size;

    public MyHashMap() {
        this(TABLELENGTHDEFAULT);
    }

    public MyHashMap(int tableLength) {
        this(tableLength, LOADFACTORDEFAULT);
    }

    public MyHashMap(int tableLength, float loadFactor) {
        this.tableLength = tableLength;
        this.loadFactor = loadFactor;
        this.table = new Node[tableLength];
        this.threshold = newThreshold();
        this.size = 0;
    }

    private int newTableLength() {
        return tableLength * 2;
    }

    private int newThreshold() {
        return (int) (tableLength * loadFactor);
    }

    @Override
    public Object put(Object key, Object value) {

        if (isTableExpand()) {
            enlargeTable();
        }

        if (key == null) {
            return putForNullKey(value);
        } else {
            return putNode(new Node(key, value));
        }
    }

    private boolean isTableExpand() {
        return size > threshold;
    }

    private void enlargeTable() {

        tableLength = newTableLength();
        threshold = newThreshold();

        Node[] oldTable = table;
        this.table = new Node[tableLength];
        size = 0;

        addAllTable(oldTable);
    }

    private void addAllTable(Node[] newTable) {
        for (Node node : newTable) {
            addAllNode(node);
        }
    }

    private void addAllNode(Node node) {
        while (node != null) {
            putNode(node);
            node = node.next;
        }
    }

    private Object putNode(Node node) {
        return addNodeInCell(indexFor(node.hash), node);
    }

    private int indexFor(int hash) {
        return hash & (tableLength - 1);
    }

    private Object addNodeInCell(int index, Node node) {

        Node firstNodeInCell = table[index];

        if (firstNodeInCell == null || node.equalsByIdentifier(firstNodeInCell)) {
            return replaceFirstNodeInCell(index, node);
        } else {
            return insertNodeBehindNode(firstNodeInCell, node);
        }
    }

    private Object replaceFirstNodeInCell(int index, Node node) {
        
        Node firstNodeInCell = table[index];
        table[index] = node;
        size++;
        
        if(firstNodeInCell == null) {
            return null;
        } else {
            node.next = firstNodeInCell.next;
            return firstNodeInCell.value;
        }
    }
    
    private Object insertNodeBehindNode(Node cellNode, Node newNode) {
        while (cellNode.hasNext()) {
            cellNode = cellNode.next;
            if (cellNode.equalsByIdentifier(newNode)) {
                return replaceValueInNode(cellNode, newNode);
            }
        }
        
        return addNodeLast(cellNode, newNode);
    }
    
    private Object replaceValueInNode(Node oldNode, Node newNode) {
        Object oldValue = newNode.value;
        oldNode.value = newNode.value;
        return oldValue;
    }
    
    private Object addNodeLast(Node lastNode, Node newNode) {
        lastNode.next = newNode;
        size++;
        return null;
    }

    public Object putForNullKey(Object value) {
        return addNodeInCell(0, new Node(null, value));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
            
        try {
            return searchNodeByKey(key) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private Node searchNodeByKey(Object key) throws Exception {
        int indexInTable = searchIndexInTable(key);
        
        return searchNodeInCallByKey(table[indexInTable], key);
    }
    
    private int searchIndexInTable(Object key) {
        return indexFor(hashKey(key));
    }
    
    private Node searchNodeInCallByKey(Node cellNode, Object key) throws Exception {
        return searchByKeyInCell(cellNode, key);
    }

    private Node searchByKeyInCell(Node cellNode, Object key) throws Exception {
        if (cellNode != null) {
            if (cellNode.equalsByIdentifier(key)) {
                return cellNode;
            } else {
                if (cellNode.hasNext()){
                    return searchByKeyInCell(cellNode.next, key);
                }
            }
        }
        throw new Exception("Значение по ключу не найдено!");
    }

    @Override
    public boolean containsValue(Object value) {
        for(Node cellNode : table) {
            if(containsValueInCell(cellNode, value)){
                return true;
            }
        }
        return false;
    }
    
    private boolean containsValueInCell(Node cellNode, Object value) {
        try{
            return searchNodeByValueInCell(cellNode, value) != null;
        } catch (Exception Ex) {
            return false;
        }
    }
    
    private Node searchNodeByValueInCell(Node cellNode, Object value) throws Exception {
        if(cellNode != null) {
            if (cellNode.value.equals(value)){
                return cellNode;
            } else {
                return searchNodeByValueInCell(cellNode.next, value);
            }
        } 
       throw new Exception();
    }
    
    @Override
    public Object get(Object key) {
        try {
            return searchNodeByKey(key).value;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Object remove(Object key) {
        int indexInTable = searchIndexInTable(key);
        Node cellNode = table[indexInTable];
        
        if(cellNode == null) {
            return null;
        } else {
            if(cellNode.equalsByIdentifier(key)){
                table[indexInTable] = null;
                return cellNode.value;
            } else {
                try{
                    Node nodePrev = searchPrevNodeByKeyInCell(cellNode, key);
                    Node nodeRemove = nodePrev.next;
                    nodePrev.next = nodeRemove.next;
                    return nodeRemove.value;
                } catch (Exception ex) {
                    return null;
                }
            }
        }
    }
    
    private Node searchPrevNodeByKeyInCell(Node prevNode, Object key) throws Exception {
        
        if (prevNode != null  && prevNode.next != null) {
            if (prevNode.next.equalsByIdentifier(key)) {
                return prevNode;
            } else {
                if (prevNode.next.hasNext()){
                    return searchPrevNodeByKeyInCell(prevNode.next, key);
                }
            }
        }
        throw new Exception("Значение по ключу не найдено!");
    }
    
    @Override
    public void putAll(MyMap map) {
        addAllTable(((MyHashMap) map).table);
    }

    @Override
    public void clear() {
        table = new Node[tableLength];
        size = 0;
    }
    
    @Override
    public MyMap getEmptyMyMap() {
        return new MyHashMap();
    }
        
    public static int hashKey(Object key) {
        if(key == null) {
            return 0;
        } else {
            return key.hashCode();
        }
    }
    
    private class Node {

        int hash;
        Object key;
        Node next;
        Object value;

        public Node(Object key, Object value) {
            this.hash = hashKey(key);
            this.key = key;
            this.value = value;
        }

        public Node(int hash, Object key, Node next, Object value) {
            this.hash = hash;
            this.key = key;
            this.next = next;
            this.value = value;
        }

        public boolean hasNext() {
            return next != null;
        }

        public boolean equalsByIdentifier(Node otherNode) {
            return otherNode.hash == hash && (otherNode.key == key || key != null && key.equals(otherNode.key));
        }
        
        public boolean equalsByIdentifier(Object otherKey) {
            return hashKey(otherKey) == hash && (otherKey == key || key != null && key.equals(otherKey));
        }
    }
}
