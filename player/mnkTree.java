package player;

import mnkgame.*;
/*import java.util.HashMap;
import java.util.PriorityQueue;

public class mnkTree {




    private int alphabeta(Node node, boolean myNode, int alpha, int beta) {
        if (node.isLeaf()) {
            return node.score;
        }
        else {
            int eval;
            for (Node child : node.children) { child.alphabeta = false; } // Marca tutti i nodi come non elaborati da alphabeta

            if (myNode) {
                eval = Integer.MAX_VALUE;
                for (Node child : node.children) {
                    eval = Math.min(eval, alphabeta(child, false, alpha, beta));
                    beta = Math.min(eval, beta);
                    child.alphabeta = true;
                    if (beta <= alpha) { break; }
                }
            }
            else {
                eval = Integer.MIN_VALUE;
                for (Node child : node.children) {
                    eval = Math.max(eval, alphabeta(child, true, alpha, beta));
                    alpha = Math.max(eval, alpha);
                    child.alphabeta = true;
                    if (beta <= alpha) { break; }
                }
            }

            node.score = eval;
            return eval;
        }
    }
}*/
