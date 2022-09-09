package mnkgame;

//import mnkgame.*;
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

      //se la prima mossa spetta al mio giocatore può essere effettuata randomicamente
      if(myWin == MNKGameState.WINP1 && MC.length==0){
  			MNKCell cell = FC[rand.nextInt(FC.length)];
  			board.markCell(cell.i, cell.j);
  			return cell;
  		}

      /*if(first){
        MNKCell cell = new MNKCell(1, 1, MNKCellState.P1);
  			board.markCell(1, 1);
  			return cell;
  		}*/

      double score = 0, maxEval = Integer.MIN_VALUE;
      int pos = rand.nextInt(FC.length);
      MNKCell result = FC[pos]; // random move


      for(MNKCell potentialCell : FC) {

        //If time is running out, return the randomly selected  cell
        // if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)) {
        //   break; 
        // }

        board.markCell(potentialCell.i, potentialCell.j);
        System.out.println("alpha INIZIO");
        score = alphabeta(board, true, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, start, TIMEOUT, first);
        System.out.println("alpha FINE");
        if(score > maxEval){
          maxEval = score;
          result = potentialCell;
        }
        // if (maxEval >= 100){
        //   break;
        // }

        board.unmarkCell();
        }

      board.markCell(result.i, result.j);

      return result;
    }


    //alphabeta pruning
  	public double alphabeta(MNKBoard board, boolean node, int depth, double alpha, double beta, long start, int timeout_in_secs, boolean first){

      double eval = 0;
  		MNKCell fc[] = board.getFreeCells();

      //poniamo un limite alla profondità dell'albero
      //if (depth>8) depth = 8;
      //per adesso ho messo che richiamiamo alphabeta con profondità 3

  		if( depth <= 0 || board.gameState != MNKGameState.OPEN || (System.currentTimeMillis()-start)/1000.0 > timeout_in_secs*(99.0/100.0)) {
        System.out.println("calcolo dello score per le combinazioni a profondità 0");
        eval = score(board);
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


    //valutazione board
    public double score(MNKBoard board){
  		double eval;

      //mia vittoria
  		if(board.gameState == myWin){
        System.out.println("mia win !!!!!!!!!!!!!");
  			eval = -1000;
      }
      //vittoria dell'avversario
      else if(board.gameState == yourWin){
        System.out.println("avversario win !!!!!!!!!!!!!!!");
  			eval = 1000;
  		}
      //pareggio
      else if(board.gameState == MNKGameState.DRAW){
        System.out.println("pareggio !!!!!!!!!!!!!!");
        eval = 0;
  		}
      //incontro un nodo non foglia
      else{
        System.out.println("chiamata per combinazioni no foglia a profondità 0 - score not leaf");
        eval = scoreNotLeaf(board);
      }

  		return eval;
  	}


    public double scoreNotLeaf(MNKBoard board){

  		double eval = 0;

      //prendiamo l'ultima cella marcata
  		MNKCell[] MC = board.getMarkedCells();
  		MNKCell lastCell = MC[MC.length-1];

      System.out.println("i "+lastCell.i);
      System.out.println("j "+lastCell.j);
  		
      //valuto la riga con ultima cella marcata (+ controlla che ci siano almeno k celle in quella riga)
  		if(board.N >= board.K){
  			MNKCellState[] row  = new MNKCellState[board.N];
        row = board.B[lastCell.i];
        System.out.println("calcolo scoreline x riga con riga: "+ row.length);
  			eval = Math.max(eval, scoreLine(row, lastCell, lastCell.j, row.length));
        System.out.println("eval dopo calcolo della riga:"+ eval);
  			//row = null;
  		}

      //valuto la colonna con l'ultima cella marcata (+ controlla che ci sono almeno k celle in quella colonna)
  		if(board.M >= board.K){
        ArrayList<MNKCellState> colList = new ArrayList<MNKCellState>();
			  for(int i = 0; i < board.M; i++){
				      colList.add(board.B[i][lastCell.j]);
         }
         MNKCellState[] col = new MNKCellState[colList.size()];
         col = colList.toArray(col);

        System.out.println("calcolo scoreline x col con colonna: "+ col.length);
        eval = Math.max(eval, scoreLine(col, lastCell, lastCell.i, col.length));
        System.out.println("eval dopo calcolo della colonna:"+ eval);
  			//col = null;
  		}

      //valuto la diagonale con l'ultima cella marcata (+ controlla che ci sono almeno k celle in quella colonna)
      System.out.println("creo la diagonale principale");
      ArrayList<MNKCellState> diagList = new ArrayList<MNKCellState>();
      int x = lastCell.i, y = lastCell.j;

      while (x > 0 && y < board.N-1){    //AND o OR ? !!!!!!
        y++;
        x--;
      }


      while (y >= 0 && x <= board.M-1){
        diagList.add(board.B[x][y]);
        x++;
        y--;
      }
      
      if(diagList.size() >= board.K){
        MNKCellState[] diag = new MNKCellState[diagList.size()];
        diag = diagList.toArray(diag);
        System.out.println("calcolo scoreline x col con diagonale principale: "+ diag.length);

        eval = Math.max(eval, scoreLine(diag, lastCell, matrixDiag(lastCell), diag.length));
        System.out.println("eval dopo calcolo della diagonale opposta:"+ eval);
      }

      //valuto la diagonale opposta con l'ultima cella marcata (+ controlla che ci sono almeno k celle in quella colonna)
      System.out.println("creo la diagonale inversa");
      ArrayList<MNKCellState> diagOppList = new ArrayList<MNKCellState>();
      x = lastCell.i;
      y = lastCell.j;

      while (x > 0 && y > 0){
        y--;
        x--;
      }

      while (x <= board.M -1 && y <= board.N -1) {
        diagOppList.add(board.B[x][y]);
        x++;
        y++;
      }

      if(diagOppList.size() >= board.K){
        MNKCellState[] diagOpp = new MNKCellState[diagOppList.size()];
        diagOpp = diagOppList.toArray(diagOpp);
        System.out.println("calcolo scoreline x col con diagonale secondaria: "+ diagOpp.length);

        eval = Math.max(eval, scoreLine(diagOpp, lastCell, matrixDiagOpp(lastCell), diagOpp.length));
        System.out.println("eval dopo calcolo della diagonale inversa :"+ eval);
      }
      System.out.println("eval tot "+ eval);
      return eval;
  	}


    //funzione che crea la matrice per determinare in base a lastCell quale è la sua posizione nell'Arrays della diagonale sx dx
    public int matrixDiag(MNKCell lastCell){
      System.out.println("matrixDiag entrata");
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
       System.out.println("matrixDiag uscita " + mat[lastCell.i][lastCell.j]);

       return mat[lastCell.i][lastCell.j];
    }


    //funzione che crea la matrice per determinare in base a lastCell quale è la sua posizione nell'Arrays della diagonale opposta dx sx
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


    /*
    Turno mio (lastCell.state==IO)
      1-ho sottovettoremax >= k (con celle occupate > k/2): aggiungo 3
      2-ho sottovettoremax >= k (con celle occupate < k/2): aggiungo 2
      3-ho bloccato suo sottovett >= k (con celle occ > k/2): aggiungo 10
      4-ho bloccato suo sottovett >= k (con celle occ < k/2): aggiungo 1
      5-richiamo riskySituation per trovare il maxSubVector solo sulle celle marcate dall'avversario, se è lungo k-1 vuol dire che al turno seguente vince: restituisco -100
        sarebbe la situazione in cui il suo vettore è completo - 1 e ha una cella libera sia a destra che a sinistra. Assegnamo -100 perchè è una sconfitta sicura ma che verrà
        bel caso maggiorato dalla valutazione sulle altre righe

    Turno avversario (lastCell.state==AVVERSARIO)
      6-ha sottovettoremax >= k (con celle occupate > k/2): tolgo 3
      7-ha sottovettoremax >= k (con celle occupate < k/2): tolgo 2
      8-ha bloccato mio sottovett >= k (con celle occ > k/2): tolgo -10
      9-ha bloccato mio sottovett >= k (con celle occ > k/2): tolgo 1
      10-richiamo riskySituation per trovare maxSubVec solo sulle celle occupate da me, se è lungo k-1 vuol dire che al turno seguente vinco: restituisco 100

      Vittoria mia 1000
      Avverasio -1000
      Pareggio 0
    */
    public double scoreLine(MNKCellState[] line, MNKCell lastCell, int x, int fin){
      System.out.println("entrata in scoreline");
      double sumScore = 0;

      MNKCellState myMove = (myWin == MNKGameState.WINP1)? MNKCellState.P1 : MNKCellState.P2;
		  MNKCellState yourMove = (yourWin == MNKGameState.WINP2)? MNKCellState.P2 : MNKCellState.P1;

      //valutazioni delle varie configurazioni nel caso in cui la cella appena posizionata sia MIA
      if (lastCell.state == myMove){
        System.out.println("sto valutando una mia mossa");
        if (justone(line, yourMove, fin, x)){
          sumScore = 90; //caso in più nel caso ne mancasse uno all'avversario
          System.out.println("all'avversario manca una sola mossa");
        }
        
        else if (riskySituation(line, yourMove, fin)){
          sumScore = Math.max(sumScore, 50); //caso 5
          System.out.println("esiste in una RiskySituation a sfavore di MrRobot");
        }

        else{
          values values = maxSubVector(line, myMove);
          System.out.println("lunghezza maxsubvector: " + values.maxEnd);
          System.out.println("lunghezza maxsubvector: " + values.maxStart);
          if (values.maxEnd - values.maxStart + 1 >= board.K){
            if (x >= values.maxStart && x <= values.maxEnd){
              if (values.totSelectedCell > board.K/2){
                sumScore = Math.max(sumScore, 3); //caso 1
                System.out.println("caso 1 di scoreline");
              } 
              else{ 
                sumScore = Math.max(sumScore, 2); //caso 2
                System.out.println("caso 2 di scoreline");
              }
            }
          }

          //per capire se ho bloccato un suo sottovettore chiamo maxSubVector togliendo il mark a lastCell in line
          MNKCellState[] modifiedLine = line;
          modifiedLine[x] = MNKCellState.FREE; //ho annullato la mia ultima cella marcata
          values blockValues = maxSubVector(modifiedLine, yourMove); //voglio calcolare un SUO sottovettore quindi gli passo yourMove
          
          System.out.println("lunghezza maxsubvector: " + blockValues.maxEnd);
          System.out.println("lunghezza maxsubvector: " + blockValues.maxStart);

          if (blockValues.maxEnd - blockValues.maxStart + 1 >= board.K){
            if (x >= blockValues.maxStart && x <= blockValues.maxEnd){
              if (blockValues.totSelectedCell > board.K/2) {
                sumScore = Math.max(sumScore, 20);
                System.out.println("caso 3 di scoreline");
              } //caso 3
              else {
                sumScore = Math.max(sumScore, 1);
                System.out.println("caso 4 di scoreline");
              } //caso 4
            }
          }
        }
      }

      //lastCell.state == yourMove: valutazioni delle varie configurazioni nel caso in cui la cella appena posizionata sia dell'AVVERSARIO
      else{
        System.out.println("sto valutando una sua mossa");
        if (justone(line, myMove, fin, x)){
          sumScore = -90; //caso in più nel caso ne mancasse uno all'avversario
          System.out.println("a me manca una una sola mossa");
        }
        
        if (riskySituation(line, myMove, fin)){
          sumScore = Math.min(sumScore, -50); //caso 10
          System.out.println("esiste in una RiskySituation a sfavore dell'umano");
        } 
        
        else{
          values values = maxSubVector(line, myMove);
          System.out.println("lunghezza maxsubvector: " + values.maxEnd);
          System.out.println("lunghezza maxsubvector: " + values.maxStart);
          
          if (values.maxEnd - values.maxStart + 1 >= board.K){
            if (x >= values.maxStart && x <= values.maxEnd){
              if (values.totSelectedCell > board.K/2){
                sumScore = Math.min(sumScore, -3); //caso 6
                System.out.println("caso 6 di scoreline");
              } 
              else{ 
                sumScore = Math.min(sumScore, -2); //caso 7
                System.out.println("caso 7 di scoreline");
              }
            }
          }

          MNKCellState[] modifiedOppositeLine = line;
          modifiedOppositeLine[x] = MNKCellState.FREE; //ho annullato la sua ultima cella marcata
          
          values blockValues = maxSubVector(modifiedOppositeLine, myMove); //voglio calcolare un MIO sottovettore quindi gli passo myMove
          System.out.println("lunghezza maxsubvector: " + blockValues.maxEnd);
          System.out.println("lunghezza maxsubvector: " + blockValues.maxStart);

          if (blockValues.maxEnd - blockValues.maxStart + 1 >= board.K){
            if (x >= blockValues.maxStart && x <= blockValues.maxEnd){
              if (blockValues.totSelectedCell > board.K/2) {
                sumScore = Math.min(sumScore, -20);
                System.out.println("caso 8 di scoreline");
              } //caso 8
              else {
                sumScore = Math.min(sumScore, -1);
                System.out.println("caso 9 di scoreline");
              } //caso 9
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


    //restitusco vairabili contenute nella classe values circa il sottovettore max costituito dalle CELLE MARCATE dal giocatore P e CELLE LIBERE
    public values maxSubVector(MNKCellState[] line, MNKCellState P){
      System.out.println("maxsubvector");
      int length = 0, maxlength = 0, start = 0, maxStart = 0, end = 0, maxEnd = 0;
      int selectedCell = 0, freeCell = 0, totSelectedCell = 0, totFreeCell = 0;

      for (int i=0; i<=line.length-1; i++){

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


    //true se il sottovettoremax costituito SOLO dalle CELLE MARCATE dal giocatore P è lungo k-1 e ha almeno una cella libera adiacente, false altrimenti
    public boolean riskySituation(MNKCellState[] line, MNKCellState P, int fin){

      boolean risky = false;
      int length = 0, start = 0,  end = 0;

      //calcolo un sottovettore massimo (NON considerando le celle libere) lungo k con celle libere accanto
      for (int i=0; i<=line.length-1 && risky==false; i++){   //AND o OR ? !!!!!!

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
          if ( (start-1 >=0 && line[start-1] == MNKCellState.FREE) && (end+1 < fin && line[end+1] == MNKCellState.FREE)){
            risky = true;
          }
        }
      }

      if (line[line.length-1] == P){
        length = length + 1;
        end = line.length;

        if (length == board.K-1){
          if ( (start-1 >=0 && line[start-1] == MNKCellState.FREE) && (end+1 < fin && line[end+1] == MNKCellState.FREE)){
            risky = true;
          }
        }
      }
      return risky;
    }

    public boolean justone(MNKCellState[] line, MNKCellState P, int fin, int x){

      boolean risky = false;
      int length = 0, start = 0,  end = 0;

      //calcolo un sottovettore massimo (NON considerando le celle libere) lungo k con celle libere accanto
      for (int i=0; i<line.length-1 && risky==false; i++){   //AND o OR ? !!!!!!

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
          // if ( (start-1 >=0 && start -1 == x && end+1 < fin && line[end+1] != MNKCellState.FREE) || 
          //       (start-1 >=0 && line[start -1] != MNKCellState.FREE && end+1 < fin && end+1 == x) ||
          //       (start == 0 && end+1 < fin && end+1 == x) || 
          //       (start-1 >=0 && start -1 == x && end == fin)){
            risky = true;
          }
        }
      

      if (line[line.length-1] == P || line.length-1 == x){
        length = length + 1;
        end = line.length;

        if (length == board.K){

          // if ((start-1 >=0 && start -1 == x && end+1 < fin && line[end+1] != MNKCellState.FREE) || 
          //       (start-1 >=0 && line[start -1] != MNKCellState.FREE && end+1 < fin && end+1 == x) ||
          //       (start == 0 && end+1 < fin && start+1 == x) || 
          //       (start-1 >=0 && start -1 == x && end == fin)){
            risky = true;
          }
        }
      
      return risky;
    }


    public String playerName(){
      return "MrRobot";
    }

}
