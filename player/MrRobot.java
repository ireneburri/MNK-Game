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
        // if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)) {
        // break;

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

      //poniamo un limite alla profondità dell'albero
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

    //valutazione board
    public double scoring(MNKBoard board, int depth, boolean first){
  		double eval;

      //mia vittoria
  		if(board.gameState == myWin){
  			eval = 2;
      }
      //vittoria dell'avversario
      else if(board.gameState == yourWin){
  			eval = 0;
  		}
      //pareggio
      else if(board.gameState == MNKGameState.DRAW){
  			eval = 1;
  		}
      else{ eval = scoreNotLeaf(board);
      }

  		return eval;
  	}

    public double scoreNotLeaf(MNKBoard board){
  		double eval;

      //prendiamo l'ultima cella marcata
  		MNKCell[] MC = board.getMarkedCells();
  		MNKCell lastCell = MC[MC.length-1];

  		MNKCellState[][] cloneBoard = new MNKCellState[board.M][board.N];

      //DA RIFARE: riga, colonna, diagonale e antidiagonale possiamo prenderle direttamente dalla matrice originale
      //copiato la board originale su una nuova matrice
  		for(int i = 0; i < board.M; i++){
  			for(int j = 0; j < board.N; j++){
  				cloneBoard[i][j] = MNKCellState.FREE;
  			}
  		}
  		for (int p = 0; p < MC.length; p++){
  			cloneBoard[MC[p].i][MC[p].j] = MC[p].state;
  		}

  		//valuto la riga con ultima cella marcata
      //controlla che ci siano almeno k celle in quella riga
  		if(board.M >= board.K){
  			MNKCellState[] row;
        row = cloneBoard[lastCell.i];
  			eval += scoreLine(row);
  			//row = null;
  		}

      //valuto la colonna con l'ultima cella marcata
      //controlla che ci sono almeno k celle in quella colonna
  		if(board.N >= board.K){
  			MNKCellState[] col;
  			col = cloneBoard[lastCell.j];
  			eval += scoreLine(col);
  			//row = null;
  		}

      //valuto la diagonale con l'ultima cella marcata
      //controlla che ci sono almeno k celle in quella colonna

      //valuto la diagonale opposta con l'ultima cella marcata
      //controlla che ci sono almeno k celle in quella colonna

  	}

    /*ritorna la somma dei punti che abbiamo assegnato a varie configurazioni favorevoli o sfavorevoli per il nostro giocatore
      configurazioni favorevoli per noi:
        -Turno Mio, sottovettore massimo mio lungo k elementi - punteggio
        -Turno Mio, sottovettore massimo avversario lungo k-x senza celle libere di fianco (punteggio alto, vuol dire che lo hai bloccato)
        -Turno Suo, ho k-x celle Mie occupate con celle libere accanto (punteggio alto inversamente proporzionale alla x)
      configurazioni sfavorevoli per noi:
        -Turno Mio, sottovettore massimo avversario lungo k-1 con celle libere di fianco (punteggio basso)
        -Turno Suo, sottovettore massimo mio lungo k-x senza celle libere di fianco (punteggio alto, vuol dire che mi ha bloccato)
        -Turno Suo, ho k-x celle Mie occupate senza celle libere accanto (punteggio basso perchè per qualsiasi x non vinciamo)
    */
    public double scoreLine(MNKCellState[] line){

      double sumScore;
      int[] index= new int[2];


      for (int i=0; i< line.length; i++){

      }

  	}

    //restitusco l'indice di inizio e di fine del sottovettore massimo costituito dalle celle marcate dal giocatore P e quelle vuote
    public int[] maxSubVector(MNKCellState[] line, MNKCellState P){

      int lenght = 0, maxLenght = 0, start = 0, maxStart = 0, end = 0, maxEnd = 0;

      for (int i=0; i<=line.length-1; i++){

        if (line[i]== P || line[i]== MNKCellState.FREE){
          lenght = lenght + 1;
          end = i;

          if (lenght>maxLenght){
            maxLenght = lenght;
            maxStart = start;
            maxEnd = end;
          }
        }
        else {
          lenght = 0;
          start = i+1;
          end = i+1;
        }
      }

      if (line[line.length-1]==P || line[line.length-1]==MNKCellState.FREE){
        lenght = lenght + 1;
        end = line.length;

        if (lenght>maxLenght){
          maxLenght = lenght;
        }
      }

      int[] index = {maxStart, maxEnd};
      return index;
    }

    public String playerName(){
        return "MrRobot";
    }

}
