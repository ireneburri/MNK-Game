package mnkgame;

import java.util.Random;
import java.util.Arrays;
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


    public  MrRobot(){
    }


    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs){

      rand    = new Random(System.currentTimeMillis());
      board = new MNKBoard(M,N,K);
      this.first = first;
      myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
		  yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
      TIMEOUT = timeout_in_secs;

    }


    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC){

      long start = System.currentTimeMillis();

  		if(MC.length > 0) {
  			MNKCell cell = MC[MC.length-1]; // Recover the last move from MC
  			board.markCell(cell.i, cell.j); // Save the last move in the local MNKBoard
  		}

  		// If there is just one possible move, return immediately
  		if(FC.length == 1)
  			return FC[0];

      //variabile che stabilisce la profondità dell'albero su cui lavorerà alphabeta in base al numero di celle libere
      int totDepth;

      if(FC.length > 49)
				totDepth=1;
			else if((FC.length > 40) && (FC.length <= 49))
				totDepth=2;
			else if((FC.length > 24) && (FC.length <= 40))
				totDepth=3;
			else if((FC.length > 15) && (FC.length <= 24))
				totDepth=4;
      else if((FC.length > 10) && (FC.length <= 15))
        totDepth=5;
      else if((FC.length > 5) && (FC.length <= 10))
        totDepth=6;
			else // <=7
        totDepth=7;

      double score = 0, maxEval = Integer.MIN_VALUE;

      // random move
      int pos = rand.nextInt(FC.length);
      MNKCell result = FC[pos];

      //controllo ogni cella libera per trovare il valore della configurazione corrispondente per trovare il più favorevole
      for(MNKCell potentialCell : FC) {
        board.markCell(potentialCell.i, potentialCell.j);

        score = alphabeta(board, true, totDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, start, TIMEOUT, first);

        //salvo il punteggio relativo alla configurazione più favorevole per me
        if ( score > maxEval) {
          maxEval = score;
          result = potentialCell;
        }

        board.unmarkCell();

        //se ho una configurazione vincente smetto di controllare le altre configurazioni
        if (maxEval == 1000){
          break;
        }
      }

      board.markCell(result.i, result.j);
      return result;
    }

    //costo: max{m^depth, M*N} con m= numero medio di mosse, depth= massima profondità di ricerca
    //alphabeta pruning
  	public double alphabeta(MNKBoard board, boolean node, int depth, double alpha, double beta, long start, int timeout_in_secs, boolean first){

      double eval = 0;
  		MNKCell fc[] = board.getFreeCells();

  		if( depth <= 0 || board.gameState != MNKGameState.OPEN || (System.currentTimeMillis()-start)/1000.0 > timeout_in_secs*(99.0/100.0)) {
        eval = score(board, depth);
  		}

    	else if(node){
        eval = Integer.MAX_VALUE;

  			for(MNKCell cell : fc){
  				board.markCell(cell.i, cell.j);
  				eval = Math.min(eval, alphabeta(board, false, depth-1, alpha, beta, start, timeout_in_secs, first));
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
  				eval = Math.max(eval, alphabeta(board, true, depth-1, alpha, beta, start, timeout_in_secs, first));
  				beta = Math.max(eval, alpha);
  				board.unmarkCell();

  				if(beta <= alpha)
  					break;
  			}
  		}

      return eval;
  	}

    //costo: O(M*N)
    //se siamo all'ultimo livello dell'albero restituiamo il punteggio della configurazione finale (nodo foglia) o di quella ancora aperta (nodo non foglia)
    public double score(MNKBoard board, int depth){
  		double eval;

      //vittoria del nostro giocatore
  		if(board.gameState == myWin){
  			eval = 1000;
        eval = eval - (600 - depth*100);
      }

      //vittoria dell'avversario
      else if(board.gameState == yourWin){
  			eval = -1000;
        eval = eval + (600 - depth*100);
  		}

      //pareggio
      else if(board.gameState == MNKGameState.DRAW){
        eval = 0;
  		}

      //incontro un nodo non foglia
      else{
        eval = scoreNotLeaf(board);
        eval = eval + (600 - depth*100);
      }

  		return eval;
  	}

    //costo: O(M*N)
    //euristica per valutare le configurazioni nell'ultimo livello dell'albero che non sono finali
    public double scoreNotLeaf(MNKBoard board){

  		double eval = 0;

      //prendiamo l'ultima cella marcata
  		MNKCell[] MC = board.getMarkedCells();
  		MNKCell lastCell = MC[MC.length-1];

      //valuto la riga contenente l'ultima cella marcata
      //controllo che ci siano abbastanza celle in quella riga per un allineamento di k
  		if(board.N >= board.K){
  			MNKCellState[] row  = new MNKCellState[board.N];
        row = board.B[lastCell.i];

        double tmp = scoreLine(row, lastCell, lastCell.j, row.length);
  			if (Math.abs(eval) < Math.abs(tmp)){
          eval = tmp;
        }
  		}

      //valuto la colonna contenente l'ultima cella marcata
      //controllo che ci siano abbastanza celle in quella colonna per un allineamento di k
  		if(board.M >= board.K){
        ArrayList<MNKCellState> colList = new ArrayList<MNKCellState>();
			  for(int i = 0; i < board.M; i++){
				      colList.add(board.B[i][lastCell.j]);
         }
         MNKCellState[] col = new MNKCellState[colList.size()];
         col = colList.toArray(col);

        double tmp = scoreLine(col, lastCell, lastCell.i, col.length);
   			if (Math.abs(eval) < Math.abs(tmp)){
           eval = tmp;
         }
  		}

      //valuto la diagonale sx-dx contenente l'ultima cella marcata
      //controllo che ci siano abbastanza celle in quella diagonale per un allineamento di k
      ArrayList<MNKCellState> diagList = new ArrayList<MNKCellState>();
      int x = lastCell.i, y = lastCell.j;

      while (x > 0 && y < board.N-1){
        y++;
        x--;
      }

      //il contenuto della diagonale è salvato nella lista da dx-sx
      while (y >= 0 && x <= board.M-1){
        diagList.add(board.B[x][y]);
        x++;
        y--;
      }

      if(diagList.size() >= board.K){
        MNKCellState[] diag = new MNKCellState[diagList.size()];
        diag = diagList.toArray(diag);

        //uso la funzione matrixDiag per trovare la posizione della lastCell nell'array sapendo la sua posizione nella matrice
        double tmp = scoreLine(diag, lastCell, matrixDiag(lastCell), diag.length);
        if (Math.abs(eval) < Math.abs(tmp)){
           eval = tmp;
         }
      }

      //valuto la diagonale opposta dx-sx contenente l'ultima cella marcata
      //controllo che ci siano abbastanza celle in quella diagonale per un allineamento di k
      ArrayList<MNKCellState> diagOppList = new ArrayList<MNKCellState>();
      x = lastCell.i;
      y = lastCell.j;

      while (x > 0 && y > 0){
        y--;
        x--;
      }

      //il contenuto della diagonale è salvato nella lista da sx-dx
      while (x <= board.M -1 && y <= board.N -1) {
        diagOppList.add(board.B[x][y]);
        x++;
        y++;
      }

      if(diagOppList.size() >= board.K){
        MNKCellState[] diagOpp = new MNKCellState[diagOppList.size()];
        diagOpp = diagOppList.toArray(diagOpp);

        //uso la funzione matrixDiag per trovare la posizione della lastCell nell'array sapendo la sua posizione nella matrice
        double tmp = scoreLine(diagOpp, lastCell, matrixDiagOpp(lastCell), diagOpp.length);
        if (Math.abs(eval) < Math.abs(tmp)){
           eval = tmp;
         }
      }

      return eval;
  	}

    //costo: O(M*N)
    //funzione che crea la matrice per determinare in base a lastCell quale è la sua posizione nell'Arrays della diagonale sx-dx
    public int matrixDiag(MNKCell lastCell){

      int mat[][]= new int[board.M][board.N];
      int p=0;

      if(board.M > board.N){
        for(int i=0; i<board.M; i++){
          for(int j=0; j< board.N-1-i; j++){
            mat[i][j] = p;
          }
          for(int k=i; k< board.M; k++){
            if (board.N-1-i>0) mat[k][board.N-1-i] = p;
              else mat[k][0] = p;
          }

          if(p < board.N-1)
            p++;
        }
      }
      else{
        for(int i=0; i<board.M; i++){
          for(int j=0; j<board.N-1-i; j++){
            mat[i][j]=p;
          }
          for(int k=i; k<board.M; k++){
            mat[k][board.N-1-i]=p;
          }
          p++;
        }
       }

       return mat[lastCell.i][lastCell.j];
    }

    //costo: O(M*N)
    //funzione che crea la matrice per determinare in base a lastCell quale è la sua posizione nell'Arrays della diagonale opposta dx-sx
    public int matrixDiagOpp(MNKCell lastCell){

      int mat[][]= new int[board.M][board.N];
      int p=0;

      if(board.M<board.N){
        for(int i=0; i<board.M; i++){
          for(int j=board.N-1; j>=i; j--){
            mat[i][j] = p;
          }
          for(int k=i; k<board.M; k++){
            mat[k][i] = p;
          }
          p++;
        }
      }
      else{
        for(int i=0; i<board.M; i++){
            for(int j=board.N-1; j>=i; j--){
                mat[i][j] = p;
            }
            for(int k=i; k<board.M; k++){
              if(i<board.N) mat[k][i] = p;
              else mat[k][board.N-1] = p;
            }

            if(p<board.N-1)
            p++;
        }
    }

       return mat[lastCell.i][lastCell.j];
    }

    //costo: O(line) dove line può essere board.M o board.N
    /*
    Funzione alla quale passo l'aarray (riga, colonna, diagonale e diagonale opposta) a cui appartiene la cella dell'ultima mossa fatta.

    Casi considerati e relativi punteggi:

    Turno mio (valuto una mia ipotetica mossa, ovvero lastCell.state == myMove)

      Configurazioni favorevoli:
      1- Ho un sottovettore massimo di lunghezza >= k, di cui ha occupate più di k/2 celle: aggiungo 30
      2- Ho un sottovettore massimo di lunghezza >= k, di cui ha occupate meno di k/2 celle: aggiungo 20
      3- Ho bloccato un suo sottovettore di lunghezza >= k, di cui ha occupate più di k/2 celle: aggiungo 10
      4- Ho bloccato un suo sottovettore di lunghezza >= k, di cui ha occupate meno di k/2 celle: aggiungo 5
      5- Ho bloccato un suo sottovettore lungo k-1: ritorno 1000

      Configurazioni sfavorevoli:
      5bis- Non sono riuscita a bloccare un suo sottovettore lungo k-e dell'avversario, al turno dopo vincerà: ritorno -1000

    Turno avversario (valuto una mia ipotetica mossa, ovvero lastCell.state == yourMove):

      Configurazione sfavorevoli:
      6- Ha un sottovettore massimo di lunghezza >= k, di cui ha occupate più di k/2 celle: tolgo 30
      7- Ha un sottovettore massimo di lunghezza >= k, di cui ha occupate meno di k/2 celle: tolgo 20
      8- Ha bloccato un mio sottovettore di lunghezza >= k, di cui ha occupate più di k/2 celle: tolgo 10
      9- Ha bloccato suo sottovettore id lunghezza >= k, di cui ha occupate meno di k/2 celle: tolgo 5
      10- Ha bloccato un mio sottovettore lungo k-1: ritorno -1000

      Configurazioni favorevoli:
      10bis- Non è riuscito a bloccare un mio sottovettore lungo k-e dell'avversario, al turno dopo vincerò: ritorno 1000

      Vittoria mia 1000
      Avverasio -1000
      Pareggio 0

    */
    public double scoreLine(MNKCellState[] line, MNKCell lastCell, int x, int fin){

      double sumScore = 0;

      MNKCellState myMove = (myWin == MNKGameState.WINP1)? MNKCellState.P1 : MNKCellState.P2;
		  MNKCellState yourMove = (yourWin == MNKGameState.WINP2)? MNKCellState.P2 : MNKCellState.P1;

      //valutazioni delle varie configurazioni nel caso in cui la cella appena posizionata sia MIA
      if (lastCell.state == myMove){

        if (justone(line, yourMove, fin, x)){
          sumScore = 1000; //caso 5
        }

        else if (riskySituation(line, yourMove, fin)){
          sumScore = -1000; //caso 5bis
        }

        else{
          values values = maxSubVector(line, myMove);
          if (values.maxEnd - values.maxStart + 1 >= board.K){
            if (x >= values.maxStart && x <= values.maxEnd){
              if (values.totSelectedCell >= board.K/2){
                //caso 1
                sumScore+=30;
              }
              else{
                //caso 2
                sumScore+=20;
              }
            }
          }

          //per capire se ho bloccato un suo sottovettore chiamo maxSubVector togliendo il mark a lastCell in line
          MNKCellState[] modifiedLine = Arrays.copyOf(line, line.length);
          modifiedLine[x] = MNKCellState.FREE; //ho annullato la mia ultima cella marcata

          values blockValues = maxSubVector(modifiedLine, yourMove);

          if (blockValues.maxEnd - blockValues.maxStart + 1 >= board.K){
            if (x >= blockValues.maxStart && x <= blockValues.maxEnd){
              if (blockValues.totSelectedCell >= board.K/2) {
                //caso 3
                sumScore+=10;
              }
              else {
                //caso 4
                sumScore+=5;
              }
            }
          }
        }
      }

      //lastCell.state == yourMove: valutazioni delle varie configurazioni nel caso in cui la cella appena posizionata sia dell'AVVERSARIO
      else{

        if (justone(line, myMove, fin, x)){
          sumScore = -1000; //caso 10
        }

        if (riskySituation(line, myMove, fin)){
          sumScore =  1000; //caso 10bis
        }

        else{
          values values = maxSubVector(line, myMove);

          if (values.maxEnd - values.maxStart + 1 >= board.K){
            if (x >= values.maxStart && x <= values.maxEnd){
              if (values.totSelectedCell >= board.K/2){
                //caso 6
                sumScore+=-30;
              }
              else{
                //caso 7
                sumScore+=-20;
              }
            }
          }

          MNKCellState[] modifiedOppositeLine = Arrays.copyOf(line, line.length);
          modifiedOppositeLine[x] = MNKCellState.FREE; //ho annullato la sua ultima cella marcata

          values blockValues = maxSubVector(modifiedOppositeLine, myMove);

          if (blockValues.maxEnd - blockValues.maxStart + 1 >= board.K){
            if (x >= blockValues.maxStart && x <= blockValues.maxEnd){
              if (blockValues.totSelectedCell >= board.K/2) {
                //caso 8
                sumScore+=-20;
              }
              else {
                //caso 9
                sumScore+=-1;
              }
            }
          }
        }
      }
      return sumScore;
    }


    //classe per ritornare i valori del metodo maxSubVector
    final class values {
      public int maxStart;
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

    //costo: O(line) dove line può essere board.M o board.N
    //restitusco vairabili contenute nella classe values circa il sottovettore max costituito dalle CELLE MARCATE dal giocatore P e CELLE LIBERE
    public values maxSubVector(MNKCellState[] line, MNKCellState P){
      int length = 0, maxlength = 0, start = 0, maxStart = 0, end = 0, maxEnd = 0;
      int selectedCell = 0, freeCell = 0, totSelectedCell = 0, totFreeCell = 0;

      for (int i=0; i<line.length-1; i++){

        if (line[i] == P || line[i] == MNKCellState.FREE){
          length = length + 1;
          end = i;

          if (line[i] == P) selectedCell++;
          else freeCell++;

          if (length>maxlength){
            maxlength = length;
            maxStart = start;
            maxEnd = end;

            totSelectedCell = selectedCell;
            totFreeCell = freeCell;
          }
        }
        else {
          length = 0;
          start = i+1;
          end = i+1;

          selectedCell = 0;
          freeCell = 0;
        }
      }

      if (line[line.length-1] == P || line[line.length-1] == MNKCellState.FREE){
        length = length + 1;
        end = line.length;

        if (line[line.length-1] == P) selectedCell++;
        else freeCell++;

        if (length>maxlength){
          maxlength = length;
          maxStart = start;
          maxEnd = end;

          totSelectedCell = selectedCell;
          totFreeCell = freeCell;
        }
      }

      return new values(maxStart, maxEnd-1, totSelectedCell, totFreeCell);
    }

    //costo: O(line) dove line può essere board.M o board.N
    //true se il sottovettore costituito SOLO dalle CELLE MARCATE dal giocatore P è lungo k-1 e ha almeno una cella libera adiacente, false altrimenti
    public boolean riskySituation(MNKCellState[] line, MNKCellState P, int fin){

      boolean risky = false;
      int length = 0, start = 0,  end = 0;

      //calcolo un sottovettore massimo (NON considerando le celle libere) lungo k con celle libere accanto
      for (int i=0; i<line.length-1 && risky==false; i++){   //AND o OR ? !!!!!!

        if (line[i] == P){
          length = length + 1;
          end = i;
        }
        else {
          length = 0;
          start = i+1;
          end = i+1;
        }

        //se mi trovo qua vuol dire che sono in una configurazione finale, non avrò mai length>=k
        if (length == board.K-1){
          if ( (start-1 >=0 && line[start-1] == MNKCellState.FREE) || (end+1 < fin && line[end+1] == MNKCellState.FREE)){
            risky = true;
          }
        }
      }

      if (line[line.length-1] == P){
        length = length + 1;
        end = line.length;

        if (length == board.K-1){
          if ( (start-1 >=0 && line[start-1] == MNKCellState.FREE) || (end+1 < fin && line[end+1] == MNKCellState.FREE)){
            risky = true;
          }
        }
      }
      return risky;
    }

    //costo: O(line) dove line può essere board.M o board.N
    //true se trovo un sottovettore lungo k costituito da k-1 celle marcate dal giocatore P e una cella corrispondente all'ultima mossa
    public boolean justone(MNKCellState[] line, MNKCellState P, int fin, int x){

      boolean risky = false;
      int length = 0, start = 0,  end = 0;

      for (int i=0; i<line.length-1 && risky==false; i++){

        if (line[i] == P || i == x){
          length = length + 1;
          end = i;
        }
        else {
          length = 0;
          start = i+1;
          end = i+1;
        }

        //se mi trovo qua vuol dire che all'avversario manca una mossa per la una configurazione finale, non avrò mai length>=k
        //infatti senza considerare la x è impossibile arrivare a length = k
        if (length == board.K){
            risky = true;
          }
        }


      if (line[line.length-1] == P || line.length-1 == x){
        length = length + 1;
        end = line.length;

        if (length == board.K){
            risky = true;
          }
        }

      return risky;
    }


    public String playerName(){
      return "MrRobot";
    }

}
