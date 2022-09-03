package player;

import mnkgame.*;
import java.util.Random;
import java.util.ArrayList;
import java.lang.Math.*;

import java.util.LinkedList;
import java.util.HashSet;

//MODIFICA CAMPI PROTECTED/PUBLIC !!!!

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

      double score = 0, maxEval = Integer.MIN_VALUE;
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
  			eval = 100;
      }
      //vittoria dell'avversario
      else if(board.gameState == yourWin){
  			eval = 0;
  		}
      //pareggio
      else if(board.gameState == MNKGameState.DRAW){
  			eval = -100;
  		}
      else{
        eval = scoreNotLeaf(board);
      }

  		return eval;
  	}

    public double scoreNotLeaf(MNKBoard board){
  		double eval = 0;

      //prendiamo l'ultima cella marcata
  		MNKCell[] MC = board.getMarkedCells();
  		MNKCell lastCell = MC[MC.length-1];

  		MNKCellState[][] cloneBoard = new MNKCellState[board.M][board.N];

      //DA RIFARE: riga, colonna, diagonale e antidiagonale possiamo prenderle direttamente dalla matrice originale !!!!!!
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
  			eval += scoreLine(row, lastCell);
  			//row = null;
  		}

      //valuto la colonna con l'ultima cella marcata
      //controlla che ci sono almeno k celle in quella colonna
  		if(board.N >= board.K){
  			MNKCellState[] col;
  			col = cloneBoard[lastCell.j];
  			eval += scoreLine(col, lastCell);
  			//row = null;
  		}

      //valuto la diagonale con l'ultima cella marcata
      //controlla che ci sono almeno k celle in quella colonna !!!!!!!!!

      //valuto la diagonale opposta con l'ultima cella marcata
      //controlla che ci sono almeno k celle in quella colonna !!!!!!!!!!!

      return eval;
  	}

    /*
    Turno mio (lastCell.state==IO)
        1-ho sottovettoremax >= k (con celle occupate > k/2) 40
        2-ho sottovettoremax >= k (con celle occupate < k/2) 20
        3-ho bloccato suo sottovett >= k (con celle occ > k/2) 60
        4-ho bloccato suo sottovett >= k (con celle occ < k/2) 20

    Turno avversario (lastCell.state==AVVERSARIO)
        5-ha bloccato mio sottovett >= k (con celle occ > k/2) -60
        6-ha bloccato mio sottovett >= k (con celle occ > k/2) -40

      Vittoria mia 100
      Avverasio -100
      Pareggio 0
    */
    public double scoreLine(MNKCellState[] line, MNKCell lastCell){

      double sumScore = 0;
      values values = maxSubVector(line, lastCell);

      //valutazioni delle varie cinfigurazioni nel caso in cui la cella appena posizionata sia MIA
      //DA MODIFICARE LA CONDIZIONE !!!!!
      if (first) {
        if ((values.maxEnd-values.maxStart)+1 >= board.K){
          //caso 1
          if (values.totSelectedCell > board.K/2) sumScore+=40;
          //caso 2
          else sumScore+=20;

//RIFAI MAXSUBVECTOR DOVE CONTROLLA SOLO LE CELLE SEGNATE (NON LIBERE) per fare i casi in cui te o lui siete a k-1 quindi dopo potresti vincere o perdere

        }

      }
      //valutazioni delle configurazioni nel caso in cui la cella appena posizionata sia dell'AVVERSARIO
      else {

      }

      return sumScore;
  	}

    //classe per ritornare i valori del metodo maxSubVector
    final class values {
        public int maxStart;   //PUBLIC?    !!!!!
        public int maxEnd;
        public int totSelectedCell;
        public int totFreeCell;

        public values(int maxStart, int maxEnd, int totSelectedCell, int totFreeCell){
            this.maxStart = maxStart;
            this.maxEnd = maxEnd;
            this.totSelectedCell = totSelectedCell;
            this.totFreeCell = totFreeCell;
        }
    }

//RICORDA CHE GLI DEVI PASSARE L'ARRAY SENZA LA CELLA SELEZIONATA PER CAPIRE SE SEI STATO BLOCCATO O BLOCCHI QUALCUNO !!!!

    //restitusco l'indice di inizio e di fine del sottovettore massimo costituito dalle celle marcate dal giocatore P e quelle vuote
    public values maxSubVector(MNKCellState[] line, MNKCell lastCell){

      int lenght = 0, maxLenght = 0, start = 0, maxStart = 0, end = 0, maxEnd = 0;
      int selectedCell = 0, freeCell = 0, totSelectedCell = 0, totFreeCell = 0;
      MNKCellState current_player = lastCell.state;
      MNKCellState opposite_player = (current_player == MNKCellState.P1) ? MNKCellState.P2 : MNKCellState.P1;

      for (int i=0; i<=line.length-1; i++){

        if (line[i] == lastCell.state || line[i] == MNKCellState.FREE){
          lenght = lenght + 1;
          end = i;

          if (line[i] == lastCell.state) selectedCell++;
          else freeCell++;

          if (lenght>maxLenght){
            maxLenght = lenght;
            maxStart = start;
            maxEnd = end;

            totSelectedCell = selectedCell;
            totFreeCell = freeCell;
          }
        }
        else {
          lenght = 0;
          start = i+1;
          end = i+1;

          selectedCell = 0;
          freeCell = 0;
        }
      }

      if (line[line.length-1] == lastCell.state || line[line.length-1] == MNKCellState.FREE){
        lenght = lenght + 1;
        end = line.length;

        if (line[line.length-1] == lastCell.state) selectedCell++;
        else freeCell++;

        if (lenght>maxLenght){
          maxLenght = lenght;
          maxStart = start;
          maxEnd = end;

          totSelectedCell = selectedCell;
          totFreeCell = freeCell;
        }
      }

      return new values(maxStart, maxEnd, totSelectedCell, totFreeCell);
    }

    public String playerName(){
        return "MrRobot";
    }

}
