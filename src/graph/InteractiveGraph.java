package graph;

import org.graphstream.graph.Graph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.geom.Point3;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Clase para mostrar e interactuar con un grafo de manera visual.
 * Permite arrastrar nodos (dragging) y selección múltiple por recuadro.
 */
public class InteractiveGraph extends JFrame implements ViewerListener {
    private Graph graph;
    private Viewer viewer;
    private View viewPanel;
    private Set<String> selectedNodes = new HashSet<>();
    private Map<String, double[]> dragStartPositions = new HashMap<>();
    private Point3 dragStartGu = null;
    private ViewerPipe pipe;
    private boolean dragging = false;
    private Point selectionStart = null;
    private Rectangle selectionRect = null;
    private Point lastDragPoint = null;
    private String nodeIdDragged = null;
    private static final double NODE_SELECT_RADIUS = 12.0; // Aumenta el área de detección

    public InteractiveGraph(Graph graph) {
        super("Grafo Interactivo");
        this.graph = graph;
        this.viewer = graph.display(false);
        this.viewer.disableAutoLayout();
        this.viewPanel = viewer.getDefaultView();
        this.pipe = viewer.newViewerPipe();
        this.pipe.addViewerListener(this);
        this.pipe.addSink(graph);
        // Panel overlay para recuadro de selección
        JPanel overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (selectionRect != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(255, 193, 7, 60));
                    g2.fill(selectionRect);
                    g2.setColor(new Color(255, 152, 0));
                    g2.setStroke(new BasicStroke(3f));
                    g2.draw(selectionRect);
                }
            }
        };
        overlayPanel.setLayout(new BorderLayout());
        overlayPanel.add((Component) viewPanel, BorderLayout.CENTER);

        // --- BOTONES PARA AGREGAR/ELIMINAR NODOS Y ARISTAS ---
        JPanel buttonPanel = new JPanel();
        JButton btnAddNode = new JButton("Agregar Nodo");
        JButton btnRemoveNode = new JButton("Eliminar Nodo");
        JButton btnAddEdge = new JButton("Agregar Arista");
        JButton btnRemoveEdge = new JButton("Eliminar Arista");
        buttonPanel.add(btnAddNode);
        buttonPanel.add(btnRemoveNode);
        buttonPanel.add(btnAddEdge);
        buttonPanel.add(btnRemoveEdge);
        add(buttonPanel, BorderLayout.NORTH);

        btnAddNode.addActionListener(e -> {
            String id = JOptionPane.showInputDialog(this, "ID del nuevo nodo:");
            String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo nodo:");
            if (id != null && nombre != null && !id.trim().isEmpty() && !nombre.trim().isEmpty()) {
                agregarNodo(id.trim(), nombre.trim());
            }
        });
        btnRemoveNode.addActionListener(e -> {
            String id = JOptionPane.showInputDialog(this, "ID del nodo a eliminar:");
            if (id != null && !id.trim().isEmpty()) {
                eliminarNodo(id.trim());
            }
        });
        btnAddEdge.addActionListener(e -> {
            String idA = JOptionPane.showInputDialog(this, "ID del nodo origen:");
            String idB = JOptionPane.showInputDialog(this, "ID del nodo destino:");
            if (idA != null && idB != null && !idA.trim().isEmpty() && !idB.trim().isEmpty()) {
                agregarArista(idA.trim(), idB.trim());
            }
        });
        btnRemoveEdge.addActionListener(e -> {
            String idA = JOptionPane.showInputDialog(this, "ID del nodo origen de la arista:");
            String idB = JOptionPane.showInputDialog(this, "ID del nodo destino de la arista:");
            if (idA != null && idB != null && !idA.trim().isEmpty() && !idB.trim().isEmpty()) {
                eliminarArista(idA.trim(), idB.trim());
            }
        });

        setLayout(new BorderLayout());
        add(overlayPanel, BorderLayout.CENTER);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        applyEnhancedStyles();
        addDraggingFunctionality(overlayPanel);
        // Hilo para mantener el pipe activo
        new Thread(() -> {
            while (isDisplayable()) {
                pipe.pump();
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    // Mejorar el estilo visual de nodos seleccionados y recuadro
    private void applyEnhancedStyles() {
        StringBuilder styleSheet = new StringBuilder();
        styleSheet.append("node { size: 18px; fill-color: #FFF; text-size: 14; text-alignment: above; stroke-mode: plain; stroke-color: #888; stroke-width: 1px; }");
        styleSheet.append("node.selected { fill-color: #FFD700; stroke-color: #FF9800; stroke-width: 4px; }");
        styleSheet.append("node:hover { stroke-color: #000; stroke-width: 2px; }");
        styleSheet.append("edge { size: 1.2px; fill-color: #AAB; }");
        graph.setAttribute("ui.stylesheet", styleSheet.toString());
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.zoom", 1.0);
    }

    private void addDraggingFunctionality(JPanel overlayPanel) {
        overlayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    String nodeId = getNodeAt(e.getPoint());
                    if (nodeId != null) {
                        if (!selectedNodes.contains(nodeId)) {
                            selectedNodes.clear();
                            selectedNodes.add(nodeId);
                        }
                        updateNodeSelectionStyles();
                        nodeIdDragged = nodeId;
                        dragging = true;
                        lastDragPoint = e.getPoint();
                        dragStartPositions.clear();
                        for (String id : selectedNodes) {
                            org.graphstream.graph.Node node = graph.getNode(id);
                            double nx = node.hasAttribute("x") ? node.getNumber("x") : 0.0;
                            double ny = node.hasAttribute("y") ? node.getNumber("y") : 0.0;
                            dragStartPositions.put(id, new double[]{nx, ny});
                        }
                        // Guardar la posición GU inicial del mouse
                        dragStartGu = viewPanel.getCamera().transformPxToGu(e.getX(), e.getY());
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        selectedNodes.clear();
                        updateNodeSelectionStyles();
                        selectionStart = e.getPoint();
                        selectionRect = new Rectangle(selectionStart);
                        dragging = false;
                        overlayPanel.repaint();
                    }
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectionRect != null && selectionStart != null) {
                    selectNodesInRect(selectionRect);
                    selectionRect = null;
                    selectionStart = null;
                    overlayPanel.repaint();
                }
                nodeIdDragged = null;
                dragging = false;
                dragStartGu = null;
                setCursor(Cursor.getDefaultCursor());
            }
        });
        overlayPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging && nodeIdDragged != null && SwingUtilities.isLeftMouseButton(e) && dragStartGu != null) {
                    Point3 guNow = viewPanel.getCamera().transformPxToGu(e.getX(), e.getY());
                    double dx = guNow.x - dragStartGu.x;
                    double dy = guNow.y - dragStartGu.y;
                    for (String id : selectedNodes) {
                        double[] start = dragStartPositions.get(id);
                        org.graphstream.graph.Node node = graph.getNode(id);
                        node.setAttribute("xyz", start[0] + dx, start[1] + dy, 0);
                    }
                    e.consume();
                } else if (selectionStart != null) {
                    selectionRect = new Rectangle(
                        Math.min(selectionStart.x, e.getX()),
                        Math.min(selectionStart.y, e.getY()),
                        Math.abs(selectionStart.x - e.getX()),
                        Math.abs(selectionStart.y - e.getY())
                    );
                    overlayPanel.repaint();
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                String nodeId = getNodeAt(e.getPoint());
                if (nodeId != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    private void selectNodesInRect(Rectangle rect) {
        selectedNodes.clear();
        for (org.graphstream.graph.Node node : graph) {
            Point3 p = viewPanel.getCamera().transformGuToPx(
                node.hasAttribute("x") ? node.getNumber("x") : 0.0,
                node.hasAttribute("y") ? node.getNumber("y") : 0.0,
                0.0
            );
            if (rect.contains(p.x, p.y)) {
                selectedNodes.add(node.getId());
            }
        }
        updateNodeSelectionStyles();
    }

    private void updateNodeSelectionStyles() {
        for (org.graphstream.graph.Node node : graph) {
            if (selectedNodes.contains(node.getId())) {
                node.setAttribute("ui.class", node.hasAttribute("ui.class") ? ((String)node.getAttribute("ui.class")) + ",selected" : "selected");
            } else {
                if (node.hasAttribute("ui.class")) {
                    String classes = (String) node.getAttribute("ui.class");
                    if (classes.contains("selected")) {
                        classes = classes.replace(",selected", "").replace("selected,", "").replace("selected", "").trim();
                        if (classes.isEmpty()) node.removeAttribute("ui.class");
                        else node.setAttribute("ui.class", classes);
                    }
                }
            }
        }
    }

    private String getNodeAt(Point p) {
        Point3 gu = viewPanel.getCamera().transformPxToGu(p.x, p.y);
        String closest = null;
        double minDist = Double.MAX_VALUE;
        for (org.graphstream.graph.Node node : graph) {
            double nx = node.hasAttribute("x") ? node.getNumber("x") : 0.0;
            double ny = node.hasAttribute("y") ? node.getNumber("y") : 0.0;
            double dist = Math.hypot(gu.x - nx, gu.y - ny);
            if (dist < NODE_SELECT_RADIUS && dist < minDist) {
                minDist = dist;
                closest = node.getId();
            }
        }
        return closest;
    }

    // Métodos de ViewerListener
    @Override
    public void viewClosed(String viewName) {}
    @Override
    public void buttonPushed(String id) {}
    @Override
    public void buttonReleased(String id) {}
    @Override
    public void mouseLeft(String id) {}
    @Override
    public void mouseOver(String id) {}

    public void agregarNodo(String id, String nombre) {
        if (graph.getNode(id) == null) {
            graph.addNode(id);
            graph.getNode(id).setAttribute("ui.label", nombre);
        }
    }
    public void eliminarNodo(String id) {
        if (graph.getNode(id) != null) {
            graph.removeNode(id);
        }
    }
    public void agregarArista(String idA, String idB) {
        String edgeId = idA + "_" + idB;
        if (graph.getEdge(edgeId) == null && graph.getNode(idA) != null && graph.getNode(idB) != null) {
            graph.addEdge(edgeId, idA, idB);
        }
    }
    public void eliminarArista(String idA, String idB) {
        String edgeId = idA + "_" + idB;
        if (graph.getEdge(edgeId) != null) {
            graph.removeEdge(edgeId);
        }
    }
}
