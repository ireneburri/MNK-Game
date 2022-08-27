package player;

import mnkgame.*;

public class mnkTree {

  public mnkTree (){
  }


  //alphabeta pruning
	public double alphabeta(MNKBoard board, boolean node, int depth, double alpha, double beta, long start, int timeout){

		double eval;
		MNKCell fc[] = board.getFreeCells();

		if(depth <= 0 || board.gameState != MNKGameState.OPEN || (System.currentTimeMillis()-start)/1000.0 > timeout*(99.0/100.0)){
			eval = score(board, depth);
		}

    else if(node){
			eval = Integer.MAX_VALUE;

			for(MNKCell cell : fc){
				board.markCell(cell.i, cell.j);
				eval = Math.min(eval, alphabeta(board, false, depth-1, alpha, beta));
				beta = Math.min(eval, beta);
				board.unmarkCell();
				if(beta <= alpha)
					break;
			}
		}

    else{
			eval = Integer.MIN_VALUE;

			for(MNKCell cell : fc){
				board.markCell(cell.i, cell.j);
				eval = Math.max(eval, alphabeta(board, true, depth-1, alpha, beta));
				beta = Math.max(eval, alpha);
				board.unmarkCell();
				if(beta <= alpha)
					break;
			}
		}

		return eval;
	}

  public double score(MNKBoard board, int depth){

  }

}
