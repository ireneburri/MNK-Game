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
		if (depth>8) depth = 8;

		if(depth <= 0 || board.gameState != MNKGameState.OPEN || (System.currentTimeMillis()-start)/1000.0 > timeout*(99.0/100.0)){
			eval = scoring(board, depth, first);
		}

    	else if(node){
			val = Integer.MAX_VALUE;

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
			eval = scoreNotLeaf(board);
		}
		return eval;
	}

	public double scoreNotLeaf(MNKBoard board){
		double eval;

		MNKCell[] MC = board.getMarkedCells(); //prendiamo l'ultima cella marcata
		MNKCell lastCell = MC[MC.length-1];
		
		MNKCellState[][] cloneBoard = new MNKCellState[board.M][board.N];

		for(int i = 0; i < board.M; i++){ //copiato la board originale su una nuova matrice
			for(int j = 0; j < board.N; j++){
				cloneBoard[i][j] = MNKCellState.FREE;
			}				
		}			
		for (int p = 0; p < MC.length; p++){
			cloneBoard[MC[p].i][MC[p].j] = MC[p].state;
		}

		//valuto riga con ultima cella marcata
		if(board.M >= board.K){ //cnotrolla che ci sono almeno k celle in quella riga
			MNKCellState[] row;			
			
			row = cloneBoard[lastCell.i];
			eval += evaluateLine(row);
			//row = null;			
		}

		if(board.N >= board.K){
			MNKCellState[] col;
			
			col = cloneBoard[lastCell.j];
			eval += evaluateLine(col);	
			//row = null;		
		}
	}

	public double scoreLine(MNKCellState[] line){
		
	}




}
