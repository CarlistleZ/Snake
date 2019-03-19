import java.util.Collections;
import java.util.Comparator;

public class UCB {

    public static double ucbValue(int totalVisit, double nodeWinScore, int nodeVisit) {
        if (nodeVisit == 0) {
            return Integer.MAX_VALUE;
        }
        return (nodeWinScore / (double) nodeVisit) + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
    }

    static Node findBestNodeWithUCB(Node node) {
        int parentVisit = node.state.visitCount;
        return Collections.max(
                node.childArray,
                Comparator.comparing(c -> ucbValue(parentVisit, c.state.winScore, c.state.visitCount)));
    }
}
