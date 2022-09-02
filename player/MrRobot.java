package player;

import mnkgame.*;
import java.util.Random;
import java.util.ArrayList;
import java.lang.Math.*;

import java.util.LinkedList;
import java.util.HashSet;

public class MrRobot implements MNKPlayer{

    private Random rand;
    private MNKBoard board;
    private boolean first;
    private MNKGameState myWin;
    private MNKGameState yourWin;
    private int TIMEOUT;
    private long seconds;


    public  MrRobot(){
    }

    public void initPlayer(int m, int n, int k, boolean first, int timeout){

      rand    = new Random(System.currentTimeMillis());
      board = new MNKBoard(m,n,k);
      this.first = first;
      myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
		  yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
      TIMEOUT = timeout;

    }

    public MNKCell selectCell(MNKCell[] MC, MNKCell[] FC){

      long start = System.currentTimeMillis();

  		if(MC.length > 0) {
  			MNKCell cell = MC[MC.length-1]; // Recover the last move from MC
  			board.markCell(cell.i, cell.j); // Save the last move in the local MNKBoard
  		}

  		// If there is just one possible move, return immediately
  		if(FC.length == 1)
  			return FC[0];

      //se la prima mossa spetta al mio giocatore può essere effettuata randomicamente
      if(myWin == MNKGameState.WINP1 && MC.length==0){
  			MNKCell cell = FC[rand.nextInt(FC.length)];
  			board.markCell(cell.i, cell.j);
  			return cell;
  		}

      int score = 0;
      double maxEval = Integer.MIN_VALUE;
      int pos = rand.nextInt(FC.length);
      MNKCell result = FC[pos]; // random move

      for(MNKCell potentialCell : FC) {

        // If time is running out, return the randomly selected  cell
        //if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)) {
        //break;

          board.markCell(potentialCell.i, potentialCell.j);

          score = alphabeta(board, true, board.K, Integer.MIN_VALUE, Integer.MAX_VALUE, start, TIMEOUT, first);

          if(score > maxEval){
            maxEval = score;
            result = potentialCell;
          }

          board.unmarkCell();
        }

      board.markCell(result.i, result.j);
      return result;
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
  			eval = scoreNotLeaf(board);
  		}
  		return eval;
  	}

    public double scoreNotLeaf(MNKBoard board){
  		double eval;

  		MNKCell[] MC = board.getMarkedCells(); //prendiamo l'ultima cella marcata
  		MNKCell lastCell = MC[MC.length-1];

  		MNKCellState[][] cloneBoard = new MNKCellState[board.M][board.N];

      //RIGHE E COLONNA PRESA DALLA MATRICE ORIGINALE
  		for(int i = 0; i < board.M; i++){ //copiato la board originale su una nuova matrice
  			for(int j = 0; j < board.N; j++){
  				cloneBoard[i][j] = MNKCellState.FREE;
  			}
  		}
  		for (int p = 0; p < MC.length; p++){
  			cloneBoard[MC[p].i][MC[p].j] = MC[p].state;
  		}

  		//valuto riga con ultima cella marcata
  		if(board.M >= board.K){ //controlla che ci sono almeno k celle in quella riga
  			MNKCellState[] row;

  			row = cloneBoard[lastCell.i];
  			eval += scoreLine(row);
  			//row = null;
  		}

  		if(board.N >= board.K){
  			MNKCellState[] col;

  			col = cloneBoard[lastCell.j];
  			eval += scoreLine(col);
  			//row = null;
  		}
  	}

    public double scoreLine(MNKCellState[] line){
      double sumScore;

      for (int i=0; i< line.length; i++){

      }

  	}



    public String playerName(){
        return "MrRobot";
    }

}
