package player;

import mnkgame.*;
import java.util.Random;
import java.util.ArrayList;
import java.lang.Math.*;

public class mnkTree {

  public mnkTree (){
  }


  //alphabeta pruning
	public double alphabeta(MNKBoard board, boolean node, int depth, double alpha, double beta, long start, int timeout, boolean first){

		double eval;
		MNKCell fc[] = board.getFreeCells();

		if(depth <= 0 || board.gameState != MNKGameState.OPEN || (System.currentTimeMillis()-start)/1000.0 > timeout*(99.0/100.0)){
			eval = scoring(board, depth, first);
		}

    else if(node){
			eval = Integer.MAX_VALUE;

			for(MNKCell cell : fc){
				board.markCell(cell.i, cell.j);
				eval = Math.min(eval, alphabeta(board, false, depth-1, alpha, beta, start, timeout, first));
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
				eval = Math.max(eval, alphabeta(board, true, depth-1, alpha, beta, start, timeout, first));
				beta = Math.max(eval, alpha);
				board.unmarkCell();
				if(beta <= alpha)
					break;
			}
		}

		return eval;
	}

  public double scoring(MNKBoard board, int depth, boolean first){ //valutazione board
		double eval;
		
		if(board.gameState == myWin){ //mia vittoria
			eval = 2;
		}else if(board.gameState == yourWin){ //vittoria dell'avversario
			eval = 0;             
		}else if(board.gameState == MNKGameState.DRAW){ //pareggio
			eval = 1;			
		}else{
			eval = scoreOpenConfig(board);
		}
		return eval;
	}




}
