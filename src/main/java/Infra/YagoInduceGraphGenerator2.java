package Infra;

import YagoGraph.YagoDataGraph;
import YagoOntology.YagoOntologyGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class YagoInduceGraphGenerator2 {

    private QueriesDependencyGraph queryGraph;
    private YagoDataGraph dataGraph;
    private YagoOntologyGraph oGraph;
    private int alpha;
    public HashMap<String, DefaultDirectedGraph<DataNode,RelationshipEdge>> graphMaps;

    public YagoInduceGraphGenerator2(QueriesDependencyGraph queryGraph, YagoDataGraph dataGraph,
                                    YagoOntologyGraph oGraph, int alpha) {

        this.dataGraph = dataGraph;
        this.oGraph = oGraph;
        this.alpha = alpha;
        this.queryGraph = queryGraph;
        this.graphMaps = new HashMap<>();

    }




    public void generateInduceGraphs() {

        for (String queryRoot : queryGraph.getQueries().keySet()) {
            OGKQuery currentQuery = queryGraph.getQueries().get(queryRoot);
            DefaultDirectedGraph iGraph = getRandomIGraph(currentQuery);
            this.graphMaps.put(currentQuery.getRoot(), iGraph);
        }

    }


    private DefaultDirectedGraph<DataNode, RelationshipEdge> getRandomIGraph(OGKQuery ogkQuery) {

        HashSet<String> predicateMap = new HashSet();
        for (OGKQueryEdge edge : ogkQuery.getQueryEdgeList()) {
            predicateMap.add(edge.getEdgePredicate());
        }
        DefaultDirectedGraph<DataNode, RelationshipEdge> iGraph = generateRandomInducedGraph(ogkQuery,
                predicateMap, oGraph, dataGraph);

        return iGraph;
    }

    private DefaultDirectedGraph<DataNode, RelationshipEdge>
    generateRandomInducedGraph(OGKQuery query, HashSet<String> predicateSet,
                               YagoOntologyGraph oGraph, YagoDataGraph dataGraph) {

        DefaultDirectedGraph iGraph = new DefaultDirectedGraph<DataNode, RelationshipEdge>(RelationshipEdge.class);
        HashSet<DataNode> induceNodes = new HashSet<>();
        HashSet<String> labelSet = new HashSet<>();


        HashSet<String> similarLabels = oGraph.getSimilarLabelSet(query.getRoot(), alpha);

        if (similarLabels != null) {
            labelSet.addAll(similarLabels);
        }

        for (DataNode dataNode : dataGraph.getDataGraph().vertexSet()) {


            ArrayList<String> commonLabels = getIntersection(labelSet, dataNode.getTypes());
            if (commonLabels.size() > 0) {
                int minDist = Integer.MAX_VALUE;
                for (String candidateType : commonLabels) {
                    if (oGraph.distMap.get(query.getRoot()).containsKey(candidateType)) {

                        int currentDist = oGraph.distMap.get(query.getRoot()).get(candidateType);
                        if (currentDist < minDist) {
                            dataNode.setType(candidateType);
                            minDist = currentDist;
                        }

                    }

                }

                if (minDist == 0) {
                    iGraph.addVertex(dataNode);
                    induceNodes.add(dataNode);
                }



            }

        }

        for (DataNode node : induceNodes) {



            for (RelationshipEdge edge : dataGraph.getDataGraph().outgoingEdgesOf(node)) {

                DataNode src = dataGraph.getDataGraph().getEdgeSource(edge);
                DataNode dst = dataGraph.getDataGraph().getEdgeTarget(edge);
                iGraph.addVertex(src);
                iGraph.addVertex(dst);

                if (predicateSet.contains(edge.getLabel())) {
                    iGraph.addEdge(src, dst, edge);
                }

            }

            for (RelationshipEdge edge : dataGraph.getDataGraph().incomingEdgesOf(node)) {

                DataNode src = dataGraph.getDataGraph().getEdgeSource(edge);
                DataNode dst = dataGraph.getDataGraph().getEdgeTarget(edge);

                iGraph.addVertex(src);
                iGraph.addVertex(dst);

                if ((predicateSet.contains(edge.getLabel()))) {
                    iGraph.addEdge(src, dst, edge);
                }

            }

        }

        return iGraph;
    }








    private ArrayList<String> getIntersection(HashSet<String> similarLabel, HashSet<String> types) {

        ArrayList<String> commonLabels = new ArrayList<>();

        for (String label : similarLabel) {
            if (types.contains(label)) {
                commonLabels.add(label);
            }
        }
        return commonLabels;
    }




    public static int randomInt(int low, int high) {

        return (int) (Math.random() * (high - low)) + low;
    }


}
